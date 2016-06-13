package com.nemator.needle.adapter;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nemator.needle.R;
import com.nemator.needle.activities.UserProfileActivity;
import com.nemator.needle.models.vo.FriendVO;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.viewHolders.ListItemViewHolder;
import com.nemator.needle.viewHolders.TitleViewHolder;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FriendRequestAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public final String TAG = "FriendRequestAdapter";

    public static final int TYPE_TITLE = 0;
    public static final int TYPE_ITEM = 1;
    public static final int TYPE_EMPTY = 2;

    private Context context;
    private ArrayList<Integer> positionTypes = new ArrayList<>();
    private ArrayList<FriendVO> receivedRequests;
    private ArrayList<FriendVO> sentRequests;

    public FriendRequestAdapter(Context context, ArrayList<FriendVO> receivedRequests, ArrayList<FriendVO> sentRequests) {
        this.context = context;
        this.receivedRequests = receivedRequests;
        this.sentRequests = sentRequests;

        if(this.receivedRequests == null) this.receivedRequests = new ArrayList<>();
        if(this.sentRequests == null) this.sentRequests = new ArrayList<>();

        initItemPositions();
    }

    private void initItemPositions() {
        positionTypes.clear();
        positionTypes.add(TYPE_TITLE);
        //Received
        if(receivedRequests.size() > 0 ){
            for (int i = 0; i < receivedRequests.size(); i++) {
                positionTypes.add(TYPE_ITEM);
            }
        }else{
            positionTypes.add(TYPE_EMPTY);
        }

        //Sent
        positionTypes.add(TYPE_TITLE);
        if(sentRequests.size() > 0 ){
            for (int i = 0; i < sentRequests.size(); i++) {
                positionTypes.add(TYPE_ITEM);
            }
        }else{
            positionTypes.add(TYPE_EMPTY);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        View itemView;

        switch (viewType){
            case TYPE_TITLE:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_title_view, parent, false);
                viewHolder = new TitleViewHolder(itemView);
                break;
            case TYPE_ITEM:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_list_item, parent, false);
                viewHolder = new ListItemViewHolder(itemView);
                break;
            case TYPE_EMPTY:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_empty_title_view, parent, false);
                viewHolder = new TitleViewHolder(itemView);
                break;
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);
        switch (type){
            case TYPE_TITLE:
                ((TitleViewHolder) holder).textView.setText(position == 0 ? R.string.friend_requests : R.string.sent_friend_requests);
                break;
            case TYPE_ITEM:
                final FriendVO item;
                if(position < receivedRequests.size() +1){
                    item = receivedRequests.get(position - 1);
                }else{
                    int offset = receivedRequests != null ? receivedRequests.size() + 3 : 2;
                    item = sentRequests.get(position - offset);
                }
                ((ListItemViewHolder) holder).title.setText(item.getUser().getReadableUserName());
                ((ListItemViewHolder) holder).subtitle.setText("Wants to be your friend");

                if(!item.getUser().getPictureURL().isEmpty()){
                    Picasso.with(context)
                            .load(item.getUser().getPictureURL())
                            .into(((ListItemViewHolder) holder).imageView);
                }

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(context, UserProfileActivity.class);
                        intent.putExtra(AppConstants.TAG_USER, (Parcelable) item.getUser());
                        context.startActivity(intent);
                    }
                });
                break;
            case TYPE_EMPTY :
                ((TitleViewHolder) holder).textView.setText(context.getString(R.string.no_one_here));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return positionTypes.size();
    }

    @Override
    public int getItemViewType(int position) {
        return positionTypes.get(position);
    }
}
