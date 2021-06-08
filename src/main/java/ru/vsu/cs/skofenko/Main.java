package ru.vsu.cs.skofenko;

import java.awt.*;

public class Main {

    public static void main(String[] args) {
        ru.vsu.cs.util.SwingUtils.setDefaultFont("Arial", 20);
        EventQueue.invokeLater(() -> {
            MainForm form = new MainForm();
            form.setVisible(true);
        });

    }
}