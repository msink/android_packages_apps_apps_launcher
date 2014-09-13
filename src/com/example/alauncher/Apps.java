package com.example.alauncher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Apps extends Activity {

    private GridView grid;
    private static class GridHolder {
    	ImageView icon;
     	TextView name;
    }

    private ArrayList<GridItem> list;
    private static class GridItem {
        Drawable icon;
        String name;
        CharSequence title;
    }

    private GridAdapter adapter;
    private class GridAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public GridItem getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            return getItemView(position, view);
        }
    }

    private View getItemView(int position, View view) {
    	GridHolder holder = null;
    	if (view != null) {
    	    holder = (GridHolder) view.getTag();
        } else {
            view = getLayoutInflater().inflate(R.layout.grid_item, null);
            holder = new GridHolder();
            holder.icon = (ImageView) view.findViewById(R.id.item_icon);
            holder.name = (TextView) view.findViewById(R.id.item_name);
            view.setTag(holder);
    	}
        holder.icon.setImageDrawable(list.get(position).icon);
        holder.name.setText(list.get(position).title);

        return view;
    }

    private ArrayList<GridItem> getItemList() {
        ArrayList<GridItem> list = new ArrayList();
        List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);

        PackageManager pm = getPackageManager();
        for (int i = 0; i < packages.size(); i++) {
            PackageInfo pi = packages.get(i);
            GridItem item = new GridItem();
            item.title = pi.applicationInfo.loadLabel(pm);
            item.icon = pi.applicationInfo.loadIcon(pm);
            item.name = pi.packageName;
            if (pm.getLaunchIntentForPackage(item.name) != null)
                list.add(item);
        }

        Collections.sort(list, new Comparator<GridItem>() {
            public int compare(GridItem item1, GridItem item2) {
                String title1 = item1.title.toString();
                String title2 = item2.title.toString();
                return title1.compareTo(title2);
            }
        });

        return list;
    }

    private static final String SCHEME =
                                "package";
    private static final String APP_PKG_NAME_21 =
                                "com.android.settings.ApplicationPkgName";
    private static final String APP_PKG_NAME_22 =
                                "pkg";
    private static final String APP_DETAILS_PACKAGE_NAME =
                                "com.android.settings";
    private static final String APP_DETAILS_CLASS_NAME =
                                "com.android.settings.InstalledAppDetails";
    private void showItemDetails(String packageName) {
        Intent intent = new Intent();
        final int apiLevel = Build.VERSION.SDK_INT;
        if (apiLevel >= 9) { // above 2.3
            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.fromParts(SCHEME, packageName, null));
        } else { // below 2.3
            String appPkgName = (apiLevel == 8 ? APP_PKG_NAME_22 : APP_PKG_NAME_21);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setClassName(APP_DETAILS_PACKAGE_NAME, APP_DETAILS_CLASS_NAME);
            intent.putExtra(appPkgName, packageName);
        }
        startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.grid_view);

        list = getItemList();
        adapter = new GridAdapter();
        grid = (GridView) findViewById(R.id.grid_view);
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent,
                                    View view, int position, long id) {
                PackageManager pm = getPackageManager();
                Intent intent = pm.getLaunchIntentForPackage(list.get(position).name);
                if (intent != null) {
                    startActivity(intent);
                }
            }
        });
        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent,
                                           View view, int position, long id) {
                showItemDetails(list.get(position).name);
                return false;
            }
        });
        grid.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        list = getItemList();
        adapter = new GridAdapter();
        grid.setAdapter(adapter);
        super.onResume();
    }
}
