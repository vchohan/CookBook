package com.vchohan.cookbook;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewAdapter.ViewHolder> {

    private List<String> mDataSet;

    private Context mContext;

    public MainRecyclerViewAdapter(Context context, List<String> list) {
        mDataSet = list;
        mContext = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mRecipeTitleHeader;

        public ImageButton mRemoveButton;

        public LinearLayout mLinearLayout;

        public ViewHolder(View v) {
            super(v);
            mLinearLayout = (LinearLayout) v.findViewById(R.id.recycler_card_view);
            mRecipeTitleHeader = (TextView) v.findViewById(R.id.recycler_recipe_title);
            mRemoveButton = (ImageButton) v.findViewById(R.id.recycler_remove_button);
        }
    }

    @Override
    public MainRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, final int viewType) {
        // Create a new View
        View v = LayoutInflater.from(mContext).inflate(R.layout.main_recycler_custom_view, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mRecipeTitleHeader.setText((String) mDataSet.get(position));

        // Set a click listener for TextView
        holder.mRecipeTitleHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String recipe = mDataSet.get(position);
                Toast.makeText(mContext, recipe, Toast.LENGTH_SHORT).show();
            }
        });

        // Set a click listener for item remove button
        holder.mRemoveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the clicked item label
                String itemLabel = mDataSet.get(position);

                // Remove the item on remove/button click
                mDataSet.remove(position);

                notifyItemRemoved(position);

                notifyItemRangeChanged(position, mDataSet.size());

                // Show the removed item label
                Toast.makeText(mContext, "Removed : " + itemLabel, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataSet.size();
    }
}
