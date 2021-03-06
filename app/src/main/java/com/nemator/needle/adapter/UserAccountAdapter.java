package com.nemator.needle.adapter;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.controller.AuthenticationController;
import com.nemator.needle.interfaces.IUserProfileListener;
import com.nemator.needle.models.vo.FriendshipVO;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.viewHolders.ButtonViewHolder;
import com.nemator.needle.viewHolders.ListItemViewHolder;

import java.util.ArrayList;

public class UserAccountAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public final String TAG = "UserAccountAdapter";

    private final int EMAIL_ACCOUNT = 0;
    private final int FACEBOOK_ACCOUNT = 1;
    private final int GOOGLE_ACCOUNT = 2;
    private final int TWITTER_ACCOUNT = 3;
    private final int ADD_FRIEND = 4;
    private final int ACCEPT_FRIEND_REQUEST = 5;
    private final int REJECT_FRIEND_REQUEST = 6;
    private final int CANCEL_FRIEND_REQUEST = 7;
    private final int UN_FRIEND = 8;
    private final int ADD_TO_GROUP = 9;
    private final int SEND_LOCATION = 10;

    private Context context;
    private UserVO user;
    private Boolean isMe;
    private FriendshipVO friendship;
    private ArrayList<Integer> positionTypes = new ArrayList<>();
    private IUserProfileListener listener;

    public UserAccountAdapter(Context context, UserVO user, Boolean isMe, FriendshipVO friendship, IUserProfileListener listener) {
        this.context = context;
        this.user = user;
        this.isMe = isMe;
        this.friendship = friendship;
        this.listener = listener;

        initItemPositions();
    }

    private void initItemPositions() {
        if(isMe){
            positionTypes.add(EMAIL_ACCOUNT);
            positionTypes.addAll(orderSocialAccounts());
        }else if (friendship != null){
            if(friendship.getStatus() == AppConstants.FRIEND_REQUESTED && friendship.getFriendId() == Needle.userModel.getUserId()){
                positionTypes.add(ACCEPT_FRIEND_REQUEST);
                positionTypes.add(REJECT_FRIEND_REQUEST);
            }else if(friendship.getStatus() == AppConstants.FRIEND_REQUESTED && friendship.getUserId() == Needle.userModel.getUserId()){
                positionTypes.add(CANCEL_FRIEND_REQUEST);
            }

            if(friendship.getStatus() == AppConstants.FRIEND){
                positionTypes.add(UN_FRIEND);
                positionTypes.add(ADD_TO_GROUP);
                positionTypes.add(SEND_LOCATION);
            }
        }else{
            positionTypes.add(ADD_FRIEND);
        }
    }

    private ArrayList<Integer> orderSocialAccounts() {
        ArrayList<Integer> result = new ArrayList<>();
        if(user.getLoginType() == AuthenticationController.LOGIN_TYPE_FACEBOOK) result.add(FACEBOOK_ACCOUNT);
        if(user.getLoginType() == AuthenticationController.LOGIN_TYPE_TWITTER) result.add(TWITTER_ACCOUNT);
        if(user.getLoginType() == AuthenticationController.LOGIN_TYPE_GOOGLE) result.add(GOOGLE_ACCOUNT);

        //if(!result.contains(FACEBOOK_ACCOUNT)) result.add(FACEBOOK_ACCOUNT);
        //if(!result.contains(TWITTER_ACCOUNT)) result.add(TWITTER_ACCOUNT);
        //if(!result.contains(GOOGLE_ACCOUNT)) result.add(GOOGLE_ACCOUNT);

        return result;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView;
        RecyclerView.ViewHolder viewHolder;

        if(viewType == ADD_FRIEND || viewType == ADD_TO_GROUP || viewType == SEND_LOCATION || viewType == UN_FRIEND ||
            viewType == ACCEPT_FRIEND_REQUEST || viewType == REJECT_FRIEND_REQUEST || viewType == CANCEL_FRIEND_REQUEST){
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.button_item, parent, false);
            viewHolder = new ButtonViewHolder(itemView);
        }else{
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
            viewHolder = new ListItemViewHolder(itemView);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        int type = getItemViewType(position);

        switch (type){
            case EMAIL_ACCOUNT :
                ((ListItemViewHolder) holder).title.setText(context.getString(R.string.email));
                ((ListItemViewHolder) holder).subtitle.setText(user.getEmail());
                ((ListItemViewHolder) holder).imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_email_black_24dp));
                break;
            case FACEBOOK_ACCOUNT :
                ((ListItemViewHolder) holder).title.setText(context.getString(R.string.facebook_account));
                ((ListItemViewHolder) holder).title.setTextColor(ContextCompat.getColor(context, R.color.com_facebook_blue));
                ((ListItemViewHolder) holder).subtitle.setText(user.getEmail());
                ((ListItemViewHolder) holder).imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_facebook_black_24dp));
                break;
            case TWITTER_ACCOUNT :
                ((ListItemViewHolder) holder).title.setText(context.getString(R.string.facebook_account));
                ((ListItemViewHolder) holder).title.setTextColor(ContextCompat.getColor(context, R.color.tw__blue_default));
                ((ListItemViewHolder) holder).subtitle.setText(user.getEmail());
                ((ListItemViewHolder) holder).imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.twitter_logo_white));
                break;
            case GOOGLE_ACCOUNT :
                ((ListItemViewHolder) holder).title.setText(context.getString(R.string.google_account_title));
                ((ListItemViewHolder) holder).subtitle.setText(user.getEmail());
                ((ListItemViewHolder) holder).imageView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_google_black_24dp));
                break;
            case ADD_FRIEND:
                ((ButtonViewHolder) holder).button.setText(context.getString(R.string.add_friend));
                ((ButtonViewHolder) holder).button.setCompoundDrawablesWithIntrinsicBounds(null,ContextCompat.getDrawable(context, R.drawable.ic_person_add_black_36dp), null, null);
                ((ButtonViewHolder) holder).button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.sendFriendRequest(user);
                    }
                });
                break;
            case UN_FRIEND:
                ((ButtonViewHolder) holder).button.setText(context.getString(R.string.unfriend));
                ((ButtonViewHolder) holder).button.setCompoundDrawablesWithIntrinsicBounds(null, ContextCompat.getDrawable(context, R.drawable.ic_account_remove_black_36dp), null, null);
                ((ButtonViewHolder) holder).button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.unfriend(user);
                    }
                });
                break;
            case ACCEPT_FRIEND_REQUEST:
                ((ButtonViewHolder) holder).button.setText(context.getString(R.string.accept_friend_request));
                ((ButtonViewHolder) holder).button.setCompoundDrawablesWithIntrinsicBounds(null,ContextCompat.getDrawable(context, R.drawable.ic_person_add_black_36dp), null, null);
                ((ButtonViewHolder) holder).button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.acceptFriendRequest(user);
                    }
                });
                break;
            case CANCEL_FRIEND_REQUEST:
                ((ButtonViewHolder) holder).button.setText(context.getString(R.string.cancel_friend_request));
                ((ButtonViewHolder) holder).button.setCompoundDrawablesWithIntrinsicBounds(null,ContextCompat.getDrawable(context, R.drawable.ic_account_remove_black_36dp), null, null);
                ((ButtonViewHolder) holder).button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.cancelFriendRequest(user);
                    }
                });
                break;
            case REJECT_FRIEND_REQUEST:
                ((ButtonViewHolder) holder).button.setText(context.getString(R.string.reject_friend_request));
                ((ButtonViewHolder) holder).button.setCompoundDrawablesWithIntrinsicBounds(null,ContextCompat.getDrawable(context, R.drawable.ic_account_remove_black_36dp), null, null);
                ((ButtonViewHolder) holder).button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.rejectFriendRequest(user);
                    }
                });
                break;
            case ADD_TO_GROUP:
                ((ButtonViewHolder) holder).button.setText(context.getString(R.string.add_to_group));
                ((ButtonViewHolder) holder).button.setCompoundDrawablesWithIntrinsicBounds(null,ContextCompat.getDrawable(context, R.drawable.ic_group_add_black_36dp), null, null);
                ((ButtonViewHolder) holder).button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.addUserToGroup(user);
                    }
                });
                break;
            case SEND_LOCATION:
                ((ButtonViewHolder) holder).button.setText(context.getString(R.string.send_needle));
                ((ButtonViewHolder) holder).button.setCompoundDrawablesWithIntrinsicBounds(null,ContextCompat.getDrawable(context, R.drawable.ic_add_needle_36dp), null, null);
                ((ButtonViewHolder) holder).button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.sendNeedle(user);
                    }
                });
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
