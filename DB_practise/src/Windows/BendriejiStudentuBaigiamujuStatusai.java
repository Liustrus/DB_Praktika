package Windows;

import sql.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class BendriejiStudentuBaigiamujuStatusai extends JFrame {
    private String destytojoId;

    public BendriejiStudentuBaigiamujuStatusai(String destytojoId) {
        this.destytojoId = destytojoId;
        setTitle("Baigiamųjų darbų statusai");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        displayThesisStatuses(textArea);
    }

    private void displayThesisStatuses(JTextArea textArea) {
        String query = "SELECT s.Vardas AS Studento_vardas, s.Pavarde AS Studento_pavarde, " +
                "b.Darbo_pavadinimas, " +
                "CASE " +
                "   WHEN b.Vertinimas IS NOT NULL THEN 'Baigiamasis darbas turi galutinį įvertinimą' " +
                "   WHEN b.Vertinimas IS NULL AND r.Siulomas_vertinimas IS NOT NULL THEN 'Baigiamojo darbo recenzentas pateikęs siūlomą vertinimą' " +
                "   WHEN b.Vertinimas IS NULL AND r.Siulomas_vertinimas IS NULL AND b.Plagiato_busenos_Id = 1 THEN 'Plagiato patikra nepradėta' " +
                "   WHEN b.Vertinimas IS NULL AND r.Siulomas_vertinimas IS NULL AND b.Plagiato_busenos_Id <> 1 THEN 'Plagiato patikra baigta' " +
                "   ELSE 'statusas nežinomas' " +
                "END AS Statusas, " +
                "CASE " +
                "   WHEN pv.Vertinimas IS NOT NULL THEN 'Praktika atlikta' " +
                "   ELSE 'Praktika neatlikta' " +
                "END AS Praktika, " +
                "CASE " +
                "   WHEN b.Plagiato_busenos_Id IS NOT NULL THEN " +
                "       CASE " +
                "           WHEN b.Plagiato_busenos_Id = 1 THEN 'Plagiato patikra nepradėta' " +
                "           ELSE 'Plagiato patikra baigta' " +
                "       END " +
                "   ELSE 'Plagiato patikros busena nežinoma' " +
                "END AS Plagiato_busena " +
                "FROM studentas s " +
                "INNER JOIN baigiamasis_darbas b ON s.Studento_Id = b.Studento_Id " +
                "LEFT JOIN recenzija r ON b.Recenzijos_Id = r.Recenzijos_Id " +
                "LEFT JOIN praktikos_info pi ON b.Studento_Id = pi.Studento_Id " +
                "LEFT JOIN praktikos_vertinimas pv ON pi.Praktikos_Id = pv.Praktikos_Id " +
                "WHERE b.Darbo_vadovo_Id = ? " +
                "ORDER BY s.Vardas, s.Pavarde";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setString(1, destytojoId);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                StringBuilder sb = new StringBuilder();
                sb.append("Studentų baigiamųjų darbų statusai:\n\n");
                while (resultSet.next()) {
                    sb.append("Studento vardas: ").append(resultSet.getString("Studento_vardas")).append("\n");
                    sb.append("Studento pavardė: ").append(resultSet.getString("Studento_pavarde")).append("\n");
                    sb.append("Baigiamojo darbo pavadinimas: ").append(resultSet.getString("Darbo_pavadinimas")).append("\n");
                    sb.append("Statusas: ").append(resultSet.getString("Statusas")).append("\n");
                    sb.append("Praktikos statusas: ").append(resultSet.getString("Praktika")).append("\n");
                    sb.append("Plagiato patikros busena: ").append(resultSet.getString("Plagiato_busena")).append("\n\n");
                }
                textArea.setText(sb.toString());
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            textArea.setText("Klaida gaunant duomenis iš duomenų bazės.");
        }
    }

    public static void main(String[] args) {
        // Example usage
        SwingUtilities.invokeLater(() -> {
            new BendriejiStudentuBaigiamujuStatusai("1").setVisible(true); // Replace "1" with actual destytojoId
        });
    }
}
