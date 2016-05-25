package com.nemator.needle.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.nemator.needle.R;

public class ButtonViewHolder extends RecyclerView.ViewHolder{

    public Button button;

    public ButtonViewHolder(View itemView) {
        super(itemView);

        button = (Button) itemView.findViewById(R.id.button);
    }
}
