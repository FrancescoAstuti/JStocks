package afin.jstocks;

import javax.swing.*;

public class StockScreener {
    public void createAndShowGUI() {
        JFrame frame = new JFrame("Stock Screener");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(800, 600);

        // Add components to the frame here
        JPanel panel = new JPanel();
        panel.add(new JLabel("This is the Stock Screener window"));

        frame.add(panel);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}