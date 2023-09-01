package com.libra.activitys;

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.firebase.geofire.util.GeoUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessaging;
import com.libra.BuildConfig;
import com.libra.Const;
import com.libra.R;
import com.libra.entity.Cafe;
import com.libra.entity.User;
import com.libra.firebase.FBHelper;
import com.libra.firebase.FirebaseEvents;
import com.libra.fragments.BaseDialogFragment;
import com.libra.notifications.GeofenceBroadcastReceiver;
import com.libra.services.GeofenceIntentService;
import com.libra.support.CloudHelper;
import com.libra.support.Logger;
import com.libra.support.PrefHelper;
import com.libra.support.Tools;
import com.libra.ui.home.CafeActivity;
import com.libra.ui.home.cafeslist.CafeLoginErrorDialog;
import com.libra.ui.home.cafeslist.viewmodels.CafeListViewModel;
import com.libra.view.CircleTransform;
import com.patloew.rxlocation.RxLocation;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.Disposable;

public class PhotoActivity extends BaseActivity implements View.OnClickListener, ResultCallback<Status>, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String USER_NAME = "userName";
    private static final String USER_GENDER = "userGender";
    private static final String CAFE_ID = "cafeId";
    private static final String CAFE_NAME = "cafeName";
    private static final String LATITUDE = "latitude";
    private static final String LOGITUDE = "logitude";

    private static final int CAMERA_REQUEST = 101;
    private static final int CAPTURE_PHOTO = 11;
    private static final int PICK_PHOTO = 12;
    private static final int THUMBNAIL_SIZE = 480;

    private View vAddCircle;
    private ImageView ivHolder, camIcon;
    TextView cafeTitle;
    Uri resultUri;
    Bitmap userPicBitmap = null;
    private CropImageView ivCrop;
    private String mUserName;
    private int mUserGender;
    private String mCafeId, mCafeName;
    double lat = 0, log = 0;
    private AsyncTask mTaskSaveAvatar;
    private GeoFire mGeofireCafe;
    private Cafe mCurrentCafe;
    //    private GoogleApiClient mGoogleApiClient;
    private String[] mParamsForUser;
    private Disposable mDisposableLocation;

    MaterialButton loginButton, genderButton;

    View loginView, genderView, photoView;
    ProgressBar formProgress;

    TextInputEditText userName;
    RadioGroup genderGroup;
    MaterialCheckBox termBox, ageBox;
    TextView termText;
    RxLocation rxLocation;
    LocationRequest locationRequest;


//    public static Intent launchIntent(Context context, String cafeId, String userName, int userGender) {
//        return new Intent(context, PhotoActivity.class).putExtra(USER_NAME, userName)
//                .putExtra(USER_GENDER, userGender)
//                .putExtra(CAFE_ID, cafeId);
//    }

    public static Intent launchIntent(Context context, String cafeId, String cafeName, double latitude, double longitude) {
        return new Intent(context, PhotoActivity.class).putExtra(CAFE_NAME, cafeName)
                .putExtra(CAFE_ID, cafeId).putExtra(LATITUDE, latitude).putExtra(LOGITUDE, longitude);
    }

    @Override
    protected boolean isLogged() {
        return BuildConfig.DEBUG;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.right_in, R.anim.left_out);
        setContentView(R.layout.activity_photo);

        findViewById(R.id.btn_login).setOnClickListener(this);
        findViewById(R.id.btn_back).setOnClickListener(this);
        findViewById(R.id.iv_place_holder).setOnClickListener(this);
//        setupTermsLabel((TextView) findViewById(R.id.tvTerms));

//        vAddCircle = findViewById(R.id.iv_circle);
//        ivCrop = (CropImageView) findViewById(R.id.iv_crop);
        ivHolder = (ImageView) findViewById(R.id.iv_place_holder);


        rxLocation = new RxLocation(this);
        CafeListViewModel cafeListViewModel = new ViewModelProvider(this).get(CafeListViewModel.class);
        cafeListViewModel.init(rxLocation);
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(TimeUnit.SECONDS.toMillis(10));

        loginView = findViewById(R.id.login_view);
        genderView = findViewById(R.id.gender_view);
        photoView = findViewById(R.id.photo_view);
        cafeTitle = findViewById(R.id.cafe_name);
        genderGroup = findViewById(R.id.genderGroup);
        camIcon = findViewById(R.id.cam_icon);
        formProgress = findViewById(R.id.progress_bar);

        loginButton = findViewById(R.id.login_btn);
        genderButton = findViewById(R.id.gender_btn);
        userName = findViewById(R.id.edtName);

        termBox = findViewById(R.id.checkbox_term);
        ageBox = findViewById(R.id.checkbox_age);
        termText = findViewById(R.id.term_text);


        loginButton.setOnClickListener(this);
        genderButton.setOnClickListener(this);

//        mUserName = getIntent().getStringExtra(USER_NAME);
//        mUserGender = getIntent().getIntExtra(USER_GENDER, 0);
        mCafeId = getIntent().getStringExtra(CAFE_ID);
        mCafeName = getIntent().getStringExtra(CAFE_NAME);
        lat = getIntent().getDoubleExtra(LATITUDE, 0);
        log = getIntent().getDoubleExtra(LOGITUDE, 0);

        cafeTitle.setText(mCafeName);

        loginView.setVisibility(View.VISIBLE);
        genderView.setVisibility(View.GONE);
        photoView.setVisibility(View.GONE);
        formProgress.setProgress(33);

        mGeofireCafe = FBHelper.getLocationsCafe();
        readLocationCafe();
        termText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TermsActivity.launch(PhotoActivity.this);
            }
        });
//        mGoogleApiClient = buildGoogleApiClient();
    }

    @Override
    protected void onDestroy() {
        if (mTaskSaveAvatar != null) {
            AsyncTask.Status status = mTaskSaveAvatar.getStatus();
            if (status != AsyncTask.Status.FINISHED) {
                mTaskSaveAvatar.cancel(true);
            }
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        findViewById(R.id.btn_back).performClick();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
//        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.left_in, R.anim.right_out);
    }

    private GoogleApiClient buildGoogleApiClient() {
        return new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    private void readLocationCafe() {
        mGeofireCafe.getLocation(mCafeId, mLocationCallback);
    }

    private LocationCallback mLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(String key, GeoLocation location) {
            mCurrentCafe = new Cafe();
            mCurrentCafe.setId(key);
            mCurrentCafe.setLocation(location);
        }

        @Override
        public void onCancelled(DatabaseError firebaseError) {
            LOG.e(new Exception(firebaseError.getMessage()));
            //TODO handler UI
        }
    };

    private void showLoginErrorScreen(Cafe cafe, String message) {
        CafeLoginErrorDialog dialog = CafeLoginErrorDialog.Companion.instance(cafe, message);
        dialog.show(getSupportFragmentManager(), "INFO");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                if (getUserName().equalsIgnoreCase(Const.PLAYUSERNAMEMALE) || getUserName().equalsIgnoreCase(Const.PLAYUSERNAMEFEMALE)) {
                    FirebaseAnalytics.getInstance(this).logEvent(FirebaseEvents.CLICKED_ON_ENTER, null);
                    enter();
                } else {

                    if (rxLocation != null) {


                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        showPd();
                        mDisposableLocation = rxLocation.location().updates(locationRequest)
                                .flatMap(location -> rxLocation.geocoding().fromLocation(location).toObservable())
                                .subscribe(address -> {
                                    mDisposableLocation.dispose();
                                    Double distance = GeoUtils.distance(new GeoLocation(lat, log), new GeoLocation(address.getLatitude(), address.getLongitude()));
                                    dismissPd();
                                    if (distance <= 500.0 && distance >= 0.0) {
                                        FirebaseAnalytics.getInstance(this).logEvent(FirebaseEvents.CLICKED_ON_ENTER, null);
                                        enter();
                                    } else {
                                        Cafe cafe = new Cafe();
                                        cafe.setName(mCafeName);
                                        cafe.setId(mCafeId);
                                        showLoginErrorScreen(cafe, getString(R.string.you_are_not_in_cafe));
                                    }
                                });


                    } else {
                        Toast.makeText(this, "invalid location", Toast.LENGTH_SHORT).show();
                    }
                }


//                FirebaseAnalytics.getInstance(this).logEvent(FirebaseEvents.CLICKED_ON_ENTER, null);
//                enter();
                break;
            case R.id.btn_back:
                if (loginView.getVisibility() == View.VISIBLE) {
                    finish();
                } else if (genderView.getVisibility() == View.VISIBLE) {
                    formProgress.setProgress(33);
                    photoView.setVisibility(View.GONE);
                    genderView.setVisibility(View.GONE);
                    loginView.setVisibility(View.VISIBLE);
                } else if (photoView.getVisibility() == View.VISIBLE) {
                    formProgress.setProgress(66);
                    photoView.setVisibility(View.GONE);
                    genderView.setVisibility(View.VISIBLE);
                    loginView.setVisibility(View.GONE);
                } else {
                    finish();
                }
                break;
            case R.id.iv_place_holder:
                addPhoto();
                break;
            case R.id.login_btn:
                if (userName.getText() == null || TextUtils.isEmpty(userName.getText())) {
                    ((TextInputLayout) userName.getParent().getParent()).setError(getString(R.string.enter_you_name));
                } else if (userName.getText().toString().length() > Const.USERNAMELIMIT) {
                    Toast.makeText(this, getString(R.string.nikename_is_limited, Const.USERNAMELIMIT + ""), Toast.LENGTH_SHORT).show();
                } else if (!termBox.isChecked()) {
                    Toast.makeText(this, getString(R.string.agree_message_login), Toast.LENGTH_SHORT).show();
                } else if (!ageBox.isChecked()) {
                    Toast.makeText(this, getString(R.string.age_message_login), Toast.LENGTH_SHORT).show();
                } else {
                    PrefHelper.markAs18YearsOld(PhotoActivity.this, ageBox.isChecked());
                    hideKeyboard();
                    formProgress.setProgress(66);
                    photoView.setVisibility(View.GONE);
                    genderView.setVisibility(View.VISIBLE);
                    loginView.setVisibility(View.GONE);
                }

                break;
            case R.id.gender_btn:
                if (genderGroup.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(this, R.string.select_you_gender, Toast.LENGTH_SHORT).show();
                } else {

                    if (resultUri != null) {
                        ((MaterialButton) findViewById(R.id.btn_login)).getBackground().setTint(ContextCompat.getColor(this, R.color.yellow_btn));
                        ((MaterialButton) findViewById(R.id.btn_login)).getIcon().setVisible(true, true);
                    } else {
                        ((MaterialButton) findViewById(R.id.btn_login)).getBackground().setTint(ContextCompat.getColor(this, R.color.grey_d7));
                        ((MaterialButton) findViewById(R.id.btn_login)).getIcon().setVisible(false, false);
                    }
                    formProgress.setProgress(95);
                    photoView.setVisibility(View.VISIBLE);
                    genderView.setVisibility(View.GONE);
                    loginView.setVisibility(View.GONE);
                }
                break;
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(userName.getRootView().getWindowToken(), 0);
    }

    private void showAgeDialog() {
        if (PrefHelper.is18YearsOld(this)) {
            cropImageAndRegisterUser();
        } else {
            AgeDialog dialog = AgeDialog.newInstance();
            dialog.listener = new AgeDialog.Listener() {
                @Override
                public void onNoClicked() {
                    startActivity(CafeActivity.launchIntent(PhotoActivity.this));
                    PhotoActivity.this.finish();
                }

                @Override
                public void onYesClicked() {
                    PrefHelper.markAs18YearsOld(PhotoActivity.this, true);
                    cropImageAndRegisterUser();
                }
            };
            dialog.show(getSupportFragmentManager(), "");
        }
    }

    private void setupTermsLabel(TextView textView) {
        String clickableLabel = getString(R.string.terms_of_use_clickable);
        int startPos = textView.getText().toString().indexOf(clickableLabel);
        int endPos = startPos + clickableLabel.length();
        int spanType = Spannable.SPAN_EXCLUSIVE_EXCLUSIVE;

        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(getResources().getColor(android.R.color.transparent));

        SpannableString spannable = new SpannableString(textView.getText());
        spannable.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View view) {
                TermsActivity.launch(PhotoActivity.this);
            }
        }, startPos, startPos + clickableLabel.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.color_accent)), startPos, endPos, spanType);
        spannable.setSpan(new UnderlineSpan(), startPos, endPos, spanType);
        textView.setText(spannable);
    }

    private void addPhoto() {
        if (!Tools.isExternalStorageWritable()) {
            Toast.makeText(this, R.string.storage_not_available, Toast.LENGTH_SHORT).show();
            return;
        }

        if (Tools.hasCamera(this)) {
            requestCameraPermission();
        } else {
            startPickStorageImage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showPickerDialog();
            } else {
                startPickStorageImage();
            }
        }
    }

    private void requestCameraPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.CAMERA
            }, CAMERA_REQUEST);
        } else {
            showPickerDialog();
        }
    }

    private void showPickerDialog() {
        String[] pickerItems = {
                getString(R.string.photo_from_camera), getString(R.string.photo_from_storage)
        };
        new AlertDialog.Builder(this).setItems(pickerItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                if (which == 0) {
                    startCaptureImage();
                } else {
                    startPickStorageImage();
                }
            }
        }).show();
    }

    private void startCaptureImage() {
        Intent startIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
        startIntent.putExtra(MediaStore.EXTRA_OUTPUT, Tools.getUriAvatar(this, true));
        startIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (startIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(startIntent, CAPTURE_PHOTO);
        }
    }

    private void startPickStorageImage() {
//        CropImage.activity()
//                .setGuidelines(CropImageView.Guidelines.ON)
//                .start(this);
        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        if (pickIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(pickIntent, PICK_PHOTO);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        Uri uri;
        switch (requestCode) {
            case CAPTURE_PHOTO:
                uri = Tools.getUriAvatar(this, false);
                CropImage.activity(uri).setCropShape(CropImageView.CropShape.OVAL).setAspectRatio(1, 1).start(this);
                break;
            case PICK_PHOTO:
                uri = data.getData();
                CropImage.activity(uri).setCropShape(CropImageView.CropShape.OVAL).setAspectRatio(1, 1).start(this);
                break;
            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                camIcon.setVisibility(View.GONE);
                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                resultUri = result.getUri();
                try {
                    userPicBitmap = getThumbnail(resultUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Picasso.get().load(resultUri).transform(new CircleTransform()).into(ivHolder);
                ivHolder.setImageURI(resultUri);

                if (resultUri != null) {
                    ((MaterialButton) findViewById(R.id.btn_login)).getBackground().setTint(ContextCompat.getColor(this, R.color.yellow_btn));
                    ((MaterialButton) findViewById(R.id.btn_login)).getIcon().setVisible(true, true);
                } else {
                    ((MaterialButton) findViewById(R.id.btn_login)).getBackground().setTint(ContextCompat.getColor(this, R.color.grey_d7));
                    ((MaterialButton) findViewById(R.id.btn_login)).getIcon().setVisible(false, false);
                }
                break;
            default:
                return;
        }


//        ivHolder.setVisibility(View.GONE);
//        ivHolder.setEnabled(false);
////        vAddCircle.setVisibility(View.GONE);
//        ivCrop.setVisibility(View.VISIBLE);
//        ivCrop.setImageUriAsync(uri);
    }

    private void enter() {
//        Log.e("TAGS", "data " + (mCurrentCafe == null) + " bitm " + (!mGoogleApiClient.isConnected()));
//        if (mCurrentCafe == null || !mGoogleApiClient.isConnected()) {
//            mGoogleApiClient.connect();
//            Toast.makeText(this, R.string.try_few_seconds, Toast.LENGTH_SHORT).show();
//        } else
        if (resultUri != null && userPicBitmap != null) {
            showAgeDialog();
        } else {
            Toast.makeText(this, R.string.add_photo, Toast.LENGTH_SHORT).show();
        }
    }

    private void cropImageAndRegisterUser() {

        showPd();
        mTaskSaveAvatar = new CreateUserAsyncTask(PhotoActivity.this, userPicBitmap).execute();
//        ivCrop.setOnCropImageCompleteListener(new CropImageView.OnCropImageCompleteListener() {
//            @Override
//            public void onCropImageComplete(CropImageView view, CropImageView.CropResult result) {
//                mTaskSaveAvatar = new CreateUserAsyncTask(PhotoActivity.this, result.getBitmap()).execute();
//            }
//        });
//        checkAndResize(ivCrop);
    }

    private void checkAndResize(CropImageView iv) {
        Rect rect = iv.getCropRect();
        int width = rect.width();
        int height = rect.height();
        float ratio = width / (float) height;
        if (width >= height) {
            width = width > Const.MAX_SIDE_AVATAR_IN_PX ? Const.MAX_SIDE_AVATAR_IN_PX : width;
            height = (int) (width / ratio);
        } else {
            height = height > Const.MAX_SIDE_AVATAR_IN_PX ? Const.MAX_SIDE_AVATAR_IN_PX : height;
            width = (int) (height * ratio);
        }
        iv.getCroppedImageAsync(width, height);
    }

    private CropImageView getCropImageView() {
        return ivCrop;
    }

    private String getUserName() {
        return userName.getText().toString();
    }

    private int getUserGender() {
        return (genderGroup.getCheckedRadioButtonId() == R.id.male_check) ? 0 : 1;
    }

    private String getCafeId() {
        return mCafeId;
    }

    @SuppressWarnings("MissingPermission")
    private void createGeofence() {

        Geofence geofence = new Geofence.Builder().setRequestId(mCurrentCafe.getId())
                .setCircularRegion(mCurrentCafe.getLocation().latitude, mCurrentCafe.getLocation().longitude, Const.RADIUS_FOR_GEOFENCE_IN_METER)
                .setLoiteringDelay(Const.TIMEOUT_GEOFENCE_DWELL)
                .setExpirationDuration(Const.TIMEOUT_GEOFENCE_DWELL + 30 * 1000)
                .setNotificationResponsiveness(Const.TIMEOUT_GEOFENCE_NOTIFY_RESPONSIVENESS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();

        GeofencingRequest geofenceRequest = new GeofencingRequest.Builder().setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();

//        Intent intent = GeofenceIntentService.getDefaultIntent(this);
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        PendingIntent pIntent;
        pIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        LocationServices.getGeofencingClient(this).addGeofences(geofenceRequest, pIntent).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                PrefHelper.setAppId(PhotoActivity.this, mParamsForUser[0]);
                final User user = new User();
                user.setPushToken(mParamsForUser[1]);
                user.setImageUrl(mParamsForUser[2]);
                user.setCafeId(getCafeId());
                user.setName(getUserName());
                user.setGender(getUserGender());
                FBHelper.getUsersFromCafeRoom(getCafeId())
                        .push()
                        .setValue(user, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                                if (firebase != null) {
                                    user.setId(firebase.getKey());
                                    PrefHelper.setUser(PhotoActivity.this, user);
                                    dismissPd();
                                    startLogoutIntent();
                                    startActivity(ChatActivity.launchIntent(PhotoActivity.this));
                                } else {
                                    LOG.e(new Exception(firebaseError.getMessage()));
                                    dismissPd();
                                    Toast.makeText(PhotoActivity.this, R.string.error, Toast.LENGTH_LONG).show();
                                    //TODO handler error
                                }
                            }
                        });
                user.setLoginedTime(System.currentTimeMillis() / 1000);
                FBHelper.getUsersFromCafeRoomStat(getCafeId())
                        .push()
                        .setValue(user, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {

                            }
                        });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("TAGS", "error is " + e + " mes " + e.getMessage());
                AlertDialog.Builder builder = new AlertDialog.Builder(PhotoActivity.this).setItems(new String[]{
                        e.getMessage()
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
                dismissPd();
                Toast.makeText(PhotoActivity.this, R.string.try_few_seconds, Toast.LENGTH_SHORT).show();
            }
        });

//        LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, geofenceRequest, pIntent)
//                .setResultCallback(this);
    }

    void obtainDataForUser(String[] params) {
        mParamsForUser = params;

        PrefHelper.setAppId(PhotoActivity.this, mParamsForUser[0]);
        final User user = new User();
        user.setPushToken(mParamsForUser[1]);
        user.setImageUrl(mParamsForUser[2]);
        user.setCafeId(getCafeId());
        user.setName(getUserName());
        user.setGender(getUserGender());
        FBHelper.getUsersFromCafeRoom(getCafeId())
                .push()
                .setValue(user, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                        if (firebase != null) {
                            user.setId(firebase.getKey());
                            PrefHelper.setUser(PhotoActivity.this, user);
                            dismissPd();
                            startLogoutIntent();
                            startActivity(ChatActivity.launchIntent(PhotoActivity.this));
                        } else {
                            LOG.e(new Exception(firebaseError.getMessage()));
                            dismissPd();
                            Toast.makeText(PhotoActivity.this, R.string.error, Toast.LENGTH_LONG).show();
                            //TODO handler error
                        }
                    }
                });
        user.setLoginedTime(System.currentTimeMillis() / 1000);
        FBHelper.getUsersFromCafeRoomStat(getCafeId())
                .push()
                .setValue(user, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {

                    }
                });

//        createGeofence();
    }

    @Override
    public void onResult(@NonNull Status status) {
        LOG.i("geofence onResult - " + status.toString());
        if (status.isSuccess()) {
            PrefHelper.setAppId(this, mParamsForUser[0]);
            final User user = new User();
            user.setPushToken(mParamsForUser[1]);
            user.setImageUrl(mParamsForUser[2]);
            user.setCafeId(getCafeId());
            user.setName(getUserName());
            user.setGender(getUserGender());
            FBHelper.getUsersFromCafeRoom(getCafeId())
                    .push()
                    .setValue(user, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                            if (firebase != null) {
                                user.setId(firebase.getKey());
                                PrefHelper.setUser(PhotoActivity.this, user);
                                dismissPd();
                                startLogoutIntent();
                                startActivity(ChatActivity.launchIntent(PhotoActivity.this));
                            } else {
                                LOG.e(new Exception(firebaseError.getMessage()));
                                dismissPd();
                                Toast.makeText(PhotoActivity.this, R.string.error, Toast.LENGTH_LONG).show();
                                //TODO handler error
                            }
                        }
                    });
            user.setLoginedTime(System.currentTimeMillis() / 1000);
            FBHelper.getUsersFromCafeRoomStat(getCafeId())
                    .push()
                    .setValue(user, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {

                        }
                    });
        } else {
//            LOG.e(new Exception(status.toString()));
            dismissPd();
            Toast.makeText(this, R.string.try_few_seconds, Toast.LENGTH_SHORT).show();

        }
    }


    private void startLogoutIntent() {
        Intent intent = GeofenceIntentService.getLogoutIntent(this);
        PendingIntent pIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                    + Const.TIMEOUT_GEOFENCE_DWELL, pIntent);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            am.setExact(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                    + Const.TIMEOUT_GEOFENCE_DWELL, pIntent);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()
                    + Const.TIMEOUT_GEOFENCE_DWELL, pIntent);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        LOG.i("onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        LOG.i("onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        LOG.e(new Exception(connectionResult.toString()));
    }

    private static class CreateUserAsyncTask extends AsyncTask<Void, Void, String[]> {

        private final Logger LOG = Logger.getLogger(getClass().getSimpleName(), true);
        private WeakReference<PhotoActivity> mRefActivity;
        private Bitmap mBitmap;
        String pushToken = null;

        CreateUserAsyncTask(PhotoActivity activity, Bitmap bitmap) {
            this.mRefActivity = new WeakReference<>(activity);
            this.mBitmap = bitmap;
        }

        @Override
        protected void onPreExecute() {
            PhotoActivity activity = mRefActivity.get();
            if (activity == null) {
                return;
            }
            activity.showPd();
        }

        @Override
        protected String[] doInBackground(Void... params) {
            PhotoActivity activity = mRefActivity.get();
            if (activity == null || mBitmap == null) {
                return null;
            }


            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (!task.isSuccessful()) {
                        return;
                    }

                    // Get new Instance ID token
                    if (task.getResult() != null) {
                        String token = task.getResult();
                        System.out.println(">>>>> TOKEN >>>>>" + token);
                        // add this token to Firebase
                        pushToken = token;
                    }
                }
            });
//            pushToken = FirebaseInstanceId.getInstance().getToken();
            String instanceId = Tools.getDeviceID(activity);

            String avatarUrl;
            try {
                avatarUrl = CloudHelper.uploadImage(activity, mBitmap);
            } catch (IOException e) {
                e.printStackTrace();
                LOG.e(e);
                return null;
            }
            return new String[]{instanceId, pushToken, avatarUrl};
        }

        @Override
        protected void onPostExecute(final String[] params) {
            final PhotoActivity activity = mRefActivity.get();
            if (activity == null) {
                return;
            }

            if (params == null) {
                Toast.makeText(activity, R.string.login_error, Toast.LENGTH_SHORT).show();
                activity.dismissPd();
                return;
            }
            activity.obtainDataForUser(params);
        }

        private Bitmap checkAndResize(CropImageView iv) {
            Rect rect = iv.getCropRect();
            int width = rect.width();
            int height = rect.height();
            float ratio = width / (float) height;
            if (width >= height) {
                width = width > Const.MAX_SIDE_AVATAR_IN_PX ? Const.MAX_SIDE_AVATAR_IN_PX : width;
                height = (int) (width / ratio);
            } else {
                height = height > Const.MAX_SIDE_AVATAR_IN_PX ? Const.MAX_SIDE_AVATAR_IN_PX : height;
                width = (int) (height * ratio);
            }
            return iv.getCroppedImage(width, height);
        }
    }

    public static class PickerDialog extends BaseDialogFragment {
        public static BaseDialogFragment newInstance() {
            Bundle args = new Bundle();
            BaseDialogFragment fragment = new PickerDialog();
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setItems(new String[]{
                    getString(R.string.photo_from_camera), getString(R.string.photo_from_storage)
            }, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        ((PhotoActivity) getActivity()).startCaptureImage();
                    } else {
                        ((PhotoActivity) getActivity()).startPickStorageImage();
                    }
                }
            });
            return builder.create();
        }
    }

    public static class AgeDialog extends BaseDialogFragment implements View.OnClickListener {

        public Listener listener = null;

        public static AgeDialog newInstance() {
            Bundle args = new Bundle();
            AgeDialog fragment = new AgeDialog();
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            View v = LayoutInflater.from(getContext())
                    .inflate(R.layout.dialog_logout, (ViewGroup) getView());
            TextView tvMessage = v.findViewById(R.id.tvMessage);
            tvMessage.setText(R.string.age_dialog_message);
            v.findViewById(R.id.iv_close).setVisibility(View.GONE);
            v.findViewById(R.id.btn_negative).setOnClickListener(this);
            v.findViewById(R.id.btn_positive).setOnClickListener(this);
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.ThemeDialog_Transparent)
                    .setView(v);
            return builder.setCancelable(false).create();
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_negative:
                    if (listener != null) listener.onNoClicked();
                    dismiss();
                    break;
                case R.id.btn_positive:
                    if (listener != null) listener.onYesClicked();
                    dismiss();
                    break;
            }
        }

        public interface Listener {
            void onNoClicked();

            void onYesClicked();
        }
    }

    public Bitmap getThumbnail(Uri uri) throws FileNotFoundException, IOException {
        InputStream input = getContentResolver().openInputStream(uri);

        BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
        onlyBoundsOptions.inJustDecodeBounds = true;
        onlyBoundsOptions.inDither = true;//optional
        onlyBoundsOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
        BitmapFactory.decodeStream(input, null, onlyBoundsOptions);
        input.close();

        if ((onlyBoundsOptions.outWidth == -1) || (onlyBoundsOptions.outHeight == -1)) {
            return null;
        }

        int originalSize = (onlyBoundsOptions.outHeight > onlyBoundsOptions.outWidth) ? onlyBoundsOptions.outHeight : onlyBoundsOptions.outWidth;

        double ratio = (originalSize > THUMBNAIL_SIZE) ? (originalSize / THUMBNAIL_SIZE) : 1.0;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = getPowerOfTwoForSampleRatio(ratio);
        bitmapOptions.inDither = true; //optional
        bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//
        input = this.getContentResolver().openInputStream(uri);
        Bitmap bitmap = BitmapFactory.decodeStream(input, null, bitmapOptions);
        input.close();
        return bitmap;
    }

    private static int getPowerOfTwoForSampleRatio(double ratio) {
        int k = Integer.highestOneBit((int) Math.floor(ratio));
        if (k == 0) return 1;
        else return k;
    }
}
