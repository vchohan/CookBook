package com.vchohan.cookbook;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class  RecipeViewHolderActivity extends AppCompatActivity {

    private static ImageView recipeImageView;

    private static TextView recipeViewIngredients;

    private static TextView recipeViewMethod;

    private static TextView recipeViewNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recipe_view_holder_activity);

        recipeImageView = (ImageView) findViewById(R.id.recipe_image_view);

        recipeViewIngredients = (TextView) findViewById(R.id.recipe_view_ingredients);
        recipeViewMethod = (TextView) findViewById(R.id.recipe_view_method);
        recipeViewNotes = (TextView) findViewById(R.id.recipe_view_notes);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            //set recipe title here.
            String keyTitle = "Title";
            String valueTitle = extras.getString(keyTitle);
            toolbar.setTitle(valueTitle);

            // set recipe image here
            //TODO: implement recipe image

            //set recipe ingredients here.
            String keyIngredients = "Ingredients";
            String valueIngredients = extras.getString(keyIngredients);
            recipeViewIngredients.setText(valueIngredients);

            //set recipe titles here.
            String keyMethod = "Method";
            String valueMethod = extras.getString(keyMethod);
            recipeViewMethod.setText(valueMethod);

            //set recipe titles here.
            String keyNotes = "Notes";
            String valueNotes = extras.getString(keyNotes);
            recipeViewNotes.setText(valueNotes);
        }

        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
}
