package edu.hfut.camerapreviewservice;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;

import edu.hfut.camerapreviewservice.preview.CameraPreviewer;
import edu.hfut.camerapreviewservice.preview.GLCameraManager;
import edu.hfut.camerapreviewservice.preview.SurfaceCameraManager;

/**
 * @author why
 * @date 2019-7-25 15:00:21
 */
public class CameraTestService extends Service {

    private static final String TAG = "CameraTestServiceWhy";

    private final int OPEN_PREVIEW_1=0X00;
    private final int OPEN_PREVIEW_2=0X01;
    private final int OPEN_PREVIEW_3=0X02;
    private final int OPEN_PREVIEW_4=0X03;
    private final int CLOSE_PREVIEW=0X04;

    private CameraPreviewer mCameraPreviewer;

    private boolean mIsDraggable;
    private int mWidth;
    private int mHeight;
    private int mLandmark_x;
    private int mLandmark_y;

    @SuppressWarnings("HandlerLeak")
    public Handler workHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case OPEN_PREVIEW_1:
                    mCameraPreviewer.openPreview(mIsDraggable);
                    break;
                case OPEN_PREVIEW_2:
                    mCameraPreviewer.openPreviewWithSize(mWidth,mHeight,mIsDraggable);
                    break;
                case OPEN_PREVIEW_3:
                    mCameraPreviewer.openPreviewWithLoc(mLandmark_x,mLandmark_y,mIsDraggable);
                    break;
                case OPEN_PREVIEW_4:
                    mCameraPreviewer.openPreviewWithAll(mWidth,mHeight,mLandmark_x,mLandmark_y,mIsDraggable);
                    break;
                case CLOSE_PREVIEW:
                    mCameraPreviewer.closePreview();
                    break;
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "onCreate: " );
        mCameraPreviewer= GLCameraManager.getInstance(this);
//        mCameraPreviewer= SurfaceCameraManager.getInstance(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return  new Client();
    }


    private class Client extends ICameraInterface.Stub{

        @Override
        public void openPreview1(boolean isDraggable) throws RemoteException {
            Log.e(TAG, "openPreview1: " );
            mIsDraggable=isDraggable;
            workHandler.sendEmptyMessage(OPEN_PREVIEW_1);
        }

        @Override
        public void openPreview2(int width, int height, boolean isDraggable) throws RemoteException {
            Log.e(TAG, "openPreview2: " );
            mIsDraggable=isDraggable;
            mWidth=width;
            mHeight=height;
            workHandler.sendEmptyMessage(OPEN_PREVIEW_2);
        }

        @Override
        public void openPreview3(int landmark_x, int landmark_y, boolean isDraggable) throws RemoteException{
            mIsDraggable=isDraggable;
            mLandmark_x=landmark_x;
            mLandmark_y=landmark_y;
            workHandler.sendEmptyMessage(OPEN_PREVIEW_3);
        }
        @Override
        public void openPreview4(int width, int height, int landmark_x, int landmark_y, boolean isDraggable) throws RemoteException {
            Log.e(TAG, "openPreview4: "+isDraggable );
            mIsDraggable=isDraggable;
            mWidth=width;
            mHeight=height;
            mLandmark_x=landmark_x;
            mLandmark_y=landmark_y;
            workHandler.sendEmptyMessage(OPEN_PREVIEW_4);
        }

        @Override
        public void closePreview() throws RemoteException {
            Log.e(TAG, "closePreview: " );
            mIsDraggable=false;
            mWidth=0;
            mHeight=0;
            mLandmark_x=0;
            mLandmark_y=0;
            workHandler.sendEmptyMessage(CLOSE_PREVIEW);

        }

        @Override
        public IBinder asBinder() {
            return  new Client();
        }
    }

}
