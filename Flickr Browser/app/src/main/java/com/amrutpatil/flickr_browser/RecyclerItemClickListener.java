package com.amrutpatil.flickr_browser;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Amrut on 2/15/15.
 * Class to check for tap and long tap on screen
 */
public class RecyclerItemClickListener implements RecyclerView.OnItemTouchListener {

        public static interface OnItemClickListener{
            public void OnItemClick(View view, int position);
            public void OnItemLongClick(View view, int position);
        }

        public OnItemClickListener mListener;
        public GestureDetector mGestureDetector;

        public RecyclerItemClickListener(Context context, final RecyclerView recyclerView, OnItemClickListener listener){
            mListener = listener;
            mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
                public boolean onSingleTapUp(MotionEvent e){
                    return true;
                }

                public void onLongPress(MotionEvent e){
                   View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                   if((childView != null) && (mListener != null)){
                       mListener.OnItemLongClick(childView, recyclerView.getChildPosition(childView));
                   }
                }
            });
        }

        public boolean onInterceptTouchEvent(RecyclerView recyclerView, MotionEvent e){
            View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
            if((childView!=null) && (mListener != null) && (mGestureDetector.onTouchEvent(e))){
                mListener.OnItemClick(childView,recyclerView.getChildPosition(childView));
            }
            return false;
        }

        public void onTouchEvent(RecyclerView recyclerView, MotionEvent e){

        }
}
