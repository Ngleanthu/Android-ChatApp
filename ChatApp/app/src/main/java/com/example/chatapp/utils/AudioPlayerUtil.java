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

    private static AudioPlayerUtil instance;
    private MediaPlayer mediaPlayer;
    private int duration;
    private String currentAudioUrl;

    public interface OnAudioStateChangeListener {
        void onAudioStart();
        void onAudioStop();
    }

    private OnAudioStateChangeListener stateChangeListener;

    public void setOnAudioStateChangeListener(OnAudioStateChangeListener listener) {
        this.stateChangeListener = listener;
    }
    // Private constructor to enforce Singleton
    private AudioPlayerUtil() {}

    // Get Singleton instance
    public static synchronized AudioPlayerUtil getInstance() {
        if (instance == null) {
            instance = new AudioPlayerUtil();
        }
        return instance;
    }

    public void addAudioPlayerToLayout(LinearLayout parentLayout, String audioUrl, Context context) {
        if (audioUrl == null || audioUrl.isEmpty()) {
            Log.e("AudioPlayerUtil", "Invalid audio URL.");
            return;
        }

        // Create a horizontal LinearLayout to hold the audio button and SeekBar
        LinearLayout audioLayout = new LinearLayout(context);
        audioLayout.setOrientation(LinearLayout.HORIZONTAL);

        // Set layout parameters for the audio layout
        LinearLayout.LayoutParams audioLayoutParams = new LinearLayout.LayoutParams(
                500,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        audioLayout.setLayoutParams(audioLayoutParams);

        // Create the ImageButton (audio play button)
        ImageButton audioPlayButton = new ImageButton(context);
        audioPlayButton.setLayoutParams(new LinearLayout.LayoutParams(50, 50));
        audioPlayButton.setImageResource(R.drawable.baseline_play_arrow_24);
        audioPlayButton.setContentDescription("Play Audio");
        audioPlayButton.setBackground(null);

        // Create the SeekBar
        SeekBar seekBar = new SeekBar(context);
        seekBar.setMax(100);
        seekBar.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT, 1
        ));

        // Add the audio button and SeekBar to the audio layout
        audioLayout.addView(audioPlayButton);
        audioLayout.addView(seekBar);

        // Add the audio layout to the parent layout
        parentLayout.addView(audioLayout);

        // Handle play/pause button click
        audioPlayButton.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying() && currentAudioUrl != null && currentAudioUrl.equals(audioUrl)) {
                stopAudio(audioPlayButton, seekBar);
            } else {
                playAudio(audioUrl, seekBar, audioPlayButton, context);
            }
        });

        // Handle SeekBar interactions
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    int playPositionInMilliseconds = (duration / 100) * progress;
                    mediaPlayer.seekTo(playPositionInMilliseconds);
                }
                if (progress >= 100) {
                    audioPlayButton.setImageResource(R.drawable.baseline_play_arrow_24);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private void playAudio(String audioUrl, SeekBar seekBar, ImageButton playButton, Context context) {
        if (audioUrl == null || audioUrl.isEmpty()) {
            Toast.makeText(context, "Invalid audio URL", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Stop and reset if another audio is playing
            if (mediaPlayer != null && mediaPlayer.isPlaying() && !audioUrl.equals(currentAudioUrl)) {
                stopAudio(playButton, seekBar);
            }

            if (mediaPlayer == null) {
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setOnCompletionListener(mp -> stopAudio(playButton, seekBar));
            }

            // Set the new data source if the URL has changed
            if (!audioUrl.equals(currentAudioUrl)) {
                mediaPlayer.reset();
                mediaPlayer.setDataSource(audioUrl);
                currentAudioUrl = audioUrl;
                mediaPlayer.setOnPreparedListener(mp -> {
                    mediaPlayer.start();
                    duration = mediaPlayer.getDuration();
                    playButton.setImageResource(R.drawable.baseline_pause_24);
                    if (stateChangeListener != null) {
                        stateChangeListener.onAudioStart();
                    }
                    updateSeekBar(seekBar);
                });
                mediaPlayer.prepareAsync();
            } else {
                // Resume playback for the same audio
                mediaPlayer.start();
                playButton.setImageResource(R.drawable.baseline_pause_24);
                if (stateChangeListener != null) {
                    stateChangeListener.onAudioStart();
                }
            }
        } catch (IOException e) {
            Toast.makeText(context, "Failed to play audio: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void stopAudio(ImageButton playButton, SeekBar seekBar) {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
        playButton.setImageResource(R.drawable.baseline_play_arrow_24);
        if (seekBar != null) seekBar.setProgress(0);
        if (stateChangeListener != null) {
            stateChangeListener.onAudioStop();
        }
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