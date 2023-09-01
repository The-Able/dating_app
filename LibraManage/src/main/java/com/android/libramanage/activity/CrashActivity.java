package com.android.libramanage.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;
import com.android.libramanage.R;

public class CrashActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_crash);
    TextView crashLog = findViewById(R.id.errorText);
    String log = getIntent().getStringExtra("error");
    if (log != null && log.length() > 0) {
      crashLog.setText(log);
    } else {
      crashLog.setText("EMPTY LOG");
    }
  }
}
