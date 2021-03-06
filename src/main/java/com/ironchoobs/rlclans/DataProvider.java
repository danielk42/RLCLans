package com.ironchoobs.rlclans;

import com.google.gson.Gson;
import com.ironchoobs.rlclans.data.*;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.Date;
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
                request, HttpResponse.BodyHandlers.ofInputStream())
                .exceptionallyAsync(ex -> {
                    if (ex.getCause() instanceof HttpTimeoutException) {
                        log.info("Http timeout in getPlayerFromWom");
                    }
                    else {
                        log.error("Unknown http error in getPlayerFromWom: " + ex.getCause().toString());
                    }
                    return null;
                }, SwingUtilities::invokeLater);

        response.thenAcceptAsync(
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
                                //log.error("404 error fetching player data from WOM");
                                error.accept(ErrorType.PLAYER_NOT_FOUND);
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
                    catch (NullPointerException e) {
                        error.accept(ErrorType.CONNECTION_ERROR);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        , SwingUtilities::invokeLater);
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
                request, HttpResponse.BodyHandlers.ofInputStream())
                .exceptionallyAsync(ex -> {
                    if (ex.getCause() instanceof HttpTimeoutException) {
                        log.info("Http timeout in getPlayerGroups");
                    }
                    else {
                        log.error("Unknown http error in getPlayerGroups: " + ex.getCause().toString());
                    }
                    return null;
                }, SwingUtilities::invokeLater);

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
                    catch (NullPointerException e) {
                        // Called if http request failed (ie timed out)
                        error.accept(ErrorType.CONNECTION_ERROR);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        , SwingUtilities::invokeLater);
    }

    // Get the competitions that a player is involved in
    public void getPlayerCompetitions(int playerId, Consumer<List<PlayerCompetition>> callback, Consumer<ErrorType> error) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(womUrl + "/players/" + playerId + "/competitions"))
                .GET()
                .setHeader("Content-Type", "application/json; utf-8")
                .setHeader("Accept", "application/json")
                .timeout(Duration.ofSeconds(20))
                .build();

        CompletableFuture<HttpResponse<InputStream>> response = httpClient.sendAsync(
                request, HttpResponse.BodyHandlers.ofInputStream())
                .exceptionallyAsync(ex -> {
                    if (ex.getCause() instanceof HttpTimeoutException) {
                        log.info("Http timeout in getPlayerCompetitions");
                    }
                    else {
                        log.error("Unknown http error in getPlayerCompetitions: " + ex.getCause().toString());
                    }
                    return null;
                }, SwingUtilities::invokeLater);

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
                                error.accept(ErrorType.COMPETITION_NOT_FOUND);
                            }

                            return;
                        }

                        InputStreamReader in = new InputStreamReader(r.body());
                        BufferedReader br = new BufferedReader(in);
                        String line;

                        if ((line = br.readLine()) != null) {
                            PlayerCompetition[] c = gson.fromJson(line, PlayerCompetition[].class);

                            callback.accept(List.of(c));
                        }
                    }
                    catch (NullPointerException e) {
                        error.accept(ErrorType.CONNECTION_ERROR);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        , SwingUtilities::invokeLater);
    }

    // Get details of a competition given its ID
    // TODO: WOM has 2 types of competition, "classic" and "team". Team competitions have an extra variable that classic
    // TODO: ones don't. Check if gson will parse a classic response into a team competition class. If it doesn't,
    // TODO: deserialization will need to take into account the "type" field in Competition.
    public void getCompetition(int competitionId, Consumer<Competition> callback, Consumer<ErrorType> error) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(womUrl + "/competitions/" + competitionId))
                .GET()
                .setHeader("Content-Type", "application/json; utf-8")
                .setHeader("Accept", "application/json")
                .timeout(Duration.ofSeconds(10))
                .build();

        CompletableFuture<HttpResponse<InputStream>> response = httpClient.sendAsync(
                        request, HttpResponse.BodyHandlers.ofInputStream())
                .exceptionallyAsync(ex -> {
                    if (ex.getCause() instanceof HttpTimeoutException) {
                        log.info("Http timeout in getCompetition");
                    }
                    else {
                        log.error("Unknown http error in getCompetition: " + ex.getCause().toString());
                    }
                    return null;
                }, SwingUtilities::invokeLater);

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
                            Competition c = gson.fromJson(line, Competition.class);

                            // TODO: Check we actually have some data.... for all these functions
                            callback.accept(c);
                        }
                    }
                    catch (NullPointerException e) {
                        error.accept(ErrorType.CONNECTION_ERROR);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        , SwingUtilities::invokeLater);
    }

    public void getTopMember(int groupId, Consumer<TopMember> callback, Consumer<ErrorType> error) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(womUrl + "/groups/" + groupId + "/monthly-top"))
                .GET()
                .setHeader("Content-Type", "application/json; utf-8")
                .setHeader("Accept", "application/json")
                .timeout(Duration.ofSeconds(10))
                .build();

        CompletableFuture<HttpResponse<InputStream>> response = httpClient.sendAsync(
                request, HttpResponse.BodyHandlers.ofInputStream())
                .exceptionallyAsync(ex -> {
                    if (ex.getCause() instanceof HttpTimeoutException) {
                        log.info("Http timeout in getTopMember");
                    }
                    else {
                        log.error("Unknown http error in getTopMember: " + ex.getCause().toString());
                    }
                    return null;
                }, SwingUtilities::invokeLater);

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
                    catch (NullPointerException e) {
                        error.accept(ErrorType.CONNECTION_ERROR);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        , SwingUtilities::invokeLater);
    }

    // Period based leaderboard (see WomPeriod)
    public void getGroupLeaderboard(int groupId, WomMetric metric, WomPeriod period, int limit, int offset,
                                    Consumer<LeaderboardEntry[]> callback, Consumer<ErrorType> error) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(womUrl + "/groups/" + groupId + "/gained?metric="
                    + metric + "&period=" + period + "&limit=" + limit + "&offset=" + offset))
                .GET()
                .setHeader("Content-Type", "application/json; utf-8")
                .setHeader("Accept", "application/json")
                .timeout(Duration.ofSeconds(20))
                .build();

        CompletableFuture<HttpResponse<InputStream>> response = httpClient.sendAsync(
                        request, HttpResponse.BodyHandlers.ofInputStream())
                .exceptionallyAsync(ex -> {
                    if (ex.getCause() instanceof HttpTimeoutException) {
                        log.info("Http timeout in getGroupLeaderboard");
                    }
                    else {
                        log.error("Unknown http error in getGroupLeaderboard: " + ex.getCause().toString());
                    }
                    return null;
                }, SwingUtilities::invokeLater);

        response.thenAcceptAsync(
                r -> {
                    try {
                        if (r.statusCode() != 200) {

                            BufferedReader b = new BufferedReader(new InputStreamReader(r.body()));
                            String line;
                            if ((line = b.readLine()) != null) {
                                log.error("Failed: Http error code: " + r.statusCode() + ", Error: " + line);
                            }
                            // TODO: error handling
                            return;
                        }

                        InputStreamReader in = new InputStreamReader(r.body());
                        BufferedReader br = new BufferedReader(in);
                        String line;

                        if ((line = br.readLine()) != null) {
                            LeaderboardEntry[] c = gson.fromJson(line, LeaderboardEntry[].class);

                            // TODO: Check we actually have some data.... for all these functions
                            callback.accept(c);
                        }
                    }
                    catch (NullPointerException e) {
                        error.accept(ErrorType.CONNECTION_ERROR);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        , SwingUtilities::invokeLater);
    }

    // Date based leaderboard
    public void getGroupLeaderboard(int groupId, WomMetric metric, Date startDate, Date endDate, int limit, int offset,
                                    Consumer<LeaderboardEntry[]> callback, Consumer<ErrorType> error) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(womUrl + "/groups/" + groupId + "/gained?metric="
                        + metric + "&startDate=" + gson.toJson(startDate) + "&endDate=" + gson.toJson(endDate)
                        + "&limit=" + limit + "&offset=" + offset))
                .GET()
                .setHeader("Content-Type", "application/json; utf-8")
                .setHeader("Accept", "application/json")
                .timeout(Duration.ofSeconds(20))
                .build();

        CompletableFuture<HttpResponse<InputStream>> response = httpClient.sendAsync(
                        request, HttpResponse.BodyHandlers.ofInputStream())
                .exceptionallyAsync(ex -> {
                    if (ex.getCause() instanceof HttpTimeoutException) {
                        log.info("Http timeout in getGroupLeaderboard");
                    }
                    else {
                        log.error("Unknown http error in getGroupLeaderboard: " + ex.getCause().toString());
                    }
                    return null;
                }, SwingUtilities::invokeLater);

        response.thenAcceptAsync(
                r -> {
                    try {
                        if (r.statusCode() != 200) {

                            BufferedReader b = new BufferedReader(new InputStreamReader(r.body()));
                            String line;
                            if ((line = b.readLine()) != null) {
                                log.error("Failed: Http error code: " + r.statusCode() + ", Error: " + line);
                            }
                            // TODO: error handling
                            return;
                        }

                        InputStreamReader in = new InputStreamReader(r.body());
                        BufferedReader br = new BufferedReader(in);
                        String line;

                        if ((line = br.readLine()) != null) {
                            LeaderboardEntry[] c = gson.fromJson(line, LeaderboardEntry[].class);

                            // TODO: Check we actually have some data.... for all these functions
                            callback.accept(c);
                        }
                    }
                    catch (NullPointerException e) {
                        error.accept(ErrorType.CONNECTION_ERROR);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        , SwingUtilities::invokeLater);
    }

    public void getRecentAchievements(int groupId, int limit, int offset,
                                      Consumer<PlayerAchievement[]> callback,
                                      Consumer<ErrorType> error) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(womUrl + "/groups/" + groupId + "/achievements?limit="
                    + limit + "&offset=" + offset))
                .GET()
                .setHeader("Content-Type", "application/json; utf-8")
                .setHeader("Accept", "application/json")
                .timeout(Duration.ofSeconds(20))
                .build();

        CompletableFuture<HttpResponse<InputStream>> response = httpClient.sendAsync(
                        request, HttpResponse.BodyHandlers.ofInputStream())
                .exceptionallyAsync(ex -> {
                    if (ex.getCause() instanceof HttpTimeoutException) {
                        log.info("Http timeout in getRecentAchievements");
                    }
                    else {
                        log.error("Unknown http error in getRecentAchievements: " + ex.getCause().toString());
                    }
                    return null;
                }, SwingUtilities::invokeLater);

        response.thenAcceptAsync(
                r -> {
                    try {
                        if (r.statusCode() != 200) {

                            BufferedReader b = new BufferedReader(new InputStreamReader(r.body()));
                            String line;
                            if ((line = b.readLine()) != null) {
                                log.error("Failed: Http error code: " + r.statusCode() + ", Error: " + line);
                            }
                            // TODO: error handling
                            return;
                        }

                        InputStreamReader in = new InputStreamReader(r.body());
                        BufferedReader br = new BufferedReader(in);
                        String line;

                        if ((line = br.readLine()) != null) {
                            PlayerAchievement[] c = gson.fromJson(line, PlayerAchievement[].class);

                            // TODO: Check we actually have some data.... for all these functions
                            callback.accept(c);
                        }
                    }
                    catch (NullPointerException e) {
                        error.accept(ErrorType.CONNECTION_ERROR);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        , SwingUtilities::invokeLater);
    }
}
