package gh2;

import edu.princeton.cs.algs4.StdAudio;

import javax.sound.midi.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Plays guitar from MIDI files.
 *
 * @author Eli Lipsitz
 */
public class GuitarPlayer {
    private Sequence sequence = null;
    private GuitarString[] strings;
    private double[] vol;

    public GuitarPlayer(InputStream source) {
        try {
            sequence = MidiSystem.getSequence(source);
        } catch (IOException | InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    public GuitarPlayer(File source) {
        try {
            sequence = MidiSystem.getSequence(source);
        } catch (IOException | InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

    private void initialize() {
        strings = new GuitarString[128];
        vol = new double[128];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = new GuitarString(440.0 * Math.pow(2.0, (i - 69.0) / 12.0));
            vol[i] = 0.0;
        }
    }

    private void tic() {
        for (int i = 0; i < strings.length; i++) {
            if (vol[i] > 0.0) {
                strings[i].tic();
            }
        }
    }

    private double sample() {
        double sum = 0.0f;
        for (int i = 0; i < strings.length; i++) {
            sum += vol[i] * strings[i].sample();
        }
        return sum;
    }

    public void play() {
        if (sequence == null) {
            return;
        }

        System.out.println("starting performance...");
        initialize();
        double bpm = 120;
        double samplesPerTick = StdAudio.SAMPLE_RATE * (60.0 / (sequence.getResolution() * bpm));

        Track[] tracks = sequence.getTracks();
        Track track = sequence.createTrack();
        int maxSize = 0;
        int lead = 0;
        for (int i = 0; i < tracks.length; i++) {
            for (int j = 0; j < tracks[i].size(); j++) {
                track.add(tracks[i].get(j));
            }
        }

        long tick = 0;
        for (int i = 0; i < track.size(); i++) {
            MidiEvent event = track.get(i);
            MidiMessage msg = event.getMessage();
            byte[] data = msg.getMessage();

            if (msg instanceof MetaMessage) {
                MetaMessage mm = (MetaMessage) msg;
                if (mm.getType() == 0x51) {
                    // set tempo
                    data = mm.getData();
                    int tempo = (data[0] & 0xff) << 16 | (data[1] & 0xff) << 8 | (data[2] & 0xff);
                    bpm = 60000000.0 / tempo;
                    samplesPerTick = StdAudio.SAMPLE_RATE
                        * (60.0 / (sequence.getResolution() * bpm));
                } else if (mm.getType() == 0x05) {
                    // lyrics
                    data = mm.getData();
                    String lyrics = new String(data);
                    lyrics = lyrics.replace("\r", "\r\n");
                    System.out.print(lyrics);
                }
                continue;
            }

            if (event.getTick() > tick) {
                int samplesToSkip = (int) ((event.getTick() - tick) * samplesPerTick);
                for (int j = 0; j < samplesToSkip; j++) {
                    tic();
                    StdAudio.play(sample());
                }
                tick = event.getTick();
            }

            int j = 0;
            while (j < data.length - 2) {
                int s = data[j++] & 0xFF;

                if (s >= 0x80 && s <= 0x8F) {
                    // note off
                    int note = data[j++] & 0xFF;
                    int vel = data[j++] & 0xFF;
                    vol[note] = 0.0;
                } else if (s >= 0x90 && s <= 0x9F) {
                    // note on?
                    int note = data[j++] & 0xFF;
                    int vel = data[j++] & 0xFF;
                    vol[note] = vel / 127.0;
                    strings[note].pluck();
                } else {
                    // status
                    int d = data[j++] & 0xFF;
                    int d2 = data[j++] & 0xFF;
                }
            }
        }

        System.out.println("please clap");
    }
}
