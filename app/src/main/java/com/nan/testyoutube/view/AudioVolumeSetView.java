package com.nan.testyoutube.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nan.testyoutube.util.AudioManagerUtil;
import com.nan.testyoutube.R;

/**
 * Author:jingnan
 * Time:2019-09-07/23
 */
public class AudioVolumeSetView extends RelativeLayout {
    private TextView textView;
    private ImageView imageView;

    private GestureDetector gestureDetector;
    private AudioManagerUtil audioManagerUtil;

    private int heght = 0;

    public AudioVolumeSetView(Context context) {
        this(context, null);
    }

    public AudioVolumeSetView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioVolumeSetView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(context).inflate(R.layout.view_audio_volume_set, this);
        textView = view.findViewById(R.id.testView);
        imageView = view.findViewById(R.id.image);
        gestureDetector = new GestureDetector(context, new GestureListener());
        audioManagerUtil = new AudioManagerUtil(context);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }

    private class GestureListener implements GestureDetector.OnGestureListener {

        @Override
        public boolean onDown(MotionEvent e) {
            if (heght == 0) {
                heght = getHeight();
            }
            Log.i("GestureListener", "onDown : x = " + e.getX() + ", y = " + e.getY());
            return true;
        }

        @Override
        public void onShowPress(MotionEvent e) {
            Log.i("GestureListener", "onShowPress : x = " + e.getX() + ", y = " + e.getY());

        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.i("GestureListener", "onSingleTapUp : x = " + e.getX() + ", y = " + e.getY());
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (e2.getY() >= 0 && e2.getY() <= heght) {
                Log.i("GestureListener",
                        "onScroll :"
//                            + " e1.x = " + e1.getX()
                                + ", e1.y = " + e1.getY()
//                            + ", e2.x = " + e2.getX()
                                + ", e2.y = " + e2.getY()
//                            + ", distanceX = " + distanceX
                                + ", distanceY = " + distanceY);
                if (distanceY > 0) {
                    audioManagerUtil.addVoice100();
                } else {
                    audioManagerUtil.subVoice100();
                }
                return true;
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Log.i("GestureListener", "onLongPress : x = " + e.getX() + ", y = " + e.getY());

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            Log.i("GestureListener", "onFling : e1.x = " + e1.getX() + ", e1.y = " + e1.getY()
                    + ", e2.x = " + e2.getX() + ", e2.y = " + e2.getY()
                    + ", velocityX = " + velocityX + ", velocityY = " + velocityY);
            return false;
        }
    }
}
