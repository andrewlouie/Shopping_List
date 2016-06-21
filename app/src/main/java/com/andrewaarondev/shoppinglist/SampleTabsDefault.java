package com.andrewaarondev.shoppinglist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ShareActionProvider;
import android.widget.TextView;
import android.widget.Toast;

import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.greenrobot.event.EventBus;

public class SampleTabsDefault extends FragmentActivity implements
        ShareActionProvider.OnShareTargetSelectedListener, TextWatcher {
    private static String[] content = null;
    private String m_Text = "";
    private String n_Text = "";
    private static Integer[] ids = null;
    private ViewPager mPager = null;
    private GoogleMusicAdapter adapter = null;
    private TextView selection;
    private AutoCompleteTextView edit;
    private int ci = -1;
    private String newcat_text = "";
    private ShareActionProvider share = null;
    private Intent shareIntent = new Intent(Intent.ACTION_SEND);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progressonly);
        EventBus.getDefault().registerSticky(this);
        shareIntent.setType("text/plain");

        if (content == null) {
            DatabaseHelper db = DatabaseHelper.getInstance(getBaseContext());
            db.loadTitles();
        } else {
            EventBus.getDefault().post(new TitlesLoadedEvent(content, ids, 0));
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before,
                              int count) {
        selection.setText(edit.getText());
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start,
                                  int count, int after) {
// needed for interface, but not used
    }

    @Override
    public void afterTextChanged(Editable s) {
// needed for interface, but not used
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        if (mPager != null) {
            SharedPreferences prefs =
                    PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("activePage", mPager.getCurrentItem());
            editor.commit();
        }
        super.onPause();
    }

    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().registerSticky(this);
        if (mPager != null) {
            SharedPreferences prefs =
                    PreferenceManager.getDefaultSharedPreferences(this);
            mPager.setCurrentItem(prefs.getInt("activePage", 0));
        }
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(AllItemsLoadedEvent event) {
        selection = (TextView) findViewById(R.id.selection);
        edit = (AutoCompleteTextView) findViewById(R.id.edit);
        edit.addTextChangedListener(this);
        edit.setAdapter(new ArrayAdapter<GroceryItem>(this,
                android.R.layout.simple_dropdown_item_1line,
                event.getAllitems()));
    }

    @SuppressWarnings("unused")
    public void onEventMainThread(TitlesLoadedEvent event) {
        content = event.getProse();
        ids = event.getIds();
        setContentView(R.layout.simple_tabs);
        adapter = new GoogleMusicAdapter(getSupportFragmentManager());
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);
        mPager = pager;
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                updateShare(0);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        TitlePageIndicator indicator = (TitlePageIndicator) findViewById(R.id.indicator);
        indicator.setViewPager(pager);
        mPager.setCurrentItem(event.getFocusPosition());
        TextView tv = (TextView) findViewById(R.id.noliststext);
        tv.setVisibility(content.length > 0 ? View.GONE : View.VISIBLE);
        DatabaseHelper db = DatabaseHelper.getInstance(getBaseContext());
        db.loadAutoCompleteList();
    }

    class GoogleMusicAdapter extends FragmentStatePagerAdapter {
        public GoogleMusicAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return TestFragment.newInstance(ids[position % content.length]);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return content[position % content.length].toUpperCase();
        }

        @Override
        public int getCount() {
            return content.length;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options, menu);
        share =
                (ShareActionProvider) menu.findItem(R.id.share)
                        .getActionProvider();
        share.setShareIntent(shareIntent);
        return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onShareTargetSelected(ShareActionProvider source,
                                         Intent intent) {
        Toast.makeText(this, intent.getComponent().toString(),
                Toast.LENGTH_LONG).show();
        return (false);
    }

    public void updateShare(int updated) {
        if (updated != 0 && updated != ids[mPager.getCurrentItem()]) return;
        if (updated == 0 && mPager.getCurrentItem() == ci) return;
        ci = mPager.getCurrentItem();
        List<Fragment> list = getSupportFragmentManager().getFragments();
        if (list == null) return;
        String[] share = new String[]{};
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) instanceof TestFragment) {
                TestFragment myFrag = (TestFragment) list.get(i);
                if (myFrag.getmContent() == ids[mPager.getCurrentItem()]) {
                    if (myFrag.getList() != null) share = myFrag.getList();
                    break;
                }
            }
        }
        String listText = content[mPager.getCurrentItem()] + System.getProperty("line.separator") + System.getProperty("line.separator");
        for (int i = 0; i < share.length; i++) {
            listText += share[i] + System.getProperty("line.separator");
        }
        shareIntent.putExtra(Intent.EXTRA_TEXT, listText);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.about:
                Intent i = new Intent(this, SimpleContentActivity.class)
                        .putExtra(SimpleContentActivity.EXTRA_FILE,
                                "file:///android_asset/misc/about.html");
                startActivity(i);
                return (true);
            case R.id.browse:
                Intent j = new Intent(this, BrowseActivity.class);
                startActivity(j);
                return (true);
            case R.id.addlist:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getResources().getString(R.string.title));

// Set up the input
                final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

// Set up the buttons
                builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        m_Text = input.getText().toString();
                        DatabaseHelper db = DatabaseHelper.getInstance(getBaseContext());
                        db.addList(m_Text, content.length);
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
                return (true);
            case R.id.removelist:
                AlertDialog.Builder rembuilder = new AlertDialog.Builder(this);
                final int activePosition = mPager.getCurrentItem();
                final String activeTitle = content[activePosition];
                final int activeId = ids[activePosition];
// Set up the input
                rembuilder.setTitle(getResources().getString(R.string.confirmdelete) + " \"" + activeTitle + "\"?");

// Set up the buttons
                rembuilder.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseHelper db = DatabaseHelper.getInstance(getBaseContext());
                        db.removeList(activeId, activePosition);
                    }
                });
                rembuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                rembuilder.show();
                return (true);
            case R.id.renamelist:
                AlertDialog.Builder renbuilder = new AlertDialog.Builder(this);
                renbuilder.setTitle(getResources().getString(R.string.newtitle));

// Set up the input
                final EditText reninput = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                reninput.setInputType(InputType.TYPE_CLASS_TEXT);
                reninput.setText(content[mPager.getCurrentItem()]);
                renbuilder.setView(reninput);
// Set up the buttons
                renbuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        n_Text = reninput.getText().toString();
                        DatabaseHelper db = DatabaseHelper.getInstance(getBaseContext());
                        db.renameList(n_Text, ids[mPager.getCurrentItem()], mPager.getCurrentItem());
                    }
                });
                renbuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                renbuilder.show();
                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }

    public void addItem(View theButton) {
        TextView sel = (TextView) findViewById(R.id.selection);
        if (sel.getText() == null || sel.getText().toString().equals("")) return;
        DatabaseHelper db = DatabaseHelper.getInstance(getBaseContext());
        db.addItem(ids[mPager.getCurrentItem()], sel.getText().toString());
        sel.setText("");
        AutoCompleteTextView actv = (AutoCompleteTextView) findViewById(R.id.edit);
        actv.setText("");
    }
    public void confirmDialog(String addedTo) {
        AlertDialog.Builder builderInner = new AlertDialog.Builder(SampleTabsDefault.this);
        builderInner.setMessage(addedTo);
        builderInner.setTitle(getResources().getString(R.string.added));
        builderInner.setPositiveButton(
                getResources().getString(R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(
                            DialogInterface dialog,
                            int which) {
                        dialog.dismiss();
                    }
                });
        builderInner.show();

    }

    @SuppressWarnings("unused")
    public void onEventMainThread(final CategoriesLoadedEvent event) {
        if (event.getItemname() == null) return;
        AlertDialog.Builder addtocat = new AlertDialog.Builder(this);
        addtocat.setTitle(getResources().getString(R.string.addtocat));
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.select_dialog_singlechoice, new ArrayList<String>(Arrays.asList(event.categories)));
        arrayAdapter.insert(getResources().getString(R.string.other), 0);

// Set up the buttons
        addtocat.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(SampleTabsDefault.this);
                            builder.setTitle(getResources().getString(R.string.category));
                            final EditText input = new EditText(SampleTabsDefault.this);
                            input.setInputType(InputType.TYPE_CLASS_TEXT);
                            builder.setView(input);
                            builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    DatabaseHelper db = DatabaseHelper.getInstance(getBaseContext());
                                    String strName = input.getText().toString();
                                    db.addToAll(event.getItemname(), strName);
                                    confirmDialog(strName);
                                }
                            });
                            builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            });
                            builder.show();
                        } else {
                            DatabaseHelper db = DatabaseHelper.getInstance(getBaseContext());
                            String strName = arrayAdapter.getItem(which);
                            db.addToAll(event.getItemname(), strName);
                            confirmDialog(strName);
                        }
                    }
                });
        addtocat.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        addtocat.show();
    }
}
