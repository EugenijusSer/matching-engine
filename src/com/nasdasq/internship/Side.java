package com.nasdasq.internship;

public enum Side {
    BUY, SELL;
    private Side opposite;

    static {
        BUY.opposite = SELL;
        SELL.opposite = BUY;
    }

    public Side getOppositeSide() {
        return opposite;
    }
}
