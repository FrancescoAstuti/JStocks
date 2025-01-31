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
import javax.swing.JScrollPane;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONArray;
import org.json.JSONObject;

public class CompanyOverview {

    public static void showCompanyOverview(String ticker, String companyName) {
        JFrame overviewFrame = new JFrame("Company Overview: " + ticker);
        overviewFrame.setSize(1200, 1200); // Increase the size of the frame
        overviewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Company Name: " + companyName, SwingConstants.CENTER);
        panel.add(label, BorderLayout.NORTH);

        // Fetch historical PE, PB ratios, quarterly EPS, PFCF ratios, and Debt to Equity ratios
        List<RatioData> peRatios = Ratios.fetchHistoricalPE(ticker);
        List<RatioData> pbRatios = Ratios.fetchHistoricalPB(ticker);
        List<RatioData> epsRatios = Ratios.fetchQuarterlyEPS(ticker);
        List<RatioData> pfcfRatios = Ratios.fetchHistoricalPFCF(ticker);
        List<RatioData> debtToEquityRatios = Ratios.fetchHistoricalDebtToEquity(ticker);

        // Filter data to the last 10 years and sort by date
        LocalDate timeRange = LocalDate.now().minusYears(20);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        peRatios = peRatios.stream()
                .filter(data -> LocalDate.parse(data.getDate(), formatter).isAfter(timeRange))
                .sorted(Comparator.comparing(data -> LocalDate.parse(data.getDate(), formatter)))
                .collect(Collectors.toList());

        pbRatios = pbRatios.stream()
                .filter(data -> LocalDate.parse(data.getDate(), formatter).isAfter(timeRange))
                .sorted(Comparator.comparing(data -> LocalDate.parse(data.getDate(), formatter)))
                .collect(Collectors.toList());

        epsRatios = epsRatios.stream()
                .filter(data -> LocalDate.parse(data.getDate(), formatter).isAfter(timeRange))
                .sorted(Comparator.comparing(data -> LocalDate.parse(data.getDate(), formatter)))
                .collect(Collectors.toList());

        pfcfRatios = pfcfRatios.stream()
                .filter(data -> LocalDate.parse(data.getDate(), formatter).isAfter(timeRange))
                .sorted(Comparator.comparing(data -> LocalDate.parse(data.getDate(), formatter)))
                .collect(Collectors.toList());

        debtToEquityRatios = debtToEquityRatios.stream()
                .filter(data -> LocalDate.parse(data.getDate(), formatter).isAfter(timeRange))
                .sorted(Comparator.comparing(data -> LocalDate.parse(data.getDate(), formatter)))
                .collect(Collectors.toList());

        // Cap PE values between 0 and 40
        List<Double> cappedPeValues = peRatios.stream()
                .map(RatioData::getValue)
                .map(value -> Math.max(Math.min(value, 40), 0))
                .collect(Collectors.toList());

        // Cap negative PFCF values at -50 and positive values at 50
        List<Double> cappedPfcfValues = pfcfRatios.stream()
                .map(RatioData::getValue)
                .map(value -> Math.max(Math.min(value, 50), -50))
                .collect(Collectors.toList());

        // Calculate average for capped PE, PB ratios, EPS, capped PFCF ratios, and Debt to Equity ratios
        double peAverage = calculateAverage(cappedPeValues);
        double pbAverage = calculateAverage(pbRatios.stream().map(RatioData::getValue).collect(Collectors.toList()));
        double epsAverage = calculateAverage(epsRatios.stream().map(RatioData::getValue).collect(Collectors.toList()));
        double pfcfAverage = calculateAverage(cappedPfcfValues);
        double debtToEquityAverage = calculateAverage(debtToEquityRatios.stream().map(RatioData::getValue).collect(Collectors.toList()));

        // Save the data to a JSON file
        saveDataToFile(ticker, peRatios, pbRatios, epsRatios, pfcfRatios, debtToEquityRatios, peAverage, pbAverage, epsAverage, pfcfAverage, debtToEquityAverage);

        // Create datasets for the charts
        DefaultCategoryDataset peDataset = new DefaultCategoryDataset();
        for (RatioData data : peRatios) {
            peDataset.addValue(Math.max(Math.min(data.getValue(), 40), 0), "PE Ratio", data.getDate()); // Cap values between 0 and 40
        }

        DefaultCategoryDataset pbDataset = new DefaultCategoryDataset();
        for (RatioData data : pbRatios) {
            pbDataset.addValue(data.getValue(), "PB Ratio", data.getDate());
        }

        DefaultCategoryDataset epsDataset = new DefaultCategoryDataset();
        for (RatioData data : epsRatios) {
            epsDataset.addValue(data.getValue(), "EPS", data.getDate());
        }

        DefaultCategoryDataset pfcfDataset = new DefaultCategoryDataset();
        for (RatioData data : pfcfRatios) {
            pfcfDataset.addValue(Math.max(Math.min(data.getValue(), 50), -50), "PFCF Ratio", data.getDate()); // Cap values between -50 and 50
        }

        DefaultCategoryDataset debtToEquityDataset = new DefaultCategoryDataset();
        for (RatioData data : debtToEquityRatios) {
            debtToEquityDataset.addValue(data.getValue(), "Debt to Equity Ratio", data.getDate());
        }

        // Create the PE chart
        JFreeChart peChart = ChartFactory.createLineChart(
                "10-Year Quarterly PE Ratios",
                "Date", "PE Ratio",
                peDataset, PlotOrientation.VERTICAL,
                true, true, false);

        // Create the PB chart
        JFreeChart pbChart = ChartFactory.createLineChart(
                "10-Year Quarterly PB Ratios",
                "Date", "PB Ratio",
                pbDataset, PlotOrientation.VERTICAL,
                true, true, false);

        // Create the EPS chart
        JFreeChart epsChart = ChartFactory.createLineChart(
                "10-Year Quarterly EPS",
                "Date", "EPS",
                epsDataset, PlotOrientation.VERTICAL,
                true, true, false);

        // Create the PFCF chart
        JFreeChart pfcfChart = ChartFactory.createLineChart(
                "10-Year Quarterly PFCF Ratios",
                "Date", "PFCF Ratio",
                pfcfDataset, PlotOrientation.VERTICAL,
                true, true, false);

        // Create the Debt to Equity chart
        JFreeChart debtToEquityChart = ChartFactory.createLineChart(
                "10-Year Quarterly Debt to Equity Ratios",
                "Date", "Debt to Equity Ratio",
                debtToEquityDataset, PlotOrientation.VERTICAL,
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

        CategoryAxis epsCategoryAxis = epsChart.getCategoryPlot().getDomainAxis();
        epsCategoryAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        epsCategoryAxis.setTickLabelsVisible(true);
        customizeAxis(epsCategoryAxis);

        CategoryAxis pfcfCategoryAxis = pfcfChart.getCategoryPlot().getDomainAxis();
        pfcfCategoryAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        pfcfCategoryAxis.setTickLabelsVisible(true);
        customizeAxis(pfcfCategoryAxis);

        CategoryAxis debtToEquityCategoryAxis = debtToEquityChart.getCategoryPlot().getDomainAxis();
        debtToEquityCategoryAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        debtToEquityCategoryAxis.setTickLabelsVisible(true);
        customizeAxis(debtToEquityCategoryAxis);

        // Add average lines to the PE chart
        addMarker(peChart, peAverage, "Average PE", Color.BLUE);

        // Add average lines to the PB chart
        addMarker(pbChart, pbAverage, "Average PB", Color.BLUE);

        // Add average lines to the EPS chart
        addMarker(epsChart, epsAverage, "Average EPS", Color.BLUE);

        // Add average lines to the PFCF chart
        addMarker(pfcfChart, pfcfAverage, "Average PFCF", Color.BLUE);

        // Add average lines to the Debt to Equity chart
        addMarker(debtToEquityChart, debtToEquityAverage, "Average Debt to Equity", Color.BLUE);

        // Create chart panels with larger dimensions
        ChartPanel peChartPanel = new ChartPanel(peChart);
        peChartPanel.setPreferredSize(new Dimension(1100, 400));

        ChartPanel pbChartPanel = new ChartPanel(pbChart);
        pbChartPanel.setPreferredSize(new Dimension(1100, 400));

        ChartPanel epsChartPanel = new ChartPanel(epsChart);
        epsChartPanel.setPreferredSize(new Dimension(1100, 400));

        ChartPanel pfcfChartPanel = new ChartPanel(pfcfChart);
        pfcfChartPanel.setPreferredSize(new Dimension(1100, 400));

        ChartPanel debtToEquityChartPanel = new ChartPanel(debtToEquityChart);
        debtToEquityChartPanel.setPreferredSize(new Dimension(1100, 400));

        // Add chart panels to the main panel
        JPanel chartsPanel = new JPanel(new GridLayout(5, 1));
        chartsPanel.add(peChartPanel);
        chartsPanel.add(pbChartPanel);
        chartsPanel.add(epsChartPanel);
        chartsPanel.add(pfcfChartPanel);
        chartsPanel.add(debtToEquityChartPanel);

        // Add the main panel to a scroll pane
        JScrollPane scrollPane = new JScrollPane(chartsPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        panel.add(scrollPane, BorderLayout.CENTER);

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
        return values.stream()
                .filter(value -> value > 0) // Filtra i valori <= 0
                .mapToDouble(Double::doubleValue)
                .average().orElse(Double.NaN);
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

    private static void saveDataToFile(String ticker, List<RatioData> peRatios, List<RatioData> pbRatios, List<RatioData> epsRatios, List<RatioData> pfcfRatios, List<RatioData> debtToEquityRatios, double peAverage, double pbAverage, double epsAverage, double pfcfAverage, double debtToEquityAverage) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ticker", ticker);
        jsonObject.put("peAverage", peAverage);
        jsonObject.put("pbAverage", pbAverage);
        jsonObject.put("epsAverage", epsAverage);
        jsonObject.put("pfcfAverage", pfcfAverage);
        jsonObject.put("debtToEquityAverage", debtToEquityAverage);

        JSONArray peArray = new JSONArray();
        for (RatioData data : peRatios) {
            JSONObject dataObject = new JSONObject();
            dataObject.put("date", data.getDate());
            dataObject.put("value", Math.max(Math.min(data.getValue(), 40), 0)); // Cap values between 0 and 40
            peArray.put(dataObject);
        }
        jsonObject.put("peRatios", peArray);

        JSONArray pbArray = new JSONArray();
        for (RatioData data : pbRatios) {
            JSONObject dataObject = new JSONObject();
            dataObject.put("date", data.getDate());
            dataObject.put("value", data.getValue());
            pbArray.put(dataObject);
        }
        jsonObject.put("pbRatios", pbArray);

        JSONArray epsArray = new JSONArray();
        for (RatioData data : epsRatios) {
            JSONObject dataObject = new JSONObject();
            dataObject.put("date", data.getDate());
            dataObject.put("value", data.getValue());
            epsArray.put(dataObject);
        }
        jsonObject.put("epsRatios", epsArray);

        JSONArray pfcfArray = new JSONArray();
        for (RatioData data : pfcfRatios) {
            JSONObject dataObject = new JSONObject();
            dataObject.put("date", data.getDate());
            dataObject.put("value", Math.max(Math.min(data.getValue(), 50), -50)); // Cap values between -50 and 50
            pfcfArray.put(dataObject);
        }
        jsonObject.put("pfcfRatios", pfcfArray);

        JSONArray debtToEquityArray = new JSONArray();
        for (RatioData data : debtToEquityRatios) {
            JSONObject dataObject = new JSONObject();
            dataObject.put("date", data.getDate());
            dataObject.put("value", data.getValue());
            debtToEquityArray.put(dataObject);
        }
        jsonObject.put("debtToEquityRatios", debtToEquityArray);

        try (FileWriter file = new FileWriter("CompanyOverview.json")) {
            file.write(jsonObject.toString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
