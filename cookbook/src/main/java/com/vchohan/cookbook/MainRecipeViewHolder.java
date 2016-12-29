package com.vchohan.cookbook;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

/**
 * Created by vchohan on 12/16/16.
 */

public class MainRecipeViewHolder extends RecyclerView.ViewHolder {

    View mView;

    ImageButton mRemoveButton;

    ImageButton mLikeButton;

    DatabaseReference mDatabaseLikes;

    FirebaseAuth mAuth;

    public MainRecipeViewHolder(View itemView) {
        super(itemView);
        mView = itemView;

        mRemoveButton = (ImageButton) mView.findViewById(R.id.recycler_options_button);

        mLikeButton = (ImageButton) mView.findViewById(R.id.recycler_like_button_one);

        mDatabaseLikes = FirebaseDatabase.getInstance().getReference().child("Likes");
        mAuth = FirebaseAuth.getInstance();

        mDatabaseLikes.keepSynced(true);
    }

    public void setImage(Context context, String image) {
        ImageView recipeImage = (ImageView) mView.findViewById(R.id.recycler_recipe_image);
        Glide.with(context).load(image).into(recipeImage);
    }

    public void setTitle(String title) {
        TextView recyclerRecipeTitle = (TextView) mView.findViewById(R.id.recycler_recipe_title);
        recyclerRecipeTitle.setText(title);
    }

    public void setUsername(String username) {
        TextView recyclerRecipeUsername = (TextView) mView.findViewById(R.id.recycler_recipe_username);
        recyclerRecipeUsername.setText(username);
    }

    public void setLikeButton(final String recipeKey) {
        mDatabaseLikes.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(recipeKey).hasChild(mAuth.getCurrentUser().getUid())) {
                    mLikeButton.setImageResource(R.drawable.ic_favorite_border_pink_24dp);
                } else {
                    mLikeButton.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
