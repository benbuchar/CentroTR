package sunyoswego.centrotr;

import android.content.res.TypedArray;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.widget.ListView;

import java.util.ArrayList;

/**
 * Created by Lucas on 3/26/2015.
 */
public class NavDrawer{

    /***** Navigation Drawer Attributes *****/
    public DrawerLayout mDrawerLayout;
    public ListView mDrawerList;
    public ActionBarDrawerToggle mDrawerToggle;
    // nav drawer title
    public CharSequence mDrawerTitle;
    // used to store app title
    public CharSequence mTitle;
    // slide menu items
    public String[] navMenuTitles;
    public TypedArray navMenuIcons;

    public ArrayList<NavDrawerItem> navDrawerItems;
    public NavDrawerListAdapter adapter;
    /**********/

}