package com.ironchoobs.rlclans;

import javax.swing.*;
import java.awt.*;

// Represents a statistic of a player, ie an achievement, leaderboard standing etc
public class StatPanel extends JPanel {

    private final String name;
    private final String stat;
    private final JLabel nameLabel = new JLabel();
    private final JLabel achievement = new JLabel();

    public StatPanel(String name, String stat, PanelAlignment alignment) {
        super();

        this.name = name;
        this.stat = stat;
        this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        nameLabel.setText(name);
        achievement.setText(stat);

        if (alignment == PanelAlignment.RIGHT) {
            this.add(Box.createHorizontalGlue());
        }
        this.add(nameLabel);
        this.add(Box.createRigidArea(new Dimension(5, 0)));
        this.add(achievement);
        if (alignment == PanelAlignment.LEFT) {
            this.add(Box.createHorizontalGlue());
        }
    }
}
