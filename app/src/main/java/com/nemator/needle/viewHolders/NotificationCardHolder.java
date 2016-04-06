package com.nemator.needle.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nemator.needle.R;
import com.nemator.needle.models.vo.NotificationVO;
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
                listener.onClickUser(notification);
            }
        });

        titleView.setText(notification.getDescription());
        sentAt.setText(notification.getSentAt());

        Picasso.with(imageView.getContext()).cancelRequest(imageView);

        Picasso.with(imageView.getContext())
                .load(notification.getSenderPictureURL())
                .into(imageView);
    }

    public interface NotificationCardListener {
        void onClick(View view, NotificationVO vo);
        void onClickUser(NotificationVO vo);
    }
}
