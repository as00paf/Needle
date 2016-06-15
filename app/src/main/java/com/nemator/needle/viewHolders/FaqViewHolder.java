package com.nemator.needle.viewHolders;

import android.animation.Animator;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nemator.needle.R;
import com.nemator.needle.adapter.FaqAdapter;

public class FaqViewHolder extends RecyclerView.ViewHolder {

    public TextView title, description;
    public ImageView icon;
    private FaqAdapter.FAQListItem data;

    public FaqViewHolder (View itemView) {
        super(itemView);

        title = (TextView) itemView.findViewById(R.id.title);
        description = (TextView) itemView.findViewById(R.id.description);
        icon = (ImageView) itemView.findViewById(R.id.icon);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentVisibility = description.getVisibility();
                if(currentVisibility == View.VISIBLE){
                    //Arrow
                    icon.animate().rotation(0).setDuration(300).start();

                    //Description
                    description.setVisibility(View.GONE);
                    description.setAlpha(0.0f);
                }else{
                    //Arrow
                    icon.animate().rotation(90).setDuration(300).start();

                    description.setAlpha(1.0f);
                    description.setVisibility(View.VISIBLE);

                    /*Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ((RecyclerView) FaqViewHolder.this.itemView.getParent()).smoothScrollToPosition(((RecyclerView) FaqViewHolder.this.itemView.getParent()).indexOfChild(FaqViewHolder.this.itemView));
                        }
                    }, 100);*/
                }
            }
        });
    }



    public void setData(FaqAdapter.FAQListItem data) {
        this.data = data;

        title.setText(data.title);
        description.setText(data.description);
    }
}
