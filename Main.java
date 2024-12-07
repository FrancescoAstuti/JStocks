package afin.jstocks;

import org.jfree.chart.ChartPanel;
import javax.swing.*;
import java.awt.BorderLayout;
import java.util.ArrayList;

public class Main {
    private static ArrayList<StockLot> stockLots = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            showOverview();
        });
    }

    public static void showOverview() {
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

        JButton stockScreenerButton = new JButton("Stock Screener");
        stockScreenerButton.addActionListener(e -> {
            StockScreener stockScreener = new StockScreener();
            stockScreener.createAndShowGUI();
        });

        // Add time frame selection buttons
        JPanel timeFramePanel = new JPanel();
        String[] timeFrames = {"1W", "1M", "2M", "3M", "6M", "1Y"};
        for (String timeFrame : timeFrames) {
            JButton button = new JButton(timeFrame);
            button.addActionListener(e -> gui.updateChart(timeFrame));
            timeFramePanel.add(button);
        }

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(timeFramePanel, BorderLayout.NORTH); // Add time frame buttons to the top
        panel.add(chartPanel, BorderLayout.CENTER);
        panel.add(portfolioButton, BorderLayout.SOUTH);
        panel.add(stockScreenerButton, BorderLayout.EAST); // Add the new button to the right

        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}