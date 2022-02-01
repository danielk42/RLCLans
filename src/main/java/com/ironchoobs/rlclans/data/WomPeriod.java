package com.ironchoobs.rlclans.data;

public enum WomPeriod {
    fiveMin("5min"),
    day("day"),
    week("week"),
    month("month"),
    year("year");

    public final String period;

    WomPeriod(String period) {
        this.period = period;
    }

    @Override
    public String toString() {
        return this.period;
    }

    public static WomPeriod valueOfPeriod(String period) {
        for (WomPeriod p : values()) {
            if (p.period.compareTo(period) == 0) {
                return p;
            }
        }
        return null;
    }
}
