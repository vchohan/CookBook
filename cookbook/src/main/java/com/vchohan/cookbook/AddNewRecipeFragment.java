package com.vchohan.cookbook;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
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

    private String userChoosenTask;

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

        return rootView;
    }

    private void initializeView(final View rootView) {
        recipeImageViewContainer = (LinearLayout) rootView.findViewById(R.id.recipe_image_view_container);
        recipeImageViewContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //launch camera
                selectImage();
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
                getRecipeUserInput(rootView);
            }
        });
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

    private void getRecipeUserInput(View rootView) {
        if (recipeTitle.getText().toString().trim().length() == 0 &&
            recipeIngredients.getText().toString().trim().length() == 0 &&
            recipeMethod.getText().toString().trim().length() == 0) {
            String errorText = "Please enter your recipe Title, Ingredients and Method";
            Snackbar.make(rootView, errorText, Snackbar.LENGTH_LONG).setAction("Action", null).show();
        } else {
            Intent recipeViewerIntent = new Intent(getContext(), RecipeViewHolderActivity.class);
            recipeViewerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // passing string value to another activity
            if (recipeViewerIntent != null) {

                // send value to the database
                FirebaseDatabase database = FirebaseDatabase.getInstance();

                String[] keyMealCategory = getResources().getStringArray(R.array.recipe_category_array);
                String keyTitle = "Title";
                String keyIngredients = "Ingredients";
                String keyMethod = "Method";
                String keyNotes = "Notes";

                String valueMealCategory = recipeCategoryValue;
                String valueTitle = recipeTitle.getText().toString();
                String valueIngredients = recipeIngredients.getText().toString();
                String valueMethod = recipeMethod.getText().toString();
                String valueNotes = recipeNotes.getText().toString();

                //set recipe image and write to database
                //TODO: implement recipe image

                DatabaseReference myRef;
                myRef = database.getReference(valueMealCategory);

                DatabaseReference childTitle = myRef.child(valueTitle);
                childTitle.setValue(valueTitle);

                DatabaseReference childIngredients = childTitle.child(keyIngredients);
                childIngredients.setValue(valueIngredients);

                DatabaseReference childMethod = childTitle.child(keyMethod);
                childMethod.setValue(valueMethod);

                DatabaseReference childNotes = childTitle.child(keyNotes);
                childNotes.setValue(valueNotes);

                // Here passing the user input values to another activity
                recipeViewerIntent.putExtra(keyTitle, valueTitle);
                //TODO: implement recipe image
                recipeViewerIntent.putExtra(keyIngredients, valueIngredients);
                recipeViewerIntent.putExtra(keyMethod, valueMethod);
                recipeViewerIntent.putExtra(keyNotes, valueNotes);

                startActivity(recipeViewerIntent);
            }

            String recipeSaved = getString(R.string.recipe_saved);
            Toast.makeText(getContext(), recipeSaved, Toast.LENGTH_SHORT).show();
        }
    }

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result = CameraUtility.checkPermission(getContext());
                if (items[item].equals("Take Photo")) {
                    userChoosenTask = "Take Photo";
                    if (result) {
                        cameraIntent();
                    }
                } else if (items[item].equals("Choose from Library")) {
                    userChoosenTask = "Choose from Library";
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

                requestPermissions(new String[]{Manifest.permission.CAMERA},
                    MY_REQUEST_CODE);
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
                            if (userChoosenTask.equals("Take Photo")) {
                                cameraIntent();
                            } else if (userChoosenTask.equals("Choose from Library")) {
                                galleryIntent();
                            }
                        } else {
                            //code for deny
                        }
                        break;
                }
            } else {
                // Your app will not have this permission. Turn off all functions
                // that require this permission or it will force close like your
                // original question
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SELECT_FILE) {
                onSelectFromGalleryResult(data);
            } else if (requestCode == REQUEST_CAMERA) {
                onCaptureImageResult(data);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bm = null;
        if (data != null) {
            try {
                bm = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), data.getData());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        recipeImage.setImageBitmap(bm);
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
