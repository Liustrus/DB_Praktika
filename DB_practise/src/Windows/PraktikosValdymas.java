package Windows;

import sql.DatabaseConnection;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import java.awt.*;
import java.sql.*;

public class PraktikosValdymas extends JFrame {
    private final String destytojasId;

    public PraktikosValdymas(String destytojasId) {
        this.destytojasId = destytojasId; // Initialize the destytojas ID
        setTitle("Praktikos Valdymas");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textPane);
        add(scrollPane, BorderLayout.CENTER);

        displayInternshipDetails(textPane);

        setVisible(true);
    }

    private void displayInternshipDetails(JTextPane textPane) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT CONCAT(s.Vardas, ' ', s.Pavarde) AS Studento_pilnas_vardas, " +
                    "       pi.Praktikos_Id, " +
                    "       pi.Pradzios_data, " +
                    "       pi.Pabaigos_data, " +
                    "       pv.Vertinimo_data, " +
                    "       pv.Vertinimas, " +
                    "       pv.Pastabos " +
                    "FROM studentas s " +
                    "INNER JOIN baigiamasis_darbas b ON s.Studento_Id = b.Studento_Id " +
                    "LEFT JOIN praktikos_info pi ON b.Studento_Id = pi.Studento_Id " +
                    "LEFT JOIN praktikos_vertinimas pv ON pi.Praktikos_Id = pv.Praktikos_Id " +
                    "WHERE b.Darbo_Vadovo_Id = ? " +
                    "ORDER BY s.Vardas, s.Pavarde";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, destytojasId); // Set the lecturer's ID
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("<html><body>");

                    while (resultSet.next()) {
                        String studentoPilnasVardas = resultSet.getString("Studento_pilnas_vardas");
                        int praktikosId = resultSet.getInt("Praktikos_Id");
                        String pradziosData = resultSet.getString("Pradzios_data");
                        String pabaigosData = resultSet.getString("Pabaigos_data");
                        String vertinimoData = resultSet.getString("Vertinimo_data");
                        String vertinimas = resultSet.getString("Vertinimas");
                        String pastabos = resultSet.getString("Pastabos");

                        sb.append("<b>Studento vardas ir pavardė:</b> ").append(studentoPilnasVardas).append("<br>")
                                .append("<b>Praktikos pradžios data:</b> ").append(pradziosData).append("<br>")
                                .append("<b>Praktikos pabaigos data:</b> ").append(pabaigosData).append("<br>")
                                .append("<b>Vertinimo data:</b> ").append(vertinimoData).append("<br>")
                                .append("<b>Vertinimas:</b> ").append(vertinimas).append("<br>")
                                .append("<b>Pastabos:</b> <a href=\"edit-").append(praktikosId).append("\">").append(pastabos).append("</a><br><br>");
                    }

                    sb.append("</body></html>");
                    textPane.setContentType("text/html");
                    textPane.setText(sb.toString());

                    textPane.addHyperlinkListener(new HyperlinkListener() {
                        @Override
                        public void hyperlinkUpdate(HyperlinkEvent e) {
                            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                                String description = e.getDescription();
                                if (description.startsWith("edit-")) {
                                    int praktikosId = Integer.parseInt(description.substring("edit-".length()));
                                    // Call a method to handle the edit action for the clicked praktikosId
                                    handleEditAction(praktikosId);
                                }
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

    private void handleEditAction(int praktikosId) {
        System.out.println("Editing Pastabos for praktikos with ID: " + praktikosId);
        String newPastabos = JOptionPane.showInputDialog(this, "Įveskite naujas pastabas:");
        if (newPastabos != null) {
            newPastabos = newPastabos.trim();

            if (newPastabos.isEmpty()) {
                newPastabos = null;
            }

            try (Connection connection = DatabaseConnection.getConnection()) {
                String updateQuery = "UPDATE praktikos_vertinimas SET Pastabos = ? WHERE Praktikos_Id = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(updateQuery)) {
                    preparedStatement.setString(1, newPastabos);
                    preparedStatement.setInt(2, praktikosId);
                    int rowsUpdated = preparedStatement.executeUpdate();
                    if (rowsUpdated > 0) {
                        JOptionPane.showMessageDialog(this, "Pastabos atnaujintos sėkmingai!", "Sėkmė", JOptionPane.INFORMATION_MESSAGE);
                        displayInternshipDetails((JTextPane) getContentPane().getComponent(0));
                    } else {
                        JOptionPane.showMessageDialog(this, "Nepavyko atnaujinti pastabų.", "Klaida", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Klaida atnaujinant pastabas: " + ex.getMessage(), "Klaida", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PraktikosValdymas frame = new PraktikosValdymas("your_destytojas_id_here");
            frame.setVisible(true);
        });
    }
}
