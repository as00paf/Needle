package com.nemator.needle.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.nemator.needle.R;

public class TitleViewHolder extends RecyclerView.ViewHolder{

    public TextView textView;

    public TitleViewHolder(View itemView) {
        super(itemView);

        textView = (TextView) itemView.findViewById(R.id.title);
    }
}
