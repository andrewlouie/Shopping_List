package com.andrewaarondev.shoppinglist;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

public class ItemBrowseActivity extends Activity {

    public static final String EXTRA_STRING = "str";
    private ArrayList<GroceryItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String file = getIntent().getStringExtra(EXTRA_STRING);
        getActionBar().setTitle(R.string.app_name);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_browse);
        DatabaseHelper db = DatabaseHelper.getInstance(this.getBaseContext());
        EventBus.getDefault().register(this);
        db.getByCategory(file);
    }
    @SuppressWarnings("unused")
    public void onEventMainThread(ListByCategoryLoadedEvent event) {
        ListView lv = (ListView)this.findViewById(R.id.itemListView);
        items = event.getAllitems();
        if (items == null || items.size() == 0) {
            finish();
        }
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MyDialogFragment.newInstance(items.get(position)).show(getFragmentManager(), "sample");
            }
        });
        lv.setAdapter(new ArrayAdapter<GroceryItem>(this,android.R.layout.simple_list_item_1,items));
    }

}
