package com.nemator.needle.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nemator.needle.R;
import com.nemator.needle.adapter.FacebookAlbumAdapter;
import com.nemator.needle.models.vo.facebook.FacebookAlbumVO;
import com.squareup.picasso.Picasso;

public class FacebookAlbumViewHolder extends RecyclerView.ViewHolder {

    public TextView title, description;
    public ImageView icon;
    private FacebookAlbumVO album;
    private FacebookAlbumAdapter.ClickListener listener;

    public FacebookAlbumViewHolder(View itemView, FacebookAlbumAdapter.ClickListener listener) {
        super(itemView);

        this.listener = listener;

        title = (TextView) itemView.findViewById(R.id.title);
        description = (TextView) itemView.findViewById(R.id.sub_title);
        icon = (ImageView) itemView.findViewById(R.id.imageView);
    }

    public void setData(FacebookAlbumVO data) {
        this.album = data;

        title.setText(data.getName());
        String count = data.getCount() + " " + itemView.getContext().getString(R.string.photos);
        description.setText(count);

        Picasso.with(itemView.getContext())
                .load(data.getPicture().getData().getUrl())
                .into(icon);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(album);
            }
        });

    }
}
