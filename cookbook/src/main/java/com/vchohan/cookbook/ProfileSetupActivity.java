package com.vchohan.cookbook;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class ProfileSetupActivity extends AppCompatActivity {

    private ImageView profileSetupImage;

    private EditText profileSetupUsername;

    private Button profileSubmitButton;

    private static final int GALLERY_REQUEST = 1;

    private Uri mImageUri = null;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabaseUsers;

    private StorageReference mStorage;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_setup_activity);

        String toastText = "Please setup your new profile image and display name.";
        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();

        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance().getReference().child("ProfileImages");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");

        profileSetupImage = (ImageView) findViewById(R.id.profile_setup_image);
        profileSetupUsername = (EditText) findViewById(R.id.profile_setup_username);

        profileSubmitButton = (Button) findViewById(R.id.profile_submit_button);
        profileSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitProfileSetupAccount();
            }
        });

        profileSetupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });


    }

    private void submitProfileSetupAccount() {

        final String username = profileSetupUsername.getText().toString().trim();
        final String userId = mAuth.getCurrentUser().getUid();

        if (!TextUtils.isEmpty(username) && mImageUri != null) {
            showProgressDialog();

            StorageReference filePath = mStorage.child(mImageUri.getLastPathSegment());
            filePath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    Toast.makeText(ProfileSetupActivity.this, "Profile was successfully created", Toast.LENGTH_SHORT).show();

                    String downlaodUri = taskSnapshot.getDownloadUrl().toString();

                    mDatabaseUsers.child(userId).child("image").setValue(downlaodUri);
                    mDatabaseUsers.child(userId).child("username").setValue(username);

                    hideProgressDialog();

                    Intent mainIntent = new Intent(ProfileSetupActivity.this, MainActivity.class);
                    mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainIntent);
                }
            });
        } else {
            String toastText = "Please select you profile image and enter your username.";
            Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            mImageUri = data.getData();
            CropImage.activity(mImageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setCropShape(CropImageView.CropShape.OVAL)
                .setAspectRatio(1, 1)
                .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                profileSetupImage.setImageURI(resultUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Please wait while we save your profile...");
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
