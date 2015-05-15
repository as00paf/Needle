package com.nemator.needle.models.vo;

public class HaystackDrawerItem {
    public static final int SimpleItem = 0;
    public static final int CheckBoxItem = 1;

    private String itemName;
    private int iconResId;
    private int type;

    public HaystackDrawerItem(int type, String itemName, int iconResId) {
        super();
        this.type = type;
        this.itemName = itemName;
        this.iconResId = iconResId;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public int getIconResId() {
        return iconResId;
    }

    public void setIconResId(int iconResId) {
        this.iconResId = iconResId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
