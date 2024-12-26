package afin.jstocks;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.ui.Layer;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;
import java.util.stream.Collectors;

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

        // Calculate average for PE ratios
        double peAverage = calculateAverage(peRatios.stream().map(RatioData::getValue).collect(Collectors.toList()));

        // Calculate average for PB ratios
        double pbAverage = calculateAverage(pbRatios.stream().map(RatioData::getValue).collect(Collectors.toList()));

        // Create datasets for the charts
        DefaultCategoryDataset peDataset = new DefaultCategoryDataset();
        for (RatioData data : peRatios) {
            peDataset.addValue(data.getValue(), "PE Ratio", data.getDate());
        }

        DefaultCategoryDataset pbDataset = new DefaultCategoryDataset();
        for (RatioData data : pbRatios) {
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

        // Customize the time axis to show only years
        CategoryAxis peCategoryAxis = peChart.getCategoryPlot().getDomainAxis();
        peCategoryAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        peCategoryAxis.setTickLabelsVisible(true);
        customizeAxis(peCategoryAxis);

        CategoryAxis pbCategoryAxis = pbChart.getCategoryPlot().getDomainAxis();
        pbCategoryAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        pbCategoryAxis.setTickLabelsVisible(true);
        customizeAxis(pbCategoryAxis);

        // Add average lines to the PE chart
        addMarker(peChart, peAverage, "Average PE", Color.BLUE);

        // Add average lines to the PB chart
        addMarker(pbChart, pbAverage, "Average PB", Color.BLUE);

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

    private static void customizeAxis(CategoryAxis axis) {
        axis.setTickLabelsVisible(true);
        axis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        axis.setTickLabelFont(axis.getTickLabelFont().deriveFont(10f));
        axis.setMaximumCategoryLabelWidthRatio(0.5f);  // Adjusts the width ratio to prevent overlapping
        axis.setTickMarksVisible(true);
        axis.setCategoryMargin(0.2);

        // Customize the labels to show only the years
        axis.setCategoryLabelPositions(CategoryLabelPositions.STANDARD);
        axis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4.0));
    }

    private static double calculateAverage(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(Double.NaN);
    }

    private static void addMarker(JFreeChart chart, double value, String label, Color color) {
        ValueMarker marker = new ValueMarker(value);
        marker.setLabel(label + ": " + String.format("%.2f", value));  // Include the average value in the label
        marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        marker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
        marker.setLabelFont(new Font("SansSerif", Font.BOLD, 12));  // Set the font for the label
        marker.setLabelPaint(Color.BLACK);  // Set the color for the label
        marker.setPaint(color);
        marker.setStroke(new BasicStroke(1.0f));  // Set the stroke width for the marker line
        chart.getCategoryPlot().addRangeMarker(marker, Layer.FOREGROUND);
    }
}