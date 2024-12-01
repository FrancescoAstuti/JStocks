import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class GUI {
    private ArrayList<StockLot> stockLots;
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tickerField, quantityField, purchasePriceField, currentPriceField;

    public GUI(ArrayList<StockLot> stockLots) {
        this.stockLots = stockLots;
    }

    public void createAndShowGUI() {
        JFrame frame = new JFrame("Stock Portfolio Manager");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JPanel panel = new JPanel(new BorderLayout());

        tableModel = new DefaultTableModel(new Object[]{"Ticker", "Quantity", "Purchase Price", "Current Price", "P/L (%)"}, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel inputPanel = new JPanel(new GridLayout(5, 2));
        tickerField = new JTextField();
        quantityField = new JTextField();
        purchasePriceField = new JTextField();
        currentPriceField = new JTextField();

        inputPanel.add(new JLabel("Ticker:"));
        inputPanel.add(tickerField);
        inputPanel.add(new JLabel("Quantity:"));
        inputPanel.add(quantityField);
        inputPanel.add(new JLabel("Purchase Price:"));
        inputPanel.add(purchasePriceField);
        inputPanel.add(new JLabel("Current Price:"));
        inputPanel.add(currentPriceField);

        JButton addButton = new JButton("Add");
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addStockLot();
            }
        });

        JButton modifyButton = new JButton("Modify");
        modifyButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                modifyStockLot();
            }
        });

        JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteStockLot();
            }
        });

        inputPanel.add(addButton);
        inputPanel.add(modifyButton);
        inputPanel.add(deleteButton);

        panel.add(inputPanel, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);
    }

    private void addStockLot() {
        String ticker = tickerField.getText();
        int quantity = Integer.parseInt(quantityField.getText());
        double purchasePrice = Double.parseDouble(purchasePriceField.getText());
        double currentPrice = Double.parseDouble(currentPriceField.getText());

        Main.addStockLot(ticker, quantity, purchasePrice, currentPrice);
        updateTable();
    }

    private void modifyStockLot() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            String ticker = tickerField.getText();
            int quantity = Integer.parseInt(quantityField.getText());
            double purchasePrice = Double.parseDouble(purchasePriceField.getText());
            double currentPrice = Double.parseDouble(currentPriceField.getText());

            Main.modifyStockLot(selectedRow, ticker, quantity, purchasePrice, currentPrice);
            updateTable();
        }
    }

    private void deleteStockLot() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            Main.deleteStockLot(selectedRow);
            updateTable();
        }
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        for (StockLot stockLot : stockLots) {
            tableModel.addRow(new Object[]{
                stockLot.getTicker(),
                stockLot.getQuantity(),
                stockLot.getPurchasePrice(),
                stockLot.getCurrentPrice(),
                stockLot.getProfitLossPercentage()
            });
        }
    }
}