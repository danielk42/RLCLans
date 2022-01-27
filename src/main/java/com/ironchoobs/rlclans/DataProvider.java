package com.ironchoobs.rlclans;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Slf4j
// Handles getting data from Wise Old Man.
public class DataProvider {

    // TODO: Error handler callbacks, if something goes wrong the functions will
    // TODO: not invoke their given callbacks but instead invoke error callbacks
    // TODO: passed to this class.

    public enum ErrorType {
        PLAYER_NOT_FOUND,
        CONNECTION_ERROR,
        GROUP_NOT_FOUND,
        COMPETITION_NOT_FOUND,
    }

    private static final DataProvider d = new DataProvider();
    public static DataProvider instance() { return d; }

    private final HttpClient httpClient;
    private final Gson gson = new Gson();
    private final String womUrl = "https://api.wiseoldman.net";

    private DataProvider() {
        httpClient = HttpClient.newHttpClient();
    }

    public void getPlayerFromWom(String name, Consumer<Player> callback, Consumer<ErrorType> error) {
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

                            Player player = gson.fromJson(line, Player.class);

                            log.info("Found id: " + player.id + " for account: " + player.username);

                            // pass the data to the given callback function
                            callback.accept(player);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    public void getPlayerGroups(int playerId, Consumer<List<PlayerGroup>> callback, Consumer<ErrorType> error) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(womUrl + "/players/" + playerId + "/groups"))
                .GET()
                .setHeader("Content-Type", "application/json; utf-8")
                .setHeader("Accept", "application/json")
                .timeout(Duration.ofSeconds(20))
                .build();

        CompletableFuture<HttpResponse<InputStream>> response = httpClient.sendAsync(
                request, HttpResponse.BodyHandlers.ofInputStream());

        response.thenAcceptAsync(
                r -> {
                    try {
                        if (r.statusCode() != 200) {

                            BufferedReader b = new BufferedReader(new InputStreamReader(r.body()));
                            String line;
                            if ((line = b.readLine()) != null) {
                                log.error("Failed: Http error code: " + r.statusCode() + ", Error: " + line);
                            }

                            if (r.statusCode() == 404) {
                                error.accept(ErrorType.GROUP_NOT_FOUND);
                            }

                            return;
                        }

                        InputStreamReader in = new InputStreamReader(r.body());
                        BufferedReader br = new BufferedReader(in);
                        String line;

                        if ((line = br.readLine()) != null) {
                            PlayerGroup[] c = gson.fromJson(line, PlayerGroup[].class);

                            callback.accept(List.of(c));
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        , SwingUtilities::invokeLater);
    }

    public void getPlayerCompetitions(int playerId, Consumer<List<PlayerCompetition>> callback, Consumer<ErrorType> error) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(womUrl + "/players/" + playerId + "/competitions"))
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

                            return; // TODO: Call error handler
                        }

                        InputStreamReader in = new InputStreamReader(r.body());
                        BufferedReader br = new BufferedReader(in);
                        String line;

                        if ((line = br.readLine()) != null) {
                            PlayerCompetition[] c = gson.fromJson(line, PlayerCompetition[].class);

                            callback.accept(List.of(c));
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    public void getTopMember(int groupId, Consumer<TopMember> callback, Consumer<ErrorType> error) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(womUrl + "/groups/" + groupId + "/monthly-top"))
                .GET()
                .setHeader("Content-Type", "application/json; utf-8")
                .setHeader("Accept", "application/json")
                .timeout(Duration.ofSeconds(20))
                .build();

        CompletableFuture<HttpResponse<InputStream>> response = httpClient.sendAsync(
                request, HttpResponse.BodyHandlers.ofInputStream());

        response.thenAcceptAsync(
                r -> {
                    try {
                        if (r.statusCode() != 200) {

                            BufferedReader b = new BufferedReader(new InputStreamReader(r.body()));
                            String line;
                            if ((line = b.readLine()) != null) {
                                log.error("Failed: Http error code: " + r.statusCode() + ", Error: " + line);
                            }
                            // TODO: Call error handler
                            return;
                        }

                        InputStreamReader in = new InputStreamReader(r.body());
                        BufferedReader br = new BufferedReader(in);
                        String line;

                        if ((line = br.readLine()) != null) {
                            TopMember c = gson.fromJson(line, TopMember.class);

                            // TODO: Check we actually have some data.... for all these functions
                            callback.accept(c);
                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        , SwingUtilities::invokeLater);
    }
}
