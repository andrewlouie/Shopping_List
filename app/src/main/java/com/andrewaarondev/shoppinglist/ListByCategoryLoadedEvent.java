package com.andrewaarondev.shoppinglist;

import java.util.ArrayList;

/**
 * Created by Andrew on 6/3/2016.
 */
public class ListByCategoryLoadedEvent {
    ArrayList<GroceryItem> allitems;

    ListByCategoryLoadedEvent(ArrayList<GroceryItem> allitems) {
        this.allitems = allitems;
    }

    public ArrayList<GroceryItem> getAllitems() {
        return allitems;
    }
}

