package edu.hfut.camerapreviewclient;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Switch;

import edu.hfut.camerapreviewservice.ICameraInterface;

/**
 * @author why
 * @date 2019-7-26 8:49:53
 */
public class Client extends AppCompatActivity {

    private static final String TAG = "ClientWhy";
    private ICameraInterface previewerClient = null;
    private EditText landmarkX;
    private EditText landmarkY;
    private EditText previewW;
    private EditText previewH;
    private Switch controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        Intent intent = new Intent();
        intent.setAction("why.camera.service");
        intent.setPackage("edu.hfut.camerapreviewservice");
        bindService(intent, new MyServiceConnection(), BIND_AUTO_CREATE);


        landmarkX = findViewById(R.id.left_top_x);
        landmarkY = findViewById(R.id.left_top_y);
        previewW = findViewById(R.id.preview_width);
        previewH = findViewById(R.id.preview_height);
        controller = findViewById(R.id.is_draggable_switch);

    }


    public void open(View view) {
        hideSoftware();

        //int width = Integer.parseInt(previewW.getText().toString());
        //int height = Integer.parseInt(previewH.getText().toString());
        //int x = Integer.parseInt(landmarkX.getText().toString());
        //int y = Integer.parseInt(landmarkY.getText().toString());

        if (previewW.getText().toString().isEmpty() && landmarkX.getText().toString().isEmpty()) {
            try {
                previewerClient.openPreview1(controller.isChecked());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (!previewW.getText().toString().isEmpty() && landmarkX.getText().toString().isEmpty()) {
            try {
                previewerClient.openPreview2(Integer.parseInt(previewW.getText().toString()), Integer.parseInt(previewH.getText().toString()), controller.isChecked());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else if (previewW.getText().toString().isEmpty() && !landmarkX.getText().toString().isEmpty()) {
            try {
                previewerClient.openPreview3(Integer.parseInt(landmarkX.getText().toString()), Integer.parseInt(landmarkY.getText().toString()), controller.isChecked());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        } else {
            try {
                previewerClient.openPreview4(Integer.parseInt(previewW.getText().toString()), Integer.parseInt(previewH.getText().toString()),
                        Integer.parseInt(landmarkX.getText().toString()), Integer.parseInt(landmarkY.getText().toString()), controller.isChecked());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void close(View view) {
        try {
            previewerClient.closePreview();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void hideSoftware() {
        InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(this.getWindow().getDecorView().getWindowToken(), 0);
    }


    /**
     *
     */
    private class MyServiceConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            previewerClient = ICameraInterface.Stub.asInterface(iBinder);
            Log.e(TAG, "onServiceConnected: 绑定服务成功了");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            System.out.println("解绑服务成功了");

        }
    }
}
