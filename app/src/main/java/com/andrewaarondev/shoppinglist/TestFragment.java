package com.andrewaarondev.shoppinglist;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

public final class TestFragment extends Fragment {

    private static final String KEY_CONTENT = "com.andrewaarondev.shoppinglist.TestFragment:Content";
    private static final String LISTKEY_CONTENT = "com.andrewaarondev.shoppinglist.TestFragment:ListContent";
    private static final String INDEXKEY_CONTENT = "com.andrewaarondev.shoppinglist.TestFragment:IndexContent";
    private static final String KEY_POSITION = "pos";
    private int mContent = 0;
    private String[] list;
    private ArrayList<Integer> ids;
    private ListView editor;
    Parcelable state;

    public int getmContent() {
        return mContent;
    }

    synchronized public String[] getList() {
        return list;
    }

    public static TestFragment newInstance(int id) {
        TestFragment fragment = new TestFragment();

        Bundle args = new Bundle();
        args.putInt(KEY_POSITION, id);
        fragment.setArguments(args);
        fragment.mContent = id;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mContent = savedInstanceState.getInt(KEY_CONTENT);
            list = savedInstanceState.getStringArray(LISTKEY_CONTENT);
            ids = savedInstanceState.getIntegerArrayList(INDEXKEY_CONTENT);
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if ((savedInstanceState != null) && savedInstanceState.containsKey(KEY_CONTENT)) {
            mContent = savedInstanceState.getInt(KEY_CONTENT);
            list = savedInstanceState.getStringArray(LISTKEY_CONTENT);
            ids = savedInstanceState.getIntegerArrayList(INDEXKEY_CONTENT);
            editor.setAdapter(new CustomAdapter(this.getActivity(), this, list, ids));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.listview, container, false);
        editor = (ListView) result.findViewById(R.id.listView);
        editor.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                deleteItem(ids.get(position));
            }
        });
        return result;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CONTENT, mContent);
        outState.putStringArray(LISTKEY_CONTENT, list);
        outState.putIntegerArrayList(INDEXKEY_CONTENT, ids);
    }

    @Override
    public void onResume() {
        super.onResume();

        EventBus.getDefault().register(this);

        if (list == null) {
            DatabaseHelper db = DatabaseHelper.getInstance(getActivity().getBaseContext());
            int position = getArguments().getInt(KEY_POSITION, 0);
            db.loadList(position);
        }
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(ListLoadedEvent event) {
        int position = getArguments().getInt(KEY_POSITION, 0);
        if (event.getListid() == position) {
            state = editor.onSaveInstanceState();
            list = event.getResults();
            ids = event.getIds();
            editor.setAdapter(new CustomAdapter(this.getActivity(), this, list, ids));
            editor.onRestoreInstanceState(state);
            ((SampleTabsDefault) getActivity()).updateShare(position);
        }
    }

    public void deleteItem(int id) {
        int listid = getArguments().getInt(KEY_POSITION, 0);
        DatabaseHelper db = DatabaseHelper.getInstance(getActivity().getBaseContext());
        db.deleteItem(id, listid);
    }
}
