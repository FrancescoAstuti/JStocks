package afin.jstocks;

import org.jfree.chart.ChartPanel;
import javax.swing.*;
import java.awt.BorderLayout;
import java.util.ArrayList;

public class Main {
    private static ArrayList<StockLot> stockLots = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Overview");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);

            GUI gui = new GUI(stockLots);
            ChartPanel chartPanel = gui.createChartPanel();
            
            JButton portfolioButton = new JButton("Portfolio");
            portfolioButton.addActionListener(e -> {
                frame.dispose();
                gui.createAndShowGUI();
            });

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(chartPanel, BorderLayout.CENTER);
            panel.add(portfolioButton, BorderLayout.SOUTH);

            frame.add(panel);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}