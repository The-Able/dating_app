package com.android.libramanage.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.DialogFragment;

import com.android.libramanage.R;
import com.android.libramanage.entity.Cafe;
import com.android.libramanage.firebase.FBHelper;
import com.android.libramanage.support.CloudHelper;
import com.android.libramanage.support.Tools;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.LocationCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteActivity;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CafeDetailsFragment extends BaseFragment implements View.OnClickListener {

    private static final String CAFE_ID = "cafeId";
    private static final String REQUEST_MAP = "geo:0,0?q=%f,%f(%s)";
    private static final String PACKAGE_G_MAPS = "com.google.android.apps.maps";

    private static final int REQUEST_LOCATION_PICKER = 10;
    private static final int CAPTURE_PHOTO = 11;
    private static final int PICK_PHOTO = 12;

    private EditText edtCafeName;
    private EditText edtCafeAddress;
    private AppCompatSpinner placeType;
    private Button btnAddGP;
    private ViewSwitcher mSwitcher;
    private String mCafeId;
    private Cafe mCafe;
    private GeoLocation mChooserGeoLoc;
    private ImageView ivCafe;
    private FrameLayout mFlImageCafe;
    private Bitmap mCafeBitmap;
    private boolean isActionMode;
    private AsyncTask mLoadRemoveImageTask;
    List<String> placeTypeValue=Arrays.asList("Bar","Restaurant","Parks","Gym","Office","Shopping","Coffee");

    public static BaseFragment newInstance(String cafeId) {
        Bundle args = new Bundle();
        args.putString(CAFE_ID, cafeId);
        BaseFragment fragment = new CafeDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getTitleId() {
        return 0;
    }

    @Override
    protected boolean isLogged() {
        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_cafe_details;
    }

    @Override
    protected int getMenuId() {
        return R.menu.cafe_details;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCafeId = getArguments().getString(CAFE_ID);

        if (getContext() != null) {
            // initialize the places sdk
            Places.initialize(getContext(), getString(R.string.places_key));
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mSwitcher = (ViewSwitcher) view.findViewById(R.id.switcher);
        edtCafeName = (EditText) view.findViewById(R.id.edt_cafe_name);
        edtCafeAddress = (EditText) view.findViewById(R.id.edt_cafe_address);
        placeType = (AppCompatSpinner) view.findViewById(R.id.place_val);
        btnAddGP = (Button) view.findViewById(R.id.btn_location);
        btnAddGP.setOnClickListener(this);
        ivCafe = (ImageView) view.findViewById(R.id.iv_cafe);
        mFlImageCafe = (FrameLayout) view.findViewById(R.id.fl_image_cafe);
        mFlImageCafe.setEnabled(false);
        mFlImageCafe.setOnClickListener(this);

        ArrayAdapter myAdapter = new ArrayAdapter<String>(getContext(),
               R.layout.simple_dropdown_item_custom, placeTypeValue);
        placeType.setAdapter(myAdapter );



    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (TextUtils.isEmpty(mCafeId)) {
            getToolbar().startActionMode(mCallbackActionMode);
            mSwitcher.setDisplayedChild(1);
        } else {
            loadCafe(mCafeId);
        }
    }

    @Override
    public void onDestroyView() {
        if (mLoadRemoveImageTask != null) {
            mLoadRemoveImageTask.cancel(true);
        }
        super.onDestroyView();
    }

    private void chooseNewImageCafe() {
        if (!Tools.isExternalStorageWritable()) {
            Toast.makeText(getContext(), R.string.storage_not_available, Toast.LENGTH_SHORT).show();
            return;
        }

        if (Tools.hasCamera(getContext())) {
            PickerDialog.newInstance().show(getChildFragmentManager(), null);
        } else {
            startPickStorageImage();
        }
    }

    private void startCaptureImage() {
        Intent startIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startIntent.putExtra("android.intent.extras.CAMERA_FACING", 1);
        startIntent.putExtra(MediaStore.EXTRA_OUTPUT, Tools.getUriAvatar(getContext(), true));
        if (startIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(startIntent, CAPTURE_PHOTO);
        }
    }

    private void startPickStorageImage() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");

        if (pickIntent.resolveActivity(getContext().getPackageManager()) != null) {
            startActivityForResult(pickIntent, PICK_PHOTO);
        }
    }

    @Override
    public void onClick(View v) {
        assert getActivity() != null;
        if (v.getId() == R.id.fl_image_cafe) {
            chooseNewImageCafe();
            return;
        }

        if (isActionMode) {
            // Set the fields to specify which types of place data to
            // return after the user has made a selection.
            List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

            // Start the autocomplete intent.
            Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                    .build(getActivity());
            startActivityForResult(intent, REQUEST_LOCATION_PICKER);
        } else if (mCafe != null) {

            GeoLocation loc = mCafe.getGeoLoc();
            if (loc != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setPackage(PACKAGE_G_MAPS);
                intent.setData(Uri.parse(String.format(Locale.ENGLISH, REQUEST_MAP, loc.latitude, loc.longitude, mCafe
                        .getName())));
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Toast.makeText(getContext(), R.string.install_g_maps, Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.remove:
                RemoveDialog.newInstance().show(getChildFragmentManager(), null);
                return true;
            case R.id.edit:
                getToolbar().startActionMode(mCallbackActionMode);
                return true;
        }
        return super.onMenuItemClick(item);
    }

    private void startActionMode(boolean isStart) {
        isActionMode = isStart;
        edtCafeAddress.setEnabled(isStart);
        edtCafeName.setEnabled(isStart);
        mFlImageCafe.setEnabled(isStart);
        if (isStart) {
            btnAddGP.setText(R.string.set_location_cafe);
            edtCafeName.requestFocus();
            edtCafeName.setSelection(edtCafeName.getText().length());
        } else {
            btnAddGP.setText(R.string.show_location_cafe);
        }
    }

    private ActionMode.Callback mCallbackActionMode = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            LOG.i("action mode onCreate");
            mode.getMenuInflater().inflate(R.menu.cafe_details_action_mode, menu);
            startActionMode(true);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            LOG.i("action mode onPrepareActionMode");
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            LOG.i("action mode onActionItemClicked");
            if (item.getItemId() == R.id.accept) {
                checkCafeInfo(mode);
                return true;
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            LOG.i("action mode onDestroyActionMode");
            if (mCafe != null) {
                bindInfo(mCafe);
            }
            startActionMode(false);
        }
    };

    private void bindInfo(Cafe cafe) {
        edtCafeName.setText(cafe.getName());
        edtCafeAddress.setText(cafe.getAddress());
        setTitle(cafe.getName());

        int place=0;
        if(cafe.getPlacesType()!=null){
            place=placeTypeValue.indexOf(cafe.getPlacesType());
        }

        placeType.setSelection(place,false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_LOCATION_PICKER:
                if (resultCode == Activity.RESULT_OK) {
                    Place place = Autocomplete.getPlaceFromIntent(data);
                    if (place.getLatLng() != null)
                        mChooserGeoLoc = new GeoLocation(place.getLatLng().latitude, place.getLatLng().longitude);
                    edtCafeAddress.setText(place.getAddress());
                    edtCafeName.setText(place.getName());
                    edtCafeName.requestFocus();
                    edtCafeName.setSelection(edtCafeName.getText().length());
                } else if (resultCode == AutocompleteActivity.RESULT_ERROR) {
                    Status status = Autocomplete.getStatusFromIntent(data);
                    Log.i("Places", status.getStatusMessage());
                }
                break;

            case CAPTURE_PHOTO:
                if (resultCode == Activity.RESULT_OK && getContext() != null) {
                    Uri uri = Tools.getUriAvatar(getContext(), false);
                    if (uri == null) {
                        return;
                    }
                    loadImageFromCamera(uri);
                }
                break;
            case PICK_PHOTO:
                if (resultCode == Activity.RESULT_OK && getContext() != null) {
                    try {
                        mCafeBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), data.getData());
                        ivCafe.setImageBitmap(mCafeBitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void loadImageFromCamera(Uri uri) {
        InputStream stream = null;
        try {
            ContentResolver resolver = getContext().getContentResolver();
            stream = resolver.openInputStream(uri);
            mCafeBitmap = BitmapFactory.decodeStream(stream, new Rect(0, 0, 0, 0), null);
            ivCafe.setImageBitmap(mCafeBitmap);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load sampled bitmap", e);
        } finally {
            closeSafe(stream);
        }
    }

    private void closeSafe(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void checkCafeInfo(final ActionMode mode) {
        String name = edtCafeName.getText().toString();
        String address = edtCafeAddress.getText().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(address) || mChooserGeoLoc == null) {
            Toast.makeText(getContext(), R.string.cafe_name_and_address, Toast.LENGTH_SHORT).show();
            return;
        }

        if (mCafe == null) {
            mCafe = new Cafe();
        }

        mCafe.setMode(mode);
        mCafe.setName(name);
        mCafe.setAddress(address);
        mCafe.setGeoLoc(mChooserGeoLoc);
        mCafe.setPlacesType(placeTypeValue.get(placeType.getSelectedItemPosition()));

        showPd();
        mLoadRemoveImageTask = new LoadRemoveImageTask(this, mCafe).execute(mCafeBitmap);
    }

    private void loadCafe(final String cafeId) {
        FBHelper.getCafe().child(cafeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Cafe cafe = dataSnapshot.getValue(Cafe.class);
                cafe.setId(dataSnapshot.getKey());
                loadLocationCafe(cafe);
                Picasso.get()
                        .load(cafe.getImageUrl())
                        .fit()
                        .centerCrop()
                        .placeholder(R.drawable.ic_restaurant_menu_24dp)
                        .error(R.drawable.ic_restaurant_menu_24dp)
                        .into(ivCafe);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                LOG.e(new Exception(firebaseError.getMessage()));
                mSwitcher.setDisplayedChild(1);
                //TODO handler error
            }
        });
    }

    private void loadLocationCafe(final Cafe cafe) {
        FBHelper.getLocationsCafe().getLocation(cafe.getId(), new LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                cafe.setGeoLoc(location);
                mCafe = cafe;
                bindInfo(cafe);
                mChooserGeoLoc = cafe.getGeoLoc();
                mSwitcher.setDisplayedChild(1);
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
                LOG.e(new Exception(firebaseError.getMessage()));
                mSwitcher.setDisplayedChild(1);
                //TODO handler error
            }
        });
    }

    protected void saveNewCafe(Cafe cafe) {
        HashMap<String,Object> valueUpdat=new HashMap<>();
        valueUpdat.put("address",cafe.getAddress());
        valueUpdat.put("imageUrl",cafe.getImageUrl());
        valueUpdat.put("name",cafe.getName());
        valueUpdat.put("placesType",cafe.getPlacesType());
        if(cafe.getPlacesId()!=null) {
            valueUpdat.put("placesType", cafe.getPlacesId());
        }
        FBHelper.getCafe().push().setValue(valueUpdat, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                if (firebase != null) {
                    mCafe.setId(firebase.getKey());
                    saveNewCafeRoom(mCafe);
                } else {
                    LOG.e(new Exception(firebaseError.getMessage()));
                    dismissPd();
                    //TODO handler ui
                }
            }
        });
    }

    private void saveNewCafeRoom(final Cafe cafe) {
        Map<String, Object> mapCafeRoom = new HashMap<>();
        mapCafeRoom.put(Cafe.NAME, cafe.getName());
        FBHelper.getCafeRooms()
                .child(cafe.getId())
                .setValue(mapCafeRoom, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                        if (firebase != null) {
                            saveLocationCafe(cafe);
                        } else {
                            LOG.e(new Exception(firebaseError.getMessage()));
                            dismissPd();
                            //TODO handler ui
                        }
                    }
                });
    }

    private void saveLocationCafe(final Cafe cafe) {
        FBHelper.getLocationsCafe()
                .setLocation(cafe.getId(), cafe.getGeoLoc(), new GeoFire.CompletionListener() {
                    @Override
                    public void onComplete(String key, DatabaseError error) {
                        dismissPd();
                        if (error == null) {
                            cafe.getMode().finish();
                        } else {
                            LOG.e(new Exception(error.getMessage()));
                            //TODO handler ui
                        }
                    }
                });
    }

    protected void updateCurrentCafe(final Cafe cafe) {
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put(Cafe.NAME, cafe.getName());
        updateMap.put(Cafe.ADDRESS, cafe.getAddress());
        updateMap.put(Cafe.IMAGE_URL, cafe.getImageUrl());
        updateMap.put(Cafe.placeType, cafe.getPlacesType());
        FBHelper.getCafe()
                .child(cafe.getId())
                .updateChildren(updateMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                        if (firebase != null) {
                            updateCurrentCafeRoom(cafe);
                        } else {
                            LOG.e(new Exception(firebaseError.getMessage()));
                            dismissPd();
                            //TODO handler ui
                        }
                    }
                });
    }

    private void updateCurrentCafeRoom(final Cafe cafe) {
        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put(Cafe.NAME, cafe.getName());
        FBHelper.getCafeRooms()
                .child(cafe.getId())
                .updateChildren(updateMap, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                        if (firebase != null) {
                            saveLocationCafe(cafe);
                        } else {
                            LOG.e(new Exception(firebaseError.getMessage()));
                            dismissPd();
                            //TODO handler ui
                        }
                    }
                });
    }

    void removeCafe() {
        showPd();
        FBHelper.getCafe().child(mCafe.getId()).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                if (firebase != null) {
                    removeCafeRoom(mCafe.getId());
                } else {
                    LOG.e(new Exception(firebaseError.getMessage()));
                    dismissPd();
                    //TODO handler error
                }
            }
        });
    }

    private void removeCafeRoom(final String cafeId) {
        FBHelper.getCafeRooms().child(cafeId).removeValue(new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError firebaseError, DatabaseReference firebase) {
                if (firebase != null) {
                    removeLocationCafe(cafeId);
                } else {
                    LOG.e(new Exception(firebaseError.getMessage()));
                    dismissPd();
                    //TODO handler error
                }
            }
        });
    }

    private void removeLocationCafe(String cafeId) {
        FBHelper.getLocationsCafe().removeLocation(cafeId, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                dismissPd();
                if (error == null) {
                    onBackPress();
                } else {
                    LOG.e(new Exception(error.getMessage()));
                    //TODO handler error
                }
            }
        });
    }

    private static class LoadRemoveImageTask extends AsyncTask<Bitmap, Void, Cafe> {

        private WeakReference<CafeDetailsFragment> mWeakFg;
        private Cafe mCafe;

        public LoadRemoveImageTask(CafeDetailsFragment fg, Cafe cafe) {
            this.mWeakFg = new WeakReference<CafeDetailsFragment>(fg);
            this.mCafe = cafe;
        }

        @Override
        protected Cafe doInBackground(Bitmap... params) {
            Bitmap bitmap = params[0];
            if (bitmap != null) {
                CafeDetailsFragment fg = mWeakFg.get();
                if (fg == null) {
                    return mCafe;
                }
                String newImageUrl;
                try {
                    if (!TextUtils.isEmpty(mCafe.getImageUrl())) {
                        CloudHelper.destroyImage(fg.getContext(), mCafe.getImageUrl());
                    }
                    newImageUrl = CloudHelper.uploadImage(fg.getContext(), bitmap);
                    mCafe.setImageUrl(newImageUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return mCafe;
        }

        @Override
        protected void onPostExecute(Cafe cafe) {
            CafeDetailsFragment fg = mWeakFg.get();
            if (fg == null) {
                return;
            }
            if (TextUtils.isEmpty(cafe.getId())) {
                fg.saveNewCafe(cafe);
            } else {
                fg.updateCurrentCafe(cafe);
            }
        }
    }

    public static class RemoveDialog extends DialogFragment {
        public static RemoveDialog newInstance() {
            Bundle args = new Bundle();
            RemoveDialog fragment = new RemoveDialog();
            fragment.setArguments(args);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setMessage(R.string.remove_cafe)
                    .setNegativeButton(android.R.string.no, null)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((CafeDetailsFragment) getParentFragment()).removeCafe();
                        }
                    });
            return builder.create();
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
                        ((CafeDetailsFragment) getParentFragment()).startCaptureImage();
                    } else {
                        ((CafeDetailsFragment) getParentFragment()).startPickStorageImage();
                    }
                }
            });
            return builder.create();
        }
    }
}
