package com.beatbox;


import javax.sound.midi.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;

public class BeatBox {
    JFrame frame;
    JPanel background;

    ArrayList<JCheckBox> checkboxList;

    Sequencer player;
    Track track;
    Sequence seq;


    String[] instrumentNames = {"Bass Drum", "Closed Hi-Hat", "Open Hi-Hat",
            "Acoustic Snare", "Crash Cymbal", "Hand Clap", "High Tom", "Hi Bongo",
            "Maracas", "Whistle", "Low Conga", "Cowbell", "Vibraslap", "Low-mid Tom",
            "High Agogo", "Open Hi Conga"};

    int [] instruments = {35, 42, 46, 38, 49, 39, 50, 60, 70, 72, 64, 56, 58,
            47, 67, 63};


    public static void main(String[] args) {
        BeatBox beatBox = new BeatBox();
        beatBox.buildGui();
    }

    public void buildGui() {

        frame = new JFrame("RhythmBox");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setVisible(true);


        BorderLayout borderLayout = new BorderLayout();
        background = new JPanel(borderLayout);
        background.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        Box buttonBox = new Box(BoxLayout.Y_AXIS);

        JButton startButton = new JButton("Start");
        buttonBox.add(startButton);
        startButton.addActionListener(new StartButtonListener());

        JButton stopButton = new JButton("Stop");
        buttonBox.add(stopButton);
        stopButton.addActionListener(new StopButtonListener());

        JButton tempoUpButton = new JButton("Tempo Up");
        buttonBox.add(tempoUpButton);
        tempoUpButton.addActionListener(new TempoUpButtonListener());

        JButton tempoDownButton = new JButton("Tempo Down");
        buttonBox.add(tempoDownButton);
        tempoDownButton.addActionListener(new TempoDownActionListener());

        JButton serializeIt = new JButton("Save");
        buttonBox.add(serializeIt);
        serializeIt.addActionListener(new SerializeItActionListener());

        JButton restoreBeats = new JButton("Upload");
        buttonBox.add(restoreBeats);
        restoreBeats.addActionListener(new RestoreBeatActionListener());

        JButton reset = new JButton("Reset");
        buttonBox.add(reset);
        reset.addActionListener(new resetButtonListener());

        Box nameBox = new Box(BoxLayout.Y_AXIS);

        for(int i=0; i<instrumentNames.length; i++) {
            nameBox.add(new Label(instrumentNames[i]));
        }

        GridLayout grid = new GridLayout(16, 16);
        grid.setHgap(2);
        grid.setVgap(1);
        JPanel mainPanel;

        mainPanel = new JPanel(grid);
//        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        checkboxList = new ArrayList<JCheckBox>();
        background.add(BorderLayout.CENTER, mainPanel);

        for (int i=0; i<256; i++) {

            JCheckBox checkBox = new JCheckBox();
            checkBox.setSelected(false);
            mainPanel.add(checkBox);
            checkboxList.add(checkBox);

        }
        background.add(BorderLayout.WEST, nameBox);
        background.add(BorderLayout.EAST, buttonBox);
        frame.getContentPane().add(background);
        frame.setBounds(700, 300, 500, 500);
        frame.pack();
    }

    public void captureBeatAndKey() {
        int[] capturedBeats = null;
        seq.deleteTrack(track);
        track = seq.createTrack();

        for(int i=0; i<16; i++) {
            capturedBeats = new int[16];
            int key = instruments[i];

            for(int j=0; j<16; j++) {

                JCheckBox c = checkboxList.get( (16*i) +j);
                if (c.isSelected()) {
                    capturedBeats[j] = key ;
                } else {
                    capturedBeats[j] = 0;
                }
            }
            makeTrack(capturedBeats);
            track.add(makeEvent(176, 1, 127, 0, 16));
        }

        try {
            player.setSequence(seq);
        } catch (Exception e) {
            e.printStackTrace();
        }

        player.start();
    }

    public void setUpSequencer() {
        try {
            player = MidiSystem.getSequencer();
            player.open();

            seq = new Sequence(Sequence.PPQ, 4);
            track = seq.createTrack();
            player.setTempoInBPM(120);
//            player.setLoopStartCount(player.LOOP_CONTINUOUSLY);
            player.setLoopCount(100);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void makeTrack(int [] capturedBeats) {

        for(int i=0; i<16; i++) {
            if(capturedBeats[i] != 0) {
                track.add(makeEvent(144, 9, capturedBeats[i], 100, i ));
                track.add(makeEvent(128, 9, capturedBeats[i], 100, i+1 ));

            }
        }
    }


    public MidiEvent makeEvent(int command, int channel, int key, int velocity, int tick) {
        ShortMessage a = new ShortMessage();
        MidiEvent event = null;
        try {
            a.setMessage(command, channel, key, velocity);
            event = new MidiEvent(a, tick);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return event;
    }

    public class StartButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            setUpSequencer();
            captureBeatAndKey();
        }
    }

    public class StopButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event){
            player.stop();
        }
    }

    public class TempoUpButtonListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            float tempo = player.getTempoFactor();
            player.setTempoFactor((float) (tempo*1.3));
        }
    }

    public class TempoDownActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {
            float tempo = player.getTempoFactor();
            player.setTempoFactor((float)(tempo*0.95));
        }
    }

    public class SerializeItActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {

            boolean[] checkboxState = new boolean[256];

            for (int i=0; i<256; i++) {
                if (checkboxList.get(i).isSelected()) {
                    checkboxState[i] = true;
                } else {
                    checkboxState[i] = false;
                }
            }

            try {
                JFileChooser fileSave = new JFileChooser();
                fileSave.showSaveDialog(frame);
                File savedBeats = fileSave.getSelectedFile();
                FileOutputStream fileOutputStream = new FileOutputStream(savedBeats);
                ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
                objectOutputStream.writeObject(checkboxState);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public class RestoreBeatActionListener implements ActionListener {
        public void actionPerformed(ActionEvent event) {

            boolean[] checkboxState = null;

            try {
                JFileChooser chooseFile = new JFileChooser();
                chooseFile.showOpenDialog(frame);
                File chosenBeats = chooseFile.getSelectedFile();
                FileInputStream fileInputStream = new FileInputStream(chosenBeats);
                ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
                checkboxState = (boolean[]) objectInputStream.readObject();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            JCheckBox checkBox = null;

            for (int i=0; i<256; i++) {

                checkBox = checkboxList.get(i);

                if (checkboxState[i] == true ) {
                    checkBox.setSelected(true);
                } else {
                    checkBox.setSelected(false);
                }
            }

        }
    }

    public class resetButtonListener implements ActionListener{
        public void actionPerformed(ActionEvent event) {

            for(int i=0; i<256; i++) {
                JCheckBox checkBox = checkboxList.get(i);

                if (checkBox.isSelected()) {
                    checkBox.setSelected(false);
                }
            }
            player.stop();
        }
    }
}
