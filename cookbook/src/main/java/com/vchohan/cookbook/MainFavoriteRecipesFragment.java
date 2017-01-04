package com.vchohan.cookbook;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import android.app.ProgressDialog;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by waw069 on 12/28/16.
 */

public class MainFavoriteRecipesFragment extends Fragment {

    public static final String TAG = MainFavoriteRecipesFragment.class.getSimpleName();

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

    public MainFavoriteRecipesFragment() {
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

//        initializeFirebaseAuth();
//        initializeFirebaseDatabase();
//        initializeRecyclerView(rootView);

        return rootView;
    }



}
