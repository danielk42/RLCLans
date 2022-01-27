package com.ironchoobs.rlclans;

import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;

/*
NOTE: This panel and others like it (displaying info) should be capable of lazy-loading their data
OR loading on startup based on user preference.

When lazy loading, panels should display a loading message and then store the data for later use.
Panels should provide a refresh button where applicable.
 */

public class OverviewPanel extends JPanel {

    private final JPanel headerPanel = new JPanel();
    private final JPanel bodyPanel = new JPanel();
    private final DataProvider dataProvider = DataProvider.instance();
    private final JLabel errorLabel = new JLabel();
    private final JLabel topPlayer = new JLabel();
    private final JLabel topPlayerGained = new JLabel();
    private final PlayerGroup group;
    private boolean loaded = false;

    public OverviewPanel(Font headerFont, Dimension padding, PlayerGroup group, boolean lazyLoad) {
        super();

        this.group = group;

        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.LINE_AXIS));
        JLabel label = new JLabel("Group Overview");
        label.setFont(headerFont);
        headerPanel.add(label);
        headerPanel.add(Box.createHorizontalGlue());

        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.PAGE_AXIS));

        JPanel topPanelLayout = new JPanel();
        topPanelLayout.setLayout(new BoxLayout(topPanelLayout, BoxLayout.LINE_AXIS));
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.PAGE_AXIS));
        JLabel topLabel = new JLabel("Monthly Top Player");
        topLabel.setFont(headerFont);

        topPanel.add(topLabel);
        topPanel.add(topPlayer);
        topPanel.add(topPlayerGained);
        topPanelLayout.add(topPanel);
        topPanelLayout.add(Box.createHorizontalGlue());
        bodyPanel.add(topPanelLayout);

        topPanelLayout.setVisible(false);

        errorLabel.setVisible(false);

        this.add(headerPanel);
        this.add(Box.createRigidArea(padding));
        this.add(bodyPanel);
        this.add(Box.createVerticalGlue());
        this.add(errorLabel);

        // for testing
        this.setBorder(new LineBorder(Color.WHITE, 1));

        // Overview elements here

        if (!lazyLoad) {
            loadData();
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        loadData();
    }

    // Can be called multiple times, but will only try to load data again if it failed last time.
    private void loadData() {
        if (!loaded) {
            errorLabel.setVisible(true);
            errorLabel.setText("Loading");

            loaded = true; // say we loaded already so we don't try to load twice
            dataProvider.getTopMember(group.id, tm -> {
                // TODO: Setup Top Member UI elements

                topPlayer.setText(tm.player.username);
                topPlayerGained.setText(tm.gained + " xp");

                errorLabel.setVisible(false);

            }, error -> {
                errorLabel.setText("Error loading top member");
                loaded = false; // Didn't load, will try again if loadData is called again
                // TODO: Refresh button
            });
        }
    }

    // Call to reload data
    public void reload() {
        loaded = false;
        loadData();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) { return true; }

        if (o instanceof String) {
            return ((String) o).compareTo(group.name) == 0;
        }
        return false;
    }
}
