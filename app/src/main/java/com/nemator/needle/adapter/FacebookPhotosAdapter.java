package com.nemator.needle.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nemator.needle.R;
import com.nemator.needle.models.vo.facebook.FacebookAlbumVO;
import com.nemator.needle.models.vo.facebook.FacebookPictureDataVO;
import com.nemator.needle.models.vo.facebook.FacebookPictureVO;
import com.nemator.needle.models.vo.facebook.FacebookPicturesVO;
import com.nemator.needle.viewHolders.FacebookAlbumViewHolder;
import com.nemator.needle.viewHolders.FacebookPhotoViewHolder;

/**
 * Created by Alex on 28/06/2016.
 */
public class FacebookPhotosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private FacebookPicturesVO photos;
    private ClickListener listener;

    public FacebookPhotosAdapter(FacebookPicturesVO photos, ClickListener listener) {
        this.photos = photos;
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate((R.layout.list_image), parent, false);
        FacebookPhotoViewHolder holder = new FacebookPhotoViewHolder(view, listener);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FacebookPictureDataVO item = photos.get(position);
        ((FacebookPhotoViewHolder) holder).setData(item);
    }

    @Override
    public int getItemCount() {
        return photos.getData().size();
    }

    public interface ClickListener{
        void onClick(FacebookPictureDataVO photo);
    }
}
