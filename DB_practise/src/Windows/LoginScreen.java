package Windows;

import sql.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginScreen extends JFrame {
    private JTextField idField;

    private class LoginButtonActionListener implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String destytojoId = idField.getText();
            if (checkCredentials(destytojoId)) {
                JOptionPane.showMessageDialog(LoginScreen.this, "Prisijungimas pavyko! Jūsų ID: " + destytojoId);
                openDestytojoLangas(destytojoId);
            } else {
                JOptionPane.showMessageDialog(LoginScreen.this, "Nepavyko prisijungti. Toks ID nerastas.");
            }
            idField.setText("");
        }
    }

    public LoginScreen() {
        setTitle("Login Screen");
        setSize(300, 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initializeComponents();
        setLayout(new GridLayout(3, 1));

        add(new JLabel("Dėstytojas ID:"));
        add(idField);

        JButton loginButton = new JButton("Login");
        loginButton.addActionListener(new LoginButtonActionListener());
        add(loginButton);

        setVisible(true);
    }

    private void initializeComponents() {
        idField = new JTextField();
    }

    private boolean checkCredentials(String destytojoId) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM destytojas WHERE Destytojo_Id = ?";
            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, destytojoId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    return resultSet.next();
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private void openDestytojoLangas(String destytojoId) {
        JFrame destytojoLangasFrame = new JFrame("Dėstytojo Langas");
        destytojoLangasFrame.setSize(500, 400);
        destytojoLangasFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        destytojoLangasFrame.add(new JScrollPane(textArea), BorderLayout.CENTER);

        JButton button1 = new JButton("Studentų baigiamųjų darbų statusai");
        button1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                BendriejiStudentuBaigiamujuStatusai statusai = new BendriejiStudentuBaigiamujuStatusai(destytojoId);
                statusai.setVisible(true);
            }
        });

        JButton button2 = new JButton("Tvarkyti baigiamojo darbo įvertinimą");
        button2.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                BaigiamujuDarbuVertinimai darbai = new BaigiamujuDarbuVertinimai(destytojoId);
                darbai.setVisible(true);
            }
        });

        JButton button3 = new JButton("Tvarkyti praktikos informaciją");
        button3.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                PraktikosValdymas valdymas = new PraktikosValdymas(destytojoId);
                valdymas.setVisible(true);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.add(button1);
        buttonPanel.add(button2);
        buttonPanel.add(button3);

        destytojoLangasFrame.add(buttonPanel, BorderLayout.NORTH);

        destytojoLangasFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginScreen());
    }
}
