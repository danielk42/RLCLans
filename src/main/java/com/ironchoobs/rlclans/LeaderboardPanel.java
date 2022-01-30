package com.ironchoobs.rlclans;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class LeaderboardPanel extends JPanel {

    // True if leaderboard is based on a time period (WomPeriod),
    // False if based on a startDate/endDate.
    private final boolean periodBased;

    private boolean loaded = false;

    private final JLabel statusLabel = new JLabel();
    private final DataProvider dataProvider = DataProvider.instance();

    private final int groupId;
    private final WomMetric metric;
    private final WomPeriod period;
    private final Date startDate;
    private final Date endDate;
    private final int limit;
    private final PanelAlignment alignment;

    private List<StatPanel> lbPanels = new ArrayList<>();

    public LeaderboardPanel(int groupId, WomMetric metric, WomPeriod period, int limit,
                            PanelAlignment alignment) {
        super();

        periodBased = true;

        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.LINE_AXIS));
        statusPanel.add(Box.createHorizontalGlue());
        statusPanel.add(statusLabel);
        statusPanel.add(Box.createHorizontalGlue());
        this.add(statusPanel);

        this.alignment = alignment;
        this.groupId = groupId;
        this.metric = metric;
        this.period = period;
        this.startDate = null;
        this.endDate = null;
        this.limit = limit;

        loadData();
    }

    public LeaderboardPanel(int groupId, WomMetric metric, Date startDate, Date endDate,
                            int limit, PanelAlignment alignment) {
        super();

        periodBased = false;

        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.add(statusLabel);

        this.alignment = alignment;
        this.groupId = groupId;
        this.metric = metric;
        this.startDate = startDate;
        this.endDate = endDate;
        this.limit = limit;
        this.period = null;

        loadData();
    }

    // Try to load data that failed to load
    public void loadData() {
        if (!loaded && periodBased) {
            loaded = true;
            statusLabel.setText("Loading");

            dataProvider.getGroupLeaderboard(groupId, metric, period, limit, 0, entries -> {
                for (LeaderboardEntry le : entries) {
                    StatPanel p = new StatPanel(
                            le.player.displayName, le.gained + " xp", alignment
                    );
                    lbPanels.add(p);
                    this.add(p);
                }
                statusLabel.setVisible(false);
            }, error -> {
                statusLabel.setText("Load failed");
                // TODO: refresh button
                loaded = false;
            });
        }
        else if (!loaded && !periodBased) {
            loaded = true;
            statusLabel.setText("loading");

            dataProvider.getGroupLeaderboard(groupId, metric, startDate, endDate, limit, 0, entries -> {
                for (LeaderboardEntry le : entries) {
                    StatPanel p = new StatPanel(
                            le.player.displayName, le.gained + " xp", alignment
                    );
                    lbPanels.add(p);
                    this.add(p);
                }
                statusLabel.setVisible(false);
            }, error -> {
                statusLabel.setText("Load failed");
                // TODO: refresh button
                loaded = false;
            });
        }
    }

    // Reload all data
    public void refresh() {
        loaded = false;
        for (StatPanel p : lbPanels) {
            this.remove(p);
        }
        lbPanels.clear();
        loadData();
    }
}
