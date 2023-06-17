package com.example.menu_template;

import android.content.Context;
import android.media.MediaPlayer;

/**
 * This class provides the functionality to play sounds
 */
public class SoundPlayer {
    private MediaPlayer mediaPlayer;

    /**
     * This method plays the selected sound
     * @param context of the file it is called in
     * @param soundResourceId the id of the sound-effect file inside the project
     */
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

    /**
     * This method stops the playing of a sound
     */
    public void stopSoundEffect() {
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
