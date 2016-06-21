package com.andrewaarondev.shoppinglist;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;

import de.greenrobot.event.EventBus;

/**
 * Created by Andrew on 6/4/2016.
 */
public class MyDialogFragment extends DialogFragment {//
    private View form=null;
    private GroceryItem mNum;
    private String[] lists;
    Integer[] ids;
    public static MyDialogFragment newInstance(GroceryItem num) {
        MyDialogFragment f = new MyDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putParcelable("num", num);
        f.setArguments(args);
        return f;
    }
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        EventBus.getDefault().register(this);
        mNum = getArguments().getParcelable("num");
        form = getActivity().getLayoutInflater().inflate(R.layout.dialog, null);
        ImageButton ib = (ImageButton)form.findViewById(R.id.addToListButton);
        ib.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToList();
            }
        });
        ImageButton ib2 = (ImageButton)form.findViewById(R.id.deleteButton);
        ib2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItem();
            }
        });
        ImageButton ib3 = (ImageButton)form.findViewById(R.id.renameButton);
        ib3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                renameItem();
            }
        });

        AlertDialog.Builder builder=new AlertDialog.Builder(getActivity());
        return(builder.setView(form)
                .setNegativeButton(android.R.string.cancel, null).create());
    }
    void addToList() {
        DatabaseHelper db = DatabaseHelper.getInstance(getActivity().getBaseContext());
        db.loadTitles();
        dismiss();
    }
    @SuppressWarnings("unused")
    public void onEventMainThread(TitlesLoadedEvent event) {
        if (event.getProse() == null) return;
        final DatabaseHelper db = DatabaseHelper.getInstance(getActivity().getBaseContext());
        AlertDialog.Builder addtocat = new AlertDialog.Builder(getActivity());
        addtocat.setTitle(getResources().getString(R.string.addtolist));
        lists = event.getProse();
        ids = event.getIds();
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                getActivity(),
                android.R.layout.select_dialog_singlechoice, lists);

// Set up the buttons
        addtocat.setAdapter(
                arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            String strName = arrayAdapter.getItem(which);
                            db.addItem(ids[which].intValue(), mNum.getName());
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
    void deleteItem() {
        DatabaseHelper db = DatabaseHelper.getInstance(getActivity().getBaseContext());
        db.deleteFromAll(mNum);
        dismiss();
    }
    void renameItem() {
        AlertDialog.Builder renbuilder = new AlertDialog.Builder(getActivity());
        renbuilder.setTitle(getResources().getString(R.string.newtitle));
        final DatabaseHelper db = DatabaseHelper.getInstance(getActivity().getBaseContext());
// Set up the input
        final EditText reninput = new EditText(getActivity());
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        reninput.setInputType(InputType.TYPE_CLASS_TEXT);
        reninput.setText(mNum.getName());
        renbuilder.setView(reninput);
// Set up the buttons
        renbuilder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mNum.setName(reninput.getText().toString());
                db.renameInAll(mNum);
                dialog.dismiss();
            }
        });
        renbuilder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        dismiss();
        renbuilder.show();
    };
    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
