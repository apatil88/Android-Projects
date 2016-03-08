package com.amrutpatil.makeanote;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Amrut on 3/7/16.
 */
public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {

    public static interface OnItemClickListener{
        public void OnItemClick(View view, int position);
        public void OnItemLongClick(View view, int position);
    }

    private OnItemClickListener mListener;
    private GestureDetector mGestureDetector;

    public RecyclerItemClickListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener ){
        mListener = listener;
        mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
            public boolean onSingleTapUp(MotionEvent motionEvent){
               return true;
            }
            public void onLongPress(MotionEvent motionEvent){
                //Location of the long tap on screen
                View childView = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
                if(childView != null && mListener != null) {
                    mListener.OnItemLongClick(childView, recyclerView.getChildAdapterPosition(childView));
                }
            }
        });

    }

    public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent){
        View childView = recyclerView.findChildViewUnder(motionEvent.getX(), motionEvent.getY());
        if(childView != null && mListener != null && mGestureDetector.onTouchEvent(motionEvent)){
            mListener.OnItemClick(childView, recyclerView.getChildAdapterPosition(childView));
        }
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}
