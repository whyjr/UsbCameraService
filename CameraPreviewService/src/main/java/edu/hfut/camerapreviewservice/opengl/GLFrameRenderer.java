package edu.hfut.camerapreviewservice.opengl;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.util.DisplayMetrics;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class GLFrameRenderer implements Renderer {

    private final String TAG = getClass().getSimpleName();

    private ISimplePlayer mParentAct;
    private GLSurfaceView mTargetSurface;
    private GLProgram prog = new GLProgram(0);
    private int mScreenWidth, mScreenHeight;
    private int mVideoWidth, mVideoHeight;
    private ByteBuffer y;
    private ByteBuffer u;
    private ByteBuffer v;

    public GLFrameRenderer(ISimplePlayer callback, GLSurfaceView surface, DisplayMetrics dm) {
        mParentAct = callback;
        mTargetSurface = surface;
        mScreenWidth = dm.widthPixels;
        mScreenHeight = dm.heightPixels;
    }

    public GLFrameRenderer(ISimplePlayer callback, GLSurfaceView surface, int parentWidth, int parentHeight) {
        mParentAct = callback;
        mTargetSurface = surface;
        mScreenWidth = parentWidth;
        mScreenHeight = parentHeight;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //Utils.LOGD("GLFrameRenderer :: onSurfaceCreated");
        if (!prog.isProgramBuilt()) {
            prog.buildProgram();
            //Utils.LOGD("GLFrameRenderer :: buildProgram done");
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        //Utils.LOGD("GLFrameRenderer :: onSurfaceChanged");
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        synchronized (this) {
            if (y != null) {
                // reset position, have to be done
                y.position(0);
                u.position(0);
                v.position(0);
                //Log.e(TAG, "onDrawFrame 1");
                try {
                    prog.buildTextures(y, u, v, mVideoWidth, mVideoHeight);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                //Log.e(TAG, "onDrawFrame 2");
                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                //Log.e(TAG, "onDrawFrame 3");
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
                //Log.e(TAG, "onDrawFrame 4");
                prog.drawFrame(y, u, v);
                //Log.e(TAG, "onDrawFrame 5");
            }
        }
    }

    /**
     * this method will be called from native code, it happens when the video is about to play or
     * the video size changes.
     * 这里是假设SurfaceView 是填满全屏，预览视频原比例缩放
     */
    public void update(int w, int h) {
//        Log.e(TAG, "mVideoWidth" + mVideoWidth + " mVideoHeight" +
//                mVideoHeight + " mScreenWidth" + mScreenWidth + " mScreenHeight" + mScreenHeight);
        if (w == mVideoWidth && h == mVideoHeight) return;

        //Utils.LOGD("INIT E");
        if (w > 0 && h > 0) {
            // 调整比例
            if (mScreenWidth > 0 && mScreenHeight > 0) {
                float f1 = 1f * mScreenHeight / mScreenWidth;
                float f2 = 1f * h / w;
                if (f1 == f2) {
                    prog.createBuffers(GLProgram.squareVertices);
                } else if (f1 < f2) {
                    float widScale = f1 / f2;
                    prog.createBuffers(new float[] { -widScale, -1.0f, widScale, -1.0f, -widScale, 1.0f, widScale,
                            1.0f, });
                } else {
                    float heightScale = f2 / f1;
                    prog.createBuffers(new float[] { -1.0f, -heightScale, 1.0f, -heightScale, -1.0f, heightScale, 1.0f,
                            heightScale, });
                }
            }
            // 初始化容器
            if (w != mVideoWidth && h != mVideoHeight) {
                this.mVideoWidth = w;
                this.mVideoHeight = h;
                int yarraySize = w * h;
                int uvarraySize = yarraySize / 4;
                synchronized (this) {
                    y = ByteBuffer.allocate(yarraySize);
                    u = ByteBuffer.allocate(uvarraySize);
                    v = ByteBuffer.allocate(uvarraySize);
                }
            }
        }

        if (mParentAct != null)
            mParentAct.onPlayStart();
        //Utils.LOGD("INIT X");
    }

    /**
     * this method will be called from native code, it's used for passing yuv data to me.
     */
    public void update(byte[] ydata, byte[] udata, byte[] vdata) {
        synchronized (this) {
            y.clear();
            u.clear();
            v.clear();
            y.put(ydata, 0, ydata.length);
            u.put(udata, 0, udata.length);
            v.put(vdata, 0, vdata.length);
        }

        // request to render
        mTargetSurface.requestRender();
    }


    public void update(byte[] yuv) {
        if (yuv == null)  return;

        // 这里必须加synchronized，否则GLProgram.glTexImage2D会报错，GPU资源同时被读写出现以下问题
        // invalid address or address of corrupt block 0x1d3d9e7f passed to dlfree
        synchronized (this) {
            y.clear();
            u.clear();
            v.clear();

            int yArraySize = mVideoWidth * mVideoHeight;
            int uvArraySize = yArraySize / 4;
            // y
            y.put(yuv, 0, yArraySize);
            // u
            u.put(yuv, yArraySize, uvArraySize);
            // v
            v.put(yuv, yArraySize + uvArraySize, uvArraySize);

            // request to render
            mTargetSurface.requestRender();
        }
    }

    /**
     * this method will be called from native code, it's used for passing play state to activity.
     */
    public void updateState(int state) {
        //Utils.LOGD("updateState E = " + state);
        if (mParentAct != null) {
            mParentAct.onReceiveState(state);
        }
        //Utils.LOGD("updateState X");
    }
}
