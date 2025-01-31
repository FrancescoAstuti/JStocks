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
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;

import javax.swing.*;
import java.awt.*;
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
    private static final Color BACKGROUND_COLOR = new Color(255, 255, 255);
    private static final Color GRID_LINE_COLOR = new Color(230, 230, 230);
    private static final Color BAR_COLOR = new Color(41, 128, 185);
    private static final Color TITLE_COLOR = new Color(44, 62, 80);
    private static final Color LABEL_COLOR = new Color(52, 73, 94);
    private static final Color MARKER_LINE_COLOR = new Color(231, 76, 60);
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final Font LABEL_FONT = new Font("Segoe UI", Font.PLAIN, 12);

    public static void showCompanyOverview(String ticker, String companyName) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        JFrame overviewFrame = new JFrame("Company Overview: " + ticker);
        overviewFrame.setSize(1200, 1200);
        overviewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("Company Name: " + companyName, SwingConstants.CENTER);
        titleLabel.setFont(TITLE_FONT);
        titleLabel.setForeground(TITLE_COLOR);
        panel.add(titleLabel, BorderLayout.NORTH);
        // Fetch historical data
        List<RatioData> peRatios = Ratios.fetchHistoricalPE(ticker);
        List<RatioData> pbRatios = Ratios.fetchHistoricalPB(ticker);
        List<RatioData> epsRatios = Ratios.fetchQuarterlyEPS(ticker);
        List<RatioData> pfcfRatios = Ratios.fetchHistoricalPFCF(ticker);
        List<RatioData> debtToEquityRatios = Ratios.fetchHistoricalDebtToEquity(ticker);

        // Filter and sort data
        LocalDate timeRange = LocalDate.now().minusYears(20);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        peRatios = filterAndSortRatios(peRatios, timeRange, formatter);
        pbRatios = filterAndSortRatios(pbRatios, timeRange, formatter);
        epsRatios = filterAndSortRatios(epsRatios, timeRange, formatter);
        pfcfRatios = filterAndSortRatios(pfcfRatios, timeRange, formatter);
        debtToEquityRatios = filterAndSortRatios(debtToEquityRatios, timeRange, formatter);

        // Calculate averages
        List<Double> cappedPeValues = capValues(peRatios, 0.0, 40.0);
        List<Double> cappedPfcfValues = capValues(pfcfRatios, -50.0, 50.0);

        double peAverage = calculateAverage(cappedPeValues);
        double pbAverage = calculateAverage(pbRatios.stream()
                .map(RatioData::getValue).collect(Collectors.toList()));
        double epsAverage = calculateAverage(epsRatios.stream()
                .map(RatioData::getValue).collect(Collectors.toList()));
        double pfcfAverage = calculateAverage(cappedPfcfValues);
        double debtToEquityAverage = calculateAverage(debtToEquityRatios.stream()
                .map(RatioData::getValue).collect(Collectors.toList()));

        // Create datasets
        DefaultCategoryDataset peDataset = createDataset(peRatios, "PE Ratio", -40.0, 40.0);
        DefaultCategoryDataset pbDataset = createDataset(pbRatios, "PB Ratio", -10.0, 10.0);
        DefaultCategoryDataset epsDataset = createDataset(epsRatios, "EPS", null, null);
        DefaultCategoryDataset pfcfDataset = createDataset(pfcfRatios, "PFCF Ratio", -50.0, 50.0);
        DefaultCategoryDataset debtToEquityDataset = createDataset(debtToEquityRatios, "Debt to Equity Ratio", null, null);

        // Create charts
        JFreeChart peChart = createStyledChart("Quarterly PE Ratios", "Date", "PE Ratio", peDataset);
        JFreeChart pbChart = createStyledChart("Quarterly PB Ratios", "Date", "PB Ratio", pbDataset);
        JFreeChart epsChart = createStyledChart("Quarterly EPS", "Date", "EPS", epsDataset);
        JFreeChart pfcfChart = createStyledChart("Quarterly PFCF Ratios", "Date", "PFCF Ratio", pfcfDataset);
        JFreeChart debtToEquityChart = createStyledChart("Quarterly Debt to Equity Ratios", 
                "Date", "Debt to Equity Ratio", debtToEquityDataset);

        // Add markers
        addMarker(peChart, peAverage, "Average PE");
        addMarker(pbChart, pbAverage, "Average PB");
        addMarker(epsChart, epsAverage, "Average EPS");
        addMarker(pfcfChart, pfcfAverage, "Average PFCF");
        addMarker(debtToEquityChart, debtToEquityAverage, "Average Debt to Equity");

        // Setup panel for charts
        JPanel chartsPanel = new JPanel(new GridLayout(5, 1, 0, 10));
        chartsPanel.setBackground(BACKGROUND_COLOR);
        chartsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add charts to panel
        addChartToPanel(chartsPanel, peChart);
        addChartToPanel(chartsPanel, pbChart);
        addChartToPanel(chartsPanel, epsChart);
        addChartToPanel(chartsPanel, pfcfChart);
        addChartToPanel(chartsPanel, debtToEquityChart);

        // Create scroll pane
        JScrollPane scrollPane = new JScrollPane(chartsPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.setBackground(BACKGROUND_COLOR);

        overviewFrame.add(panel);
        overviewFrame.setLocationRelativeTo(null);
        overviewFrame.setVisible(true);

        // Save data
        saveDataToFile(ticker, peRatios, pbRatios, epsRatios, pfcfRatios, debtToEquityRatios,
                peAverage, pbAverage, epsAverage, pfcfAverage, debtToEquityAverage);
    }
    private static List<RatioData> filterAndSortRatios(List<RatioData> ratios, LocalDate timeRange, 
            DateTimeFormatter formatter) {
        return ratios.stream()
                .filter(data -> LocalDate.parse(data.getDate(), formatter).isAfter(timeRange))
                .sorted(Comparator.comparing(data -> LocalDate.parse(data.getDate(), formatter)))
                .collect(Collectors.toList());
    }

    private static List<Double> capValues(List<RatioData> ratios, Double min, Double max) {
        return ratios.stream()
                .map(RatioData::getValue)
                .map(value -> {
                    if (min != null && max != null) {
                        return Math.max(Math.min(value, max), min);
                    }
                    return value;
                })
                .collect(Collectors.toList());
    }

    private static double calculateAverage(List<Double> values) {
        return values.stream()
                .filter(value -> value > 0)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(Double.NaN);
    }

    private static DefaultCategoryDataset createDataset(List<RatioData> ratios, String series, 
            Double min, Double max) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (RatioData data : ratios) {
            double value = data.getValue();
            if (min != null && max != null) {
                value = Math.max(Math.min(value, max), min);
            }
            dataset.addValue(value, series, data.getDate());
        }
        return dataset;
    }

    private static JFreeChart createStyledChart(String title, String xLabel, String yLabel, 
            DefaultCategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createBarChart(
                title, xLabel, yLabel,
                dataset, PlotOrientation.VERTICAL,
                true, true, false);
        styleChart(chart);
        return chart;
    }

    private static void styleChart(JFreeChart chart) {
        org.jfree.chart.plot.CategoryPlot plot = chart.getCategoryPlot();
        
        // Background and grid
        chart.setBackgroundPaint(BACKGROUND_COLOR);
        plot.setBackgroundPaint(BACKGROUND_COLOR);
        plot.setRangeGridlinePaint(GRID_LINE_COLOR);
        plot.setDomainGridlinePaint(GRID_LINE_COLOR);
        
        // Bar styling
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);
        renderer.setMaximumBarWidth(0.1);
        
        // Gradient paint
        GradientPaint gradientPaint = new GradientPaint(
            0.0f, 0.0f, BAR_COLOR,
            0.0f, 0.0f, BAR_COLOR.darker()
        );
        renderer.setSeriesPaint(0, gradientPaint);

        // Axis styling
        CategoryAxis domainAxis = plot.getDomainAxis();
        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
        domainAxis.setTickLabelFont(LABEL_FONT);
        domainAxis.setLabelFont(LABEL_FONT);
        domainAxis.setTickLabelPaint(LABEL_COLOR);
        domainAxis.setLabelPaint(LABEL_COLOR);

        org.jfree.chart.axis.ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setTickLabelFont(LABEL_FONT);
        rangeAxis.setLabelFont(LABEL_FONT);
        rangeAxis.setTickLabelPaint(LABEL_COLOR);
        rangeAxis.setLabelPaint(LABEL_COLOR);

        // Title and legend styling
        chart.getTitle().setFont(TITLE_FONT);
        chart.getTitle().setPaint(TITLE_COLOR);
        chart.getLegend().setItemFont(LABEL_FONT);
        chart.getLegend().setBackgroundPaint(BACKGROUND_COLOR);
    }

    private static void addChartToPanel(JPanel panel, JFreeChart chart) {
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(1100, 400));
        chartPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(5, 5, 5, 5),
            BorderFactory.createLineBorder(GRID_LINE_COLOR)
        ));
        panel.add(chartPanel);
    }

    private static void addMarker(JFreeChart chart, double value, String label) {
        ValueMarker marker = new ValueMarker(value);
        marker.setLabel(String.format("%s: %.2f", label, value));
        marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        marker.setLabelTextAnchor(TextAnchor.BOTTOM_RIGHT);
        marker.setLabelFont(LABEL_FONT);
        marker.setPaint(MARKER_LINE_COLOR);
        marker.setStroke(new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 
                1.0f, new float[] {2.0f, 2.0f}, 0.0f));
        chart.getCategoryPlot().addRangeMarker(marker, Layer.FOREGROUND);
    }

    private static void saveDataToFile(String ticker, List<RatioData> peRatios, List<RatioData> pbRatios,
            List<RatioData> epsRatios, List<RatioData> pfcfRatios, List<RatioData> debtToEquityRatios,
            double peAverage, double pbAverage, double epsAverage, double pfcfAverage, 
            double debtToEquityAverage) {
        JSONObject jsonObject = new JSONObject();
        
        jsonObject.put("ticker", ticker);
        jsonObject.put("lastUpdated", LocalDate.now().toString());
        jsonObject.put("peAverage", peAverage);
        jsonObject.put("pbAverage", pbAverage);
        jsonObject.put("epsAverage", epsAverage);
        jsonObject.put("pfcfAverage", pfcfAverage);
        jsonObject.put("debtToEquityAverage", debtToEquityAverage);

        addRatiosToJson(jsonObject, "peRatios", peRatios, 0.0, 40.0);
        addRatiosToJson(jsonObject, "pbRatios", pbRatios, null, null);
        addRatiosToJson(jsonObject, "epsRatios", epsRatios, null, null);
        addRatiosToJson(jsonObject, "pfcfRatios", pfcfRatios, -50.0, 50.0);
        addRatiosToJson(jsonObject, "debtToEquityRatios", debtToEquityRatios, null, null);

        try (FileWriter file = new FileWriter("data/" + ticker + "_overview.json")) {
            file.write(jsonObject.toString(2));
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void addRatiosToJson(JSONObject jsonObject, String key, List<RatioData> ratios, 
            Double min, Double max) {
        JSONArray ratioArray = new JSONArray();
        for (RatioData data : ratios) {
            JSONObject dataObject = new JSONObject();
            dataObject.put("date", data.getDate());
            
            double value = data.getValue();
            if (min != null && max != null) {
                value = Math.max(Math.min(value, max), min);
            }
            dataObject.put("value", value);
            
            ratioArray.put(dataObject);
        }
        jsonObject.put(key, ratioArray);
    }
}
