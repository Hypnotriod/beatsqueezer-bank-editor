package com.hypnotriod.beatsqueezereditor.tools;

import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;

/**
 *
 * @author Ilya Pikin
 */
public class RawPCMDataPlayer {

    public static final AudioFormat AUDIO_FORMAT_44_16_STEREO = new AudioFormat(44100, 16, 2, true, false);

    private static AudioInputStream audioStream = null;
    private static Clip clip = null;
    private static int loop = 0;

    public static void play(AudioInputStream audioInputStream, int loopStart, int loopEnd, float pan, double position, LineListener lineListener) throws IOException, LineUnavailableException {
        stop();

        audioStream = audioInputStream;

        if (clip == null) {
            clip = (Clip) AudioSystem.getClip();
            clip.addLineListener(lineListener);
        }
        audioInputStream.reset();
        clip.open(audioInputStream);

        if (loopEnd != 0 && loopStart < loopEnd) {
            clip.setLoopPoints(loopStart, loopEnd);
            clip.loop(Clip.LOOP_CONTINUOUSLY);
            loop = loopStart;
        } else {
            loop = 0;
        }

        if (clip.isControlSupported(FloatControl.Type.PAN)) {
            FloatControl panControl = (FloatControl) clip.getControl(FloatControl.Type.PAN);
            panControl.setValue(pan);
        }

        clip.setFramePosition((int) (position * clip.getFrameLength()));
        clip.start();
    }

    public static void stop() {
        if (clip != null && clip.isOpen()) {
            clip.stop();
            clip.close();
            clip.drain();
        }

        if (audioStream != null) {
            try {
                audioStream.reset();
                audioStream.close();
                audioStream = null;
            } catch (IOException ex) {
            }
        }
    }

    public static int getFramePosition() {
        if (clip.getFramePosition() > clip.getFrameLength()) {
            return loop + (clip.getFramePosition() - clip.getFrameLength()) % (clip.getFrameLength() - loop);
        } else {
            return clip.getFramePosition();
        }
    }
}