package com.example.helloworld2;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.GestureDetector;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {

    GestureDetector gestureDetector;
    int finger_count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        gestureDetector = new GestureDetector(this, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //当手指按下的时候触发下面的方法
    @Override
    public boolean onDown(MotionEvent e) {
        System.out.println("onDown");
        return false;
    }

    //当用户手指在屏幕上按下,而且还未移动和松开的时候触发这个方法
    @Override
    public void onShowPress(MotionEvent e) {
        System.out.println("onShowPress");
    }

    //当手指在屏幕上轻轻点击的时候触发下面的方法
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        System.out.println("onSingleTapUp");
        return false;
    }

    //当手指在屏幕上滚动的时候触发这个方法
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        int finger_count_now = e2.getPointerCount();
        if(finger_count_now > finger_count) finger_count = finger_count_now;
        System.out.println("scroll count: " + finger_count_now);
        System.out.println("onScroll");
        return false;
    }

    //当用户手指在屏幕上长按的时候触发下面的方法
    @Override
    public void onLongPress(MotionEvent e) {
        System.out.println("onLongPress");
    }

    //当用户的手指在触摸屏上拖过的时候触发下面的方法,velocityX代表横向上的速度,velocityY代表纵向上的速度
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        System.out.println("count: " + finger_count);
        System.out.println("onFling");
        if(finger_count != 2){
            finger_count = 1;
            return false;
        }
        finger_count = 1;
        int angle = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        int flag = 0;
        if (Math.abs(e1.getX() - e2.getX()) > Math.abs(e1.getY() - e2.getY())) {
            if (e1.getX() > e2.getX()) flag = 3;
            else flag = 1;
        }
        else {
            if (e1.getY() < e2.getY()) flag = 2;
            else flag = 0;
        }
        int orientation = (flag + angle) % 4;
        System.out.println("angle: " + angle + "   flag:" + flag + "    orientation:" + orientation);
        if (orientation == 0) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        else if (orientation == 1) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        else if (orientation == 2) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
        }
        else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
        }
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event);
    }
}
