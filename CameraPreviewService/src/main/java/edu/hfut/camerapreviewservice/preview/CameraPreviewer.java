package edu.hfut.camerapreviewservice.preview;

/**
 * author:why
 * created on: 2019/7/25 15:10
 * description:
 */
public interface CameraPreviewer {
    void openPreview(boolean isDraggable);
    void openPreviewWithSize(int width,int height,boolean isDraggable);
    void openPreviewWithLoc(int landmark_x,int landmark_y,boolean isDraggable);
    void openPreviewWithAll(int width,int height,int landmark_x,int landmark_y,boolean isDraggable);
    void closePreview();

}
