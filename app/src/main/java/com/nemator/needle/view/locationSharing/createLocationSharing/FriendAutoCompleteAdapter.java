package com.nemator.needle.view.locationSharing.createLocationSharing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nemator.needle.R;
import com.nemator.needle.models.vo.UserVO;
import com.nemator.needle.tasks.ImageDownloaderTask;

import java.util.ArrayList;

/**
 * Created by Alex on 01/07/2015.
 */
public class FriendAutoCompleteAdapter extends ArrayAdapter<UserVO> {

    private ArrayList<UserVO> items;
    private ArrayList<UserVO> itemsAll;
    private int viewResourceId;
    private ArrayList<UserVO> suggestions;

    public FriendAutoCompleteAdapter(Context context, int resource, ArrayList<UserVO> objects) {
        super(context, resource, objects);

        this.viewResourceId = resource;
        this.items = objects;
        this.itemsAll = (ArrayList<UserVO>) items.clone();
        this.suggestions = new ArrayList<UserVO>();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if (v == null) {
            LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = vi.inflate(viewResourceId, null);
        }

        UserVO user = items.get(position);
        if (user != null) {
            TextView userNameLabel = (TextView) v.findViewById(R.id.friend_auto_complete_user_name_label);
            if (userNameLabel != null) {
                userNameLabel.setText(user.getUserName());
            }

            ImageView userPicture = (ImageView) v.findViewById(R.id.friend_auto_complete_user_picture);
            if(userPicture != null){
               /* if(user.getPictureURL() != null){
                    new ImageDownloaderTask(userPicture, getContext().getResources().getDrawable(R.drawable.person_placeholder)).execute(user.getPictureURL());
                }*/
                userPicture.setImageDrawable(getContext().getResources().getDrawable(R.drawable.person_placeholder));
            }
        }
        return v;
    }
    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    Filter nameFilter = new Filter() {
        @Override
        public String convertResultToString(Object resultValue) {
            String str = ((UserVO)(resultValue)).getUserName();
            return str;
        }
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if(constraint != null) {
                suggestions.clear();
                for (UserVO user : itemsAll) {
                    if(user.getUserName().toLowerCase().startsWith(constraint.toString().toLowerCase())){
                        suggestions.add(user);
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            ArrayList<UserVO> filteredList = (ArrayList<UserVO>) results.values;
            if(results != null && results.count > 0) {
                clear();
                for (UserVO c : filteredList) {
                    add(c);
                }
                notifyDataSetChanged();
            }
        }
    };
}