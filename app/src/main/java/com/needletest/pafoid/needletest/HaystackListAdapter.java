package com.needletest.pafoid.needletest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.needletest.pafoid.needletest.asynctask.ImageDownloaderTask;
import com.needletest.pafoid.needletest.models.Haystack;

import java.util.ArrayList;
import java.util.TreeSet;

public class HaystackListAdapter extends BaseAdapter {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;
    private static final int TYPE_EMPTY = 2;

	private ArrayList<Object> listData;
    private TreeSet<Integer> sectionHeader = new TreeSet<Integer>();
    private TreeSet<Integer> emptyItems = new TreeSet<Integer>();
    private LayoutInflater layoutInflater;
    private Context mContext;

    public HaystackListAdapter(Context context) {
        layoutInflater = LayoutInflater.from(context);
        mContext = context;
    }

    public void addAllItems(ArrayList<Object> data){
        listData = data;
        sectionHeader = new TreeSet<Integer>();

        for(int i=0;i<listData.size();i++){
            if(!(getItem(i) instanceof Haystack)){
                if(getItem(i).equals(mContext.getResources().getString(R.string.noHaystackAvailable))){
                    emptyItems.add(i);
                }else{
                    sectionHeader.add(i);
                }
            }
        }

        notifyDataSetChanged();
    }

    public void addItem(final Haystack item) {
        listData.add(item);
        notifyDataSetChanged();
    }

    public void addEmptyItem(final String item) {
        listData.add(item);
        emptyItems.add(listData.size() - 1);
        notifyDataSetChanged();
    }

    public void addSectionHeaderItem(final String item) {
        listData.add(item);
        sectionHeader.add(listData.size() - 1);
        notifyDataSetChanged();
    }

    @Override
	public int getCount() {
		return (listData != null) ? listData.size() : 0;
	}

	@Override
	public Object getItem(int position) {
		return listData.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

    @Override
    public int getItemViewType(int position) {
        if(sectionHeader.contains(position)){
            return TYPE_SEPARATOR;
        }else if(emptyItems.contains(position)){
            return TYPE_EMPTY;
        }else{
            return TYPE_ITEM;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        int rowType = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();

            switch (rowType) {
                case TYPE_ITEM:
                    convertView = layoutInflater.inflate(R.layout.haystack_list_item_layout, null);
                    holder.titleView = (TextView) convertView.findViewById(R.id.title);
                    holder.userCountView = (TextView) convertView.findViewById(R.id.active_users);
                    holder.imageView = (ImageView) convertView.findViewById(R.id.thumbImage);

                    Haystack haystack = (Haystack) listData.get(position);
                    holder.titleView.setText(haystack.getName());
                    holder.userCountView.setText(haystack.getActiveUsers().size()+" active users");

                    if (holder.imageView != null) {
                        new ImageDownloaderTask(holder.imageView).execute(haystack.getPictureURL());
                    }
                    break;
                case TYPE_SEPARATOR:
                    convertView = layoutInflater.inflate(R.layout.haystack_list_separator_layout, null);
                    holder.separatorText = (TextView) convertView.findViewById(R.id.textSeparator);
                    holder.separatorText.setText(mContext.getResources().getString((position == 0) ? R.string.publicHeader : R.string.privateHeader));
                    break;
                case TYPE_EMPTY:
                    convertView = layoutInflater.inflate(R.layout.haystack_empty_list_item_layout, null);
                    holder.emptyText = (TextView) convertView.findViewById(R.id.emptyText);
                    holder.emptyText.setText(mContext.getResources().getString(R.string.noHaystackAvailable));
                    break;
            }


            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        return convertView;
    }
 
    static class ViewHolder {
        //Item
        TextView titleView;
        TextView userCountView;
        ImageView imageView;

        //Header
        TextView separatorText;

        //Empty
        TextView emptyText;
    }

}
