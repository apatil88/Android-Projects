package com.amrutpatil.makeanote;

/**
 * Created by Amrut on 2/27/16.
 * Description: Represents a single item in the Navigation Drawer.
 */
public class NavigationDrawerItem {

    private int iconId;
    private String title;

    public NavigationDrawerItem(int iconId, String title) {
        this.iconId = iconId;
        this.title = title;
    }

    public int getIconId() {
        return iconId;
    }

    public String getTitle() {
        return title;
    }
}
