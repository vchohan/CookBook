package com.vchohan.cookbook;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;

/**
 * A placeholder fragment containing a simple view.
 */
public class AddNewRecipeFragment extends Fragment {

    public static final String TAG = AddNewRecipeFragment.class.getSimpleName();

    private static LinearLayout recipeImageViewContainer;

    private ImageView recipeImageView;

    private static Button recipeSaveButton;

    private static EditText recipeTitle;

    private static EditText recipeIngredients;

    private static EditText recipeMethod;

    private static EditText recipeNotes;

    private Uri file;

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
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                file = Uri.fromFile(getOutputMediaFile());
                intent.putExtra(MediaStore.EXTRA_OUTPUT, file);
                startActivityForResult(intent, 100);

                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    recipeImageViewContainer.setEnabled(false);
                    ActivityCompat.requestPermissions((Activity) getContext(),
                        new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                }
            }
        });

        recipeImageView = (ImageView) rootView.findViewById(R.id.recipe_image_view);

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

    private void getRecipeUserInput(View rootView) {
        if (recipeTitle.getText().toString().trim().length() == 0 &&
            recipeIngredients.getText().toString().trim().length() == 0 &&
            recipeMethod.getText().toString().trim().length() == 0) {
            String errorText = "Please enter your recipe Title, Ingredients and Method";
            Snackbar.make(rootView, errorText, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
        } else {
            Intent recipeViewerIntent = new Intent(getContext(), RecipeViewHolderActivity.class);
            recipeViewerIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            // passing string value to another activity
            if (recipeViewerIntent != null) {
                String keyTitle = "Title";
                String keyIngredients = "Ingredients";
                String keyMethod = "Method";
                String keyNotes = "Notes";

                String valueTitle = recipeTitle.getText().toString();
                String valueIngredients = recipeIngredients.getText().toString();
                String valueMethod = recipeMethod.getText().toString();
                String valueNotes = recipeNotes.getText().toString();

                recipeViewerIntent.putExtra(keyTitle, valueTitle);
                recipeViewerIntent.putExtra(keyIngredients, valueIngredients);
                recipeViewerIntent.putExtra(keyMethod, valueMethod);
                recipeViewerIntent.putExtra(keyNotes, valueNotes);

                startActivity(recipeViewerIntent);
            }
            String recipeSaved = getString(R.string.recipe_saved);
            Toast.makeText(getContext(), recipeSaved, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                recipeImageViewContainer.setEnabled(true);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                recipeImageView.setImageURI(file);
            }
        }
    }

    private static File getOutputMediaFile() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES), "CameraDemo");

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        return new File(mediaStorageDir.getPath() + File.separator +
            "IMG_" + timeStamp + ".jpg");
    }
}
