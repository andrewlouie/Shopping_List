package com.andrewaarondev.shoppinglist;

import java.util.ArrayList;

/**
 * Created by Andrew on 5/31/2016.
 */
public class AllItemsLoadedEvent {
    ArrayList<GroceryItem> allitems;

    AllItemsLoadedEvent(ArrayList<GroceryItem> allitems) {
        this.allitems = allitems;
    }

    public ArrayList<GroceryItem> getAllitems() {
        return allitems;
    }
}
