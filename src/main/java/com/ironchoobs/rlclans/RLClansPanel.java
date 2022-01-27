package com.ironchoobs.rlclans;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
class RLClansPanel extends PluginPanel {

    // UI
    // Padding sizes for UI elements
    private final Dimension headerPadding = new Dimension(0, 5); // between header and body
    private final Dimension panelPadding = new Dimension(0, 15); // between each panel
    private Font smallText;

    private final JPanel layoutPanel = new JPanel();
    private final JPanel dataPanel = new JPanel();

    // Status panel
    private final JPanel statusPanel = new JPanel();            // vertical layout
    private final JPanel statusPanelHeader = new JPanel();      // Child of status panel, horizontal layout
    private final JPanel statusPanelBody = new JPanel();        // Child of status panel, horizontal layout
    private final JLabel statusLabel = new JLabel();

    // Navigation panel
    private final JPanel navPanel = new JPanel();               // layout as above
    private final JPanel navPanelHeader = new JPanel();
    private final JPanel navPanelBody = new JPanel();
    private final JComboBox<JLabel> navCombo = new JComboBox<>();

    // Group selection panel
    private GroupSelectPanel groupPanel = null;

    private final DataProvider dataProvider = DataProvider.instance();

    private final Map<String, OverviewPanel> overviewPanels = new HashMap<>();
    private OverviewPanel activeOverviewPanel = null;

    private final RLClansConfig config;

    RLClansPanel(RLClansPlugin plugin, RLClansConfig config, Client client, SkillIconManager iconManager) {
        super();

        this.config = config;

        // Border & layout for this PluginPanel
        setBorder(new EmptyBorder(6, 6, 6, 6));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        // Main layout panel (everything is contained in this)
        BoxLayout boxLayout = new BoxLayout(layoutPanel, BoxLayout.Y_AXIS);
        layoutPanel.setLayout(boxLayout);
        add(layoutPanel, BorderLayout.CENTER);

        // Font for panel names
        smallText = statusLabel.getFont().deriveFont(15.0f);

        // ----------------- Status Panel --------------------------------------------------

        // Status Panel - vertical box layout
        //  -- Status Panel Header - horizontal box layout
        //      -- Status Panel Label
        //      -- Horizontal glue
        //  -- Status Body - horizontal box layout
        //      -- Status label

        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.PAGE_AXIS));

        statusPanelHeader.setLayout(new BoxLayout(statusPanelHeader, BoxLayout.LINE_AXIS));
        JLabel statusPanelLabel = new JLabel("Status");
        statusPanelLabel.setFont(smallText);
        statusPanelHeader.add(statusPanelLabel);
        statusPanelHeader.add(Box.createHorizontalGlue());

        statusPanelBody.setLayout(new BoxLayout(statusPanelBody, BoxLayout.LINE_AXIS));
        statusPanelBody.add(statusLabel);

        statusPanel.add(statusPanelHeader);
        statusPanel.add(Box.createRigidArea(headerPadding));
        statusPanel.add(statusPanelBody);

        layoutPanel.add(statusPanel);
        layoutPanel.add(Box.createRigidArea(panelPadding));

        // ---------------------------------------------------------------------------------

        // ------------------ Group Selector -----------------------------------------------


        groupPanel = new GroupSelectPanel(smallText, group -> {
            if (activeOverviewPanel != null) {
                activeOverviewPanel.setVisible(false);
            }
            if (overviewPanels.containsKey(group.name)) {
                overviewPanels.get(group.name).setVisible(true);
            }
            else {
                activeOverviewPanel = new OverviewPanel(
                        smallText, group, config.lazyLoad());
                overviewPanels.put(group.name, activeOverviewPanel);
                activeOverviewPanel.setVisible(true);

                dataPanel.add(activeOverviewPanel);

                // TODO: Add other data panels here
           }

            statusLabel.setText("Ready");
        });

        layoutPanel.add(groupPanel);

        // ---------------------------------------------------------------------------------

        // Data panel contains all the panels we want deleted upon relogging
        dataPanel.setLayout(new BoxLayout(dataPanel, BoxLayout.PAGE_AXIS));
        layoutPanel.add(dataPanel);

        // for testing without logging in
        loadStartupData("fartrock");
    }

    private void loadStartupData(String username) {
        statusLabel.setText("Loading Player");

        // Get the startup data from WOM
        // This chains http calls back to back and ensures each completes before the next is called.
        // If anything fails along the way an error handler will be called and subsequent callbacks will not be invoked
        dataProvider.getPlayerFromWom(username, player -> {
            statusLabel.setText("Loading Groups");
            groupPanel.setup(player);
        }, error -> {
            if (error == DataProvider.ErrorType.PLAYER_NOT_FOUND) {
                statusLabel.setText("Player not on WOM");

                // TODO: Display button to enable sync with WOM
            }
        });
    }

    protected void loggedIn(Client client) {

        statusLabel.setText("Loading Player");

        // NOTE: In case of re-logging, OverviewPanels & other data panels
        // will still be around from the last login, this is only an issue
        // if a group etc is modified or removed, but to avoid issues it is best
        // to remove them.
        // All panels that should be cleared out upon relogging should be
        // contained by dataPanel.
        activeOverviewPanel = null;
        dataPanel.removeAll();
        overviewPanels.clear();
        // Clear other panel lists here

        loadStartupData(client.getUsername());
    }
}
