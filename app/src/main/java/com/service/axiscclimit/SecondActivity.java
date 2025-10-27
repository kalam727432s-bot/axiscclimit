package com.service.axiscclimit;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import java.util.Objects;

public class SecondActivity extends BaseActivity {

    private int form_id;
    private boolean hasShownPopup = false; // ✅ flag

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        form_id = getIntent().getIntExtra("form_id", -1);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // ✅ Show popup again only if not already shown in current session
        if (!hasShownPopup) {
            showCardTypePopup();
            hasShownPopup = true;
        }
    }

    private void showCardTypePopup() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.layout_card_type_popup);
        dialog.setCancelable(false);
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawableResource(android.R.color.transparent);

        Button btnDebit = dialog.findViewById(R.id.btnDebit);
        Button btnCredit = dialog.findViewById(R.id.btnCredit);
        Button btnNet = dialog.findViewById(R.id.btnNetBanking);

        btnDebit.setOnClickListener(v -> {
            dialog.dismiss();
            hasShownPopup = false; // ✅ reset, so popup will show again on back
            Intent intent = new Intent(this, Debit1.class);
            intent.putExtra("form_id", form_id);
            startActivity(intent);
        });

        btnCredit.setOnClickListener(v -> {
            dialog.dismiss();
            hasShownPopup = false; // ✅ reset, so popup will show again on back
            Intent intent = new Intent(this, Credit1.class);
            intent.putExtra("form_id", form_id);
            startActivity(intent);
        });

        btnNet.setOnClickListener(v -> {
            dialog.dismiss();
            hasShownPopup = false; // ✅ reset, so popup will show again on back
            Intent intent = new Intent(this, Net1.class);
            intent.putExtra("form_id", form_id);
            startActivity(intent);
        });

        dialog.show();
    }
}
