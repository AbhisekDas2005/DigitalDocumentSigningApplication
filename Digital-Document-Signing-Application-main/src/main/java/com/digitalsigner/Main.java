package com.digitalsigner;

import com.digitalsigner.ui.MainWindow;

import javax.swing.*;

/**
 * Entry point for the Digital Document Signing Application.
 */
public class Main {

    public static void main(String[] args) {
        // Apply system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set system look and feel: " + e.getMessage());
        }

        // Launch GUI on Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}
