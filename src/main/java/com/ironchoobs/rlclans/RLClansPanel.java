package com.ironchoobs.rlclans;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
class RLClansPanel extends PluginPanel {

    // UI

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
    private final WomProvider womProvider = new WomProvider();


    // Containers for WOM data (Gson deserializes data from WOM into these)
    private Player player;
    private final ArrayList<PlayerCompetition> competitions = new ArrayList<>();
    private final ArrayList<PlayerGroup> groups = new ArrayList<>();

    RLClansPanel(RLClansPlugin plugin, RLClansConfig config, Client client, SkillIconManager iconManager) {
        super();

        // Border & layout for this PluginPanel
        setBorder(new EmptyBorder(6, 6, 6, 6));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        // Main layout panel (everything is contained in this)
        final JPanel layoutPanel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(layoutPanel, BoxLayout.Y_AXIS);
        layoutPanel.setLayout(boxLayout);
        add(layoutPanel, BorderLayout.NORTH);

        // Font for panel names
        Font smallText = statusLabel.getFont().deriveFont(15.0f);

        // Padding sizes for UI elements
        final Dimension headerPadding = new Dimension(0, 5); // between header and body
        final Dimension panelPadding = new Dimension(0, 15); // between each panel

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

        // TODO: PanelManager will own this panel + all other "data" panels
        // TODO: as we only want one panel to display at any given time
        overviewPanel = new OverviewPanel(smallText, headerPadding);
        layoutPanel.add(overviewPanel);
        overviewPanel.setVisible(true);

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

        // NOTE: getPlayerFromWom()'s callback MUST complete before calling other getPlayer* functions!

        // NOTE: We need to know when we have all the info, for now each callback makes the next
        // request so the last one in the chain knows that all the required data has been received
        // and we can start doing stuff with the Player* classes

        // NOTE: If we want getPlayerGroups and getPlayerCompetitions to run at the same time, keep
        // track of when both have finished with thread safe (volatile in java???) bools.
        // Then check in the update cycle whether both have finished and proceed with UI setup from there.

        // Current setup is loggedIn -> getPlayerFromWom -> getPlayerGroups -> getPlayerCompetitions -> buildMainPanel

        // for testing without logging in
        loadStartupData("fartrock");
    }

    protected void loadStartupData(String username) {
        // Get the startup data from WOM
        womProvider.getPlayerFromWom(username, player -> {
            this.player = player;

            statusLabel.setText("Loading Groups");

            womProvider.getPlayerGroups(player.id, groups -> {
                this.groups.clear();
                this.groups.addAll(groups);

                statusLabel.setText("Loading Competitions");

                womProvider.getPlayerCompetitions(player.id, comps -> {
                    this.competitions.clear();
                    this.competitions.addAll(comps);

                    statusLabel.setText("Load success");

                    buildMainPanel();
                });
            });
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

        // TODO: Navigation menu

        // TODO: Setup group overview on active group
    }
}
