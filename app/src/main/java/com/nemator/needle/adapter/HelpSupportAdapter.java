package com.nemator.needle.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nemator.needle.R;
import com.nemator.needle.viewHolders.HelpSupportViewHolder;


public class HelpSupportAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public static String TAG = "NeedleCardAdapter";

    public static final int HEADER       = 0;
    public static final int HELP = 1;
    public static final int FAQ          = 2;
    public static final int WEBSITE      = 3;
    public static final int EMAIL        = 4;
    public static final int PHONE        = 5;
    public static final int FACEBOOK     = 6;
    public static final int TWITTER      = 7;
    public static final int YOUTUBE      = 8;
    public static final int DEBUG        = 9;
    public static final int FOOTER       = 10;

    private ClickHandler handler;

    public HelpSupportAdapter(ClickHandler handler) {
        this.handler = handler;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder holder = null;
        View view;

        switch (viewType){
            case HEADER:
                view = LayoutInflater.from(parent.getContext()).inflate((R.layout.help_support_header), parent, false);
                holder = new HelpSupportViewHolder.Header(view);
                break;
            case FAQ:
            case HELP:
            case PHONE:
            case EMAIL:
            case WEBSITE:
            case FACEBOOK:
            case TWITTER:
            case YOUTUBE:
            case DEBUG:
                view = LayoutInflater.from(parent.getContext()).inflate((R.layout.help_support_item), parent, false);
                holder = new HelpSupportViewHolder.Item(view);
                break;
            case FOOTER:
                view = LayoutInflater.from(parent.getContext()).inflate((R.layout.help_support_header), parent, false);
                holder = new HelpSupportViewHolder.Header(view);
                break;
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        int type = getItemViewType(position);
        if(type != HEADER && type != FOOTER){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handler.onClickItem(position);
                }
            });

            ((HelpSupportViewHolder.Item) holder).title.setText(getTitleByPosition(position));
            ((HelpSupportViewHolder.Item) holder).icon.setImageResource(getIconByPosition(position));
        }
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    private int getTitleByPosition(int position) {
        switch (position){
            case FAQ:
                return R.string.faq;
            case HELP:
                return R.string.help;
            case PHONE:
                return R.string.call;
            case EMAIL:
                return R.string.send_email;
            case WEBSITE:
                return R.string.visit_website;
            case FACEBOOK:
                return R.string.follow_facebook;
            case TWITTER:
                return R.string.follow_twitter;
            case YOUTUBE:
                return R.string.follow_youtube;
            case DEBUG:
                return R.string.debug;
            default:
                return -1;
        }
    }

    private int getIconByPosition(int position) {
        switch (position){
            case FAQ:
                return R.drawable.ic_question_answer_black_24dp;
            case HELP:
                return R.drawable.ic_help_black_24dp;
            case PHONE:
                return R.drawable.ic_phone_black_24dp;
            case EMAIL:
                return R.drawable.ic_email_black_24dp;
            case WEBSITE:
                return R.drawable.ic_web_black_24dp;
            case FACEBOOK:
                return R.drawable.ic_facebook_black_24dp;
            case TWITTER:
                return R.drawable.ic_twitter_black_24dp;
            case YOUTUBE:
                return R.drawable.ic_youtube_24dp;
            case DEBUG:
                return R.drawable.ic_settings_black_24dp;
            default:
                return R.drawable.ic_settings_black_24dp;
        }
    }

    @Override
    public int getItemCount() {
        return 10;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    //Interfaces
    public interface ClickHandler{
        void onClickItem(int position);
    }
}
