package Windows;

import sql.DatabaseConnection;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BaigiamujuDarbuVertinimai extends JFrame {
    private final String destytojoId;

    public BaigiamujuDarbuVertinimai(String destytojoId) {
        this.destytojoId = destytojoId;
        setTitle("Studentų Baigiamieji Darbai");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textPane);
        add(scrollPane, BorderLayout.CENTER);

        displayStudentTheses(textPane);
    }

    private void displayStudentTheses(JTextPane textPane) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT CONCAT(s.Vardas, ' ', s.Pavarde) AS Studento_pilnas_vardas, " +
                    "b.Darbo_pavadinimas, " +
                    "b.Vertinimas, " +
                    "b.Baigiamojo_darbo_Id " +
                    "FROM studentas s " +
                    "INNER JOIN baigiamasis_darbas b ON s.Studento_Id = b.Studento_Id " +
                    "WHERE b.Darbo_vadovo_Id = ? " +
                    "ORDER BY s.Vardas, s.Pavarde";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, destytojoId);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<html><body>");
                    while (resultSet.next()) {
                        String studentoPilnasVardas = resultSet.getString("Studento_pilnas_vardas");
                        String darboPavadinimas = resultSet.getString("Darbo_pavadinimas");
                        String vertinimas = resultSet.getString("Vertinimas");
                        int baigiamojoDarboId = resultSet.getInt("Baigiamojo_darbo_Id");
                        sb.append("<b>Studento vardas ir pavardė:</b> ").append(studentoPilnasVardas).append("<br>")
                                .append("<b>Baigiamojo darbo pavadinimas:</b> ").append(darboPavadinimas).append("<br>")
                                .append("<b>Vertinimas:</b> <a href=\"update-").append(baigiamojoDarboId).append("\">").append(vertinimas).append("</a><br><br>");
                    }

                    sb.append("</body></html>");
                    textPane.setContentType("text/html");
                    textPane.setText(sb.toString());

                    textPane.addHyperlinkListener(e -> {
                        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                            String description = e.getDescription();
                            if (description.startsWith("update-")) {
                                int baigiamojoDarboId = Integer.parseInt(description.substring("update-".length()));

                                handleUpdateAction(baigiamojoDarboId);
                            }
                        }
                    });
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Klaida gaunant duomenis: " + ex.getMessage(),
                    "Klaida", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void handleUpdateAction(int baigiamojoDarboId) {

        System.out.println("Updating Vertinimas for baigiamojo darbo with ID: " + baigiamojoDarboId);
        String newVertinimas = JOptionPane.showInputDialog(this, "Įveskite naują vertinimą:");
        if (newVertinimas != null) {
            try (Connection connection = DatabaseConnection.getConnection()) {
                String updateQuery = "UPDATE baigiamasis_darbas SET Vertinimas = ? WHERE Baigiamojo_darbo_Id = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                    preparedStatement.setString(1, newVertinimas);
                    preparedStatement.setInt(2, baigiamojoDarboId);
                    int rowsUpdated = preparedStatement.executeUpdate();
                    if (rowsUpdated > 0) {
                        JOptionPane.showMessageDialog(this, "Vertinimas atnaujintas sėkmingai!", "Sėkmė", JOptionPane.INFORMATION_MESSAGE);
                        displayStudentTheses((JTextPane) getContentPane().getComponent(0));
                    } else {
                        JOptionPane.showMessageDialog(this, "Nepavyko atnaujinti vertinimo.", "Klaida", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Klaida atnaujinant vertinimą: " + ex.getMessage(), "Klaida", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BaigiamujuDarbuVertinimai frame = new BaigiamujuDarbuVertinimai("your_destytojo_id_here");
            frame.setVisible(true);
        });
    }
}
