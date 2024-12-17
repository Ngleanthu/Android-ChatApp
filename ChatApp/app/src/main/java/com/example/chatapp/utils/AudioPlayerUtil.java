package com.example.chatapp.utils;
import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.chatapp.R;

import java.io.IOException;

public class AudioPlayerUtil {

    private MediaPlayer mediaPlayer;
    private int duration;
    private String audioUrl;
    ImageButton playButton;
    SeekBar seekBar;

    public SeekBar getSeekBar() {
        return seekBar;
    }

    public void setSeekBar(SeekBar seekBar) {
        this.seekBar = seekBar;
    }

    public ImageButton getPlayButton() {
        return playButton;
    }

    public void setPlayButton(ImageButton playButton) {
        this.playButton = playButton;
    }

    public interface OnAudioStateChangeListener {
        void onAudioStart();
        void onAudioStop();
    }

    private OnAudioStateChangeListener stateChangeListener;

    public AudioPlayerUtil(OnAudioStateChangeListener stateChangeListener) {
        this.stateChangeListener = stateChangeListener;
    }

    public void addAudioPlayerToLayout(LinearLayout parentLayout, String audioUrl, Context context) {
        this.audioUrl = audioUrl;
        PlaybackManager.getInstance().stopActivePlayback();
        // Create a horizontal LinearLayout to hold the audio button and SeekBar
        LinearLayout audioLayout = new LinearLayout(context);
        audioLayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams audioLayoutParams = new LinearLayout.LayoutParams(
                500,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        audioLayout.setLayoutParams(audioLayoutParams);

        // Create the ImageButton (audio play button)
        playButton = new ImageButton(context);
        playButton.setLayoutParams(new LinearLayout.LayoutParams(50, 50));
        seekBar = new SeekBar(context);
        seekBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1
        ));

        configureUI(audioLayout, parentLayout, playButton, seekBar);

        // Set up click listener for the play button
        playButton.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                stopAudio(seekBar, playButton);
            } else {
                PlaybackManager.getInstance().stopActivePlayback(); // Stop any currently playing audio
                playAudio(audioUrl, seekBar, playButton, context);
            }
        });

        // Set up SeekBar listener
        setupSeekBarListener(seekBar, playButton);
    }

    private void playAudio(String audioUrl, SeekBar seekBar, ImageButton playButton, Context context) {
        try {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.setOnPreparedListener(mp -> {
                mediaPlayer.start();
                duration = mediaPlayer.getDuration();
                playButton.setImageResource(R.drawable.baseline_pause_24);
                if (stateChangeListener != null) {
                    stateChangeListener.onAudioStart();
                }
                PlaybackManager.getInstance().setActivePlayer(this); // Register this instance as active
                updateSeekBar(seekBar);
            });
            mediaPlayer.setOnCompletionListener(mp -> stopAudio(seekBar, playButton));
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            Toast.makeText(context, "Failed to play audio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void stopAudio(SeekBar seekBar, ImageButton playButton) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            playButton.setImageResource(R.drawable.baseline_play_arrow_24);
            seekBar.setProgress(0);
        }
        if (stateChangeListener != null) {
            stateChangeListener.onAudioStop();
        }
    }

    private void setupSeekBarListener(SeekBar seekBar, ImageButton playButton) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    int playPositionInMilliseconds = (duration / 100) * progress;
                    mediaPlayer.seekTo(playPositionInMilliseconds);
                }
                if (progress >= 100) {
                    playButton.setImageResource(R.drawable.baseline_play_arrow_24);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void configureUI(LinearLayout audioLayout, LinearLayout parentLayout, ImageButton playButton, SeekBar seekBar) {
        // Set up layout parameters and add components
        playButton.setImageResource(R.drawable.baseline_play_arrow_24);
        playButton.setBackground(null);
        audioLayout.addView(playButton);

        seekBar.setMax(100);
        audioLayout.addView(seekBar);

        parentLayout.addView(audioLayout);
    }

    private void updateSeekBar(SeekBar seekBar) {
        new Thread(() -> {
            while (mediaPlayer != null && mediaPlayer.isPlaying()) {
                try {
                    if (seekBar != null) {
                        seekBar.setProgress((int) (((float) mediaPlayer.getCurrentPosition() / duration) * 100));
                    }
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
