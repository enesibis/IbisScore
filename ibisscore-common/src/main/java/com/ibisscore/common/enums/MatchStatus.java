package com.ibisscore.common.enums;

public enum MatchStatus {
    NS,         // Not Started
    TBD,        // To Be Defined
    LIVE,       // In Play
    HT,         // Half Time
    FT,         // Full Time
    AET,        // After Extra Time
    PEN,        // Penalty Shootout
    PST,        // Postponed
    CANC,       // Cancelled
    SUSP,       // Suspended
    INT,        // Interrupted
    ABD,        // Abandoned
    AWD,        // Technical Loss
    WO;         // Walk Over

    public boolean isFinished() {
        return this == FT || this == AET || this == PEN || this == AWD;
    }

    public boolean isLive() {
        return this == LIVE || this == HT;
    }

    public boolean isUpcoming() {
        return this == NS || this == TBD;
    }
}
