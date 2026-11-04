package cbank.gui;

import javax.swing.*;
import java.awt.*;

public class DashBoardWindow extends AbstractCBankWindow{
    // Dashboard Components
    private JLabel welcomeLabel;
    private JLabel headerLabel1;
    private JLabel headerLabel2;
    private JButton adminLogBtn;
    private JButton adminVaultBtn;

    private JPanel mainPanel;
    private JPanel headerPanel;
    private JPanel IndividualAccountPanel;
    private JPanel AdminPanel;

    private JButton withdrawBtn;
    private JButton depositBtn;
    private JButton foreignBtn;
    private JButton changePinBtn;

    private JButton logoutBtn;

    public DashBoardWindow(CBankGUI mainWindow) {
        super(mainWindow);
    }

    public JPanel buildDashboardPanel() {
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Header
        headerPanel = new JPanel(new GridLayout(3, 1));
        welcomeLabel = new JLabel("", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        headerLabel1 = new JLabel("", SwingConstants.CENTER);
        headerLabel2 = new JLabel("", SwingConstants.CENTER);

        headerPanel.add(welcomeLabel);
        headerPanel.add(headerLabel1);
        headerPanel.add(headerLabel2);
        //panel.add(headerPanel, BorderLayout.NORTH);

        // Actions Grid
        IndividualAccountPanel = new JPanel(new GridLayout(6, 1, 8, 8));
        withdrawBtn = new JButton("Withdraw Cash (CAD)");
        depositBtn = new JButton("Deposit Cash");
        foreignBtn = new JButton("Foreign Currency Deposit");
        changePinBtn = new JButton("Change PIN");

        IndividualAccountPanel.add(new JLabel(""));
        IndividualAccountPanel.add(withdrawBtn);
        IndividualAccountPanel.add(depositBtn);
        IndividualAccountPanel.add(foreignBtn);
        IndividualAccountPanel.add(changePinBtn);


        //panel.add(IndividualAccountPanel, BorderLayout.CENTER);

        AdminPanel = new JPanel(new GridLayout(4, 1, 8, 8));
        adminLogBtn = new JButton("View Audit Logs");
        adminVaultBtn = new JButton("Replenish Vault");

        //panel.add(AdminPanel, BorderLayout.CENTER);

        AdminPanel.add(adminLogBtn);
        AdminPanel.add(adminVaultBtn);

        logoutBtn = new JButton("Logout / End Session");

        //mainPanel.add(logoutBtn, BorderLayout.SOUTH);

        // Action Handlers
        withdrawBtn.addActionListener(_ ->MAIN_WINDOW.handleWithdrawal());
        depositBtn.addActionListener(_ -> MAIN_WINDOW.handleDeposit());
        foreignBtn.addActionListener(_ -> MAIN_WINDOW.handleForeignDeposit());
        changePinBtn.addActionListener(_ -> MAIN_WINDOW.handleChangePin());

        adminLogBtn.addActionListener(_ -> MAIN_WINDOW.handleAuditLog());
        adminVaultBtn.addActionListener(_ -> MAIN_WINDOW.handleVaultAccess());

        logoutBtn.addActionListener(_ -> MAIN_WINDOW.logout());
        return mainPanel;
    }

    public void setWindow(String line1, String line2, String line3, boolean admin) {
        mainPanel.removeAll();

        welcomeLabel.setText(line1);
        headerLabel1.setText(line2);
        headerLabel2.setText(line3);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        if(admin){
            mainPanel.add(AdminPanel, BorderLayout.CENTER);
        }
        else{
            mainPanel.add(IndividualAccountPanel,BorderLayout.CENTER);
        }

        mainPanel.add(logoutBtn, BorderLayout.SOUTH);
    }
}
