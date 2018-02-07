package com.teslyuk.android.sensors.helper;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.teslyuk.android.sensors.C;
import com.teslyuk.android.sensors.R;

import java.util.HashMap;

/**
 * Created by teslyuk.taras on 2/7/18.
 */

public class SoundEffectHelper {

    private SoundPool soundPool;
    private HashMap<Integer, Integer> soundPoolMap;
    private int soundID = 1;

    public SoundEffectHelper(Context context) {
        initSoundPool(context);
    }

    private void initSoundPool(Context context) {
        if (!C.SOUND_EFFECTS_ENABLE) {
            return;
        }
        soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
        soundPoolMap = new HashMap<Integer, Integer>();
        soundPoolMap.put(soundID, soundPool.load(context, R.raw.pumpkin_break_01_0, 1));
    }

    public void playSoundEffect(Context context) {
        if (!C.SOUND_EFFECTS_ENABLE) {
            return;
        }
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        float curVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float leftVolume = curVolume / maxVolume;
        float rightVolume = curVolume / maxVolume;
        int priority = 1;
        int no_loop = 0;
        float normal_playback_rate = 1f;
        soundPool.play(soundID, leftVolume, rightVolume, priority, no_loop, normal_playback_rate);
    }
}
