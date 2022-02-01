package com.ironchoobs.rlclans.ui.datapanels;

import com.ironchoobs.rlclans.DataProvider;
import com.ironchoobs.rlclans.ui.helpers.StatPanel;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class CompStandingsPanel extends JPanel {

    private boolean loaded = false;

    private final JLabel statusLabel = new JLabel();
    private final DataProvider dataProvider = DataProvider.instance();

    private final int playerId;
    private final int limit;
    private final int offset = 0;

    private final List<StatPanel> standingPanels = new ArrayList<>();


    public CompStandingsPanel(int playerId, int limit) {
        super();

        this.playerId = playerId;
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

    protected void loadData() {
        if (!loaded) {
            loaded = true;
            statusLabel.setText("Loading");

            dataProvider.getPlayerCompetitions(playerId, pcl -> {

            }, error -> {
                if (error == DataProvider.ErrorType.COMPETITION_NOT_FOUND) {

                }
            });
        }
    }
}
