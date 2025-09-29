package com.sahabatgula;

import com.formdev.flatlaf.FlatLightLaf;
import com.sahabatgula.ui.MainFrame;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        FlatLightLaf.setup();
        SwingUtilities.invokeLater(() -> new MainFrame().setVisible(true));
    }
}
