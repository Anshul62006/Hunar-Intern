import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class ATMInterface extends JFrame implements ActionListener {
    private final JTextField accountField;
    private final JPasswordField pinField;
    private final JLabel resultLabel;
    private final JButton checkBalanceButton;
    private final JButton withdrawButton;
    private final JButton depositButton;

    private Connection connection;

    public ATMInterface() {
        // Set up the frame
        setTitle("ATM Interface");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridBagLayout());

        // Create components
        JLabel accountLabel = new JLabel("Account Number:");
        accountField = new JTextField(10);

        JLabel pinLabel = new JLabel("PIN:");
        pinField = new JPasswordField(10);

        checkBalanceButton = new JButton("Check Balance");
        withdrawButton = new JButton("Withdraw");
        depositButton = new JButton("Deposit");

        resultLabel = new JLabel("");

        // Set up layout constraints
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;

        // Add components to the frame
        add(accountLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(accountField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.EAST;
        add(pinLabel, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        add(pinField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        add(checkBalanceButton, gbc);

        gbc.gridx = 1;
        add(withdrawButton, gbc);

        gbc.gridx = 2;
        add(depositButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 3;
        gbc.anchor = GridBagConstraints.CENTER;
        add(resultLabel, gbc);

        // Add action listeners
        checkBalanceButton.addActionListener(this);
        withdrawButton.addActionListener(this);
        depositButton.addActionListener(this);

        // Initialize database connection
        initializeDBConnection();

        // Make the frame visible
        setVisible(true);
    }

    private void initializeDBConnection() {
        try {
            String url = "jdbc:mysql://localhost:3306/atm_db";
            String user = "root";
            String password = "Akshat189@2003";
            connection = DriverManager.getConnection(url, user, password);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection failed!");
            System.exit(1);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String accountNumber = accountField.getText();
        String pin = new String(pinField.getPassword());

        try {
            if (e.getSource() == checkBalanceButton) {
                checkBalance(accountNumber, pin);
            } else if (e.getSource() == withdrawButton) {
                performTransaction(accountNumber, pin, "withdraw");
            } else if (e.getSource() == depositButton) {
                performTransaction(accountNumber, pin, "deposit");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            resultLabel.setText("Transaction failed!");
        }
    }

    private void checkBalance(String accountNumber, String pin) throws SQLException {
        String query = "SELECT balance FROM accounts WHERE account_number = ? AND pin = ?";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setString(1, accountNumber);
        pstmt.setString(2, pin);

        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            double balance = rs.getDouble("balance");
            resultLabel.setText("Current Balance: " + balance);
        } else {
            resultLabel.setText("Invalid account number or PIN.");
        }
    }

    private void performTransaction(String accountNumber, String pin, String transactionType) throws SQLException {
        String query = "SELECT balance FROM accounts WHERE account_number = ? AND pin = ?";
        PreparedStatement pstmt = connection.prepareStatement(query);
        pstmt.setString(1, accountNumber);
        pstmt.setString(2, pin);

        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            double balance = rs.getDouble("balance");
            double amount = Double.parseDouble(JOptionPane.showInputDialog("Enter amount:"));

            if (transactionType.equals("withdraw")) {
                if (balance >= amount) {
                    balance -= amount;
                } else {
                    resultLabel.setText("Insufficient balance.");
                    return;
                }
            } else if (transactionType.equals("deposit")) {
                balance += amount;
            }

            query = "UPDATE accounts SET balance = ? WHERE account_number = ? AND pin = ?";
            pstmt = connection.prepareStatement(query);
            pstmt.setDouble(1, balance);
            pstmt.setString(2, accountNumber);
            pstmt.setString(3, pin);
            pstmt.executeUpdate();

            resultLabel.setText("Transaction successful! New Balance: " + balance);
        } else {
            resultLabel.setText("Invalid account number or PIN.");
        }
    }

    public static void main(String[] args) {
        new ATMInterface();
    }
}
