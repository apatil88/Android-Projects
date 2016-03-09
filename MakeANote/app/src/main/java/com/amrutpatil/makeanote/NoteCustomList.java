package com.amrutpatil.makeanote;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Amrut on 3/7/16.
 * Description: Class to create our own LinearLayout for notes containing list of items and displaying them in the RecyclerView.
 */
public class NoteCustomList extends LinearLayout {

    private Context mContext;
    private LinearLayout mListItem;
    private List<EditText> mTextBoxes = new ArrayList<>();

    public NoteCustomList(Context context){
        super(context);
        mContext = context;
    }

    public void setUp(){
        setOrientation(VERTICAL);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(layoutParams);
    }

    //Processing notes with list of items
    public void setUpForHomeAdapter(String listEntries){
        // Eggs$false%Ham$false%Bread$false%Vegemite$false
        setOrientation(VERTICAL);
        String[] listEntryTokens = listEntries.split("%");
        // [0] Eggs$false
        // [1] Ham$false
        // ...

        boolean isStrikeOut = false;
        String listItem = "";

        for(String entryDetails : listEntryTokens){
            mListItem = new LinearLayout(mContext);
            mListItem.setOrientation(HORIZONTAL);
            String[] listEntry = entryDetails.split("\\$");
            //[0] Eggs
            //[1] false
            //...
            for(int i = 0; i < listEntry.length; i++){
                if( i%2 == 0){
                    listItem = listEntry[i];
                } else{
                    isStrikeOut = Boolean.valueOf(listEntry[i]);
                }
            }

            //Draw the checkbox
            CheckBox checkBox = new CheckBox(mContext);
            checkBox.setEnabled(false);
            checkBox.setChecked(isStrikeOut);

            //Draw the textview
            TextView textView = new TextView(mContext);
            textView.setText(listItem);
            textView.setBackgroundColor(Color.TRANSPARENT);

            if(isStrikeOut){
                textView.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            }

            //Add the checkbox and textview to the view
            mListItem.addView(checkBox);
            mListItem.addView(textView);
            addView(mListItem);
        }

    }

    //Editing notes with list of items
    public void setUpForEditMode(String listEntries) {
        setOrientation(VERTICAL);
        mTextBoxes = new ArrayList<>();
        String[] listEntryTokens = listEntries.split("%");
        boolean isStrikeOut = false;
        String listItem = "";

        for (String entryDetails : listEntryTokens) {
            mListItem = new LinearLayout(mContext);
            mListItem.setOrientation(HORIZONTAL);
            String[] listEntry = entryDetails.split("\\$");
            for (int i = 0; i < listEntry.length; i++) {
                if (i % 2 == 0)
                    listItem = listEntry[i];
                else
                    isStrikeOut = Boolean.valueOf(listEntry[i]);
            }

            final CheckBox checkBox = new CheckBox(mContext);
            checkBox.setChecked(isStrikeOut);

            final ImageView deleteImageView = new ImageView(mContext);
            deleteImageView.setImageResource(android.R.drawable.ic_menu_delete);

            final EditText textBox = new EditText(mContext);
            textBox.setText(listItem);
            textBox.setBackgroundColor(Color.TRANSPARENT);

            if (isStrikeOut) {
                textBox.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            }

            mListItem.addView(deleteImageView);
            mListItem.addView(checkBox);
            mListItem.addView(textBox);
            mTextBoxes.add(textBox);
            addView(mListItem);

            deleteImageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinearLayout layout = (LinearLayout) v.getParent();
                    mTextBoxes.remove(mTextBoxes.indexOf(layout.getChildAt(2)));
                    layout.removeAllViews();
                }
            });

            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked) {
                        textBox.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                    } else {
                        textBox.setPaintFlags(checkBox.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    }
                }
            });
        }
    }

    public void setUpForListNotification(String listEntries) {

        setOrientation(VERTICAL);
        mTextBoxes = new ArrayList<>();
        String[] listEntryTokens = listEntries.split("%");
        boolean isStrikeOut = false;
        String listItem = "";

        for (String entryDetails : listEntryTokens) {
            mListItem = new LinearLayout(mContext);
            mListItem.setOrientation(HORIZONTAL);
            //
            if(entryDetails.contains(AppConstant.TRUE)) {
                listItem = entryDetails.split(AppConstant.TRUE)[0];
                isStrikeOut = true;
            } else if(entryDetails.contains(AppConstant.FALSE)){
                listItem = entryDetails.split(AppConstant.FALSE)[0];
                isStrikeOut = false;
            }

            final CheckBox checkBox = new CheckBox(mContext);
            checkBox.setChecked(isStrikeOut);
            checkBox.setEnabled(false);
            final EditText textBox = new EditText(mContext);
            textBox.setText(listItem);
            textBox.setEnabled(false);
            textBox.setBackgroundColor(Color.TRANSPARENT);

            if (isStrikeOut) {
                textBox.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            }

            mListItem.addView(checkBox);
            mListItem.addView(textBox);
            mTextBoxes.add(textBox);
            addView(mListItem);
        }
    }

    public void addNewCheckbox() {
        mListItem = new LinearLayout(mContext);
        mListItem.setOrientation(HORIZONTAL);
        final ImageView deleteImageView = new ImageView(mContext);
        deleteImageView.setImageResource(android.R.drawable.ic_menu_delete);

        final EditText textBox = new EditText(mContext);
        textBox.setBackgroundColor(Color.TRANSPARENT);
        textBox.setHint("....");
        textBox.setSelection(0);
        textBox.requestFocus();
        final CheckBox checkBox = new CheckBox(mContext);
        checkBox.setPadding(10, 10, 10, 10);


        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    textBox.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    textBox.setPaintFlags(checkBox.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }
            }
        });
        mListItem.addView(deleteImageView);
        mListItem.addView(checkBox);
        mListItem.addView(textBox);
        mTextBoxes.add(textBox);
        addView(mListItem);

        deleteImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout layout = (LinearLayout) v.getParent();
                mTextBoxes.remove(mTextBoxes.indexOf(layout.getChildAt(2)));
                layout.removeAllViews();
            }
        });
    }

    public String getLists() {

        String stringToReturn = "";
        boolean isStrikeThrough;
        int itemsCount = mTextBoxes.size();

        for (int i = 0; i < itemsCount; i++) {
            EditText editText = mTextBoxes.get(i);
            if (editText.getPaintFlags() == Paint.STRIKE_THRU_TEXT_FLAG)
                isStrikeThrough = true;
            else
                isStrikeThrough = false;
            if (editText.getText().toString().length() > 0)
                stringToReturn = stringToReturn + editText.getText().toString() + "$" + isStrikeThrough + "%";
        }

        return stringToReturn;
    }


}
