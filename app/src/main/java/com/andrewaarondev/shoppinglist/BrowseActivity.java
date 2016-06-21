package com.andrewaarondev.shoppinglist;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import de.greenrobot.event.EventBus;

public class BrowseActivity extends Activity implements AdapterView.OnItemClickListener {
    private static String[] categories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActionBar().setTitle(R.string.app_name);
        setContentView(R.layout.activity_browse);
        DatabaseHelper db = DatabaseHelper.getInstance(getBaseContext());
        EventBus.getDefault().register(this);
        db.loadCategories(null);
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(CategoriesLoadedEvent event) {
        categories = event.getCategories();
        GridView g = (GridView) findViewById(R.id.grid);
        g.setAdapter(new ArrayAdapter<String>(this,
                R.layout.cell,
                categories));
        g.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v,
                            int position, long id) {
        Intent j = new Intent(this, ItemBrowseActivity.class);
        j.putExtra(ItemBrowseActivity.EXTRA_STRING, categories[position]);
        startActivity(j);//do something with categories[position]
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        DatabaseHelper db = DatabaseHelper.getInstance(getBaseContext());
        db.loadCategories(null);
    }
}
