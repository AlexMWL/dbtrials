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
        frame = new JFrame("Database Trial v0.02d-dev");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textField = new JTextField(40);
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new SaveButtonListener());

        JPanel inputPanel = new JPanel();
        inputPanel.add(textField);
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
//            ex.printStackTrace();
        }

        if (outputTextArea.getText().isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Data not found in the database.");
        }
    }

    private class SaveButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String userInput = textField.getText().trim();

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

                JOptionPane.showMessageDialog(frame, "Saved!");

                textField.setText("");

                loadData();

            } catch (SQLException ex) {
//                ex.printStackTrace();
            }
        }
    }
}
