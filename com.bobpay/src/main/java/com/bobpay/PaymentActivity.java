package com.bobpay;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PaymentActivity extends AppCompatActivity implements View.OnClickListener {
    private final Handler mHandler = new Handler();
    private boolean mError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        findViewById(R.id.continue_button).setOnClickListener(this);

        Intent callingIntent = getIntent();
        if (null == callingIntent) {
            showError("Calling intent is null.");
            return;
        }

        Bundle extras = callingIntent.getExtras();
        if (extras == null) {
            showError("Calling intent contains no extras.");
            return;
        }

        String details = extras.getString("details");
        if (TextUtils.isEmpty(details)) {
            showError("No shopping cart details in the extras.");
            return;
        }

        JSONObject json = null;
        try {
            json = new JSONObject(details);
        } catch (JSONException e) {
            showError("Cannot parse the shopping cart details into JSON.");
            return;
        }

        JSONArray displayItems = json.optJSONArray("displayItems");
        JSONObject total = json.optJSONObject("total");
        if (total == null) {
            showError("Total is not specified.");
            return;
        }

        LinearLayout container = (LinearLayout) findViewById(R.id.line_items);
        if (displayItems != null) {
            for (int i = 0; i < displayItems.length(); i++) {
                if (!addItem(container, displayItems.optJSONObject(i))) {
                    showError("Invalid shopping cart item.");
                }
            }
        }

        if (!addItem(container, total)) showError("Invalid total.");
    }
    private boolean addItem(LinearLayout container, JSONObject item) {
        if (item == null) return false;

        JSONObject amount = item.optJSONObject("amount");
        if (amount == null) return false;

        TextView line = new TextView(this);
        line.setText(String.format("%s %s %s", item.optString("label"),
                amount.optString("currency"), amount.optString("value")));
        container.addView(line);
        return true;
    }

    private void showError(String error) {
        mError = true;
        ((TextView) findViewById(R.id.error_message)).setText(error);
    }

    @Override
    public void onClick(View v) {
        Intent result = new Intent();
        if (!mError) {
            Bundle extras = new Bundle();
            extras.putString("methodName", "https://bobpay.xyz/pay");
            extras.putString("instrumentDetails", "{\"token\": \"put-some-data-here\"}");
            result.putExtras(extras);
        }
        setResult(mError ? RESULT_CANCELED : RESULT_OK, result);
        finish();
    }
}
