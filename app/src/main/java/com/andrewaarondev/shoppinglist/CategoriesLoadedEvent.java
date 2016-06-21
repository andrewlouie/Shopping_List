package com.andrewaarondev.shoppinglist;

/**
 * Created by Andrew on 6/3/2016.
 */
public class CategoriesLoadedEvent {
    String[] categories;
    String itemname;

    CategoriesLoadedEvent(String[] categories, String itemname) {
        this.categories = categories;
        this.itemname = itemname;
    }

    public String[] getCategories() {
        return categories;
    }

    public String getItemname() {
        return itemname;
    }
}
