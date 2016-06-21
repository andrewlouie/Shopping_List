package com.andrewaarondev.shoppinglist;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Andrew on 5/31/2016.
 */
public class GroceryItem implements Parcelable {
    private String name;
    private int id;
    private String category;

    GroceryItem(int id, String name, String category) {
        this.name = name;
        this.id = id;
        this.category = category;
    }

    GroceryItem(String name, String category) {
        this.name = name;
        this.category = category;
    }

    GroceryItem(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    public String getName() {
        return this.name;
    }

    public int getId() {
        return this.id;
    }

    public String getCategory() {
        return this.category;
    }

    public void setName(String name) {
        this.name = name;
    }

    protected GroceryItem(Parcel in) {
        name = in.readString();
        id = in.readInt();
        category = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(id);
        dest.writeString(category);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<GroceryItem> CREATOR = new Parcelable.Creator<GroceryItem>() {
        @Override
        public GroceryItem createFromParcel(Parcel in) {
            return new GroceryItem(in);
        }

        @Override
        public GroceryItem[] newArray(int size) {
            return new GroceryItem[size];
        }
    };
}