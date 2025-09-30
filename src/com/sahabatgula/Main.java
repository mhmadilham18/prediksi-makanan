package com.sahabatgula;

import com.sahabatgula.ui.MainFrame;
import com.sahabatgula.ui.SplashScreenPanel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            final JWindow splashWindow = new JWindow();
            splashWindow.add(new SplashScreenPanel());
            splashWindow.pack();
            splashWindow.setLocationRelativeTo(null);
            splashWindow.setVisible(true);

            Timer timer = new Timer(3000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    splashWindow.dispose();

                    new MainFrame().setVisible(true);
                }
            });

            timer.setRepeats(false);
            timer.start();
        });
    }
}