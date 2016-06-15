package com.nemator.needle.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nemator.needle.R;
import com.nemator.needle.viewHolders.FaqViewHolder;
import com.nemator.needle.viewHolders.HelpSupportViewHolder;

import java.util.ArrayList;


public class FaqAdapter extends RecyclerView.Adapter<FaqViewHolder>{

    public static String TAG = "FaqAdapter";

    private ArrayList<FAQListItem> data = new ArrayList<>();
    private Context context;

    public FaqAdapter(Context context) {
        this.context = context;
        initData();
    }

    private void initData() {
        //Items
        String[] titles = context.getResources().getStringArray(R.array.faq_question_titles);
        String[] descriptions = context.getResources().getStringArray(R.array.faq_question_descriptions);
        for (int i = 0; i < titles.length; i++) {
            String title = titles[i];
            String desc = descriptions[i];
            data.add(new FaqAdapter.FAQListItem(title, desc));
        }

    }

    @Override
    public FaqViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate((R.layout.faq_item), parent, false);
        FaqViewHolder holder = new FaqViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(FaqViewHolder holder, final int position) {
        FAQListItem item = data.get(position);
        holder.setData(item);
    }

    @Override
    public int getItemCount() {
        return 8;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class FAQListItem{
        public String title;
        public String description;

        public FAQListItem(String title, String description) {
            super();

            this.title = title;
            this.description = description;
        }
    }
}
