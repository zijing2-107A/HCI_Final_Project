package com.example.helloworld2;

import android.Manifest;
import android.app.Activity;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.app.ActivityCompat;

import java.io.IOException;

public class MainActivity extends Activity {

    private final String TAG = MainActivity.class.getSimpleName();

    private long last_check_time;

    /**
     * 操作的是SurfaceHolder，所以定义全局变量
     */
    private SurfaceHolder surfaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},1);

        last_check_time = 0;

        setContentView(R.layout.content_main);

        // 获取在布局文件中定义的SurfaceView
        SurfaceView surfaceView = findViewById(R.id.surface_view);

        // 不能直接操作SurfaceView，需要通过SurfaceView拿到SurfaceHolder
        surfaceHolder = surfaceView.getHolder();

        // 使用SurfaceHolder设置屏幕高亮，注意：所有的View都可以设置 设置屏幕高亮
        surfaceHolder.setKeepScreenOn(true);
//
//        // 使用SurfaceHolder设置把画面或缓存 直接显示出来
//        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        surfaceHolder.addCallback(callback);
    }

    private Camera camera;

    /**
     * 定义SurfaceView监听回调
     */
    private SurfaceHolder.Callback callback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // Camera要选择hardware.Camera，因为Camera属于硬件hardware
            // Camera.open(1); // 这了传入的值，可以指定为：前置摄像头/后置摄像头
            camera = Camera.open(1);

            /**
             * 设置Camera与SurfaceHolder关联，Camera的数据让SurfaceView显示
             */
            try {
                camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "相机设置预览失败");
            }
            camera.setFaceDetectionListener(new FaceDetectorListener());
            camera.startPreview();
        }


        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            // 以后做具体业务功能的时候，才会用到改变的值来处理
            // ......
            System.out.println("surfaceChanged");
            if (surfaceHolder.getSurface() == null) {
                // preview surface does not exist
                Log.e(TAG, "mHolder.getSurface() == null");
                return;
            }

            try {
                camera.stopPreview();

            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
                Log.e(TAG, "Error stopping camera preview: " + e.getMessage());
            }

            try {
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();

                startFaceDetection(); // re-start face detection feature

            } catch (Exception e) {
                // ignore: tried to stop a non-existent preview
                Log.d(TAG, "Error starting camera preview: " + e.getMessage());
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            camera.stopPreview(); // (一定要有，不然只release也可能出问题)
            camera.release();
            camera = null;
            System.gc();
        }
    };


    /**
     * 当Activity被销毁的时候，一定要移除监听
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, ">>>> onDestroy()");
        if (null != callback) surfaceHolder.removeCallback(callback);
    }


    /**
     * 启动脸部检测，如果getMaxNumDetectedFaces()!=0说明不支持脸部检测
     */
    public void startFaceDetection() {
        // Try starting Face Detection
        Camera.Parameters params = camera.getParameters();
        // start face detection only *after* preview has started
        if (params.getMaxNumDetectedFaces() > 0) {
            // mCamera supports face detection, so can start it:
            camera.startFaceDetection();
        } else {
            Log.e("tag", "【FaceDetectorActivity】类的方法：【startFaceDetection】: " + "不支持");
        }
    }

    /**
     * 脸部检测接口
     */
    private class FaceDetectorListener implements Camera.FaceDetectionListener {
        @Override
        public void onFaceDetection(Camera.Face[] faces, Camera camera) {
            if (faces.length > 0) {
                long current_time = System.currentTimeMillis();
                if (current_time - last_check_time > 5000) {
                    Camera.Face face = faces[0];
                    Log.e("tag", "【FaceDetectorListener】类的方法：【onFaceDetection】: 检测到人脸");
                    Log.d("tag", "Left eye: (" + face.leftEye.x + ", " + face.leftEye.y + ")");
                    Log.d("tag", "Right eye: (" + face.rightEye.x + ", " + face.rightEye.y + ")");
                    Log.d("tag", "Mouth: (" + face.mouth.x + ", " + face.mouth.y + ")");
                    Point mid = new Point((face.leftEye.x + face.rightEye.x) / 2, (face.leftEye.y + face.rightEye.y) / 2);
                    Point v = new Point(face.mouth.x - mid.x, face.mouth.y - mid.y);
                    int angle;
                    if (Math.abs(v.x) > Math.abs(v.y)) {
                        if (v.x > 0) angle = 2;
                        else angle = 0;
                    }
                    else {
                        if (v.y > 0) angle = 1;
                        else angle = 3;
                    }
                    Settings.System.putInt(getContentResolver(),Settings.System.USER_ROTATION, angle);
                    last_check_time = current_time;
                }
            } else {
                // 只会执行一次
                Log.e("tag", "【FaceDetectorListener】类的方法：【onFaceDetection】: 没有人脸");
            }
        }
    }
}