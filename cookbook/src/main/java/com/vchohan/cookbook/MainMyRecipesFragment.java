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
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.vchohan.cookbook.login.LoginActivity;

public class MainMyRecipesFragment extends Fragment {

    public static final String TAG = MainMyRecipesFragment.class.getSimpleName();

    private RecyclerView mRecipeRecyclerView;

    private LinearLayoutManager mLinearLayoutManager;

    private FirebaseAuth mAuth = null;

    private FirebaseAuth.AuthStateListener mAuthListener;

    private DatabaseReference mDatabaseRecipes;

    private DatabaseReference mDatabaseUsers;

    private DatabaseReference mDatabaseCurrentUser;

    private Query mQueryCurrentUser;

    private DatabaseReference mDatabaseLikes;

    private boolean mProcessLike = false;

    private ProgressDialog mProgressDialog;

    public MainMyRecipesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.main_recipe_recycle_view, container, false);

        initializeFirebaseAuth();
        initializeFirebaseDatabase();
        initializeRecyclerView(rootView);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        setupFirebaseRecyclerView();
    }

    private void initializeFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (mAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(getContext(), LoginActivity.class);
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
    }

    private void initializeFirebaseDatabase() {

        mDatabaseRecipes = FirebaseDatabase.getInstance().getReference().child("Recipes");
        mDatabaseRecipes.keepSynced(true);

        mDatabaseUsers = FirebaseDatabase.getInstance().getReference().child("Users");
        mDatabaseUsers.keepSynced(true);

        mDatabaseLikes = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseLikes.keepSynced(true);
    }

    private void initializeRecyclerView(View rootView) {
        //setup recipe recyclerView
        mRecipeRecyclerView = (RecyclerView) rootView.findViewById(R.id.recipe_recycler_view);

        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mLinearLayoutManager.setReverseLayout(true);
        mLinearLayoutManager.setStackFromEnd(true);

        mRecipeRecyclerView.setHasFixedSize(true);
        mRecipeRecyclerView.setLayoutManager(mLinearLayoutManager);
    }

    private void setupFirebaseRecyclerView() {
        showProgressDialog();

        mAuth.addAuthStateListener(mAuthListener);

        // user mDatabaseRecipe for all users, else mQueryCurrentUser for logged in single user
        final FirebaseRecyclerAdapter<RecipeUtils, MainRecipeViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<RecipeUtils,
            MainRecipeViewHolder>(RecipeUtils.class, R.layout.main_recipe_custom_card_view, MainRecipeViewHolder.class, mQueryCurrentUser) {
            @Override
            protected void populateViewHolder(MainRecipeViewHolder viewHolder, RecipeUtils model, int position) {

                final String recipeKey = getRef(position).getKey();

                viewHolder.setImage(getContext(), model.getImage());
                viewHolder.setTitle(model.getTitle());
                viewHolder.setUsername(model.getUsername());

                viewHolder.setLikeButton(recipeKey);

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent addRecipeIntent = new Intent(getContext(), SingleRecipeActivity.class);
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
                                Log.w(TAG, "getUser:onCancelled", databaseError.toException());
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

    private void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(getContext());
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
}
