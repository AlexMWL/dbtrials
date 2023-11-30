import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class home {
    private static final String DB_URL = "jdbc:sqlite:data";
    private static final String USER = "admin";
    private static final String PASSWORD = "root";
    private static final String TABLE_NAME = "input_data";

    private JFrame frame;
    private JTextField textField;
    private JTextArea outputTextArea;

    public static void main(String[] args) {
        home application = new home();
        application.createAndShowGUI();
    }

    private void createAndShowGUI() {
        frame = new JFrame("database trial v0.02a-dev");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textField = new JTextField(20);
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new SaveButtonListener());

        JButton retrieveButton = new JButton("Load");
        retrieveButton.addActionListener(new RetrieveButtonListener());

        JPanel inputPanel = new JPanel();
        inputPanel.add(textField);
        inputPanel.add(saveButton);
        inputPanel.add(retrieveButton);

        outputTextArea = new JTextArea(10, 20);
        outputTextArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputTextArea);

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setVisible(true);
    }

    private class SaveButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String userInput = textField.getText();

            try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
                 Statement statement = connection.createStatement()) {

                // Create the table if it doesn't exist
                statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (input TEXT)");

                // Insert the user input into the table
                String query = "INSERT INTO " + TABLE_NAME + " (input) VALUES (?)";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, userInput);
                preparedStatement.executeUpdate();

                JOptionPane.showMessageDialog(frame, "Saved!");

                // Clear the text field
                textField.setText("");

            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }

    private class RetrieveButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            outputTextArea.setText("");

            try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
                 Statement statement = connection.createStatement()) {

                // Retrieve all entries from the table
                String query = "SELECT input FROM " + TABLE_NAME;
                ResultSet resultSet = statement.executeQuery(query);

                while (resultSet.next()) {
                    String userInput = resultSet.getString("input");
                    outputTextArea.append(userInput + "\n");
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
            }

            if (outputTextArea.getText().isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Data not found in the database.");
            } else {
                JFrame outputFrame = new JFrame("Stored Data");
                outputFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                outputFrame.getContentPane().add(outputTextArea);
                outputFrame.pack();
                outputFrame.setVisible(true);
            }
        }
    }
}