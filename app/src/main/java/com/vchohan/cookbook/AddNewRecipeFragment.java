package com.vchohan.cookbook;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

/**
 * A placeholder fragment containing a simple view.
 */
public class AddNewRecipeFragment extends Fragment {

    public static final String TAG = AddNewRecipeFragment.class.getSimpleName();

    private static Button recipeSaveButton;

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

        initializeView(rootView);

        return rootView;
    }

    private void initializeView(View rootView) {
        recipeSaveButton = (Button) rootView.findViewById(R.id.save_button);
        recipeSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String recipeSaved = getString(R.string.recipe_saved);
                Toast.makeText(getContext(), recipeSaved, Toast.LENGTH_SHORT).show();

                Intent 
            }
        });
    }
}
