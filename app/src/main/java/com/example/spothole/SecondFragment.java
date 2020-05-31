package com.example.spothole;

import android.Manifest;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static android.app.Activity.RESULT_OK;

/**
 * Created by user on 12/31/15.
 */
public class SecondFragment extends Fragment {

    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAPTURE_CODE = 1001;
    ImageView mCaptureButton;
    ImageView mImageView;
    Button submitimage, getlocation;
    TextView displayLocation;
    EditText title, description;
    Uri image_uri;
    String url;
    GoogleApiClient gac;
    Location location;
    public static final String FB_STORAGE_PATH = "images/";
    public static final String FB_DATABASE_PATH = "images";
    TextView postcomplain;

    private FirebaseStorage storage;
    private StorageReference mStorageRef;
    private FirebaseAuth auth;
    private DatabaseReference mDatabaseReference;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    SharedPreferences sp;

    View myView;
    String UserID = auth.getInstance().getCurrentUser().getUid();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        sp = getActivity().getPreferences(Context.MODE_PRIVATE);
        storage = FirebaseStorage.getInstance();
        mStorageRef = storage.getReference();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(FB_DATABASE_PATH);

        myView = inflater.inflate(R.layout.fragment_second, container, false);
        //startActivity(new Intent(getActivity(),postcomplain.class));
        mImageView = (ImageView) myView.findViewById(R.id.setimage);
        mCaptureButton = myView.findViewById(R.id.setimage);
        submitimage = myView.findViewById(R.id.submitimage);
        getlocation = myView.findViewById(R.id.getlocation);
        displayLocation = myView.findViewById(R.id.displayLocation);

        GoogleApiClient.Builder b = new GoogleApiClient.Builder(getActivity());
        b.addApi(LocationServices.API);
        b.addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) this);
        b.addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) this);
        gac = b.build();

        getlocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED ||
                            ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        String[] permission = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        requestPermissions(permission, PERMISSION_CODE);
                    } else {
                        openCamera();
                    }
                } else {
                    openCamera();
                }
            }
        });

        title = (EditText) myView.findViewById(R.id.title);
        description = myView.findViewById(R.id.description);
        submitimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String stitle = title.getText().toString();
                String sdesciption = description.getText().toString();

                if (stitle.length() == 0) {
                    title.setError("Empty!");
                    title.requestFocus();
                    return;
                }

                if (sdesciption.length() == 0) {
                    description.setError("Empty!");
                    description.requestFocus();
                    return;
                }

                String x = sp.getString("n", " ");
                PostComplaint s = new PostComplaint(stitle, sdesciption, x);


                mDatabaseReference.push().setValue(s);
                Toast.makeText(getActivity(), "Request sent successfully", Toast.LENGTH_SHORT).show();

                mCaptureButton.setImageResource(0);
                title.setText("");
                description.setText("");
                title.requestFocus();

                uploadImage();


            }
        });

        return myView;
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Pictures");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        image_uri = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        camera.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        startActivityForResult(camera, IMAGE_CAPTURE_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera();
                } else {
                    Toast.makeText(getActivity(), "Permission denied...", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK) {
            mImageView.setImageURI(image_uri);
        }
    }

    // final String userID = auth.getCurrentUser().getUid();


    private void uploadImage() {
        if (image_uri != null) {
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("Uploading...");
            progressDialog.show();

            StorageReference ref = mStorageRef.child(FB_STORAGE_PATH + UserID + "/" + System.currentTimeMillis() + "." + getImageExt(image_uri));
            ref.putFile(image_uri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Uploaded", Toast.LENGTH_SHORT).show();
                            ImageUpload imageUpload = new ImageUpload(taskSnapshot.getMetadata().getReference().getDownloadUrl().toString());
                            String uploadId = mDatabaseReference.push().getKey();

                            //Save Image Info Firebase database
                            mDatabaseReference.child(uploadId).setValue(imageUpload);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getActivity(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        } else {
            Toast.makeText(getActivity(), "Please Select Image", Toast.LENGTH_SHORT).show();
        }
    }

    Context applicationContext = MainActivity.getContextOfApplication();

    public String getImageExt(Uri uri) {
        ContentResolver contentResolver = applicationContext.getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }


    public String getUrl(StorageReference ref) {


        auth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();
        String imgref = FB_STORAGE_PATH + UserID + "/" + System.currentTimeMillis() + "." + getImageExt(image_uri);

        ref.child(imgref).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                // Got the download URL for 'users/me/profile.png'
                url = uri.toString();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
            }
        });

        return url;
    }

    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            location = LocationServices.FusedLocationApi.getLastLocation(gac);
            if(location != null)
            {
                double lat = location.getLatitude();
                double lon = location.getLongitude();

                Geocoder g = new Geocoder(getActivity(), Locale.ENGLISH);
                try {
                    List<Address> address = g.getFromLocation(lat, lon, 1);
                    android.location.Address add = address.get(0);
                    String msg = add.getCountryName() + ", " + add.getAdminArea()+ ", " + add.getSubAdminArea()+ ", "
                            + add.getLocality()+", "+add.getPostalCode()+", "+ add.getThoroughfare()+", "+add.getSubThoroughfare();
                    displayLocation.setText(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                getlocation.setEnabled(true);
            }
            else
                Toast.makeText(getActivity(), "please start gps/open area", Toast.LENGTH_SHORT).show();
        }

    }
}