package com.android.libramanage.activity;

import android.os.Bundle;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;

import androidx.fragment.app.FragmentTransaction;
import com.android.libramanage.ExceptionHandler;
import com.android.libramanage.R;
import com.android.libramanage.fragments.BaseFragment;
import com.android.libramanage.fragments.CafeDetailsFragment;
import com.android.libramanage.fragments.CafeFragment;
import com.android.libramanage.fragments.CafeStatFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends BaseActivity implements HostActivity {

  private Toolbar mToolbar;
  private FirebaseAuth mAuth;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
//    Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
    setContentView(R.layout.activity_main);

    mToolbar = (Toolbar) findViewById(R.id.toolbar);

    mAuth = FirebaseAuth.getInstance();
    mAuth.signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
      @Override
      public void onComplete(@NonNull Task<AuthResult> task) {
        if (task.isSuccessful()) {
          FirebaseUser user = mAuth.getCurrentUser();
        } else {
          Log.e("TAG", "signInAnonymously:failure", task.getException());


        }
      }
    });

    if (savedInstanceState == null) {
      showFragment(CafeFragment.newInstance(), false);
    }
    
  }

  @Override public void showFragment(BaseFragment fg, boolean addBackStack) {
    FragmentTransaction fgTr = getSupportFragmentManager().beginTransaction()
        .replace(R.id.container, fg);
    if (addBackStack) {
      fgTr.addToBackStack(null);
    }
    fgTr.commit();
  }

  @Override public void onBackPres() {
    onBackPressed();
  }

  @Override public void changeFragment(BaseFragment fg) {
    if (fg instanceof CafeDetailsFragment || fg instanceof CafeStatFragment) {
      mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_24dp);
      mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
        @Override public void onClick(View v) {
          onBackPressed();
        }
      });
    } else {
      mToolbar.setNavigationIcon(null);
      mToolbar.setNavigationOnClickListener(null);
    }
  }

  @Override public void setTitle(String title) {
    mToolbar.setTitle(title);
  }

  @Override public void setMenu(@MenuRes int res, Toolbar.OnMenuItemClickListener listener) {
    if (res != 0) {
      mToolbar.getMenu().clear();
      mToolbar.inflateMenu(res);
      mToolbar.setOnMenuItemClickListener(listener);
    } else {
      mToolbar.getMenu().clear();
      mToolbar.setOnMenuItemClickListener(null);
    }
  }

  @Override public Toolbar getToolbar() {
    return mToolbar;
  }
}
