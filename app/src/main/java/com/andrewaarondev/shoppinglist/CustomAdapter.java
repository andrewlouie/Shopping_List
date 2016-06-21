package com.andrewaarondev.shoppinglist;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {
    String[] result;
    Context context;
    ArrayList<Integer> prgmIdsList;
    TestFragment tf;
        private static LayoutInflater inflater = null;

    public CustomAdapter(FragmentActivity mainActivity, TestFragment tf, String[] prgmNameList, ArrayList<Integer> ids) {
        result = prgmNameList;
        this.tf = tf;
        prgmIdsList = ids;
        context = mainActivity;
        inflater = (LayoutInflater) context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return result.length;
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder {
        TextView tv;
    }

    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        Holder holder = new Holder();
        View rowView;
        rowView = inflater.inflate(R.layout.listviewitemtemplate, null);
        holder.tv = (TextView) rowView.findViewById(R.id.textView1);
        holder.tv.setText(result[position]);
        ImageButton ib = (ImageButton) rowView.findViewById(R.id.checkbutton);
        ib.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                tf.deleteItem(prgmIdsList.get(position));
            }
        });
        ImageButton ib2 = (ImageButton) rowView.findViewById(R.id.addtolibrary);
        ib2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseHelper db = DatabaseHelper.getInstance(context);
                db.loadCategories(result[position]);
            }
        });
        return rowView;
    }

} 