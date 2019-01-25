package ru.otus.java32;

import com.google.common.collect.Iterables;
import com.google.common.primitives.Bytes;
import com.google.common.primitives.Chars;

import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

class Transmitter {

    private static final int SPEED = 20; // words per second
    private static final int FREQ = 600; // Hertz
    private static final int SAMPLE_RATE = 22050; // samples per second
    /* Based upon a 50 dot duration standard word such as PARIS, the time for one dot duration or one unit can
     * be computed by the formula:
     * dotDurationMilliseconds = 1200.0 / SPEED
     * https://en.wikipedia.org/wiki/Morse_code
     */
    private static final double DOT_DURATION_MILLISECONDS = 1200.0 / SPEED;

    private final byte[] dotWave;
    private final byte[] dashWave;
    private final byte[] spaceWave;
    private static final Character dot = '.';
    private static final Character dash = '-';
    private static final Character shortSpace = '|';

    private static Logger logger = Logger.getLogger(Transmitter.class.getSimpleName());

    private Clip clip;

    Transmitter() {
        dotWave = generateSineWave(DOT_DURATION_MILLISECONDS);
        dashWave = generateSineWave(DOT_DURATION_MILLISECONDS * 3);
        spaceWave = generatePause(DOT_DURATION_MILLISECONDS);
    }

    void transmit(String morseEncoded) {
        byte[] outputData = Bytes.concat(
                Iterables.toArray(
                        Iterables.transform(
                                Chars.asList(morseEncoded.toCharArray()), c -> {
                                    if (dot.equals(c)) return Bytes.concat(dotWave, spaceWave);
                                    if (dash.equals(c)) return Bytes.concat(dashWave, spaceWave);
                                    if (shortSpace.equals(c)) return Bytes.concat(spaceWave, spaceWave);
                                    else return Bytes.concat(spaceWave, spaceWave, spaceWave);
                                }), byte[].class));

        try {
            transmitData(outputData);
        } catch (LineUnavailableException | IOException | InterruptedException e) {
            logger.log(Level.SEVERE, "Can't play sound", e);
            throw new RuntimeException(e);
        }
    }

    private void transmitData(byte[] outputData) throws LineUnavailableException, IOException, InterruptedException {
        if (clip != null) {
            clip.stop();
            clip.close();
        } else {
            clip = AudioSystem.getClip();
        }

        Object playSync = new Object();

        LineListener listener = event -> {
            if (event.getType() == LineEvent.Type.STOP) {
                clip.stop();
                clip.close();
                clip = null;
                logger.log(Level.INFO, "Data has been transmitted.");
                synchronized (playSync) {
                    playSync.notifyAll();
                }
            }
        };
        clip.addLineListener(listener);

        AudioFormat af = new AudioFormat(
                SAMPLE_RATE,
                8,
                1,
                true,
                false
        );

        AudioInputStream ais = new AudioInputStream(
                new ByteArrayInputStream(outputData),
                af,
                outputData.length);

        clip.open(ais);
        logger.log(Level.INFO, "Start data transmitting...");
        clip.start();
        synchronized (playSync) {
            playSync.wait();
        }
    }

    private static byte[] generateSineWave(double durationMilliseconds) {
        int len = getNumOfSamples(durationMilliseconds);
        byte[] result = new byte[len];
        for (int n = 0; n < len; n++) {
            result[n] = (byte) (Byte.MAX_VALUE * Math.sin(n * 2 * Math.PI * FREQ / SAMPLE_RATE));
        }
        return result;
    }

    private static int getNumOfSamples(double durationMilliseconds) {
        return (int) Math.round(SAMPLE_RATE * durationMilliseconds / 1000.0);
    }

    private static byte[] generatePause(double durationMilliseconds) {
        return new byte[getNumOfSamples(durationMilliseconds)];
    }
}
