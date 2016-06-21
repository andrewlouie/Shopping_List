package com.andrewaarondev.shoppinglist;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.XmlResourceParser;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Process;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by Andrew on 5/27/2016.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "shoppinglist.db";
    private static final int SCHEMA_VERSION = 1;
    private static final String TABLE = "lists";
    private static final String LISTTABLE = "items";
    private static final String ALLITEMSTABLE = "allitems";
    private static DatabaseHelper singleton = null;
    private Context ctxt;

    synchronized static DatabaseHelper getInstance(Context ctxt) {
        if (singleton == null) {
            singleton = new DatabaseHelper(ctxt.getApplicationContext());
        }
        return (singleton);
    }

    private DatabaseHelper(Context ctxt) {
        super(ctxt, DATABASE_NAME, null, SCHEMA_VERSION);
        this.ctxt = ctxt;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (position INTEGER PRIMARY KEY, prose TEXT);");
        ContentValues cv = new ContentValues();
        cv.put("prose", "Grocery Store");
        db.insert(TABLE, "prose", cv);
        cv.put("prose", "Drug Store");
        db.insert(TABLE, "prose", cv);
        cv.put("prose", "Hardware Store");
        db.insert(TABLE, "prose", cv);
        cv.put("prose", "Other Store");
        db.insert(TABLE, "prose", cv);
        db.execSQL("CREATE TABLE " + LISTTABLE + " (position INTEGER PRIMARY KEY, prose TEXT,list INTEGER);");
        cv.put("prose", "Sample item");
        cv.put("list", 1);
        db.insert(LISTTABLE, null, cv);
        db.execSQL("CREATE TABLE " + ALLITEMSTABLE + " (position INTEGER PRIMARY KEY,prose TEXT,category TEXT);");
        XmlResourceParser parser = ctxt.getResources().getXml(R.xml.initialvalues);
        ArrayList<GroceryItem> items = readFeed(parser);
        for (int i = 0; i < items.size(); i++) {
            ContentValues cv3 = new ContentValues();
            cv3.put("prose", items.get(i).getName());
            cv3.put("category", items.get(i).getCategory());
            db.insert(ALLITEMSTABLE, null, cv3);

        }
    }

    private ArrayList<GroceryItem> readFeed(XmlResourceParser parser) {
        ArrayList<GroceryItem> entries = new ArrayList();
        try {
            String cat = "";
            while (true) {
                int event = parser.nextToken();
                if (event == XmlResourceParser.START_TAG) {
                    if (parser.getName().equals("category")) {
                        cat = parser.getAttributeValue(0);
                    } else if (parser.getName().equals("item")) {
                        entries.add(new GroceryItem(parser.nextText(), cat));
                    }
                } else if (event == XmlResourceParser.END_DOCUMENT) {

                    break;
                } // end else if
            }  // end while
        } // end try
        catch (XmlPullParserException ex) {
            Log.i("TAG", ex.getMessage());
        } catch (IOException e) {
            Log.i("TAG", "IOException while parsing " + e.getMessage());
        }

        return entries;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        throw new RuntimeException("This should not be called");
    }

    void loadTitles() {
        new LoadThread().start();
    }

    void addList(String title, int length) {
        new AddThread(title, length).start();
    }

    void removeList(int id, int position) {
        new RemoveThread(id, position).start();
    }

    void renameList(String newText, int id, int position) {
        new RenameThread(newText, id, position).start();
    }

    void loadCategories(String itemname) {
        new LoadCategories(itemname).start();
    }

    void loadList(int id) {
        new LoadList(id).start();
    }

    void loadAutoCompleteList() {
        new LoadAutoCompleteList().start();
    }

    void addItem(int id, String item) {
        new AddItem(id, item).start();
    }

    void deleteItem(int id, int listid) {
        new DeleteItem(id, listid).start();
    }

    void addToAll(String name, String category) {
        new AddToAll(name, category).start();
    }

    void renameInAll(GroceryItem gi) {
        new RenameInAll(gi).start();
    }

    void getByCategory(String cat) {
        new GetByCategory(cat).start();
    }

    void deleteFromAll(GroceryItem gi) {
        new DeleteFromAll(gi).start();
    }

    private class RenameInAll extends Thread {
        GroceryItem gi;

        RenameInAll(GroceryItem gi) {
            this.gi = gi;
        }

        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            ContentValues args2 = new ContentValues();
            args2.put("prose", gi.getName());
            String strFilter = "position=" + gi.getId();
            getWritableDatabase().update(ALLITEMSTABLE, args2, strFilter, null);
            new GetByCategory(gi.getCategory()).start();
        }
    }

    private class DeleteFromAll extends Thread {
        GroceryItem gi;

        DeleteFromAll(GroceryItem gi) {
            this.gi = gi;
        }

        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            getReadableDatabase().delete(ALLITEMSTABLE, "position = ?", new String[]{gi.getId() + ""});
            new GetByCategory(gi.getCategory()).start();
        }
    }

    private class GetByCategory extends Thread {
        String cat;

        GetByCategory(String cat) {
            this.cat = cat;
        }

        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            Cursor c = getReadableDatabase().rawQuery("SELECT * FROM " + ALLITEMSTABLE + " WHERE category = ?", new String[]{cat});
            ArrayList<GroceryItem> result = new ArrayList<>();
            c.moveToFirst();
            while (!c.isAfterLast()) {
                result.add(new GroceryItem(c.getInt(0), c.getString(1), c.getString(2)));
                c.moveToNext();
            }
            EventBus.getDefault().post(new ListByCategoryLoadedEvent(result));
            c.close();
        }
    }

    private class AddToAll extends Thread {
        String name;
        String category;

        AddToAll(String name, String category) {
            this.name = name;
            this.category = category;
        }

        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            ContentValues cv3 = new ContentValues();
            cv3.put("prose", name);
            cv3.put("category", category);
            getReadableDatabase().insert(ALLITEMSTABLE, null, cv3);
            new LoadAutoCompleteList().start();
        }
    }

    private class LoadCategories extends Thread {
        String itemname;

        LoadCategories(String itemname) {
            this.itemname = itemname;
        }

        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            Cursor c = getReadableDatabase().rawQuery("SELECT DISTINCT category FROM " + ALLITEMSTABLE + " ORDER BY category", new String[]{});
            ArrayList<String> result = new ArrayList<>();
            c.moveToFirst();
            while (!c.isAfterLast()) {
                result.add(c.getString(0));
                c.moveToNext();
            }

            EventBus.getDefault().post(new CategoriesLoadedEvent(result.toArray(new String[result.size()]), itemname));
            c.close();
        }
    }

    private class DeleteItem extends Thread {
        int id;
        int listid;

        DeleteItem(int id, int listid) {
            this.id = id;
            this.listid = listid;
        }

        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            getReadableDatabase().delete(LISTTABLE, "position = ? AND list = ?", new String[]{id + "", listid + ""});
            new LoadList(listid).start();
        }
    }

    private class AddItem extends Thread {
        int id;
        String item;

        AddItem(int id, String item) {
            this.id = id;
            this.item = item;
        }

        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            ContentValues cv = new ContentValues();
            cv.put("prose", item);
            cv.put("list", id);
            getReadableDatabase().insert(LISTTABLE, null, cv);
            new LoadList(id).start();
        }
    }

    private class LoadAutoCompleteList extends Thread {
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            Cursor c = getReadableDatabase().rawQuery("SELECT * FROM " + ALLITEMSTABLE + " ORDER BY prose", new String[]{});
            ArrayList<GroceryItem> result = new ArrayList<GroceryItem>();
            c.moveToFirst();
            while (!c.isAfterLast()) {
                result.add(new GroceryItem(c.getInt(0), c.getString(1), c.getString(2)));
                c.moveToNext();
            }
            EventBus.getDefault().post(new AllItemsLoadedEvent(result));
            c.close();
        }
    }

    private class LoadList extends Thread {
        private int id;

        LoadList(int id) {
            super();
            this.id = id;
        }

        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            String[] args = {String.valueOf(id)};
            Cursor c = getReadableDatabase().rawQuery("SELECT position,prose FROM " + LISTTABLE + " WHERE list = ?", args);
            ArrayList<String> result = new ArrayList<String>();
            ArrayList<Integer> ids = new ArrayList<Integer>();
            c.moveToFirst();
            while (!c.isAfterLast()) {
                result.add(c.getString(1));
                ids.add(c.getInt(0));
                c.moveToNext();
            }
            EventBus.getDefault().post(new ListLoadedEvent(result.toArray(new String[result.size()]), ids, id));
            c.close();
        }
    }

    private class LoadThread extends Thread {
        private int focusPosition = 0;

        LoadThread() {
            super();
        }

        LoadThread(int focusPosition) {
            super();
            this.focusPosition = focusPosition;
        }

        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            Cursor c =
                    getReadableDatabase().rawQuery("SELECT * FROM " + TABLE, new String[]{});
            c.moveToFirst();
            ArrayList<String> result = new ArrayList<String>();
            ArrayList<Integer> ids = new ArrayList<Integer>();
            while (!c.isAfterLast()) {
                result.add(c.getString(1));
                ids.add(c.getInt(0));
                c.moveToNext();
            }
            EventBus.getDefault().postSticky(new TitlesLoadedEvent(result.toArray(new String[result.size()]), ids.toArray(new Integer[ids.size()]), focusPosition));
            c.close();
        }
    }

    public class AddThread extends Thread {
        private int length = 0;
        private String title;

        AddThread(String title, int length) {
            super();
            this.title = title;
            this.length = length;
        }

        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            ContentValues cv = new ContentValues();
            cv.put("prose", title);
            getReadableDatabase().insert(TABLE, "prose", cv);
            new LoadThread(length).start();
        }

    }

    private class RemoveThread extends Thread {
        private int position;
        private int id;

        RemoveThread(int id, int position) {
            super();
            this.id = id;
            this.position = position;
        }

        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            getWritableDatabase().delete(TABLE, "position" + "=" + id, null);
            getWritableDatabase().delete(LISTTABLE, "list" + "=" + id, null);
            new LoadThread(position > 0 ? position - 1 : 0).start();
        }
    }

    private class RenameThread extends Thread {
        private String newText;
        private int id;
        private int position;

        RenameThread(String newText, int id, int position) {
            this.newText = newText;
            this.id = id;
            this.position = position;
        }

        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            String strFilter = "position=" + id;
            ContentValues args2 = new ContentValues();
            args2.put("prose", newText);
            getWritableDatabase().update(TABLE, args2, strFilter, null);
            new LoadThread(position).start();
        }
    }
}
