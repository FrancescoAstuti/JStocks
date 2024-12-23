package afin.jstocks;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;

public class CompanyOverview {

    public static void showCompanyOverview(String ticker, String companyName) {
        JFrame overviewFrame = new JFrame("Company Overview: " + ticker);
        overviewFrame.setSize(400, 200);
        overviewFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("Company Name: " + companyName, SwingConstants.CENTER);
        panel.add(label, BorderLayout.CENTER);

        overviewFrame.add(panel);
        overviewFrame.setLocationRelativeTo(null);
        overviewFrame.setVisible(true);
    }
}