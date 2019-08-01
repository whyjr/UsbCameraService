// ICameraInterface.aidl
package edu.hfut.camerapreviewservice;

// Declare any non-default types here with import statements

interface ICameraInterface {
              void openPreview1(boolean isDraggable);
              void openPreview2(int width,int height,boolean isDraggable);
              void openPreview3(int landmark_x,int landmark_y,boolean isDraggable);
              void openPreview4(int width,int height,int landmark_x,int landmark_y,boolean isDraggable);
              void closePreview();
}
