package com.vchohan.cookbook;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static android.app.Activity.RESULT_OK;

/**
 * A placeholder fragment containing a simple view.
 */
public class AddNewRecipeFragment extends Fragment implements AdapterView.OnItemSelectedListener {

    public static final String TAG = AddNewRecipeFragment.class.getSimpleName();

    private static LinearLayout recipeImageViewContainer;

    private static ImageView recipeImage;

    private static Spinner recipeCategory;

    private static String recipeCategoryValue;

    private static EditText recipeTitle;

    private static EditText recipeIngredients;

    private static EditText recipeMethod;

    private static EditText recipeNotes;

    private static Button recipeSaveButton;

    private static final int REQUEST_CAMERA = 0;

    private static final int SELECT_FILE = 0;

    private static final int MY_REQUEST_CODE = 100;

    private String selectImageOptions;

    private Uri mImageUri = null;

    private Bitmap mBitmap;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase;

    private StorageReference mStorage;

    private ProgressDialog mProgressDialog;

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    public AddNewRecipeFragment() {

    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
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

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Recipe");
        mStorage = FirebaseStorage.getInstance().getReference();

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

    @Override
    public void onStart() {
        super.onStart();
        mAuth.getCurrentUser();
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

                    Uri downlaodUri = taskSnapshot.getDownloadUrl();

                    DatabaseReference newRecipe = mDatabase.push();

                    newRecipe.child(keyImage).setValue(downlaodUri.toString());
                    newRecipe.child(keyMealCategory).setValue(valueMealCategory);
                    newRecipe.child(keyTitle).setValue(valueTitle);
                    newRecipe.child(keyIngredients).setValue(valueIngredients);
                    newRecipe.child(keyMethod).setValue(valueMethod);
                    newRecipe.child(keyNotes).setValue(valueNotes);

                    hideProgressDialog();
                    Toast.makeText(getContext(), "Your Recipe was successfully saved!", Toast.LENGTH_LONG).show();

                    //once the user has saved the info direct them to main activity.
                    startActivity(new Intent(getActivity(), MainActivity.class));
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
                requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_REQUEST_CODE);
            }
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    private void galleryIntent() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);//
        startActivityForResult(Intent.createChooser(intent, "Select File"), SELECT_FILE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == MY_REQUEST_CODE) {
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

        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data);
            } else if (requestCode == REQUEST_CAMERA) {
                onCaptureImageResult(data);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {

//        if (data != null) {
//            try {
//                mBitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), data.getData());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }

        if (data != null) {
            try {
                mImageUri = data.getData();
                mBitmap = decodeSampledBitmapFromUri(getActivity(), mImageUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
            recipeImage.setImageBitmap(mBitmap);
        }
    }

    public static Bitmap decodeSampledBitmapFromUri(Activity callingActivity, Uri uri) {
        try {
            InputStream input = callingActivity.getContentResolver().openInputStream(uri);

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize = 2; // make the bitmap size half of the original image.
            BitmapFactory.decodeStream(input, null, options);

            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options);
            input.close();

            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;

            input = callingActivity.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
            return bitmap;
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        } catch (IOException e) {// TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }

    }

    public static int calculateInSampleSize(BitmapFactory.Options options) {
        // Raw height and width of image
        final int width = options.outWidth;
        final int height = options.outHeight;

        int inSampleSize = 1;

//        if (height > regularHeight || width > regularWidth) {
//
//            final int halfHeight = height / 2;
//            final int halfWidth = width / 2;
//
//            // Calculate the largest inSampleSize value that is a power of 2 and
//            // keep both height and width larger than the requested height and width.
//            while ((halfHeight / inSampleSize) > regularHeight
//                && (halfWidth / inSampleSize) > regularWidth) {
//                inSampleSize *= 2;
//            }
//        }
        return inSampleSize;
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        File destination = new File(Environment.getExternalStorageDirectory(),
            System.currentTimeMillis() + ".jpg");
        FileOutputStream fo;
        try {
            destination.createNewFile();
            fo = new FileOutputStream(destination);
            fo.write(bytes.toByteArray());
            fo.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        recipeImage.setImageBitmap(thumbnail);
    }
}
