package com.ironchoobs.rlclans;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class RecentAchievementsPanel extends JPanel {

    private boolean loaded = false;

    private final JLabel statusLabel = new JLabel();
    private final DataProvider dataProvider = DataProvider.instance();

    private final int groupId;
    private final int limit;
    private final int offset = 0;

    private final List<StatPanel> achPanels = new ArrayList<>();

    public RecentAchievementsPanel(int groupId, int limit) {
        super();

        this.groupId = groupId;
        this.limit = limit;

        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.LINE_AXIS));
        statusPanel.add(Box.createHorizontalGlue());
        statusPanel.add(statusLabel);
        statusPanel.add(Box.createHorizontalGlue());
        this.add(statusPanel);

        loadData();
    }

    public void loadData() {
        if (!loaded) {
            loaded = true;
            statusLabel.setText("Loading");

            dataProvider.getRecentAchievements(groupId, limit, offset, achs -> {
                for (PlayerAchievement ach : achs) {
                    StatPanel p = new StatPanel(
                            ach.player.displayName, ach.name, PanelAlignment.LEFT
                    );
                    achPanels.add(p);
                    this.add(p);
                }
                statusLabel.setVisible(false);
            }, error -> {
                statusLabel.setText("Load failed");
                // TODO: retry
                loaded = false;
            });
        }
    }

    public void refresh() {
        loaded = false;
        for (StatPanel p : achPanels) {
            this.remove(p);
        }
        achPanels.clear();
        loadData();
    }
}
