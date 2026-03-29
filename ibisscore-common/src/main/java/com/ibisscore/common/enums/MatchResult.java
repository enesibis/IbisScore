package com.ibisscore.common.enums;

public enum MatchResult {
    HOME_WIN("1"),
    DRAW("X"),
    AWAY_WIN("2"),
    UNKNOWN("-");

    private final String code;

    MatchResult(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static MatchResult fromGoals(Integer homeGoals, Integer awayGoals) {
        if (homeGoals == null || awayGoals == null) return UNKNOWN;
        if (homeGoals > awayGoals) return HOME_WIN;
        if (homeGoals.equals(awayGoals)) return DRAW;
        return AWAY_WIN;
    }

    public static MatchResult fromCode(String code) {
        for (MatchResult r : values()) {
            if (r.code.equals(code)) return r;
        }
        return UNKNOWN;
    }
}
