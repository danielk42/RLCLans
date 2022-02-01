package com.ironchoobs.rlclans.data;

import java.util.Date;

public class Competition {
    int id;
    int title;
    String metric;
    String type;
    int score;
    Date startsAt;
    Date endsAt;
    int groupId;
    Date createdAt;
    Date updatedAt;
    String group;
    String duration;
    long totalGained;
    Participant[] participants;

    public Competition() {}
}


