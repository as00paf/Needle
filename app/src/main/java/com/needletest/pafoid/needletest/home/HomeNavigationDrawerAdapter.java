package com.needletest.pafoid.needletest.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.needletest.pafoid.needletest.R;
import com.needletest.pafoid.needletest.haystack.HaystackDrawerItem;

import java.util.List;

public class HomeNavigationDrawerAdapter extends ArrayAdapter<HaystackDrawerItem> {

    private Context context;
    private List<HaystackDrawerItem> drawerItemList;
    private int layoutResID;
    private LayoutInflater inflater;

    public HomeNavigationDrawerAdapter(Context context, int layoutResourceID, List<HaystackDrawerItem> listItems, LayoutInflater inflater){
        super(context, layoutResourceID, listItems);

        this.context = context;
        this.layoutResID = layoutResourceID;
        this.drawerItemList = listItems;
        this.inflater = inflater;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DrawerSimpleItemHolder drawerHolder;
        View view = convertView;

        if (view == null) {
            drawerHolder = new DrawerSimpleItemHolder();

            view = inflater.inflate(layoutResID, parent, false);
            drawerHolder.ItemName = (TextView) view.findViewById(R.id.drawer_item_label);
            drawerHolder.icon = (ImageView) view.findViewById(R.id.drawer_item_icon);

            view.setTag(drawerHolder);
        } else {
            drawerHolder = (DrawerSimpleItemHolder) view.getTag();
        }

        HaystackDrawerItem dItem = (HaystackDrawerItem) this.drawerItemList.get(position);

        drawerHolder.icon.setImageDrawable(view.getResources().getDrawable(dItem.getIconResId()));
        drawerHolder.ItemName.setText(dItem.getItemName());

        return view;
    }

    private static class DrawerSimpleItemHolder {
        TextView ItemName;
        ImageView icon;
    }

    private static class DrawerCheckBoxItemHolder {
        CheckBox checkBox;
        ImageView icon;
    }

}
