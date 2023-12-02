import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;

public class home {
    private static final String DB_URL = "jdbc:sqlite:data";
    private static final String USER = "admin";
    private static final String PASSWORD = "root";
    private static final String TABLE_NAME = "input_data";

    private JFrame frame;
    private JTextArea textArea;
    private JTextArea outputTextArea;

    public static void main(String[] args) {
        home application = new home();
        application.createAndShowGUI();
    }

    private void createAndShowGUI() {
        frame = new JFrame("Database Trial v0.03a-dev");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textArea = new JTextArea(5, 40);
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new SaveButtonListener());

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JScrollPane(textArea));
        inputPanel.add(saveButton);

        outputTextArea = new JTextArea(30, 50);
        outputTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputTextArea);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setVisible(true);

        loadData();
    }

    private void loadData() {
        outputTextArea.setText("");

        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
             Statement statement = connection.createStatement()) {

            String query = "SELECT input FROM " + TABLE_NAME;
            ResultSet resultSet = statement.executeQuery(query);

            while (resultSet.next()) {
                String userInput = resultSet.getString("input");
                outputTextArea.append(userInput + "\n");
            }

        } catch (SQLException ex) {
            System.out.println("Error accessing database. Attempting recovery from Shadow_Data...");
            recoverDatabase();
            loadData();
            return;
        }

        if (outputTextArea.getText().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Database failure!");
        }
    }

    private class SaveButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String userInput = textArea.getText().trim();

            if (userInput.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Input cannot be empty!");
                return;
            }

            try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
                 Statement statement = connection.createStatement()) {

                statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (input TEXT)");

                String query = "INSERT INTO " + TABLE_NAME + " (input) VALUES (?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, userInput);
                preparedStatement.executeUpdate();

                createDatabaseCopy();

                JOptionPane.showMessageDialog(frame, "Saved!");

                textArea.setText("");

                loadData();

            } catch (SQLException ex) {
//                ex.printStackTrace();
            }
        }
    }

    private void createDatabaseCopy() {
        try {
            File originalFile = new File("Data");
            File copyFile = new File("Shadow_Data");

            Files.copy(originalFile.toPath(), copyFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception e) {
//            e.printStackTrace();
        }
    }

    private void recoverDatabase() {
        try {
            File shadowFile = new File("Shadow_Data");
            File dataFile = new File("Data");

            Files.copy(shadowFile.toPath(), dataFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Database recovery successful");
        } catch (Exception e) {
//            e.printStackTrace();
        }
    }
}
