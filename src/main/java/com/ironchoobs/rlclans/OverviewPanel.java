package com.ironchoobs.rlclans;

import javax.swing.*;
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
    private final DataProvider dataProvider;
    private final JLabel errorLabel = new JLabel();
    private final PlayerGroup group;
    private boolean loaded = false;

    public OverviewPanel(Font headerFont, Dimension padding, DataProvider provider, PlayerGroup group, boolean lazyLoad) {
        super();

        dataProvider = provider;
        this.group = group;

        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.LINE_AXIS));
        JLabel label = new JLabel("Group Overview");
        label.setFont(headerFont);
        headerPanel.add(label);
        headerPanel.add(Box.createHorizontalGlue());

        bodyPanel.setLayout(new BoxLayout(bodyPanel, BoxLayout.LINE_AXIS));

        errorLabel.setVisible(false);

        this.add(headerPanel);
        this.add(Box.createRigidArea(padding));
        this.add(bodyPanel);
        this.add(Box.createVerticalGlue());
        this.add(errorLabel);

        // Overview elements here

        if (!lazyLoad) {
            loadData();
        }
    }

    // Can be called multiple times, but will only try to load data again if it failed last time.
    public void loadData() {
        if (!loaded) {
            errorLabel.setVisible(true);
            errorLabel.setText("Loading");

            loaded = true; // say we loaded already so we don't try to load twice
            dataProvider.getTopMember(group.id, tm -> {
                // Setup UI stuff

                errorLabel.setVisible(false);

            }, error -> {
                errorLabel.setText("Error loading top member");
                loaded = false; // Didn't load, will try again if loadData is called again
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
