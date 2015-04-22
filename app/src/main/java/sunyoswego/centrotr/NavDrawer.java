package sunyoswego.centrotr;

import android.content.res.TypedArray;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.widget.ListView;

import java.util.ArrayList;

public class NavDrawer{

    // Navigation Drawer Attributes
    public DrawerLayout mDrawerLayout;
    public ListView mDrawerList;
    public ActionBarDrawerToggle mDrawerToggle;
    //Title
    public CharSequence mDrawerTitle;
    //App Title
    public CharSequence mTitle;
    //Slide Menu Items
    public String[] navMenuTitles;
    public TypedArray navMenuIcons;

    public ArrayList<NavDrawerItem> navDrawerItems;
    public NavDrawerListAdapter adapter;
}