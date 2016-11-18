package com.vchohan.cookbook;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by vchohan on 11/18/16.
 */

/**
 * A placeholder fragment containing a simple view.
 */
public class AddNewRecipeFragment extends Fragment {

    public static final String TAG = AddNewRecipeFragment.class.getSimpleName();

    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    public AddNewRecipeFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static AddNewRecipeFragment newInstance() {
        AddNewRecipeFragment fragment = new AddNewRecipeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.add_new_recipe_fragment, container, false);
        return rootView;
    }
}
