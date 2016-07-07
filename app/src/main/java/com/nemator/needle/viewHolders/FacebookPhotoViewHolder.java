package com.nemator.needle.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.facebook.AccessToken;
import com.nemator.needle.R;
import com.nemator.needle.adapter.FacebookPhotosAdapter;
import com.nemator.needle.models.vo.facebook.FacebookPictureDataVO;
import com.squareup.picasso.Picasso;

public class FacebookPhotoViewHolder extends RecyclerView.ViewHolder {

    public ImageView icon;
    private FacebookPictureDataVO photo;
    private FacebookPhotosAdapter.ClickListener listener;

    public FacebookPhotoViewHolder(View itemView, FacebookPhotosAdapter.ClickListener listener) {
        super(itemView);

        this.listener = listener;

        icon = (ImageView) itemView.findViewById(R.id.imageView);
    }

    public void setData(FacebookPictureDataVO data) {
        this.photo = data;

        String token = AccessToken.getCurrentAccessToken().getToken();
        String url = "https://graph.facebook.com/" + photo.getId() + "/picture?access_token=" + token;
        this.photo.setUrl(url);

        Picasso.with(itemView.getContext())
                .load(url)
                .error(R.drawable.person_placeholder)
                .into(icon);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(photo);
            }
        });

    }
}
