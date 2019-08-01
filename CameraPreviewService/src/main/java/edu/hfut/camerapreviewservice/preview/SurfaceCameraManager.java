package edu.hfut.camerapreviewservice.preview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import edu.hfut.camerapreviewservice.R;


/**
 * author:why
 * created on: 2019/7/29 10:42
 * description:
 */
public class SurfaceCameraManager implements CameraPreviewer {

    private static final String TAG = "SurfaceCamManagerWhy";

    private static volatile CameraPreviewer mCameraPreviewer;
    private Context mContext;
    private Camera mCamera;
    private int default_width;
    private int default_height;
    private int tempWidth;
    private int tempHeight;
    private int touchPoint_x;
    private int touchPoint_y;
    private WindowManager.LayoutParams params;
    private boolean isPreviewing = false;
    private boolean mIsDraggable = false;
    private View windowView;
    private WindowManager wm;
    CameraPreview cameraPreview;
    private boolean moveTrigger = false;


    private SurfaceCameraManager(Context context) {
        this.mContext = context;
        wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Resources resources = mContext.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        default_width = dm.widthPixels;
        default_height = dm.heightPixels;
        Log.e(TAG, default_width + "  " + default_height);

        tempWidth = default_width / 2;
        tempHeight = default_height / 2;
    }


    /**
     * DCL
     *
     * @param context
     * @return
     */
    public static CameraPreviewer getInstance(Context context) {
        if (mCameraPreviewer == null) {
            synchronized (SurfaceCameraManager.class) {
                if (mCameraPreviewer == null) {
                    mCameraPreviewer = new SurfaceCameraManager(context);
                }
            }
        }
        return mCameraPreviewer;
    }


    @Override
    public void openPreview(boolean isDraggable) {
        openPreviewImp(-1, -1, -1, -1, isDraggable);
    }

    @Override
    public void openPreviewWithSize(int width, int height, boolean isDraggable) {
        openPreviewImp(width, height, -1, -1, isDraggable);
    }

    @Override
    public void openPreviewWithLoc(int landmark_x,int landmark_y,boolean isDraggable) {
        openPreviewImp(-1, -1, landmark_x, landmark_y, isDraggable);
    }

    @Override
    public void openPreviewWithAll(int width, int height, int landmark_x, int landmark_y, boolean isDraggable) {
        openPreviewImp(width, height, landmark_x, landmark_y, isDraggable);
    }

    @Override
    public void closePreview() {

        isPreviewing = false;
        mIsDraggable=false;
        if (windowView != null) {
            wm.removeView(windowView);
            windowView = null;
            cameraPreview.stopPreview();
            cameraPreview = null;
        }

    }


    private void openPreviewImp(int width, int height, int landmark_x, int landmark_y, boolean isDraggable) {

        if (width == -1) {
            tempWidth = default_width / 2;
            tempHeight = default_height / 2;
        } else {
            tempWidth = width;
            tempHeight = height;
        }

        params = new WindowManager.LayoutParams(tempWidth, tempHeight,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSPARENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.alpha = PixelFormat.TRANSPARENT;
        if (landmark_x != -1) {
            params.x = landmark_x;
            params.y = landmark_y;
        } else {
            params.x = (default_width-tempWidth) / 2;
            params.y = (default_height-tempHeight) / 2;
        }

        if (!isPreviewing) {
            windowView  = LayoutInflater.from(mContext).inflate(R.layout.surface_camera_layout, null);
            cameraPreview = windowView.findViewById(R.id.cf_frame_camera_sv);
            if (mCamera == null) {
                mCamera = Camera.open();
            }
            cameraPreview.setCamera(mCamera);
            cameraPreview.surfaceCreated(cameraPreview.getHolder());
        }

        if (isDraggable && !mIsDraggable) {
            Log.e(TAG, "openPreviewImp: 添加监听");
            mIsDraggable = true;

            windowView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    //TODO 处理拖拽逻辑
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_MOVE:

                            if (moveTrigger) {//解决首次拖动画面抖动问题
                                touchPoint_x = (int) event.getRawX();
                                touchPoint_y = (int) event.getRawY();
                                moveTrigger = false;
                            } else {
                                params.x = params.x + (int) event.getRawX() - touchPoint_x;
                                params.y = params.y + (int) event.getRawY() - touchPoint_y;
                                if (params.x < 0) {
                                    params.x = 0;
                                }
                                if (params.y < 0) {
                                    params.y = 0;
                                }
                                touchPoint_x = (int) event.getRawX();
                                touchPoint_y = (int) event.getRawY();
                                wm.updateViewLayout(windowView, params);
                            }
                            break;
                        case MotionEvent.ACTION_DOWN:
                            moveTrigger = true;
                            break;
                    }
                    return false;
                }
            });
        } else {
            mIsDraggable = false;
        }

        if (!isPreviewing) {
            isPreviewing = true;
            wm.addView(windowView, params);
        } else {
            wm.updateViewLayout(windowView, params);
        }

    }
}
