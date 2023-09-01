package com.libra.tools;

import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.libra.activitys.CrashActivity;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author Ilya on 03.11.2017.
 */

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
  private Context context;
  private final String LINE_SEPARATOR = "\n";

  public ExceptionHandler(Context context) {
    this.context = context;
  }

  @Override public void uncaughtException(Thread thread, Throwable throwable) {
    StringWriter stackTrace = new StringWriter();
    throwable.printStackTrace();
    FirebaseCrashlytics.getInstance().recordException(throwable);
    StringBuilder errorReport = new StringBuilder();
    errorReport.append("************ CAUSE OF ERROR ************\n\n");
    errorReport.append(stackTrace);

    errorReport.append("\n************ DEVICE INFORMATION ***********\n");
    errorReport.append("Brand: ");
    errorReport.append(Build.BRAND);
    errorReport.append(LINE_SEPARATOR);
    errorReport.append("Device: ");
    errorReport.append(Build.DEVICE);
    errorReport.append(LINE_SEPARATOR);
    errorReport.append("Model: ");
    errorReport.append(Build.MODEL);
    errorReport.append(LINE_SEPARATOR);
    errorReport.append("Id: ");
    errorReport.append(Build.ID);
    errorReport.append(LINE_SEPARATOR);
    errorReport.append("Product: ");
    errorReport.append(Build.PRODUCT);
    errorReport.append(LINE_SEPARATOR);
    errorReport.append("\n************ FIRMWARE ************\n");
    errorReport.append("SDK: ");
    errorReport.append(Build.VERSION.SDK);
    errorReport.append(LINE_SEPARATOR);
    errorReport.append("Release: ");
    errorReport.append(Build.VERSION.RELEASE);
    errorReport.append(LINE_SEPARATOR);
    errorReport.append("Incremental: ");
    errorReport.append(Build.VERSION.INCREMENTAL);
    errorReport.append(LINE_SEPARATOR);

    Intent intent = new Intent(context, CrashActivity.class);
    intent.putExtra("error", errorReport.toString());
    context.startActivity(intent);

    android.os.Process.killProcess(android.os.Process.myPid());
    System.exit(10);
  }
}

