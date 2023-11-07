import javax.swing.*;

import ws.schild.jave.Encoder;
import ws.schild.jave.EncoderException;
import ws.schild.jave.InputFormatException;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.*;
import ws.schild.jave.encode.enums.X264_PROFILE;
import ws.schild.jave.info.MultimediaInfo;
import ws.schild.jave.progress.EncoderProgressListener;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class VideoConverterApp extends JFrame {
    private JButton convertButton;

    public VideoConverterApp() {
        setTitle("Video Converter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        convertButton = new JButton("Convert in MP4");

        convertButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO: Implementare la logica per la conversione dei file in MP4
                JFileChooser fileChooser = new JFileChooser(System.getProperty("user.home") + File.separator +"Desktop");
                fileChooser.setMultiSelectionEnabled(true);
                fileChooser.setDialogTitle("Select video");

                int result = fileChooser.showOpenDialog(VideoConverterApp.this);
                if (result == JFileChooser.APPROVE_OPTION) {
                    File[] selectedFiles = fileChooser.getSelectedFiles();
                    JFileChooser folderChooser = new JFileChooser(System.getProperty("user.home") + File.separator +"Desktop/Projects/ClubOlimpia/Finali");
                        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                        folderChooser.setDialogTitle("Select folder");

                    int result1 = folderChooser.showOpenDialog(VideoConverterApp.this);
                    for (File file : selectedFiles) {
                        
                        final File target;
                        if (result1 == JFileChooser.APPROVE_OPTION) {
                            target = new File(folderChooser.getSelectedFile(), file.getName().replaceFirst("[.][^.]+$", ".mp4"));
                        }else{
                            target = new File(file.getName().replaceFirst("[.][^.]+$", ".mp4"));
                        }
                        MultimediaObject source = new MultimediaObject(file);

                        /* Step 2. Set Audio Attrributes for conversion*/
                        AudioAttributes audio = new AudioAttributes();
                        audio.setCodec("aac");
                        // here 64kbit/s is 64000
                        audio.setBitRate(64000);
                        audio.setChannels(2);
                        audio.setSamplingRate(44100);

                        /* Step 3. Set Video Attributes for conversion*/
                        MultimediaInfo info = null;
                        try {
                            info = source.getInfo();
                        } catch (InputFormatException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        } catch (EncoderException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }

                        VideoAttributes video = new VideoAttributes();
                        video.setCodec("h264");
                        video.setX264Profile(X264_PROFILE.BASELINE);
                        // Here 160 kbps video is 160000
                        video.setBitRate((int)(((float)info.getVideo().getBitRate())*1.3));
                        // More the frames more quality and size, but keep it low based on devices like mobile
                        video.setFrameRate((int) info.getVideo().getFrameRate());
                        video.setSize(info.getVideo().getSize());

                        /* Step 4. Set Encoding Attributes*/
                        EncodingAttributes attrs = new EncodingAttributes();
                        attrs.setOutputFormat("mp4");
                        attrs.setAudioAttributes(audio);
                        attrs.setVideoAttributes(video);

                        /* Step 5. Do the Encoding*/
                        Encoder encoder = new Encoder();
                        Runnable task = () -> {                                                           
                            // Creazione della finestra e della barra di caricamento
                            JFrame frame = new JFrame("Encoder Progress");
                            JProgressBar progressBar = new JProgressBar(0, 100);
                            progressBar.setStringPainted(true);
                            frame.getContentPane().add(progressBar, BorderLayout.CENTER);
                            frame.setSize(300, 100);
                            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                            frame.setLocation((int) (Math.random() * Toolkit.getDefaultToolkit().getScreenSize().getWidth() - 300), (int) (Math.random() * Toolkit.getDefaultToolkit().getScreenSize().getHeight() - 70));
                            frame.setVisible(true);

                            boolean success = false; int attempts = 0, maxAttempts = 10;
                            while (!success && attempts < maxAttempts) {
                                try {
                                    // Esecuzione dell'encoding
                                    encoder.encode(source, target, attrs, new EncoderProgressListener() {
                                        @Override
                                        public void progress(int permil) {
                                            // Aggiornamento della barra di caricamento
                                            SwingUtilities.invokeLater(() -> {
                                                progressBar.setValue(permil / 10);
                                                progressBar.setString(permil / 10 + "%");
                                            });
                                        }

                                        @Override
                                        public void sourceInfo(MultimediaInfo multimediaObjectInfo) {
                                            // Ottieni le informazioni sul sorgente
                                            long duration = multimediaObjectInfo.getDuration();
                                            int videoWidth = multimediaObjectInfo.getVideo().getSize().getWidth();
                                            int videoHeight = multimediaObjectInfo.getVideo().getSize().getHeight();

                                            // Visualizza le informazioni sul sorgente
                                            System.out.println("Duration: " + duration + " ms");
                                            System.out.println("Dimensions: " + videoWidth + "x" + videoHeight);
                                        }

                                        @Override
                                        public void message(String message) {
                                            // TODO Auto-generated method stub
                                            throw new UnsupportedOperationException("Unimplemented method 'message'");
                                        }
                                    });
                                    success = true;
                                } catch (Exception ex) {
                                    // Gestione degli errori durante l'encoding
                                    ex.printStackTrace();
                                    attempts++;
                                }
                            }

                            // Chiusura della finestra della barra di caricamento
                            frame.dispose();                                                                            
                        };                                                                               
                                                                                  
                        Thread thread = new Thread(task);
                        thread.start();
                    }
                }
            }
        });

        add(convertButton);

        setSize(200, 70);
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        VideoConverterApp app = new VideoConverterApp();
        app.setVisible(true);
    }
}