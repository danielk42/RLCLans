package com.ironchoobs.rlclans.data;

import java.util.Date;

public class Participant {
    int exp;
    int id;
    String username;
    String displayName;
    String type;
    String build;
    String country;
    boolean flagged;
    float ehp;
    float ehb;
    float ttm;
    float tt200m;
    Date lastImportedAt;
    Date lastChangedAt;
    Date registeredAt;
    Date updatedAt;
    String teamName;
    Progress progress;
    History[] history;

    public Participant() {
    }
}
