package com.nemator.needle.viewHolders;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nemator.needle.R;
import com.nemator.needle.activities.UserProfileActivity;
import com.nemator.needle.models.vo.NotificationVO;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.utils.AppUtils;
import com.squareup.picasso.Picasso;

public class NotificationCardHolder extends RecyclerView.ViewHolder {

    private static final String TAG = "NotificationCard";
    public TextView titleView, sentAt;
    public ImageView imageView;

    private NotificationVO notification;
    private NotificationCardListener listener;

    public NotificationCardHolder(View view, NotificationCardListener listener) {
        super(view);

        this.listener = listener;
        titleView =  (TextView) view.findViewById(R.id.title);
        sentAt =  (TextView) view.findViewById(R.id.sent_at);
        imageView = (ImageView) view.findViewById(R.id.imageView);
    }

    public void setData(NotificationVO data){
        notification = data;
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(v, notification);
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickUser();
            }
        });

        //Title
        String desc = data.getDescription().substring(0,1).toUpperCase() + data.getDescription().substring(1).toLowerCase();//Cap first letter

        //Make user clickable
        SpannableString ss = new SpannableString(desc);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                onClickUser();
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        int start = data.getDescription().indexOf(data.getSenderName());
        int end = start + data.getSenderName().length();
        ss.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        //Make item clickable
        //TODO

        titleView.setText(ss);
        titleView.setMovementMethod(LinkMovementMethod.getInstance());
        titleView.setHighlightColor(ContextCompat.getColor(itemView.getContext(), R.color.primary_dark));

        //Date
        sentAt.setText(AppUtils.formatDateRelative(notification.getSentAt()));

        //User Profile Picture
        Picasso.with(imageView.getContext()).cancelRequest(imageView);

        if(notification.getSenderPictureURL() != null && !notification.getSenderPictureURL().isEmpty()){
            Picasso.with(imageView.getContext())
                    .load(notification.getSenderPictureURL())
                    .placeholder(R.color.bg_grey)
                    .error(R.drawable.person_placeholder)
                    .into(imageView);
        }else{
            Picasso.with(imageView.getContext())
                    .load(R.drawable.person_placeholder)
                    .into(imageView);
        }
    }

    private void onClickUser(){
        Intent intent = new Intent(itemView.getContext(), UserProfileActivity.class);
        intent.putExtra(AppConstants.TAG_USER_ID, notification.getSenderId());
        itemView.getContext().startActivity(intent);
    }

    public interface NotificationCardListener {
        void onClick(View view, NotificationVO vo);
    }
}
