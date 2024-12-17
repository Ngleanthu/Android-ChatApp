package com.example.chatapp.utils;

public class PlaybackManager {

    private static PlaybackManager instance;
    private AudioPlayerUtil activePlayer;

    private PlaybackManager() {}

    public static synchronized PlaybackManager getInstance() {
        if (instance == null) {
            instance = new PlaybackManager();
        }
        return instance;
    }

    public void setActivePlayer(AudioPlayerUtil player) {
        if (activePlayer != null && activePlayer != player) {
            activePlayer.stopAudio(activePlayer.getSeekBar(), activePlayer.getPlayButton());
        }
        activePlayer = player;
    }

    public void stopActivePlayback() {
        if (activePlayer != null) {
            activePlayer.stopAudio(activePlayer.getSeekBar(), activePlayer.getPlayButton());
            activePlayer = null;
        }
    }
}
