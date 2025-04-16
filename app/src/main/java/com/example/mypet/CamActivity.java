package com.example.mypet;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.github.niqdev.mjpeg.Mjpeg;
import com.github.niqdev.mjpeg.MjpegView;
import com.github.niqdev.mjpeg.DisplayMode;

public class CamActivity extends AppCompatActivity {

    private MjpegView mjpegView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam);

        mjpegView = findViewById(R.id.mjpegView);

        String streamUrl = "http://192.168.0.100:8080/?action=stream";

        Mjpeg.newInstance()
                .open(streamUrl, 5_000)
                .subscribe(
                        inputStream -> {
                            mjpegView.setSource(inputStream);
                            mjpegView.setDisplayMode(DisplayMode.BEST_FIT);
                            mjpegView.showFps(true);
                        },
                        throwable -> {
                            throwable.printStackTrace();
                        }
                );
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mjpegView != null) mjpegView.stopPlayback();
    }
}
