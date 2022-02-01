package com.ironchoobs.rlclans.ui.views;

import com.ironchoobs.rlclans.data.Player;
import com.ironchoobs.rlclans.data.PlayerGroup;
import com.ironchoobs.rlclans.DataProvider;
import com.ironchoobs.rlclans.ui.helpers.CollapsiblePanel;

import javax.swing.*;
import java.awt.*;

public class CompetitionsPanel extends CollapsiblePanel {

    private boolean loaded = false;

    private final DataProvider dataProvider = DataProvider.instance();
    private final PlayerGroup group;
    private final Player player;

    public CompetitionsPanel(Font headerFont, PlayerGroup group, Player player, boolean lazyLoad) {
        super();

        this.group = group;
        this.player = player;

        JLabel label = new JLabel("Competitions");
        label.setFont(headerFont);
        header.add(label);
        header.add(Box.createHorizontalGlue());

        if (!lazyLoad) {
            loadData();
        }
    }

    protected void loadData() {

    }

    public void refresh() {

    }
}
