package com.benbenlaw.abilitylock.events.client;

import com.benbenlaw.abilitylock.AbilityLock;
import com.benbenlaw.abilitylock.util.SpeedrunTimeUtil;
import net.minecraft.client.Minecraft;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class SpeedrunTimer {

    private static boolean running = false;
    private static boolean finished = false;
    private static long startTimeMillis = 0L;
    private static long endTimeMillis = 0L;

    private static final Path SAVE_FILE =
            FMLPaths.GAMEDIR.get().resolve("config/abilitylock/speedrun_timers.properties");

    private static String cachedWorldKey = "unknown";

    private SpeedrunTimer() {}

    public static void start() {
        if (!running && !finished) {
            running = true;
            startTimeMillis = System.currentTimeMillis();
        }
    }

    public static void stop() {
        if (running) {
            running = false;
            finished = true;
            endTimeMillis = System.currentTimeMillis();
        }
    }

    public static void reset() {
        running = false;
        finished = false;
        startTimeMillis = 0L;
        endTimeMillis = 0L;
    }

    public static boolean isRunning() {
        return running;
    }

    public static boolean isFinished() {
        return finished;
    }

    public static long getElapsedMillis() {
        if (running) {
            return System.currentTimeMillis() - startTimeMillis;
        } else if (finished) {
            return endTimeMillis - startTimeMillis;
        }
        return 0L;
    }

    public static String getFormattedTime() {
        return SpeedrunTimeUtil.format(getElapsedMillis());
    }

    private static String computeWorldKey() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getSingleplayerServer() != null) {
            return "sp_" + mc.getSingleplayerServer().getWorldData().getLevelName();
        } else if (mc.getCurrentServer() != null) {
            return "mp_" + mc.getCurrentServer().ip;
        }
        return "unknown";
    }

    public static void onWorldJoin() {
        cachedWorldKey = computeWorldKey();
        load();
    }

    public static void save() {
        if (!running && !finished) {
            return;
        }
        if ("unknown".equals(cachedWorldKey)) {
            AbilityLock.LOGGER.warn("Speedrun timer save skipped: no cached world key");
            return;
        }

        Properties props = loadProperties();

        props.setProperty(cachedWorldKey + ".elapsed", String.valueOf(getElapsedMillis()));
        props.setProperty(cachedWorldKey + ".running", String.valueOf(running));
        props.setProperty(cachedWorldKey + ".finished", String.valueOf(finished));

        try {
            Files.createDirectories(SAVE_FILE.getParent());
            try (OutputStream out = Files.newOutputStream(SAVE_FILE)) {
                props.store(out, "AbilityLock Speedrun Timer Data");
            }
        } catch (IOException e) {
            AbilityLock.LOGGER.error("Failed to save speedrun timer data", e);
        }
    }

    private static void load() {
        reset();

        Properties props = loadProperties();

        boolean savedRunning = Boolean.parseBoolean(props.getProperty(cachedWorldKey + ".running", "false"));
        boolean savedFinished = Boolean.parseBoolean(props.getProperty(cachedWorldKey + ".finished", "false"));
        long elapsed = Long.parseLong(props.getProperty(cachedWorldKey + ".elapsed", "0"));

        if (savedRunning) {
            running = true;
            startTimeMillis = System.currentTimeMillis() - elapsed;
        } else if (savedFinished) {
            finished = true;
            startTimeMillis = 0L;
            endTimeMillis = elapsed;
        }
    }

    private static Properties loadProperties() {
        Properties props = new Properties();
        if (Files.exists(SAVE_FILE)) {
            try (InputStream in = Files.newInputStream(SAVE_FILE)) {
                props.load(in);
            } catch (IOException e) {
                AbilityLock.LOGGER.error("Failed to load speedrun timer data", e);
            }
        }
        return props;
    }
}