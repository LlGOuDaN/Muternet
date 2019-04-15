package com.example.l8411.muternet;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import jp.co.recruit_lifestyle.android.widget.PlayPauseButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PlayPauseButton playPauseButton = findViewById(R.id.play_pause_button);
        playPauseButton.setColor(Color.WHITE);
    }
}
