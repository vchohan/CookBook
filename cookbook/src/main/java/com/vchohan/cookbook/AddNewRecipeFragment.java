package com.vchohan.cookbook;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import static android.app.Activity.RESULT_OK;

/**
 * A placeholder fragment containing a simple view.
 */
public class AddNewRecipeFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    public static final String TAG = AddNewRecipeFragment.class.getSimpleName();

    private static final String URI = "uri";

    private static LinearLayout recipeImageViewContainer;

    private static ImageView recipeImage;

    private static Spinner recipeCategory;

    private static String recipeCategoryValue;

    private static EditText recipeTitle;

    private static EditText recipeIngredients;

    private static EditText recipeMethod;

    private static EditText recipeNotes;

    private static Button recipeSaveButton;

    private static final int CAMERA_REQUEST = 0;

    private static final int GALLERY_REQUEST = 100;

    private String selectImageOptions;

    private Uri mImageUri = null;

    private FirebaseAuth mAuth;

    private FirebaseUser mCurrentUser;

    private DatabaseReference mDatabase, mDatabaseUsers;

    private StorageReference mStorage;

    private ProgressDialog mProgressDialog;

    /**
     * The fragment argument representing the section number for this fragment.
     */
    public AddNewRecipeFragment() {

    }

    /**
     * Returns a new instance of this fragment for the given section number.
     */
    public static AddNewRecipeFragment newInstance() {
        AddNewRecipeFragment fragment = new AddNewRecipeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.add_new_recipe_fragment, container, false);
        initializeView(rootView);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Recipes");
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users").child(mCurrentUser.getUid());
        mStorage = FirebaseStorage.getInstance().getReference();

        if (savedInstanceState != null) {
            mImageUri = savedInstanceState.getParcelable(URI);
            recipeImage.setImageURI(mImageUri);
        }

        return rootView;
    }

    private void initializeView(final View rootView) {
        recipeImageViewContainer = (LinearLayout) rootView.findViewById(R.id.recipe_image_view_container);
        recipeImageViewContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //launch camera
                selectRecipeImageFrom();
            }
        });

        recipeImage = (ImageView) rootView.findViewById(R.id.recipe_image);

        recipeCategory = (Spinner) rootView.findViewById(R.id.recipe_category);

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
            R.array.recipe_category_array, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        recipeCategory.setAdapter(adapter);
        recipeCategory.setOnItemSelectedListener(this);

        recipeTitle = (EditText) rootView.findViewById(R.id.recipe_title);
        recipeIngredients = (EditText) rootView.findViewById(R.id.recipe_ingredients);
        recipeMethod = (EditText) rootView.findViewById(R.id.recipe_method);
        recipeNotes = (EditText) rootView.findViewById(R.id.recipe_notes);

        recipeSaveButton = (Button) rootView.findViewById(R.id.save_button);
        recipeSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveRecipeToDatabase();
            }
        });
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
            mProgressDialog.setMessage("Please wait while we save your recipe...");
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    private void saveRecipeToDatabase() {
        showProgressDialog();

        final String keyImage = "image";
        final String keyMealCategory = "meal";
        final String keyTitle = "title";
        final String keyIngredients = "ingredients";
        final String keyMethod = "method";
        final String keyNotes = "notes";

        final String valueMealCategory = recipeCategoryValue.trim();
        final String valueTitle = recipeTitle.getText().toString().trim();
        final String valueIngredients = recipeIngredients.getText().toString().trim();
        final String valueMethod = recipeMethod.getText().toString().trim();
        final String valueNotes = recipeNotes.getText().toString().trim();

        if (mImageUri != null &&
            !TextUtils.isEmpty(valueMealCategory) &&
            !TextUtils.isEmpty(valueTitle) &&
            !TextUtils.isEmpty(valueIngredients) &&
            !TextUtils.isEmpty(valueMethod)) {

            StorageReference filePath = mStorage.child("RecipeImages").child(mImageUri.getLastPathSegment());
            filePath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    final Uri downlaodUri = taskSnapshot.getDownloadUrl();

                    final DatabaseReference newRecipe = mDatabase.push();

                    mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            newRecipe.child(keyImage).setValue(downlaodUri.toString());
                            newRecipe.child(keyMealCategory).setValue(valueMealCategory);
                            newRecipe.child(keyTitle).setValue(valueTitle);
                            newRecipe.child(keyIngredients).setValue(valueIngredients);
                            newRecipe.child(keyMethod).setValue(valueMethod);
                            newRecipe.child(keyNotes).setValue(valueNotes);

                            newRecipe.child("uid").setValue(mCurrentUser.getUid());
                            newRecipe.child("username").setValue(dataSnapshot.child("username").getValue()).addOnCompleteListener(
                                new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {

                                            Toast.makeText(getContext(), "Your Recipe was successfully saved!", Toast.LENGTH_LONG).show();

                                            //once the user has saved the info direct them to main activity.
                                            Intent mainIntent = new Intent(getActivity(), MainActivity.class);
                                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(mainIntent);
                                        } else {
                                            Log.w(TAG, "Error: Something went wrong", task.getException());
                                        }
                                    }
                                });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    hideProgressDialog();
                }
            });
        } else {
            hideProgressDialog();
            String errorText = "Please enter your recipe Title, Ingredients and Method";
            Snackbar.make(getView(), errorText, Snackbar.LENGTH_LONG).setAction("Action", null).show();
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(position);
        recipeCategoryValue = recipeCategory.getSelectedItem().toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void selectRecipeImageFrom() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = CameraUtility.checkPermission(getContext());
                if (items[item].equals("Take Photo")) {
                    selectImageOptions = "Take Photo";
                    if (result) {
                        cameraIntent();
                    }
                } else if (items[item].equals("Choose from Library")) {
                    selectImageOptions = "Choose from Library";
                    if (result) {
                        galleryIntent();
                    }
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    private void cameraIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, GALLERY_REQUEST);
            }
        }
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(cameraIntent, CAMERA_REQUEST);
    }

    private void galleryIntent() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == GALLERY_REQUEST) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Now user should be able to use camera
                switch (requestCode) {
                    case CameraUtility.MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                            if (selectImageOptions.equals("Take Photo")) {
                                cameraIntent();
                            } else if (selectImageOptions.equals("Choose from Library")) {
                                galleryIntent();
                            }
                        } else {
                            //code for deny
                        }
                        break;
                }
            } else {
                // Your app will not have this permission.
                String errorText = "Permission was denied to access your camera, please visit app settings to allow permission";
                Snackbar.make(getView(), errorText, Snackbar.LENGTH_LONG).setAction("Action", null).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST || requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                try {
                    mImageUri = data.getData();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                recipeImage.setImageURI(mImageUri);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (recipeImage != null) {
            outState.putParcelable(URI, mImageUri);
        }
    }
}
