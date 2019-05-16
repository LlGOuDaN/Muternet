package team.edu.app.muternet.Fragment;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import jp.co.recruit_lifestyle.android.widget.PlayPauseButton;
import team.edu.app.muternet.player.PlayerController;
import team.edu.app.muternet.R;

public class PlayerFragment extends Fragment implements PlayerController {
    View view;
    Runnable timer;
    MediaPlayer mediaPlayer;
    TextView fileNameView;
    PlayPauseButton playPauseButton;
    SeekBar seekBar;
    ObjectAnimator animator;
    Uri songUri;

    public PlayerFragment() {
        // Required empty public constructor
    }

    public static PlayerFragment newInstance() {
        PlayerFragment fragment = new PlayerFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_player, container, false);
        //setup
        fileNameView = view.findViewById(R.id.file_name);
        setUpMediaPlayer();
        setUpPlayPauseButton();
        setUpSeekBar();
        return view;
    }

    private void setUpPlayPauseButton() {
        //Set Play Pause Button Color
        playPauseButton = view.findViewById(R.id.play_pause_button);
        playPauseButton.setColor(Color.WHITE);
        final ImageView imageView = view.findViewById(R.id.image_view);
        animator = ObjectAnimator.ofFloat(imageView, "rotation", 0, 360);
        animator.setDuration(10000);
        animator.setRepeatCount(ObjectAnimator.INFINITE);
        animator.setInterpolator(new LinearInterpolator());
        final Parcelable s = playPauseButton.onSaveInstanceState();
        playPauseButton.setOnControlStatusChangeListener(new PlayPauseButton.OnControlStatusChangeListener() {
            @Override
            public void onStatusChange(View view, boolean state) {
                if (state) {
                    if (getArguments() == null || getArguments().getParcelable("musicURI") == null) {
                        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                        alertDialog.setTitle("Alert");
                        alertDialog.setMessage("Please select a music file to play");
                        alertDialog.show();
                        playPauseButton.onRestoreInstanceState(s);
                        return;
                    }

                    if (songUri == null) {
                        songUri = getArguments().getParcelable("musicURI");
                        try {
                            mediaPlayer.setDataSource(getContext(), songUri);
                            mediaPlayer.prepare();
                            fileNameView.setText(getArguments().getString("musicName"));
                            if(seekBar!=null){
                                seekBar.setMax(mediaPlayer.getDuration()/1000);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (!songUri.toString().equals(getArguments().getParcelable("musicURI").toString())) {
                        songUri = getArguments().getParcelable("musicURI");
                        try {
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(getContext(), songUri);
                            mediaPlayer.prepare();
                            fileNameView.setText(getArguments().getString("musicName"));
                            if(seekBar!=null){
                                seekBar.setMax(mediaPlayer.getDuration()/1000);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    float f = imageView.getRotation();
                    animator.setFloatValues(f, f + 360);
                    animator.start();
                    mediaPlayer.seekTo((mediaPlayer.getCurrentPosition()/1000)*1000, MediaPlayer.SEEK_CLOSEST_SYNC);
                    mediaPlayer.start();
                    Log.d("player", mediaPlayer.getCurrentPosition()+"");
                    ((ServerFragment)getFragmentManager().findFragmentByTag("2")).onPlayerPlay();
                } else {
                    animator.pause();
                    mediaPlayer.pause();
                    ((ServerFragment)getFragmentManager().findFragmentByTag("2")).onPlayerPause();
                }
            }
        });
    }

    private void setUpSeekBar() {
        seekBar = view.findViewById(R.id.seek_bar);

        final TextView timestampPassed = view.findViewById(R.id.time_stamp_passed);
        final TextView timestampRemain = view.findViewById(R.id.time_stamp_remain);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress * 1000);
                    ((ServerFragment)getFragmentManager().findFragmentByTag("2")).onPlayerDrag();
                }
                if(!mediaPlayer.isPlaying()){
                    return;
                }
                int duration = mediaPlayer.getDuration() / 1000;
                int currentPosition = mediaPlayer.getCurrentPosition() / 1000;
                String timePassed = (currentPosition / 60 < 10 ? "0" + currentPosition / 60 : currentPosition / 60) + ":"
                        + (currentPosition % 60 < 10 ? "0" + currentPosition % 60 : currentPosition % 60);
                timestampPassed.setText(timePassed);
                String timeRemain = ((duration - currentPosition) / 60 < 10 ? "0" + (duration - currentPosition) / 60 : ((duration - currentPosition) / 60)) + ":"
                        + ((duration - currentPosition) % 60 < 10 ? "0" + (duration - currentPosition) % 60 : (duration - currentPosition) % 60);
                timestampRemain.setText(timeRemain);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekBar.setProgress(0);
        timer = new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null) {
                    int currentPosition = mediaPlayer.getCurrentPosition() / 1000;
                    seekBar.setProgress(currentPosition);
                }else{
                    animator.pause();
                    seekBar.setProgress(0);
                }
                new Handler().postDelayed(timer, 1000);
            }
        };
        timer.run();
    }

    private void setUpMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build());
    }

    public void loadFile(File file) throws IOException {
        mediaPlayer.reset();
        mediaPlayer.setDataSource(getContext(), Uri.fromFile(file));
        mediaPlayer.prepare();
        Log.d("load", "loadFile");
    }


    @Override
    public void play() {
//        animator.start();
        mediaPlayer.start();
    }

    @Override
    public void pause() {
//        animator.pause();
        mediaPlayer.pause();
    }

    @Override
    public int getPosition(){
        return mediaPlayer.getCurrentPosition();
    }

    @Override
    public void seekTo(int t){
        mediaPlayer.seekTo(t, MediaPlayer.SEEK_PREVIOUS_SYNC);
    }

    @Override
    public void load(File file) throws IOException{
        mediaPlayer.reset();
        mediaPlayer.setDataSource(getContext(), Uri.fromFile(file));
        mediaPlayer.prepare();
        Log.d("load", "loadFile");
    }
}
