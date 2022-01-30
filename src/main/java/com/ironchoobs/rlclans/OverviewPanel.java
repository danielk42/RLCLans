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

public class OverviewPanel extends CollapsiblePanel {

    private final DataProvider dataProvider = DataProvider.instance();
    private final PlayerGroup group;

    private final LeaderboardPanel topDailyGainsPanel;
    private final RecentAchievementsPanel recentAchievementsPanel;

    public OverviewPanel(Font headerFont, PlayerGroup group, boolean lazyLoad) {
        super();

        this.group = group;

        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        JLabel label = new JLabel("Group Overview");
        label.setFont(headerFont);
        header.add(label);
        header.add(Box.createHorizontalGlue());

        JPanel tdgLabelPanel = new JPanel();
        tdgLabelPanel.setLayout(new BoxLayout(tdgLabelPanel, BoxLayout.LINE_AXIS));
        JLabel tdgLabel = new JLabel("Top Gains Today");
        tdgLabel.setFont(headerFont);
        tdgLabelPanel.add(Box.createHorizontalGlue());
        tdgLabelPanel.add(tdgLabel);
        tdgLabelPanel.add(Box.createHorizontalGlue());
        body.add(tdgLabelPanel);
        topDailyGainsPanel = new LeaderboardPanel(group.id,
                WomMetric.overall, WomPeriod.day, 3, PanelAlignment.LEFT);
        body.add(topDailyGainsPanel);

        body.add(Box.createRigidArea(new Dimension(0, 5)));

        JPanel recentAchLabelPanel = new JPanel();
        recentAchLabelPanel.setLayout(new BoxLayout(recentAchLabelPanel, BoxLayout.LINE_AXIS));
        JLabel achLabel = new JLabel("Recent Achievements");
        achLabel.setFont(headerFont);
        recentAchLabelPanel.add(Box.createHorizontalGlue());
        recentAchLabelPanel.add(achLabel);
        recentAchLabelPanel.add(Box.createHorizontalGlue());
        body.add(recentAchLabelPanel);
        recentAchievementsPanel = new RecentAchievementsPanel(group.id, 5);
        body.add(recentAchievementsPanel);

        // for testing
        this.setBorder(new LineBorder(Color.WHITE, 1));

        // Overview elements here

        if (!lazyLoad) {
            loadData();
        }

        // TODO: Add refresh button to reload all data
    }

    @Override
    public void setCollapsed(boolean collapsed) {
        super.setCollapsed(collapsed);
        if (!collapsed) {
            loadData();
        }

        // TODO: Refresh button on this panel should call refresh() on child LeaderboardPanels

        // TODO: Reload button should call loadData()
    }

    // Can be called multiple times, but will only try to load data again if it failed last time.
    private void loadData() {
        topDailyGainsPanel.loadData();
        recentAchievementsPanel.loadData();
    }

    // Call to reload all data
    public void refresh() {
        topDailyGainsPanel.refresh();
        recentAchievementsPanel.refresh();
    }
}
