package com.andrewaarondev.shoppinglist;

import java.util.ArrayList;

/**
 * Created by Andrew on 5/30/2016.
 */
public class ListLoadedEvent {
    String[] results;
    ArrayList<Integer> ids;
    int listid;
    ArrayList<GroceryItem> list;

    ListLoadedEvent(String[] results, ArrayList<Integer> ids, int listid) {
        this.results = results;
        this.ids = ids;
        this.listid = listid;
    }

    ListLoadedEvent(ArrayList<GroceryItem> list, int listid) {
        this.list = list;
        this.listid = listid;
    }

    String[] getResults() {
        return (results);
    }

    int getListid() {
        return listid;
    }

    ArrayList<Integer> getIds() {
        return (ids);
    }

    ArrayList<GroceryItem> getList() {
        return list;
    }
}
