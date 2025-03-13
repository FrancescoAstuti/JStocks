package afin.jstocks;

import javax.swing.*;

import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.Color;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import java.io.FileOutputStream;

public class CellsFormat {
    // Soft base colors
    private static final Color LIGHT_RED = new Color(255, 235, 235);
    private static final Color LIGHT_YELLOW = new Color(255, 255, 220);
    
    // Soft Graham Number colors
    private static final Color LIGHT_GREEN = new Color(220, 255, 220);    // Very soft green
    private static final Color MEDIUM_GREEN = new Color(198, 255, 198);   // Soft mint green
    private static final Color DARK_GREEN = new Color(178, 255, 178);     // Pastel green
    
    private static final Color LIGHT_PINK = new Color(255, 230, 230);     // Very soft pink
    private static final Color MEDIUM_PINK = new Color(255, 200, 200);    // Soft pink
    private static final Color DARK_PINK = new Color(255, 180, 180);      // Pastel pink

    public static class CustomCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component cell = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            if (!isSelected) { // Don't change background if cell is selected
                String columnName = table.getColumnName(column);
                
                if (columnName.equals("Graham Number") && value instanceof Double) {
                    double grahamNumber = (Double) value;
                    // Get the price from the "Price" column
                    double price = getPriceForRow(table, row);
                    if (price > 0) {
                        double percentDiff = (grahamNumber - price) / price * 100;
                        setBackgroundColor(cell, percentDiff);
                        cell.setForeground(new Color(51, 51, 51));
                        return cell;
                    }
                } else if (columnName.equals("Debt to Equity") && value instanceof Double) {
                    double debtToEquity = (Double) value;
                    double deAvg = getAverageForRow(table, row, "DE Avg");
                    if (deAvg > 0 && debtToEquity > 0) {
                        double ratio = debtToEquity / deAvg;
                        setBackgroundColor(cell, ratio, true);
                        cell.setForeground(new Color(51, 51, 51));
                        return cell;
                    }
                /*} else if (columnName.equals("PE TTM") && value instanceof Double) {
                    double peTtm = (Double) value;
                    double peAvg = getAverageForRow(table, row, "PE Avg");
                    if (peAvg > 0 && peTtm > 0) {
                        double ratio = peTtm / peAvg;
                        setBackgroundColor(cell, ratio);
                        cell.setForeground(new Color(51, 51, 51));
                        return cell;
                    }*/
                
                
                
                } else if (columnName.equals("PB TTM") && value instanceof Double) {
                    double pbTtm = (Double) value;
                    double pbAvg = getAverageForRow(table, row, "PB Avg");
                    if (pbAvg > 0 && pbTtm > 0) {
                        double ratio = pbTtm / pbAvg;
                        setBackgroundColor(cell, ratio);
                        cell.setForeground(new Color(51, 51, 51));
                        return cell;
                    }
                } else if (columnName.equals("P/FCF") && value instanceof Double) {
                    double pfcfTtm = (Double) value;
                    double pfcfAvg = getAverageForRow(table, row, "PFCF Avg");
                    if (pfcfAvg > 0 && pfcfTtm > 0) {
                        double ratio = pfcfTtm / pfcfAvg;
                        setBackgroundColor(cell, ratio);
                        cell.setForeground(new Color(51, 51, 51));
                        return cell;
                    }
                } else if (columnName.equals("ROE TTM") && value instanceof Double) {
                    double roeTtm = (Double) value;
                    double roeAvg = getAverageForRow(table, row, "ROE Avg");
                    if (roeAvg > 0 && roeTtm > 0) {
                        double ratio = roeTtm / roeAvg;
                        setBackgroundColor(cell, ratio, false);
                        cell.setForeground(new Color(51, 51, 51));
                        return cell;
                    }
                }

                // Default coloring for other cells
                if (value instanceof Double) {
                    double numValue = (Double) value;
                    if (numValue < 0) {
                        cell.setBackground(LIGHT_RED);
                    } else if (numValue == 0.0) {
                        cell.setBackground(LIGHT_YELLOW);
                    } else {
                        cell.setBackground(Color.WHITE);
                    }
                } else if ("n/a".equals(value)) {
                    cell.setBackground(Color.LIGHT_GRAY);
                } else {
                    cell.setBackground(Color.WHITE);
                }
                cell.setForeground(Color.BLACK); // Reset text color for non-special cells
            }
            
            return cell;
        }

        private static double getPriceForRow(JTable table, int row) {
            int priceColumn = -1;
            for (int i = 0; i < table.getColumnCount(); i++) {
                if (table.getColumnName(i).equals("Price")) {
                    priceColumn = i;
                    break;
                }
            }
            if (priceColumn != -1) {
                Object value = table.getValueAt(row, priceColumn);
                if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                }
            }
            return 0.0;
        }

        private static double getAverageForRow(JTable table, int row, String columnName) {
            int column = -1;
            for (int i = 0; i < table.getColumnCount(); i++) {
                if (table.getColumnName(i).equals(columnName)) {
                    column = i;
                    break;
                }
            }
            if (column != -1) {
                Object value = table.getValueAt(row, column);
                if (value instanceof Number) {
                    return ((Number) value).doubleValue();
                }
            }
            return 0.0;
        }

        private static void setBackgroundColor(Component cell, double value) {
            setBackgroundColor(cell, value, false);
        }

        private static void setBackgroundColor(Component cell, double value, boolean isRatio) {
            if (isRatio) {
                if (value < 1) {
                    if (value >= 0.75) {
                        cell.setBackground(LIGHT_GREEN);
                    } else if (value >= 0.5) {
                        cell.setBackground(MEDIUM_GREEN);
                    } else {
                        cell.setBackground(DARK_GREEN);
                    }
                } else {
                    if (value <= 1.25) {
                        cell.setBackground(LIGHT_PINK);
                    } else if (value <= 1.5) {
                        cell.setBackground(MEDIUM_PINK);
                    } else {
                        cell.setBackground(DARK_PINK);
                    }
                }
            } else {
                if (value > 0) {
                    if (value <= 25) {
                        cell.setBackground(LIGHT_GREEN);
                    } else if (value <= 50) {
                        cell.setBackground(MEDIUM_GREEN);
                    } else {
                        cell.setBackground(DARK_GREEN);
                    }
                } else {
                    value = Math.abs(value);
                    if (value <= 25) {
                        cell.setBackground(LIGHT_PINK);
                    } else if (value <= 50) {
                        cell.setBackground(MEDIUM_PINK);
                    } else {
                        cell.setBackground(DARK_PINK);
                    }
                }
            }
        }
    }

    public static XSSFCellStyle createCustomColorStyle(XSSFWorkbook workbook, byte[] rgb) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFColor color = new XSSFColor(rgb, null);
        style.setFillForegroundColor(color);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }
}