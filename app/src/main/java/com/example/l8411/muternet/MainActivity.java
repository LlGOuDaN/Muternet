package com.example.l8411.muternet;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import jp.co.recruit_lifestyle.android.widget.PlayPauseButton;

public class MainActivity extends AppCompatActivity {
    Runnable timer;
    MediaPlayer mediaPlayer;
    PlayPauseButton playPauseButton;
    SeekBar seekBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //setup
        setUpMediaPlayer();
        setUpPlayPauseButton();
        setUpSeekBar();
    }

    private void setUpPlayPauseButton() {
        //Set Play Pause Button Color
        playPauseButton = findViewById(R.id.play_pause_button);
        playPauseButton.setColor(Color.WHITE);

        final ImageView imageView = findViewById(R.id.image_view);
        final ObjectAnimator animator = ObjectAnimator.ofFloat(imageView,"rotation",0,360);
        animator.setDuration(mediaPlayer.getDuration()/10);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());

        playPauseButton.setOnControlStatusChangeListener(new PlayPauseButton.OnControlStatusChangeListener() {
            @Override
            public void onStatusChange(View view, boolean state) {
                if(state){
                    float f = imageView.getRotation();
                    animator.setFloatValues(f,f+360);
                    animator.start();
                    mediaPlayer.start();
                }else {
                    animator.pause();
                    mediaPlayer.pause();
                }
            }
        });
    }

    private void setUpSeekBar() {
        seekBar = findViewById(R.id.seek_bar);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(mediaPlayer!=null && fromUser){
                    mediaPlayer.seekTo(progress*1000);
                    mediaPlayer.start();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBar.setMax(mediaPlayer.getDuration()/1000);
        timer = new Runnable() {
            @Override
            public void run() {
                if(mediaPlayer!=null){
                    int currentPosition = mediaPlayer.getCurrentPosition()/1000;
                    seekBar.setProgress(currentPosition);
                    ImageView imageView = findViewById(R.id.image_view);
                    Log.d("??",playPauseButton.isPlayed()+"");
                }
                new Handler().postDelayed(timer, 1000);
            }
        };
        timer.run();
    }

    private void setUpMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
        try {
            String url = "https://firebasestorage.googleapis.com/v0/b/muternet-cd7bf.appspot.com/o/Alan%20Walker%20-%20Faded.mp3?alt=media&token=3bc99504-fda8-424d-92ef-e2c2abbd13bc";
            mediaPlayer.setDataSource(url);
            mediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
