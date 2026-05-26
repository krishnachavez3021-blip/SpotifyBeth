package com.proyectofinal.spotify;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class LoginFrame extends JFrame {
    private final JTextField email = new JTextField("demo@gmail.com");
    private final JPasswordField password = new JPasswordField("1234");

    public LoginFrame() {
        setTitle("Spotify Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 420);
        setLocationRelativeTo(null);
        setContentPane(content());
    }

    private JPanel content() {
        JPanel root = new JPanel(new BorderLayout(0, 20));
        root.setBackground(new Color(18, 18, 18));
        root.setBorder(new EmptyBorder(34, 42, 34, 42));

        JLabel title = new JLabel("Spotify", SwingConstants.CENTER);
        title.setForeground(new Color(30, 215, 96));
        title.setFont(new Font("Segoe UI", Font.BOLD, 42));
        root.add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(4, 1, 0, 10));
        fields.setOpaque(false);
        fields.add(label("Correo"));
        fields.add(field(email));
        fields.add(label("Contraseña"));
        fields.add(field(password));
        root.add(fields, BorderLayout.CENTER);

        JButton login = new JButton("Entrar");
        login.setBackground(new Color(30, 215, 96));
        login.setForeground(Color.BLACK);
        login.setFont(new Font("Segoe UI", Font.BOLD, 16));
        login.addActionListener(e -> enter());
        root.add(login, BorderLayout.SOUTH);
        return root;
    }

    private JLabel label(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("Segoe UI", Font.BOLD, 14));
        return l;
    }

    private JTextField field(JTextField f) {
        f.setPreferredSize(new Dimension(250, 42));
        f.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        f.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        return f;
    }

    private void enter() {
        if (email.getText().contains("@") && password.getPassword().length > 0) {
            dispose();
            new SpotifyFrame().setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Ingrese correo y contraseña.");
        }
    }
}
