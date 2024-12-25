package afin.jstocks;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
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
        List<RatioData> peRatios = Ratios.fetchHistoricalPE(ticker);
        List<RatioData> pbRatios = Ratios.fetchHistoricalPB(ticker);

        System.out.println("PE Ratios: " + peRatios);  // Debugging statement
        System.out.println("PB Ratios: " + pbRatios);  // Debugging statement

        // Create datasets for the charts
        DefaultCategoryDataset peDataset = new DefaultCategoryDataset();
        for (RatioData data : peRatios) {
            String year = data.getDate().split("-")[0];  // Extract year from date
            peDataset.addValue(data.getValue(), "PE Ratio", data.getDate());
        }

        DefaultCategoryDataset pbDataset = new DefaultCategoryDataset();
        for (RatioData data : pbRatios) {
            String year = data.getDate().split("-")[0];  // Extract year from date
            pbDataset.addValue(data.getValue(), "PB Ratio", data.getDate());
        }

        // Create the PE chart
        JFreeChart peChart = ChartFactory.createLineChart(
                "20-Year Historical PE Ratios",
                "Date", "PE Ratio",
                peDataset, PlotOrientation.VERTICAL,
                true, true, false);

        // Create the PB chart
        JFreeChart pbChart = ChartFactory.createLineChart(
                "20-Year Historical PB Ratios",
                "Date", "PB Ratio",
                pbDataset, PlotOrientation.VERTICAL,
                true, true, false);

        // Rotate category labels to display only years and avoid overlap
        CategoryAxis peCategoryAxis = peChart.getCategoryPlot().getDomainAxis();
        peCategoryAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

        CategoryAxis pbCategoryAxis = pbChart.getCategoryPlot().getDomainAxis();
        pbCategoryAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

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