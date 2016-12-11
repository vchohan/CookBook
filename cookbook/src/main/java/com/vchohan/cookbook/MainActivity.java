package com.vchohan.cookbook;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.squareup.picasso.Picasso;

import java.util.Random;

public class MainActivity extends AppCompatActivity
    implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private DatabaseReference mDatabase;

    private RecyclerView mRecipeRecyclerView;

    private TextView recipeTip;

    private int mPosition = 0;

    private int mInterval = 5000;

    private Handler mHandler = new Handler();

    private Runnable mRunnable;

    private FirebaseAuth mAuth;

    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {

                }
            }
        };

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Recipe");

        //setup recipe recyclerView
        mRecipeRecyclerView = (RecyclerView) findViewById(R.id.recipe_recycler_view);
        mRecipeRecyclerView.setHasFixedSize(true);
        mRecipeRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newRecipeIntent = new Intent(getApplicationContext(), AddNewRecipeActivity.class);
                newRecipeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(newRecipeIntent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        setupRecipeTip();

    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<RecipeUtils, RecipeViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<RecipeUtils,
            RecipeViewHolder>(RecipeUtils.class, R.layout.main_recipe_custom_card_view, RecipeViewHolder.class, mDatabase) {
            @Override
            protected void populateViewHolder(RecipeViewHolder viewHolder, RecipeUtils model, int position) {
                viewHolder.setTitle(model.getTitle());
                viewHolder.setImage(getApplicationContext(), model.getImage());
            }
        };

        mRecipeRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public RecipeViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setTitle(String title) {
            TextView recyclerRecipeTitle = (TextView) mView.findViewById(R.id.recycler_recipe_title);
            recyclerRecipeTitle.setText(title);
        }

        public void setImage(Context context, String image) {
            ImageView recipeImage = (ImageView) mView.findViewById(R.id.recycler_recipe_image);
            Picasso.with(context).load(image).into(recipeImage);
        }
    }

    private void setupRecipeTip() {
        recipeTip = (TextView) findViewById(R.id.recipe_tip);
        recipeTip.setShadowLayer(2, 2, 2, Color.GRAY);

        mRunnable = new Runnable() {
            @Override
            public void run() {
                final String[] mRecipeTipArray;
                mRecipeTipArray = getResources().getStringArray(R.array.recipe_tips_array);
                Random random = new Random();
                final int maxIndex = mRecipeTipArray.length;
                int generatedIndex = random.nextInt(maxIndex);

                if (mPosition >= mRecipeTipArray.length) {
                    mPosition = 0;
                }

                recipeTip.setText(mRecipeTipArray[generatedIndex]);
                recipeTip.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, android.R.anim.slide_in_left));
                mPosition++;
                mHandler.postDelayed(mRunnable, mInterval);
            }
        };
        mRunnable.run();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
