package edu.hfut.camerapreviewservice.preview;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import edu.hfut.camerapreviewservice.opengl.GLFrameRenderer;
import edu.hfut.camerapreviewservice.opengl.GLFrameSurface;

/**
 * author:why
 * created on: 2019/7/25 15:01
 * description:
 */
public class GLCameraManager implements CameraPreviewer {

    private static final String TAG = "WhyCameraManagerWhy";
    private static volatile CameraPreviewer mCameraPreviewer;
    private Context mContext;
    private WindowManager wm;
    private int default_width;
    private int default_height;
    private int tempWidth;
    private int tempHeight;
    private int touchPoint_x;
    private int touchPoint_y;
    private WindowManager.LayoutParams params;
    private boolean isPreviewing = false;
    private boolean mIsDraggable = false;

    private Camera mCamera;
    /**
     * 视频预览Surface
     */
    private GLFrameSurface mGLFrameSurface;
    public GLFrameRenderer mGLFRenderer;

    private GLCameraManager(Context context) {

        this.mContext = context;
        wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);

        Resources resources = mContext.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        default_width = dm.widthPixels ;
        default_height = dm.heightPixels ;
        Log.e(TAG, default_width + "  " + default_height);

        tempWidth=default_width/2;
        tempHeight=default_height/2;

        if (mCamera == null) {
            mCamera = Camera.open();
            mCamera.startPreview();
        }
        if (mCamera != null) {
            mCamera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    Log.e(TAG, "数据长度: " + data.length);
                    if (data != null && mGLFRenderer != null) {
                        mGLFRenderer.update(tempWidth, tempHeight);
                        mGLFRenderer.update(data);
                    }
                }
            });
        }
    }


    /**
     * DCL
     *
     * @param context
     * @return
     */
    public static CameraPreviewer getInstance(Context context) {

        if (mCameraPreviewer == null) {
            synchronized (GLCameraManager.class) {
                if (mCameraPreviewer == null) {
                    mCameraPreviewer = new GLCameraManager(context);
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
       // mCamera.stopPreview();
        isPreviewing=false;
        mIsDraggable=false;
        if (mGLFrameSurface != null) {
            wm.removeView(mGLFrameSurface);
            mGLFrameSurface = null;
        }
    }


    /**
     * @param width
     * @param height
     * @param landmark_x
     * @param landmark_y
     * @param isDraggable
     */
    private void openPreviewImp(int width, int height, int landmark_x, int landmark_y, boolean isDraggable) {

       // mCamera.startPreview();
        if (width == -1) {
            tempWidth = default_width/2;
            tempHeight = default_height/2;
        } else {
            tempWidth = width;
            tempHeight = height;
        }

        params = new WindowManager.LayoutParams(tempWidth, tempHeight,
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
//                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,//不可点击
                PixelFormat.TRANSPARENT);

        params.gravity = Gravity.TOP | Gravity.START;
        params.alpha = PixelFormat.TRANSPARENT;
        if (landmark_x != -1) {
            params.x = landmark_x;
            params.y = landmark_y;
        } else {
            params.x = tempWidth/2;
            params.y = tempHeight/2;
        }

        if (!isPreviewing) {
            GLFrameSurface glFrameSurface = new GLFrameSurface(mContext);
            mGLFrameSurface = glFrameSurface;
            mGLFrameSurface.setEGLContextClientVersion(2);
            mGLFRenderer = new GLFrameRenderer(
                    null,
                    mGLFrameSurface,
                    tempWidth,
                    tempHeight
            );
            mGLFrameSurface.setRenderer(mGLFRenderer);
        }

        if (isDraggable && !mIsDraggable) {
            Log.e(TAG, "openPreviewImp: 添加监听");
            mIsDraggable = true;

            mGLFrameSurface.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    // Log.e(TAG, "onTouch: " + event.getAction() + "  " + event.getRawX());
                    //TODO 处理拖拽逻辑
                    switch (event.getAction()) {
                        case MotionEvent.ACTION_MOVE:
                            params.x = params.x + (int)event.getRawX() - touchPoint_x;
                            params.y = params.y + (int)event.getRawY() - touchPoint_y;
                            if(params.x<0){
                                params.x=0;
                            }
                            if(params.y<0){
                                params.y=0;
                            }
                            touchPoint_x = (int) event.getRawX();
                            touchPoint_y = (int) event.getRawY();

                            wm.updateViewLayout(mGLFrameSurface, params);
                            break;
                        case MotionEvent.ACTION_DOWN:
                            //init aviation
                            touchPoint_x = (int) event.getRawX();
                            touchPoint_y = (int) event.getRawX();

                            break;
                        case MotionEvent.ACTION_UP:
                            //release aviation
                            touchPoint_x = 0;
                            touchPoint_y = 0;
                            break;
                    }

                    return false;
                }
            });
        } else {
            mIsDraggable = false;
        }
        Log.e(TAG, "openPreviewImp: " + mGLFrameSurface);

        if(!isPreviewing){
            isPreviewing=true;
            wm.addView(mGLFrameSurface, params);
        }
        else {
            wm.updateViewLayout(mGLFrameSurface,params);
        }

    }

}
