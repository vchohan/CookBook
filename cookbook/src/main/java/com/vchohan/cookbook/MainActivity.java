package com.vchohan.cookbook;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.squareup.picasso.Picasso;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mRecipeRecyclerView;

    private LinearLayoutManager mLinearLayoutManager;

    private TextView recipeTip;

    private int mPosition = 0;

    private int mInterval = 5000;

    private Handler mHandler = new Handler();

    private Runnable mRunnable;

    private FirebaseAuth mAuth = null;

    private FirebaseAuth.AuthStateListener mAuthListener;

    private DatabaseReference mDatabase;

    private DatabaseReference mDatabaseUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                } else {
                    new RegisterActivity().finish();
                }
            }
        };

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Recipe");
        mDatabase.keepSynced(true);

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);

        //setup recipe recyclerView
        mRecipeRecyclerView = (RecyclerView) findViewById(R.id.recipe_recycler_view);

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setReverseLayout(true);
        mLinearLayoutManager.setStackFromEnd(true);

        mRecipeRecyclerView.setHasFixedSize(true);
        mRecipeRecyclerView.setLayoutManager(mLinearLayoutManager);

        setupToolBarAndNavigationDrawer();
        setupRecipeTipsCardView();
//        checkExistingUser();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mAuth.addAuthStateListener(mAuthListener);
        FirebaseRecyclerAdapter<RecipeUtils, RecipeViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<RecipeUtils,
            RecipeViewHolder>(RecipeUtils.class, R.layout.main_recipe_custom_card_view, RecipeViewHolder.class, mDatabase) {
            @Override
            protected void populateViewHolder(RecipeViewHolder viewHolder, RecipeUtils model, int position) {

                final String recipeKey = getRef(position).toString();

                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setTitle(model.getTitle());
                viewHolder.setUsername(model.getUsername());

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Toast.makeText(MainActivity.this, recipeKey, Toast.LENGTH_LONG).show();
                    }
                });

            }
        };
        mRecipeRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    private void setupToolBarAndNavigationDrawer() {
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
    }

    private void checkExistingUser() {
        if (mAuth.getCurrentUser() != null) {
            final String userId = mAuth.getCurrentUser().getUid();
            mDatabaseUsers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChild(userId)) {
                        Intent setupIntent = new Intent(MainActivity.this, ProfileSetupActivity.class);
                        setupIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(setupIntent);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    public static class RecipeViewHolder extends RecyclerView.ViewHolder {

        View mView;

        public RecipeViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setImage(Context context, String image) {
            ImageView recipeImage = (ImageView) mView.findViewById(R.id.recycler_recipe_image);
            Picasso.with(context).load(image).into(recipeImage);
        }

        public void setTitle(String title) {
            TextView recyclerRecipeTitle = (TextView) mView.findViewById(R.id.recycler_recipe_title);
            recyclerRecipeTitle.setText(title);
        }

        public void setUsername(String username) {
            TextView recyclerRecipeUsername = (TextView) mView.findViewById(R.id.recycler_recipe_username);
            recyclerRecipeUsername.setText(username);
        }

    }

    private void setupRecipeTipsCardView() {
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
        } else if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void logout() {
        mAuth.signOut();
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
