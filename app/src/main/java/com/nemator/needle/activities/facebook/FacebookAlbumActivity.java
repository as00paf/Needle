package com.nemator.needle.activities.facebook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.nemator.needle.R;
import com.nemator.needle.adapter.FacebookAlbumAdapter;
import com.nemator.needle.adapter.FacebookPhotosAdapter;
import com.nemator.needle.models.vo.facebook.FacebookAlbumVO;
import com.nemator.needle.models.vo.facebook.FacebookPictureDataVO;
import com.nemator.needle.models.vo.facebook.FacebookPictureVO;
import com.nemator.needle.utils.AppConstants;

public class FacebookAlbumActivity extends AppCompatActivity implements FacebookPhotosAdapter.ClickListener {

    private static final String TAG = "FbAlbumActivity";
    private Toolbar toolbar;
    private RecyclerView list;
    private FacebookAlbumAdapter albumAdapter;
    private FacebookAlbumVO album;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_list);

        Intent intent = getIntent();
        if(intent != null){
            album = (FacebookAlbumVO) intent.getExtras().getSerializable(AppConstants.TAG_ALBUM);
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        list = (RecyclerView) findViewById(R.id.list);

        toolbar.setTitle(album.getName());
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3, LinearLayoutManager.VERTICAL, false);
        list.setLayoutManager(layoutManager);

        FacebookPhotosAdapter adapter = new FacebookPhotosAdapter(album.getPhotos(), this);
        list.setAdapter(adapter);
    }

    @Override
    public void onClick(FacebookPictureDataVO photo) {
        finishWithResult(photo.getUrl());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                super.onBackPressed();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void finishWithResult(String url){
        Intent returnIntent = new Intent();
        returnIntent.putExtra("result", url);
        setResult(RESULT_OK, returnIntent);
        finish();
    }
}
