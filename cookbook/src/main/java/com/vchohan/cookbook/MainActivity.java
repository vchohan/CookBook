package com.vchohan.cookbook;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    public static final String TAG = MainActivity.class.getSimpleName();

    private TextView recipeTip;

    private int mPosition = 0;

    private int mInterval = 5000;

    private Handler mHandler = new Handler();

    private Runnable mRunnable;

    private FirebaseAuth mAuth = null;

    private DatabaseReference mDatabaseUsers;

    private NavigationView mNavigationView;

    private Toolbar mToolbar;

    private DrawerLayout mDrawerLayout;

    private View mNavigationHeaderView, mToolbarProfileView;

    private ImageView navProfileImage, toolbarProfileImage;

    private TextView navProfileUsername, navLogout;

    private TabLayout tabLayout;

    private ViewPager viewPager;

    private FloatingActionButton mFab;

    private int[] tabIcons = {
        R.drawable.ic_audiotrack,
        R.drawable.ic_person_white_24dp,
        R.drawable.ic_favorite_white_24dp
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        setupRecipeTipsCardView();
        initializeFirebase();
        setupToolBarAndNavigationDrawer();

        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        setupTabIcons();

        navProfileImage = (ImageView) findViewById(R.id.nav_profile_image);
        navProfileUsername = (TextView) findViewById(R.id.nav_profile_username);
        navLogout = (TextView) findViewById(R.id.nav_logout);

        toolbarProfileImage = (ImageView) findViewById(R.id.toolbar_profile_image);

        setupAddNewRecipeFAB();
    }

    private void setupRecipeTipsCardView() {

        recipeTip = (TextView) findViewById(R.id.recipe_tip);

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

    private void setupAddNewRecipeFAB() {
        mFab = (FloatingActionButton) findViewById(R.id.main_add_recipe_fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent newRecipeIntent = new Intent(getApplicationContext(), AddNewRecipeActivity.class);
                newRecipeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(newRecipeIntent);
            }
        });
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);
    }

    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new MainAllRecipesFragment(), "ALL RECIPES");
        adapter.addFragment(new MainMyRecipesFragment(), "MY RECIPES");
        adapter.addFragment(new MainFavoriteRecipesFragment(), "MY FAVORITES");
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {

        private final List<Fragment> mFragmentList = new ArrayList<>();

        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
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

//            mDatabaseUsers.child(userId).child("image").addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//
//                    toolbarProfileImage = (ImageView) mToolbarProfileView.findViewById(R.id.toolbar_profile_image);
//
//                    String profileImage = dataSnapshot.getValue(String.class);
//
//                    // Loading toolbar profile image
//                    Glide.with(getApplicationContext())
//                        .load(profileImage)
//                        .bitmapTransform(new CircleTransform(getApplicationContext()))
//                        .crossFade()
//                        .diskCacheStrategy(DiskCacheStrategy.ALL)
//                        .thumbnail(0.1f)
//                        .into(toolbarProfileImage);
//                }
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//            });

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
            Intent setupIntent = new Intent(MainActivity.this, MainActivity.class);
            setupIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(setupIntent);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setBackgroundColor(getResources().getColor(R.color.colorIndigo900));
//        EditText searchEditText = (EditText) searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
//        searchEditText.setTextColor(getResources().getColor(R.color.colorIndigo900));
//        searchEditText.setHintTextColor(getResources().getColor(R.color.colorIndigo900));

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
    protected void onDestroy() {
        super.onDestroy();
    }

}