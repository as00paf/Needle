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

public class HaystackListAdapter extends BaseAdapter {

	private ArrayList listData;
    private LayoutInflater layoutInflater;
    private Context mContext;

    public HaystackListAdapter(Context context, ArrayList listData) {
        this.listData = listData;
        layoutInflater = LayoutInflater.from(context);
        mContext = context;
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
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.list_row_layout, null);
            holder = new ViewHolder();
            holder.titleView = (TextView) convertView.findViewById(R.id.title);
            holder.userCountView = (TextView) convertView.findViewById(R.id.active_users);
            holder.imageView = (ImageView) convertView.findViewById(R.id.thumbImage);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
 
        Haystack haystack = (Haystack) listData.get(position);
        holder.titleView.setText(haystack.getName());
        holder.userCountView.setText(haystack.getActiveUsers().size()+" active users");
 
        if (holder.imageView != null) {
            new ImageDownloaderTask(holder.imageView).execute(haystack.getPictureURL());
        }
 
        return convertView;
    }
 
    static class ViewHolder {
        TextView titleView;
        TextView userCountView;
        ImageView imageView;
    }

}
