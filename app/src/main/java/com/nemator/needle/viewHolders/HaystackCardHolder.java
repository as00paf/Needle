package com.nemator.needle.viewHolders;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.nemator.needle.Needle;
import com.nemator.needle.R;
import com.nemator.needle.fragments.haystacks.HaystackListTabFragment;
import com.nemator.needle.models.vo.HaystackVO;
import com.nemator.needle.utils.AppUtils;

public class HaystackCardHolder extends RecyclerView.ViewHolder implements PopupMenu.OnMenuItemClickListener, View.OnClickListener {

    //Item
    public TextView titleView, userCountView, active_until;
    public ImageView imageView;
    private ImageButton menuButton;

    //Empty
    public TextView emptyText;

    private HaystackVO haystackData;
    private Boolean isOwner;

    private HaystackListTabFragment.HaystackListFragmentInteractionListener listener;

    public HaystackCardHolder(View view, HaystackListTabFragment.HaystackListFragmentInteractionListener listener) {
        super(view);
        this.listener = listener;
        titleView =  (TextView) view.findViewById(R.id.title);
        userCountView = (TextView)  view.findViewById(R.id.active_users);
        active_until = (TextView)  view.findViewById(R.id.active_until);
        menuButton = (ImageButton) view.findViewById(R.id.location_sharing_card_menu_button);
        emptyText = (TextView) view.findViewById(R.id.title);
        imageView = (ImageView) view.findViewById(R.id.thumbImage);
    }

    public void setData(HaystackVO haystack){
        haystackData = haystack;
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClickHaystackCard(haystackData);
            }
        });

        isOwner = haystack.getOwner() == Needle.userModel.getUserId();

        titleView.setText(haystack.getName());

        int count = haystack.getActiveUsers().size();
        String userCount = count + " " + itemView.getResources().getString(R.string.activeUsers);
        userCountView.setText(userCount);

        active_until.setText(AppUtils.formatDateUntil(itemView.getContext(), haystack.getTimeLimit()));

        menuButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        PopupMenu popup = new PopupMenu(v.getContext(), menuButton);
        MenuInflater inflater = popup.getMenuInflater();

        int resourceId = isOwner ? R.menu.menu_haystack_card_owner : R.menu.menu_haystack_card;
        inflater.inflate(resourceId, popup.getMenu());
        popup.setOnMenuItemClickListener(this);

        popup.show();
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_option_cancel_haystack:
                new AlertDialog.Builder(itemView.getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(itemView.getContext().getString(R.string.cancel))
                        .setMessage(itemView.getContext().getString(R.string.cancel_haystack_confirmation))
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                listener.onCancelHaystack(haystackData);
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();

                break;
            case R.id.menu_option_leave:
                new AlertDialog.Builder(itemView.getContext())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle(itemView.getContext().getString(R.string.leave_haystack))
                        .setMessage(itemView.getContext().getString(R.string.cancel_haystack_leave_confirmation))
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                listener.onLeaveHaystack(haystackData);
                            }
                        })
                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();

                break;
            default:
                return false;
        }

        return true;
    }
}