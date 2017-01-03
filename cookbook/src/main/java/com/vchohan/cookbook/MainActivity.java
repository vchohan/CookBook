package com.vchohan.cookbook;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseRecyclerAdapter;

import java.util.Random;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private NavigationView mNavigationView;

    private Toolbar mToolbar;

    private DrawerLayout mDrawerLayout;

    private View mNavigationHeaderView, mToolbarProfileView;

    private FloatingActionButton mFab;

    private RecyclerView mRecipeRecyclerView;

    private LinearLayoutManager mLinearLayoutManager;

    private TextView recipeTip;

    private int mPosition = 0;

    private int mInterval = 5000;

    private Handler mHandler = new Handler();

    private Runnable mRunnable;

    private FirebaseAuth mAuth = null;

    private FirebaseAuth.AuthStateListener mAuthListener;

    private DatabaseReference mDatabaseRecipes;

    private DatabaseReference mDatabaseUsers;

    private DatabaseReference mDatabaseCurrentUser;

    private Query mQueryCurrentUser;

    private DatabaseReference mDatabaseLikes;

    private boolean mProcessLike = false;

    private ProgressDialog mProgressDialog;

    private ImageView navProfileImage, toolbarProfileImage;

    private TextView navProfileUsername, navLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (mAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
            }
        };

        if (mAuth.getCurrentUser() != null) {
            String currentUserId = mAuth.getCurrentUser().getUid();
            mDatabaseCurrentUser = FirebaseDatabase.getInstance().getReference().child("Recipes");
            mDatabaseCurrentUser.keepSynced(true);
            mQueryCurrentUser = mDatabaseCurrentUser.orderByChild("uid").equalTo(currentUserId);
        }

        mDatabaseRecipes = FirebaseDatabase.getInstance().getReference().child("Recipes");
        mDatabaseRecipes.keepSynced(true);

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);

        mDatabaseLikes = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseLikes.keepSynced(true);

        //setup recipe recyclerView
        mRecipeRecyclerView = (RecyclerView) findViewById(R.id.recipe_recycler_view);

        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setReverseLayout(true);
        mLinearLayoutManager.setStackFromEnd(true);

        mRecipeRecyclerView.setHasFixedSize(true);
        mRecipeRecyclerView.setLayoutManager(mLinearLayoutManager);

        navProfileImage = (ImageView) findViewById(R.id.nav_profile_image);
        navProfileUsername = (TextView) findViewById(R.id.nav_profile_username);
        navLogout = (TextView) findViewById(R.id.nav_logout);

        toolbarProfileImage = (ImageView) findViewById(R.id.toolbar_profile_image);

        mFab = (FloatingActionButton) findViewById(R.id.main_add_recipe_fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newRecipeIntent = new Intent(getApplicationContext(), AddNewRecipeActivity.class);
                newRecipeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(newRecipeIntent);
            }
        });

        setupToolBarAndNavigationDrawer();
        setupRecipeTipsCardView();
    }

    @Override
    protected void onStart() {
        super.onStart();
        setupFirebaseRecyclerView();
    }

    private void setupFirebaseRecyclerView() {
        showProgressDialog();

        mAuth.addAuthStateListener(mAuthListener);

        // user mDatabaseRecipe for all users, else mQueryCurrentUser for logged in single user
        final FirebaseRecyclerAdapter<RecipeUtils, MainRecipeViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<RecipeUtils,
            MainRecipeViewHolder>(RecipeUtils.class, R.layout.main_recipe_custom_card_view, MainRecipeViewHolder.class, mDatabaseRecipes) {
            @Override
            protected void populateViewHolder(MainRecipeViewHolder viewHolder, RecipeUtils model, int position) {

                final String recipeKey = getRef(position).getKey();

                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setTitle(model.getTitle());
                viewHolder.setUsername(model.getUsername());

                viewHolder.setLikeButton(recipeKey);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent addRecipeIntent = new Intent(MainActivity.this, SingleRecipeActivity.class);
                        addRecipeIntent.putExtra("recipeId", recipeKey);
                        addRecipeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(addRecipeIntent);
                    }
                });

                viewHolder.mLikeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        mProcessLike = true;

                        mDatabaseLikes.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessLike) {
                                    if (dataSnapshot.child(recipeKey).hasChild(mAuth.getCurrentUser().getUid())) {
                                        mDatabaseLikes.child(recipeKey).child(mAuth.getCurrentUser().getUid()).removeValue();
                                        mProcessLike = false;
                                    } else {
                                        mDatabaseLikes.child(recipeKey).child(mAuth.getCurrentUser().getUid()).setValue("RandomValue");
                                        mProcessLike = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
                hideProgressDialog();
            }
        };

        firebaseRecyclerAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                int friendlyMessageCount = firebaseRecyclerAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                    (positionStart >= (friendlyMessageCount - 1) &&
                        lastVisiblePosition == (positionStart - 1))) {
                    mLinearLayoutManager.scrollToPosition(positionStart);
                }
            }
        });
        mRecipeRecyclerView.setAdapter(firebaseRecyclerAdapter);
    }

    private void setupToolBarAndNavigationDrawer() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        // navigation view header
        mNavigationHeaderView = mNavigationView.getHeaderView(0);

        mToolbarProfileView = mToolbar.getRootView();

        if (mAuth.getCurrentUser() != null) {
            final String userId = mAuth.getCurrentUser().getUid();

            mDatabaseUsers.child(userId).child("image").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    toolbarProfileImage = (ImageView) mToolbarProfileView.findViewById(R.id.toolbar_profile_image);

                    String profileImage = dataSnapshot.getValue(String.class);

                    // Loading toolbar profile image
                    Glide.with(getApplicationContext())
                        .load(profileImage)
                        .bitmapTransform(new CircleTransform(getApplicationContext()))
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .thumbnail(0.1f)
                        .into(toolbarProfileImage);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            mDatabaseUsers.child(userId).child("image").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    navProfileImage = (ImageView) mNavigationHeaderView.findViewById(R.id.nav_profile_image);

                    String image = dataSnapshot.getValue(String.class);

                    // loading nav profile image
                    Glide.with(getApplicationContext())
                        .load(image)
                        .bitmapTransform(new CircleTransform(getApplicationContext()))
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .thumbnail(0.5f)
                        .into(navProfileImage);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            mDatabaseUsers.child(userId).child("username").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    navProfileUsername = (TextView) mNavigationHeaderView.findViewById(R.id.nav_profile_username);
                    navProfileUsername.setText(dataSnapshot.getValue(String.class));
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        navLogout = (TextView) mNavigationHeaderView.findViewById(R.id.nav_logout);
        navLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
            }
        });
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
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setBackgroundColor(getResources().getColor(R.color.colorIndigo900));
//        EditText searchEditText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
//        searchEditText.setTextColor(getResources().getColor(R.color.colorBlack));
//        searchEditText.setHintTextColor(getResources().getColor(R.color.colorBlack));

        // Configure the search info and add any event listeners...
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_profile) {
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

        } else if (id == R.id.nav_profile) {
            Intent setupIntent = new Intent(MainActivity.this, TabbedActivity.class);
            setupIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(setupIntent);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage("Loading...");
            mProgressDialog.setIndeterminate(true);
        }
        mProgressDialog.show();
    }

    private void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
