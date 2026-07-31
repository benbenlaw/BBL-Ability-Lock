package com.benbenlaw.abilitylock.util;

public class SpeedrunTimeUtil {
    private SpeedrunTimeUtil() {}

    public static String format(long elapsedMillis) {
        long minutes = (elapsedMillis / 1000) / 60;
        long seconds = (elapsedMillis / 1000) % 60;
        long millis = elapsedMillis % 1000;
        return String.format("%02d:%02d.%03d", minutes, seconds, millis);
    }
}