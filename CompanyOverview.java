package afin.jstocks;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

public class CompanyOverview {

    public static void showCompanyOverview(String ticker, String companyName) {
        JFrame overviewFrame = new JFrame("Company Overview: " + ticker);
        overviewFrame.setSize(1000, 800);
        overviewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Company Name: " + companyName, SwingConstants.CENTER);
        panel.add(label, BorderLayout.NORTH);

        // Fetch historical PE and PB ratios
        List<Double> peRatios = Ratios.fetchHistoricalPE(ticker);
        List<Double> pbRatios = Ratios.fetchHistoricalPB(ticker);

        System.out.println("PE Ratios: " + peRatios);  // Debugging statement
        System.out.println("PB Ratios: " + pbRatios);  // Debugging statement

        // Create datasets for the charts
        DefaultCategoryDataset peDataset = new DefaultCategoryDataset();
        for (int i = 0; i < peRatios.size(); i++) {
            peDataset.addValue(peRatios.get(i), "PE Ratio", String.valueOf(i + 1));
        }

        DefaultCategoryDataset pbDataset = new DefaultCategoryDataset();
        for (int i = 0; i < pbRatios.size(); i++) {
            pbDataset.addValue(pbRatios.get(i), "PB Ratio", String.valueOf(i + 1));
        }

        // Check if datasets are populated
        System.out.println("PE Dataset: " + peDataset.getRowCount() + " rows, " + peDataset.getColumnCount() + " columns");  // Debugging statement
        System.out.println("PB Dataset: " + pbDataset.getRowCount() + " rows, " + pbDataset.getColumnCount() + " columns");  // Debugging statement

        // Create the PE chart
        JFreeChart peChart = ChartFactory.createLineChart(
                "10-Year Historical PE Ratios",
                "Year", "PE Ratio",
                peDataset, PlotOrientation.VERTICAL,
                true, true, false);

        // Create the PB chart
        JFreeChart pbChart = ChartFactory.createLineChart(
                "10-Year Historical PB Ratios",
                "Year", "PB Ratio",
                pbDataset, PlotOrientation.VERTICAL,
                true, true, false);

        // Create chart panels
        ChartPanel peChartPanel = new ChartPanel(peChart);
        ChartPanel pbChartPanel = new ChartPanel(pbChart);

        // Add chart panels to the main panel
        JPanel chartsPanel = new JPanel(new GridLayout(2, 1));
        chartsPanel.add(peChartPanel);
        chartsPanel.add(pbChartPanel);

        panel.add(chartsPanel, BorderLayout.CENTER);

        overviewFrame.add(panel);
        overviewFrame.setLocationRelativeTo(null);
        overviewFrame.setVisible(true);
    }
}