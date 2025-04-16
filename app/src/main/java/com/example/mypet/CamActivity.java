package com.example.mypet;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.github.niqdev.ipcam.view.IpCamView;
import com.github.niqdev.ipcam.view.IpCamUri;

public class CamActivity extends AppCompatActivity {

    private IpCamView ipCamView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam);

        ipCamView = findViewById(R.id.ipCamView);

        IpCamUri uri = new IpCamUri("Camera1", "http://192.168.0.100:8080/?action=stream");
        ipCamView.start(uri);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (ipCamView != null) ipCamView.stopPlayback();
    }
}
