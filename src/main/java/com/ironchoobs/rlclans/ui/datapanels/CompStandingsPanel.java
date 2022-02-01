package com.ironchoobs.rlclans.ui.datapanels;

import com.ironchoobs.rlclans.DataProvider;
import com.ironchoobs.rlclans.data.PlayerCompetition;
import com.ironchoobs.rlclans.ui.helpers.StatPanel;

import javax.swing.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CompStandingsPanel extends JPanel {

    private boolean loaded = false;

    private final JLabel statusLabel = new JLabel();
    private final DataProvider dataProvider = DataProvider.instance();

    private final int playerId;

    // Whether to include past competitions or not.
    private final boolean onlyCurrent;

    // Whether to show results around the player or the top results
    private final boolean scrollToPlayer;

    // How many results to display
    private final int count;
    private final int offset = 0;

    private final List<StatPanel> standingPanels = new ArrayList<>();

    private final List<PlayerCompetition> currentComps = new ArrayList<>();
    private final List<PlayerCompetition> oldComps = new ArrayList<>();

    public CompStandingsPanel(int playerId, boolean onlyCurrent, boolean scrollToPlayer,
                              int count) {
        super();

        this.playerId = playerId;
        this.onlyCurrent = onlyCurrent;
        this.scrollToPlayer = scrollToPlayer;
        this.count = count;

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
                for (PlayerCompetition pc : pcl) {
                    if (pc.endsAt.before(new Date())) {
                        // Competition has finished already
                        if (!onlyCurrent) {
                            oldComps.add(pc);
                            // TODO: Get standings (maybe 5 total)
                        }
                    }
                    else {
                        // Competition is current
                        currentComps.add(pc);
                    }
                }


            }, error -> {
                if (error == DataProvider.ErrorType.COMPETITION_NOT_FOUND) {

                }
            });
        }
    }
}
