package com.nemator.needle.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.fragments.haystacks.HaystackListFragment;
import com.nemator.needle.fragments.haystacks.HaystackListTabFragment;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.utils.AppConstants;
import com.nemator.needle.viewHolders.HaystackCardHolder;

import java.util.ArrayList;

public class HaystackCardAdapter extends RecyclerView.Adapter<HaystackCardHolder> {
    public static String TAG = "HaystackCardAdapter";

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_EMPTY = 2;

    private ArrayList<HaystackVO> listData;
    private Context mContext;
    private HaystackListTabFragment.HaystackListFragmentInteractionListener mListener;

    public HaystackCardAdapter(ArrayList<HaystackVO> data, Context context, HaystackListTabFragment.HaystackListFragmentInteractionListener listener) {
        listData = data;
        mContext = context;
        mListener = listener;

        if(listData == null){
            listData = new ArrayList<HaystackVO>();
        }
    }

    public Object getItem(int position) {
        if(listData.size() == 0){
            return "empty";
        }
        return listData.get(position);
    }

    @Override
    public HaystackCardHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        HaystackCardHolder viewHolder;
        View haystackCard;

        if(viewType == TYPE_ITEM){
            haystackCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_haystack, parent, false);
            viewHolder = new HaystackCardHolder(haystackCard, mListener);
        }else{
            haystackCard = LayoutInflater.from(parent.getContext()).inflate(R.layout.haystack_empty_card_layout, parent, false);
            viewHolder = new HaystackCardHolder(haystackCard, mListener);
        }

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(HaystackCardHolder holder, int position) {
        int cardType = getItemViewType(position);

        switch (cardType) {
            case TYPE_ITEM:
                HaystackVO haystack = listData.get(position);
                holder.setData(haystack);
                break;
            case TYPE_EMPTY:
                holder.emptyText.setText(mContext.getResources().getString(R.string.noHaystackAvailable));
                break;
        }
    }

    @Override
    public int getItemCount() {
        if(listData.size() == 0)
            return 1;
        return listData.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(getItem(position) instanceof HaystackVO){
            return TYPE_ITEM;
        }else{
            return TYPE_EMPTY;
        }
    }
}

