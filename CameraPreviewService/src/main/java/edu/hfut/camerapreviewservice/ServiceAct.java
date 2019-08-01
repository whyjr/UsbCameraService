package edu.hfut.camerapreviewservice;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;

import edu.hfut.camerapreviewservice.preview.CameraPreviewer;
import edu.hfut.camerapreviewservice.preview.SurfaceCameraManager;

/**
 * @author why
 * @date 2019-7-25 8:43:12
 */
public class ServiceAct extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        startService(new Intent(this,CameraTestService.class));
    }


    @Override
    protected void onStart() {
        super.onStart();
    }
}
