package com.vchohan.cookbook;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

public class SingleRecipeActivity extends AppCompatActivity {

    private FloatingActionButton mSingleRecipeFab;

    private ImageView singleRecipeImage;

    private TextView singleRecipeIngredients, singleRecipeMethod, singleRecipeNotes;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabase;

    private String mRecipeKey = null;

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.single_recipe_activity);

        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Recipes");

        mRecipeKey = getIntent().getExtras().getString("recipeId");

        singleRecipeImage = (ImageView) findViewById(R.id.single_recipe_image);

        singleRecipeIngredients = (TextView) findViewById(R.id.single_recipe_ingredients);
        singleRecipeMethod = (TextView) findViewById(R.id.single_recipe_method);
        singleRecipeNotes = (TextView) findViewById(R.id.single_recipe_notes);

        mDatabase.child(mRecipeKey).addValueEventListener(new ValueEventListener() {
            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String recipeTitle = (String) dataSnapshot.child("title").getValue();
                String recipeImage = (String) dataSnapshot.child("image").getValue();
                String recipeIngredients = (String) dataSnapshot.child("ingredients").getValue();
                String recipeMethod = (String) dataSnapshot.child("method").getValue();
                String recipeNotes = (String) dataSnapshot.child("notes").getValue();
                String recipeUid = (String) dataSnapshot.child("uid").getValue();

                mToolbar = (Toolbar) findViewById(R.id.toolbar);
                mToolbar.setTitle(recipeTitle);
                setSupportActionBar(mToolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                Glide.with(SingleRecipeActivity.this).load(recipeImage)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(singleRecipeImage);

                singleRecipeIngredients.setText(recipeIngredients);
                singleRecipeMethod.setText(recipeMethod);
                singleRecipeNotes.setText(recipeNotes);

//                Toast.makeText(SingleRecipeActivity.this, recipeUid, Toast.LENGTH_SHORT).show();

                if (mAuth.getCurrentUser().getUid().equals(recipeUid)) {

                    mSingleRecipeFab = (FloatingActionButton) findViewById(R.id.single_recipe_fab);
                    mSingleRecipeFab.setVisibility(View.VISIBLE);
                    mSingleRecipeFab.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            mDatabase.child(mRecipeKey).removeValue();

                            Intent mainIntent = new Intent(SingleRecipeActivity.this, MainActivity.class);
                            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(mainIntent);

                            Snackbar.make(view, "Recipe deleted ", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
