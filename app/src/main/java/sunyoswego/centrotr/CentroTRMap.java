package sunyoswego.centrotr;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;

public class CentroTRMap extends FragmentActivity {

    private GoogleMap mMap;
    NavDrawer drawer = new NavDrawer();
    BusRoute currentRoute = new BusRoute();
    Bus b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_centro_trmap);
        setUpMapIfNeeded();
        setUpDrawerNavigation();
        try {
            currentRoute.loadRoute(mMap);
        } catch (IOException e) {
            e.printStackTrace();
        }
        b = new Bus(mMap);
        b.track();
    }

    private void setUpDrawerNavigation() {

        /*** DRAWER ****/
        drawer.mTitle = drawer.mDrawerTitle = getTitle();
        // load slide menu items -> Bus Routes names
        drawer.navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

        // nav drawer icons from resources
        drawer.navMenuIcons = getResources()
                .obtainTypedArray(R.array.nav_drawer_icons);

        drawer.mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

        drawer.navDrawerItems = new ArrayList<>();

        // Add nav drawer items to array
        // Blue Route
        drawer.navDrawerItems.add(new NavDrawerItem(drawer.navMenuTitles[0], drawer.navMenuIcons.getResourceId(0, -1)));
        // Green Route
        drawer.navDrawerItems.add(new NavDrawerItem(drawer.navMenuTitles[1], drawer.navMenuIcons.getResourceId(0, -1)));
        // 1A
//        drawer.navDrawerItems.add(new NavDrawerItem(drawer.navMenuTitles[2], drawer.navMenuIcons.getResourceId(0, -1)));

        // Setting the nav drawer list adapter
        drawer.adapter = new NavDrawerListAdapter(getApplicationContext(), drawer.navDrawerItems);
        drawer.mDrawerList.setAdapter(drawer.adapter);

        //Enabling action bar app icon and behaving it as toggle button
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        drawer.mDrawerToggle = new ActionBarDrawerToggle(this, drawer.mDrawerLayout,
                R.string.app_name, // nav drawer open - description for accessibility
                R.string.app_name // nav drawer close - description for accessibility
        ) {
            public void onDrawerClosed(View view) {
                getActionBar().setTitle(drawer.mTitle);
                // calling onPrepareOptionsMenu() to show action bar icons
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActionBar().setTitle(drawer.mDrawerTitle);
                // calling onPrepareOptionsMenu() to hide action bar icons
                invalidateOptionsMenu();
            }
        };
        drawer.mDrawerLayout.setDrawerListener(drawer.mDrawerToggle);
        drawer.mDrawerList.setOnItemClickListener(new SlideMenuClickListener());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Toggle nav drawer on selecting action bar app icon/title
        if (drawer.mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action bar actions click
        switch (item.getItemId()) {
            case R.id.action_settings:
                //changes to settings page
                Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivityForResult(settingsIntent, 1);
                return true;
            case R.id.action_schedule:
                Uri uriUrl = Uri.parse("http://www.centro.org/Schedules-Oswego.aspx");
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Called when invalidateOptionsMenu() is triggered
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        //If nav drawer is opened, hide the action items
        boolean drawerOpen = drawer.mDrawerLayout.isDrawerOpen(drawer.mDrawerList);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void setTitle(CharSequence title) {
        drawer.mTitle = title;
        getActionBar().setTitle(drawer.mTitle);
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drawer.mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        drawer.mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /**
     * Diplaying fragment view for selected nav drawer list item
     */
    private void displayView(int position) throws IOException {
        switch (position) {
            case 0:
                currentRoute.change(mMap, "blue");
                break;
            case 1:
                currentRoute.change(mMap, "green");
                break;
            case 2:
                break;
            default:
                break;
        }

        // update selected item and title, then close the drawer
        drawer.mDrawerList.setItemChecked(position, true);
        drawer.mDrawerList.setSelection(position);
        setTitle(drawer.navMenuTitles[position]);
        drawer.mDrawerLayout.closeDrawer(drawer.mDrawerList);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        LatLng one = new LatLng(43.453838, -76.540628);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(one, (float) 14.5));
    }

    /**
     * Slide menu item click listener
     */
    private class SlideMenuClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            //Display view for selected nav drawer item
            try {
                displayView(position);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}