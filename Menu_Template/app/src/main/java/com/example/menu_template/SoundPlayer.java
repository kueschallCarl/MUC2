package com.example.menu_template;

import android.content.Context;
import android.media.MediaPlayer;

public class SoundPlayer {
    private MediaPlayer mediaPlayer;

    public void playSoundEffect(Context context, int soundResourceId) {
        stopSoundEffect(); // Stop any previously playing sound effect

        mediaPlayer = MediaPlayer.create(context, soundResourceId);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // Release the MediaPlayer resources after playback is complete
                stopSoundEffect();
            }
        });

        mediaPlayer.start();
    }

    public void stopSoundEffect() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
