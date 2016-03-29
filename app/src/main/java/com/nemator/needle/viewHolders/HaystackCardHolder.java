package com.nemator.needle.viewHolders;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.nemator.needle.R;
import com.nemator.needle.fragments.haystacks.HaystackListFragment;
import com.nemator.needle.models.vo.HaystackVO;

public class HaystackCardHolder extends RecyclerView.ViewHolder{
    //Item
    public TextView titleView, userCountView, active_until;
    public ImageView imageView;

    //Empty
    public TextView emptyText;

    private HaystackVO haystackData;

    private HaystackListFragment.HaystackListFragmentInteractionListener mListener;

    public HaystackCardHolder(View view, Boolean isNotEmpty, HaystackListFragment.HaystackListFragmentInteractionListener listener) {
        super(view);
        mListener = listener;
        titleView =  (TextView) view.findViewById(R.id.description);
        userCountView = (TextView)  view.findViewById(R.id.active_users);
        active_until = (TextView)  view.findViewById(R.id.active_until);
        emptyText = (TextView) view.findViewById(R.id.title);
        imageView = (ImageView) view.findViewById(R.id.thumbImage);

            /*if(isNotEmpty){
                view.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClickCard(View view) {
                        Haystack haystackData = (Haystack) getItem(getPosition());
                        Intent intent = new Intent(view.getContext(), HaystackActivity.class);
                        intent.putExtra(AppConstants.HAYSTACK_DATA_KEY, (Parcelable) haystackData);
                        view.getContext().startActivity(intent);
                    }
                });
            }*/

    }

    public HaystackCardHolder(View view) {
        super(view);
        titleView =  (TextView) view.findViewById(R.id.title);
        userCountView = (TextView)  view.findViewById(R.id.active_users);
        active_until = (TextView)  view.findViewById(R.id.active_until);
        emptyText = (TextView) view.findViewById(R.id.title);
        imageView = (ImageView) view.findViewById(R.id.thumbImage);
    }

    public void setData(HaystackVO haystack){
        haystackData = haystack;
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onClickHaystackCard(haystackData);
            }
        });
    }
}