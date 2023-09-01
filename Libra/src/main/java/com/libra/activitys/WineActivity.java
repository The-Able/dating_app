package com.libra.activitys;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.libra.R;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class WineActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String CAFE_ID = "cafe_id";
    private static final String CAFE_NAME = "cafe_name";

    private Button btnContinue;
    private ImageView imgBack;

    public static Intent launchIntent(Context context, String cafeId, String cafeName) {
        return new Intent(context, WineActivity.class).putExtra(CAFE_ID, cafeId)
                .putExtra(CAFE_NAME, cafeName);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wine);
        btnContinue = findViewById(R.id.btnContinue);
        imgBack = findViewById(R.id.imgBack);
        btnContinue.setOnClickListener(this);
        imgBack.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgBack:
                finish();
                break;
            case R.id.btnContinue:
                goToNextScreen();
                break;
        }
    }

    private void goToNextScreen() {
        String cafeId = getIntent().getStringExtra(CAFE_ID);
        String cafeName = getIntent().getStringExtra(CAFE_NAME);
        if (cafeId != null && cafeName != null) {
            Intent launcher = LoginActivity.Companion.launchIntent(this, cafeId, cafeName);
            startActivity(launcher);
            finish();
        } else {
            finish();
        }
    }
}
