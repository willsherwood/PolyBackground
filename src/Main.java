import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.win32.W32APIOptions;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.*;

/**
 * Created by Will on 11/16/2014.
 */
public class Main {

    public static File[] files;
    public static Thread thread;
    public static int delay;

    public static void main (String[] args) throws IOException {
        JFrame frame = new JFrame();
        frame.setTitle("Poly|Background >> Sherwood");
        JPanel panel = new JPanel();
        JTextField delay = new JTextField("100");
        ((AbstractDocument) delay.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString (FilterBypass fb, int offset, String text, AttributeSet attrs) throws BadLocationException {
                fb.insertString(offset, text.replaceAll("\\D++", ""), attrs);
            }
            @Override
            public void replace (FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                fb.replace(offset, length, text.replaceAll("\\D++", ""), attrs);
            }
        });
        delay.setPreferredSize(new Dimension(delay.getPreferredSize().width * 4, delay.getPreferredSize().height));
        JLabel label = new JLabel("Delay (ms)");
        panel.add(label);
        panel.add(delay);
        new FileDrop(panel, files -> {
            Main.files = files[0].listFiles();
        });
        frame.getContentPane().add(panel);
        JButton start = new JButton("start");
        JButton stop = new JButton("stop");
        start.addActionListener((e) -> {
            if (thread != null) {
                thread.stop();
            }
            if (files != null) {
                Main.delay = Integer.parseInt(delay.getText());
                thread = getNewThread();
                thread.start();
            } else {
                JOptionPane.showMessageDialog(panel, "Please drag a folder with pictures in it first");
            }
        });
        stop.addActionListener((e) -> {
            if (thread != null)
                thread.stop();
        });
        panel.add(start);
        panel.add(stop);
        frame.pack();
        frame.setAlwaysOnTop(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    public static Thread getNewThread () {
        return new Thread(() -> {
            try {
                while (true) {
                    long t1 = System.currentTimeMillis();
                    setBackground(files[(int) (files.length * Math.random())].getAbsolutePath());
                    int sleep = (int) (-System.currentTimeMillis() + t1 + Main.delay);
                    if (sleep > 0)
                        Thread.sleep(sleep);
                }
            } catch (InterruptedException e) {
                System.out.println("interrupted exception: " + e);
            }
        });
    }

    public static void setBackground (String path) {
        User32.INSTANCE.SystemParametersInfo(0x0014, 0, path, 1);
    }

    public static interface User32 extends Library {
        User32 INSTANCE = (User32) Native.loadLibrary("user32", User32.class, W32APIOptions.DEFAULT_OPTIONS);

        boolean SystemParametersInfo (int one, int two, String s, int three);
    }
}
