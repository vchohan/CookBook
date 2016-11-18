package com.vchohan.cookbook;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class AddNewRecipeActivity extends AppCompatActivity {

    public static final String TAG = AddNewRecipeActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_new_recipe_activity);

        createAddNewRecipeFragment(savedInstanceState);
    }

    private void createAddNewRecipeFragment(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                .replace(R.id.new_recipe_fragment_container, AddNewRecipeFragment.newInstance(), AddNewRecipeFragment.TAG).commit();
        }
    }

}
