package com.sahabatgula.ui;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class SplashScreenPanel extends JPanel {

    public SplashScreenPanel() {
        setLayout(new BorderLayout());
        setOpaque(true); 
        setBackground(new Color(30, 30, 30));

        ImageIcon logoIcon = null;
        try {
            logoIcon = new ImageIcon(Objects.requireNonNull(getClass().getResource("/resource/icons/logo.png")));
        } catch (Exception e) {
            System.err.println("Logo aplikasi tidak ditemukan di");
            e.printStackTrace();
        }

        if (logoIcon != null) {
            JLabel logoLabel = new JLabel(logoIcon);
            logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
            logoLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
            add(logoLabel, BorderLayout.CENTER);
        }
    }
}