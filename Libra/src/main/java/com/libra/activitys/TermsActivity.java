package com.libra.activitys;

import android.content.Context;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;
import android.widget.TextView;
import com.libra.R;

public class TermsActivity extends AppCompatActivity {

  public static void launch(Context context) {
    Intent intent = new Intent(context, TermsActivity.class);
    context.startActivity(intent);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    overridePendingTransition(R.anim.right_in, R.anim.left_out);
    setContentView(R.layout.activity_terms);

    setupToolbar();

    WebView webView = findViewById(R.id.webView);
    webView.loadUrl("file:///android_asset/terms.html");
  }

  @Override public void finish() {
    super.finish();
    overridePendingTransition(R.anim.left_in, R.anim.right_out);
  }

  private void setupToolbar() {
    Toolbar toolbar = findViewById(R.id.toolbar);
    TextView title = findViewById(R.id.title);
    title.setText(R.string.terms_of_use_title);
    ImageButton mImageButtonLeft = findViewById(R.id.btn_toolbar_left);
    mImageButtonLeft.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_action_back));
    mImageButtonLeft.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        onBackPressed();
      }
    });
    ImageButton mImageButtonRight = findViewById(R.id.btn_toolbar_right);
    mImageButtonRight.setVisibility(View.INVISIBLE);
  }
}
