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
import java.util.Random;

public class MainRecyclerViewAdapter extends RecyclerView.Adapter<MainRecyclerViewAdapter.ViewHolder> {

    private List<String> mDataSet;

    private Context mContext;

    private Random mRandom = new Random();

    public MainRecyclerViewAdapter(Context context, List<String> list) {
        mDataSet = list;
        mContext = context;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView mTextView;

        public ImageButton mRemoveButton;

        public LinearLayout mLinearLayout;

        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.recipe_title_header);
            mRemoveButton = (ImageButton) v.findViewById(R.id.remove_button);
            mLinearLayout = (LinearLayout) v.findViewById(R.id.recycler_card_view);
        }
    }

    @Override
    public MainRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create a new View
        View v = LayoutInflater.from(mContext).inflate(R.layout.main_recycler_custom_view, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        holder.mTextView.setText((String) mDataSet.get(position));

        // Set a click listener for TextView
        holder.mTextView.setOnClickListener(new View.OnClickListener() {
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

                /*
                    public final void notifyItemRemoved (int position)
                        Notify any registered observers that the item previously located at position
                        has been removed from the data set. The items previously located at and
                        after position may now be found at oldPosition - 1.

                        This is a structural change event. Representations of other existing items
                        in the data set are still considered up to date and will not be rebound,
                        though their positions may be altered.

                    Parameters
                        position : Position of the item that has now been removed
                */
                notifyItemRemoved(position);

                /*
                    public final void notifyItemRangeChanged (int positionStart, int itemCount)
                        Notify any registered observers that the itemCount items starting at
                        position positionStart have changed. Equivalent to calling
                        notifyItemRangeChanged(position, itemCount, null);.

                        This is an item change event, not a structural change event. It indicates
                        that any reflection of the data in the given position range is out of date
                        and should be updated. The items in the given range retain the same identity.

                    Parameters
                        positionStart : Position of the first item that has changed
                        itemCount : Number of items that have changed
                */
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

//    // Custom method to apply emboss mask filter to TextView
//    protected void applyEmbossMaskFilter(TextView recipe_title_header) {
//        EmbossMaskFilter embossFilter = new EmbossMaskFilter(
//            new float[]{1f, 5f, 1f}, // direction of the light source
//            0.8f, // ambient light between 0 to 1
//            8, // specular highlights
//            7f // blur before applying lighting
//        );
//        recipe_title_header.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
//        recipe_title_header.getPaint().setMaskFilter(embossFilter);
//    }
//
//    // Custom method to generate random HSV color
//    protected int getRandomHSVColor() {
//        // Generate a random hue value between 0 to 360
//        int hue = mRandom.nextInt(361);
//        // We make the color depth full
//        float saturation = 1.0f;
//        // We make a full bright color
//        float value = 1.0f;
//        // We avoid color transparency
//        int alpha = 255;
//        // Finally, generate the color
//        int color = Color.HSVToColor(alpha, new float[]{hue, saturation, value});
//        // Return the color
//        return color;
//    }
//
//    // Custom method to create a GradientDrawable object
//    protected GradientDrawable getGradientDrawable() {
//        GradientDrawable gradient = new GradientDrawable();
//        gradient.setGradientType(GradientDrawable.SWEEP_GRADIENT);
//        gradient.setColors(new int[]{getRandomHSVColor(), getRandomHSVColor(), getRandomHSVColor()});
//        return gradient;
//    }
//
//    // Custom method to get a darker color
//    protected int getDarkerColor(int color) {
//        float[] hsv = new float[3];
//        Color.colorToHSV(color, hsv);
//        hsv[2] = 0.8f * hsv[2];
//        return Color.HSVToColor(hsv);
//    }
//
//    // Custom method to get a lighter color
//    protected int getLighterColor(int color) {
//        float[] hsv = new float[3];
//        Color.colorToHSV(color, hsv);
//        hsv[2] = 0.2f + 0.8f * hsv[2];
//        return Color.HSVToColor(hsv);
//    }
//
//    // Custom method to get reverse color
//    protected int getReverseColor(int color) {
//        float[] hsv = new float[3];
//        Color.RGBToHSV(
//            Color.red(color), // Red value
//            Color.green(color), // Green value
//            Color.blue(color), // Blue value
//            hsv
//        );
//        hsv[0] = (hsv[0] + 180) % 360;
//        return Color.HSVToColor(hsv);
//    }
}
