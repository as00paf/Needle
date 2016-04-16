package com.nemator.needle.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nemator.needle.R;
import com.nemator.needle.adapter.UserProfileAdapter;

/**
 * Created by Alex on 10/04/2016.
 */
public class ListItemViewHolder extends RecyclerView.ViewHolder{

    public TextView title, subtitle;
    public ImageView imageView;

    public ListItemViewHolder(View itemView) {
        super(itemView);

        title = (TextView) itemView.findViewById(R.id.title);
        subtitle = (TextView) itemView.findViewById(R.id.sub_title);
        imageView = (ImageView) itemView.findViewById(R.id.imageView);
    }
}
