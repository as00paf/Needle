package com.nemator.needle.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.nemator.needle.models.vo.facebook.FacebookAlbumsVO;

/**
 * Created by Alex on 28/06/2016.
 */
public class FacebookAlbumAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private FacebookAlbumsVO albums;

    public FacebookAlbumAdapter(FacebookAlbumsVO albums) {
        this.albums = albums;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
