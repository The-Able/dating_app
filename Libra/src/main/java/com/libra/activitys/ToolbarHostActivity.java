package com.libra.activitys;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Window;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;

import com.libra.R;
import com.libra.fragments.BaseFragment;
import com.libra.fragments.BaseHostedFragment;
import com.libra.fragments.OnChangeFragments;

public abstract class ToolbarHostActivity extends BaseActivity implements OnChangeFragments, View.OnClickListener {

  protected ImageButton mImageButtonLeft;
  protected ImageButton mImageButtonRight;
  protected AppCompatTextView mTitle;
  protected Toolbar mToolbar;

  @Override public void onAttachedToWindow() {
    super.onAttachedToWindow();

    Window window = getWindow();
    window.setFormat(PixelFormat.RGBA_8888);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_host_with_toolbar);

    mToolbar = (Toolbar) findViewById(R.id.toolbar);
    mTitle = findViewById(R.id.title);

    mTitle.setVisibility(View.VISIBLE);
    mImageButtonLeft = (ImageButton) findViewById(R.id.btn_toolbar_left);
    mImageButtonLeft.setOnClickListener(this);
    mImageButtonRight = (ImageButton) findViewById(R.id.btn_toolbar_right);
    mImageButtonRight.setOnClickListener(this);
  }

  @Override public void setTitleToolbar(String str) {
    if (str == null) {
      return;
    }
    mTitle.setText(str);
  }

  @Override public void setTitleToolbar(@StringRes int res) {
    if (res == 0) {
      return;
    }
    mTitle.setText(res);
  }

  @Override public void changeFragment(BaseFragment fg, boolean addBackStack) {
    FragmentTransaction fgTr = getSupportFragmentManager().beginTransaction()
        .replace(R.id.container, fg, fg.getClass().getSimpleName());
    if (addBackStack) {
      fgTr.addToBackStack(null);
    }
    fgTr.commit();
  }

  @Override public ImageButton getRightImageButton() {
    return mImageButtonRight;
  }

  @Override public ImageButton getLeftImageButton() {
    return mImageButtonLeft;
  }

  @Override public Toolbar getToolbar() {
    return mToolbar;
  }

  @Override public void onClick(View v) {
    BaseHostedFragment fg = (BaseHostedFragment) getSupportFragmentManager().findFragmentById(R.id.container);
    switch (v.getId()) {
      case R.id.btn_toolbar_left:
        if (fg != null && fg.isVisible()) {
          if (!fg.handlerClickButtonToolbar(v.getId())) {
            onBackPressed();
          }
        }
        break;
      case R.id.btn_toolbar_right:
        if (fg.handlerClickButtonToolbar(v.getId())) {
          //TODO if need handle from activity
        }
        break;
    }
  }
}
