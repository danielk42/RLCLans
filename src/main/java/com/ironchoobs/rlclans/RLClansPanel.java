package com.ironchoobs.rlclans;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
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
    private final JPanel groupSelectPanel = new JPanel();
    private final JPanel groupSelectPanelHeader = new JPanel();
    private final JPanel groupSelectPanelBody = new JPanel();
    private final JComboBox<JLabel> clanNamesCombo = new JComboBox<>();
    private final JLabel clanNameLabel = new JLabel();
    private String activeGroup;

    // Group overview panel
    private OverviewPanel overviewPanel;

    private PanelManager panelManager;
    private final DataProvider dataProvider = new DataProvider();


    // Containers for WOM data (Gson deserializes data from WOM into these)
    private Player player;
    private final ArrayList<PlayerCompetition> competitions = new ArrayList<>();
    private final ArrayList<PlayerGroup> groups = new ArrayList<>();
    //private final ArrayList<OverviewPanel> overviewPanels = new ArrayList<>();
    private final Map<String, OverviewPanel> overviewPanels = new HashMap<>();

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
        add(layoutPanel, BorderLayout.NORTH);

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

        groupSelectPanel.setLayout(new BoxLayout(groupSelectPanel, BoxLayout.PAGE_AXIS));

        groupSelectPanelHeader.setLayout(new BoxLayout(groupSelectPanelHeader, BoxLayout.LINE_AXIS));
        JLabel groupSelectPanelLabel = new JLabel("Group");
        groupSelectPanelLabel.setFont(smallText);
        groupSelectPanelHeader.add(groupSelectPanelLabel);
        groupSelectPanelHeader.add(Box.createHorizontalGlue());

        groupSelectPanelBody.setLayout(new BoxLayout(groupSelectPanelBody, BoxLayout.LINE_AXIS));
        groupSelectPanelBody.add(clanNameLabel);
        groupSelectPanelBody.add(clanNamesCombo);
        clanNameLabel.setVisible(false);        // Enabled if only one group is found
        clanNamesCombo.setVisible(false);       // Enabled if multiple groups are found

        groupSelectPanel.add(groupSelectPanelHeader);
        groupSelectPanel.add(Box.createRigidArea(headerPadding));
        groupSelectPanel.add(groupSelectPanelBody);

        layoutPanel.add(groupSelectPanel);
        layoutPanel.add(Box.createRigidArea(panelPadding));

        // ---------------------------------------------------------------------------------

        // ------------------ Navigation Panel ---------------------------------------------

        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.PAGE_AXIS));

        navPanelHeader.setLayout(new BoxLayout(navPanelHeader, BoxLayout.LINE_AXIS));
        JLabel navPanelLabel = new JLabel("Navigation");
        navPanelLabel.setFont(smallText);
        navPanelHeader.add(navPanelLabel);
        navPanelHeader.add(Box.createHorizontalGlue());

        navPanelBody.setLayout(new BoxLayout(navPanelBody, BoxLayout.LINE_AXIS));
        navPanelBody.add(navCombo);
        navCombo.setVisible(false);

        navPanel.add(navPanelHeader);
        navPanel.add(Box.createRigidArea(headerPadding));
        navPanel.add(navPanelBody);

        layoutPanel.add(navPanel);
        layoutPanel.add(Box.createRigidArea(panelPadding));

        // ---------------------------------------------------------------------------------

        // ------------------ Group Overview -----------------------------------------------

        // Create OverviewPanels after loading groups, one for each group.
        //overviewPanel = new OverviewPanel(smallText, headerPadding, dataProvider, config.lazyLoad());
        //layoutPanel.add(overviewPanel);
        //overviewPanel.setVisible(true);

        // ---------------------------------------------------------------------------------


        panelManager = new PanelManager();


        // TODO: Create other panels (competitions, clan stats, gains, stuff like that)


        // Add action listeners on startup to avoid double-adding when re-logging in
        // NOTE: This isn't called when we manually set the active element
        // Handles group selection
        clanNamesCombo.addActionListener(e -> {
            if (clanNamesCombo.getSelectedItem() != null) {
                JLabel l = (JLabel) clanNamesCombo.getSelectedItem();
                activeGroup = l.getText();

                // TODO: Active group is set, load stuff
            }
        });

        // for testing without logging in
        loadStartupData("fartrock");
    }

    private void loadStartupData(String username) {
        // Get the startup data from WOM
        // This chains http calls back to back and ensures each completes before the next is called.
        // If anything fails along the way an error handler will be called and subsequent callbacks will not be invoked
        dataProvider.getPlayerFromWom(username, player -> {
            this.player = player;

            statusLabel.setText("Loading Groups");

            dataProvider.getPlayerGroups(player.id, groups -> {
                this.groups.clear();
                this.groups.addAll(groups);

                this.overviewPanels.clear();
                for (PlayerGroup g : groups) {
                    OverviewPanel p = new OverviewPanel(smallText, headerPadding, dataProvider, g, config.lazyLoad());
                    overviewPanels.put(g.name, p);
                    layoutPanel.add(p);
                    p.setVisible(false);
                }

                statusLabel.setText("Loading complete");
                buildMainPanel();
            }, error -> {
                if (error == DataProvider.ErrorType.GROUP_NOT_FOUND) {
                    statusLabel.setText("No groups found");
                }
                // TODO: Connection error handling & clear groupPanels list on fail
            });
        }, error -> {
            if (error == DataProvider.ErrorType.PLAYER_NOT_FOUND) {
                statusLabel.setText("Player not on WOM");

                // TODO: Display button to enable sync with WOM
            }
        });
    }

    protected void loggedIn(Client client) {

        statusLabel.setText("Loading Player");
        loadStartupData(client.getUsername());

        // NOTE: It's possible to start the loading sequence as soon as a username is present however
        // threading makes it more difficult to stop the process if the user types a different username.
        // For now, we wait until the user has fully logged in before loading data from WOM.
    }

    // Call when all startup data from WOM has been retrieved
    // Only call if the player is in a group (competitions aren't necessary).
    // Sets up the "main" panel showing an overview of the group. More detailed info (ie competitions)
    // will be displayed in separate "pages" of the UI.
    private void buildMainPanel() {

        // NOTE: Can be called more than once if the player logs in and out, so keep the actual
        // instance creation in the constructor and just set the data and show/hide stuff here.

        log.info("buildMainPanel() called, group size: " + groups.size());

        // Hide status label (might keep it as a startup message)
        //statusLabel.setVisible(false);

        // If the player is in multiple groups we want a drop down box to set the "active" group.
        // If not, a label for the group name will be fine
        // If player isn't in any groups buildMainPanel will not be called (may instead display a different UI later)
        if (groups.size() == 1) {
            // Player is in only one group
            // Show group name label
            clanNameLabel.setVisible(true);
            clanNameLabel.setText(groups.get(0).name);

            //log.info("Group name: " + groups.get(0).name);

            activeGroup = groups.get(0).name;

            // If there are any competitions, show a button or something to navigate to the competition panel.
        }
        else {
            // Multiple groups, show group drop-down box

            clanNamesCombo.setVisible(true);
            clanNamesCombo.removeAllItems();
            for (PlayerGroup g : groups) {
                JLabel l = new JLabel(g.name);
                clanNamesCombo.addItem(l);
            }

            clanNamesCombo.setSelectedIndex(0);  // TODO: Default group setting
            activeGroup = ((JLabel) clanNamesCombo.getSelectedItem()).getText();  // remember to set active group

        }

        overviewPanels.get(activeGroup).setVisible(true);
        overviewPanels.get(activeGroup).loadData();

        // TODO: Navigation menu

        // TODO: Setup group overview on active group
    }
}
