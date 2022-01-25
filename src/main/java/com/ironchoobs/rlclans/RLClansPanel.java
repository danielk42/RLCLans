package com.ironchoobs.rlclans;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.widgets.WidgetInfo;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
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

    // Navigation panel
    private final JPanel navPanel = new JPanel();               // layout as above
    private final JPanel navPanelHeader = new JPanel();
    private final JPanel navPanelBody = new JPanel();

    private final JPanel groupSelectorPanel = new JPanel();
    private final JPanel groupOverviewPanel = new JPanel();

    //private final JPanel mainPanel = new JPanel();
    private final JLabel statusLabel = new JLabel(); // testing only
    private final JLabel clanNameLabel = new JLabel();
    private final JComboBox<JLabel> clanNames = new JComboBox<JLabel>();

    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    // Containers for WOM data
    private Player player;
    private final ArrayList<PlayerCompetition> competitions = new ArrayList<PlayerCompetition>();
    private final ArrayList<PlayerGroup> groups = new ArrayList<PlayerGroup>();

    private String activeGroup;

    // Wise Old Man base API address
    private final String womUrl = "https://api.wiseoldman.net";

    RLClansPanel(RLClansPlugin plugin, RLClansConfig config, Client client, SkillIconManager iconManager) {
        super();

        // TODO: Present "waiting for login" panel

        httpClient = HttpClient.newHttpClient();

        setBorder(new EmptyBorder(6, 6, 6, 6));
        setBackground(ColorScheme.DARK_GRAY_COLOR);
        setLayout(new BorderLayout());

        final JPanel layoutPanel = new JPanel();
        BoxLayout boxLayout = new BoxLayout(layoutPanel, BoxLayout.Y_AXIS);
        layoutPanel.setLayout(boxLayout);
        add(layoutPanel, BorderLayout.NORTH);

        // Font for panel names
        Font smallText = statusLabel.getFont().deriveFont(15.0f);



        // ----------------- Status Panel --------------------------------------------------

        // Status Panel - vertical box layout
        //  -- Status Panel Header - horizontal box layout
        //      -- Status Panel Label
        //      -- Horizontal glue
        //  -- Status Body - horizontal box layout
        //      -- Status label

        statusPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.PAGE_AXIS));

        statusPanelHeader.setBackground(ColorScheme.DARK_GRAY_COLOR);
        statusPanelHeader.setLayout(new BoxLayout(statusPanelHeader, BoxLayout.LINE_AXIS));
        JLabel statusPanelLabel = new JLabel("Status");
        statusPanelLabel.setFont(smallText);
        statusPanelHeader.add(statusPanelLabel);
        statusPanelHeader.add(Box.createHorizontalGlue());

        statusPanelBody.setBackground(ColorScheme.DARK_GRAY_COLOR);
        statusPanelBody.setLayout(new BoxLayout(statusPanelBody, BoxLayout.LINE_AXIS));
        statusPanelBody.add(statusLabel);

        statusPanel.add(statusPanelHeader);
        statusPanel.add(statusPanelBody);

        layoutPanel.add(statusPanel);
        layoutPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // ---------------------------------------------------------------------------------

        // ------------------ Navigation Panel ---------------------------------------------
        navPanel.setBackground(ColorScheme.DARK_GRAY_COLOR);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.LINE_AXIS));

        // TODO: Navigation items
        JLabel navLabel = new JLabel("Navigation");
        navLabel.setFont(smallText);
        navPanel.add(navLabel);
        layoutPanel.add(navPanel);

        // ---------------------------------------------------------------------------------

        // TODO: Create other panels (competitions, clan status, gains, stuff like that)


        // Add action listeners on startup to avoid double-adding when re-logging in
        // NOTE: This isn't called when we manually set the active element
        // Handles group selection
        clanNames.addActionListener(e -> {
            if (clanNames.getSelectedItem() != null) {
                JLabel l = (JLabel) clanNames.getSelectedItem();
                activeGroup = l.getText();

                // TODO: Active group is set, load stuff
            }
        });

        // for testing without logging in
        // TODO: Remove
        getPlayerFromWom("fartrock");

        // NOTE: getPlayerFromWom()'s callback MUST complete before calling other getPlayer* functions!

        // NOTE: We need to know when we have all the info, for now each callback makes the next
        // request so the last one in the chain knows that all the required data has been received
        // and we can start doing stuff with the Player* classes

        // NOTE: If we want getPlayerGroups and getPlayerCompetitions to run at the same time, keep
        // track of when both have finished with thread safe (volatile in java???) bools.
        // Then check in the update cycle whether both have finished and proceed with UI setup from there.

        // Current setup is loggedIn -> getPlayerFromWom -> getPlayerGroups -> getPlayerCompetitions -> buildMainPanel
    }

    protected void loggedIn(Client client) {

        // TODO: Present "loading" panel
        statusLabel.setText("Loading");

        getPlayerFromWom(client.getUsername());

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

            clanNames.setVisible(true);
            clanNames.removeAllItems();
            for (PlayerGroup g : groups) {
                JLabel l = new JLabel(g.name);
                clanNames.addItem(l);
            }

            clanNames.setSelectedIndex(0);  // TODO: Default group setting
            activeGroup = ((JLabel) clanNames.getSelectedItem()).getText();  // remember to set active group
        }

        // TODO: Navigation menu

        // TODO: Setup group overview on active group
    }

    private void getPlayerFromWom(String name) {

        // Requests to /players/username/<name> always return one result
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(womUrl + "/players/username/" + name))
                .GET()
                .setHeader("Content-Type", "application/json; utf-8")
                .setHeader("Accept", "application/json")
                .timeout(Duration.ofSeconds(20))
                .build();

        CompletableFuture<HttpResponse<InputStream>> response = httpClient.sendAsync(
                request, HttpResponse.BodyHandlers.ofInputStream());

        // NOTE: Code in here is executed on the main thread, not the worker. So we can
        // safely set variables in this class here. Will be called when the http request
        // is completed on the worker thread.
        response.thenAccept(
                r -> {
                    try {
                        if (r.statusCode() != 200) {
                            // Http error, read the response

                            BufferedReader b = new BufferedReader(new InputStreamReader(r.body()));
                            String line;
                            if ((line = b.readLine()) != null) {
                                log.error("Failed: HTTP error code: " + r.statusCode() + ", Error: " + line);
                            }

                            // TODO: If player isn't in WOM, we will get "player not found" as a response.
                            // TODO: Check if this comes with an error code too, and if it does, handle it here
                            // TODO: 400 is bad request and 404 is missing data (ie player not in database)?
                            // TODO: Present option to add account to WOM database if account not found

                            // TODO: Network issues? Check other http codes we should handle!

                            // If we always get a 404 if player isn't in WOM database then we can prompt to
                            // add the account.
                            if (r.statusCode() == 404) {
                                log.error("404 error fetching player data from WOM");
                            }

                            return;
                        }

                        InputStreamReader in = new InputStreamReader(r.body());
                        BufferedReader br = new BufferedReader(in);
                        String line;

                        // NOTE: WOM returns entire result in one line.
                        if ((line = br.readLine()) != null) {
                            log.info(line);

                            player = gson.fromJson(line, Player.class);

                            log.info("Found id: " + player.id + " for account: " + player.username);
                            statusLabel.setText("WOM ID: " + player.id);
                            //statusLabel.setVisible(true);

                            // We have the player info from WOM, now get the group info
                            getPlayerGroups();
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    private void getPlayerCompetitions() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(womUrl + "/players/" + player.id + "/competitions"))
                .GET()
                .setHeader("Content-Type", "application/json; utf-8")
                .setHeader("Accept", "application/json")
                .timeout(Duration.ofSeconds(20))
                .build();

        CompletableFuture<HttpResponse<InputStream>> response = httpClient.sendAsync(
                request, HttpResponse.BodyHandlers.ofInputStream());

        response.thenAccept(
                r -> {
                    try {
                        if (r.statusCode() != 200) {

                            BufferedReader b = new BufferedReader(new InputStreamReader(r.body()));
                            String line;
                            if ((line = b.readLine()) != null) {
                                log.error("Failed: Http error code: " + r.statusCode() + ", Error: " + line);
                            }

                            // TODO: Handle case where there isn't any competitions

                            return;
                        }

                        InputStreamReader in = new InputStreamReader(r.body());
                        BufferedReader br = new BufferedReader(in);
                        String line;

                        if ((line = br.readLine()) != null) {
                            PlayerCompetition[] c = gson.fromJson(line, PlayerCompetition[].class);

                            competitions.clear();
                            competitions.addAll(List.of(c));

                            for (PlayerCompetition pc : competitions) {
                                log.info("Found competition: " + pc.title);
                            }

                            /*
                            TODO: We now have all competitions the player is involved in, which may come from
                            TODO: multiple groups. Data will be displayed later when a group is selected
                            TODO: in the plugin.
                            TODO: This data includes PAST competitions!
                             */

                            // getPlayerFromWom(), getPlayerGroups() and now getPlayerCompetitions()
                            // are all complete, so we can use the Player* data in the UI now.
                            // TODO: Present main panel entry point (group selector)

                            statusLabel.setText("Loaded data from WOM");
                            buildMainPanel();
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    private void getPlayerGroups() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(womUrl + "/players/" + player.id + "/groups"))
                .GET()
                .setHeader("Content-Type", "application/json; utf-8")
                .setHeader("Accept", "application/json")
                .timeout(Duration.ofSeconds(20))
                .build();

        CompletableFuture<HttpResponse<InputStream>> response = httpClient.sendAsync(
                request, HttpResponse.BodyHandlers.ofInputStream());

        response.thenAccept(
                r -> {
                    try {
                        if (r.statusCode() != 200) {

                            BufferedReader b = new BufferedReader(new InputStreamReader(r.body()));
                            String line;
                            if ((line = b.readLine()) != null) {
                                log.error("Failed: Http error code: " + r.statusCode() + ", Error: " + line);
                            }

                            // TODO: Handle case where there isn't any groups
                            // NOTE: Using the status label for now instead of setting up the full UI.
                            statusLabel.setText("No groups found");

                            return; // skip loading competitions & ui setup
                        }

                        InputStreamReader in = new InputStreamReader(r.body());
                        BufferedReader br = new BufferedReader(in);
                        String line;

                        if ((line = br.readLine()) != null) {
                            PlayerGroup[] c = gson.fromJson(line, PlayerGroup[].class);

                            groups.clear();
                            groups.addAll(List.of(c));

                            for (PlayerGroup g : groups) {
                                log.info("Found group: " + g.name);
                            }

                            // TODO: Add groups to drop-down box to select "active" group
                            // TODO: If there is only one group, use a label instead of a drop-down box.
                            // TODO: Default group should be adjustable in settings menu and save to config file (by group ID)

                            // This will be the last request in the startup chain
                            getPlayerCompetitions();
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );
    }
}
