package com.nemator.needle.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nemator.needle.R;
import com.nemator.needle.models.vo.facebook.FacebookAlbumVO;
import com.nemator.needle.models.vo.facebook.FacebookAlbumsVO;
import com.nemator.needle.viewHolders.FacebookAlbumViewHolder;

/**
 * Created by Alex on 28/06/2016.
 */
public class FacebookAlbumAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private FacebookAlbumsVO albums;
    private ClickListener listener;

    public FacebookAlbumAdapter(FacebookAlbumsVO albums, ClickListener listener) {
        this.albums = albums;
        this.listener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate((R.layout.list_image_item), parent, false);
        FacebookAlbumViewHolder holder = new FacebookAlbumViewHolder(view, listener);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        FacebookAlbumVO item = albums.get(position);
        ((FacebookAlbumViewHolder) holder).setData(item);
    }

    @Override
    public int getItemCount() {
        return albums.getData().size();
    }

    public static class FacebookAlbumListItem{
        public String title;
        public String description;
        public String url;

        public FacebookAlbumListItem(String title, String description, String url) {
            super();

            this.title = title;
            this.description = description;
            this.url = url;
        }
    }

    public interface ClickListener{
        void onClick(FacebookAlbumVO album);
    }
}
