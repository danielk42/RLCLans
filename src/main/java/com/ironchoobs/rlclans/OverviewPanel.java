package com.ironchoobs.rlclans;

import javax.swing.*;
import java.awt.*;

public class OverviewPanel extends JPanel {

    private final JPanel headerPanel = new JPanel();
    private final JPanel bodyPanel = new JPanel();

    public OverviewPanel(Font headerFont, Dimension padding) {
        super();

        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.LINE_AXIS));
        JLabel label = new JLabel("Group Overview");
        label.setFont(headerFont);
        headerPanel.add(label);
        headerPanel.add(Box.createHorizontalGlue());

        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.LINE_AXIS));

        this.add(headerPanel);
        this.add(Box.createRigidArea(padding));
        this.add(bodyPanel);

        // Overview elements here
    }

}
