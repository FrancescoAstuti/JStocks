package afin.jstocks;

import javax.swing.*;
import java.util.ArrayList;

public class Main {
    private static ArrayList<StockLot> stockLots = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Overview");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(300, 200);
            JButton portfolioButton = new JButton("Portfolio");

            portfolioButton.addActionListener(e -> {
                frame.dispose();
                GUI gui = new GUI(stockLots);
                gui.createAndShowGUI();
            });

            JPanel panel = new JPanel();
            panel.add(portfolioButton);
            frame.add(panel);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}