package com.nemator.needle.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nemator.needle.R;

public class HelpSupportViewHolder {

    public static class Header extends RecyclerView.ViewHolder{
        public Header(View itemView) {
            super(itemView);
        }
    }

    public static class Item extends RecyclerView.ViewHolder{

        public TextView title;
        public ImageView icon;

        public Item(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.title);
            icon = (ImageView) itemView.findViewById(R.id.icon);
        }
    }
}
