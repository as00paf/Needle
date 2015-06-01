package com.nemator.needle.view.haystack;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nemator.needle.R;
import com.nemator.needle.tasks.ImageDownloaderTask;
import com.nemator.needle.models.vo.UserVO;

import java.util.List;

public class HaystackUserListAdapter extends BaseAdapter {

    private Context context;
    private List<UserVO> userItemList, alreadyAddedUserList;
    private int layoutResID;
    private LayoutInflater inflater;

    public HaystackUserListAdapter(Context context, int resource, List<UserVO> listItems, List<UserVO> alreadyAddedUserList, LayoutInflater inflater){
        super();
        this.context = context;
        this.layoutResID = resource;
        this.userItemList = listItems;
        this.alreadyAddedUserList = alreadyAddedUserList;
        this.inflater = inflater;
    }

    @Override
    public int getCount() {
        return userItemList.size();
    }

    @Override
    public UserVO getItem(int position) {
        return userItemList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        UserItemHolder userItemHolder;
        View view = convertView;

        if (view == null) {
            userItemHolder = new UserItemHolder();

            view = inflater.inflate(layoutResID, parent, false);
            userItemHolder.userName = (TextView) view.findViewById(R.id.drawer_item_label);
            userItemHolder.icon = (ImageView) view.findViewById(R.id.drawer_item_icon);

            view.setTag(userItemHolder);
        } else {
            userItemHolder = (UserItemHolder) view.getTag();
        }

        UserVO dItem = (UserVO) this.userItemList.get(position);
        userItemHolder.userName.setText(dItem.getUserName());

        String url = dItem.getPictureURL();
        if(!TextUtils.isEmpty(url)){
            new ImageDownloaderTask(userItemHolder.icon, context.getResources().getDrawable(R.drawable.ic_action_person)).execute(url);
        }else{
            userItemHolder.icon.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_action_person));
        }

        if(parent != null)
            ((ListView) parent).setItemChecked(position, wasAlreadyAdded(dItem));

        return view;
    }

    private boolean wasAlreadyAdded(UserVO user){
        if(alreadyAddedUserList == null)
            return false;

        for(int i = 0;i<alreadyAddedUserList.size();i++){
            if(user.getUserId() == alreadyAddedUserList.get(i).getUserId()){
                return true;
            }
        }

        return false;
    }

    private static class UserItemHolder {
        TextView userName;
        ImageView icon;
    }
}
