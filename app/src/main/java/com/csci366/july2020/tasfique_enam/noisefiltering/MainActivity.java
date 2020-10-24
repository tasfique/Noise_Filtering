package com.csci366.july2020.tasfique_enam.noisefiltering;

import androidx.appcompat.app.AppCompatActivity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    ImageView btNoise, btDeNoise, audioPicture;

    MediaPlayer mediaPlayer;
    Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Assigning Values
        btNoise = findViewById(R.id.bt_noise);
        btDeNoise = findViewById(R.id.bt_denoise);
        audioPicture = findViewById(R.id.audio_picture);
    }
}