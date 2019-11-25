package com.it_tech613.tvmulti.models;

public enum FirstServer {
    first(1), second(2), third(3), fourth(4), fifth(5),
    sixth(6), seventh(7), eighth(8), ninth(9), tenth(10);
    private final int value;
    FirstServer(final int val){
        value=val;
    }
    public int getValue() { return value; }

}
