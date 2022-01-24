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
    private final JPanel mainPanel = new JPanel();
    private final JLabel statusLabel = new JLabel(); // testing only

    private final HttpClient httpClient;
    private final Gson gson = new Gson();

    // Containers for WOM data
    private Player player;
    private final ArrayList<PlayerCompetition> competitions = new ArrayList<PlayerCompetition>();
    private final ArrayList<PlayerGroup> groups = new ArrayList<PlayerGroup>();

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

        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        mainPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        mainPanel.setLayout(new BorderLayout());

        layoutPanel.add(mainPanel, BorderLayout.NORTH);

        // TODO: Create other panels (competitions, clan status, gains, stuff like that)

        // TODO: Keeping the status label for messages during startup, but it should be outside
        // TODO: the main panel, OR whenever the player logs out the plugin must show the main panel.
        mainPanel.add(statusLabel, BorderLayout.NORTH);
        statusLabel.setText("Waiting for login");

        // for testing without logging in
        // TODO: Remove
        //getPlayerFromWom("fartrock");

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

        // Hide status label (might keep it as a startup message)
        statusLabel.setVisible(false);

        // If the player is in multiple groups we want a drop down box to set the "active" group.
        // If not, a label for the group name will be fine
        // If player isn't in any groups buildMainPanel will not be called (may instead display a different UI later)
        if (groups.size() == 1) {
            // Player is in only one group
            // Show group name label

            // If there are any competitions, show a button or something to navigate to the competition panel.
        }
        else {
            // Multiple groups, show group drop-down box
            // Selection must have a callback or something to trigger loading
            // ui elements.
        }
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
                            statusLabel.setVisible(true);

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

                            //statusLabel.setText("Loaded");
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
