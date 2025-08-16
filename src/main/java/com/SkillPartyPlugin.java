package com.skillparty;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Slf4j
@PluginDescriptor(
        name = "SkillParty",
        description = "Party overlay: live skills, deaths, drops, quests, pets, time played",
        tags = {"overlay","party","skills","friends"}
)
public class SkillPartyPlugin extends Plugin {
    @Inject private Client client;
    @Inject private SkillPartyConfig config;
    @Inject private ConfigManager configManager;

    private WebServer web;
    private final State state = new State();
    private Instant sessionStart;

    @Override
    protected void startUp() throws Exception {
        sessionStart = Instant.now();

        // Initial state
        state.name   = "Player";
        state.world  = client.getWorld();
        state.members = client.getWorldType() != null && !client.getWorldType().isEmpty();

        try {
            web = new WebServer(11400);
            web.setStateSupplier(this::snapshot);
            web.setConfigConsumer(this::applyConfig);
            web.start();
            log.info("SkillParty server running at http://127.0.0.1:11400/");
        } catch (IOException e) {
            log.error("Failed to start SkillParty server", e);
        }
    }

    @Override
    protected void shutDown() throws Exception {
        if (web != null) {
            web.stop();
            web = null;
        }
    }

    private Object snapshot() {
        // Update time played
        state.timePlayed = fmtDuration(Duration.between(sessionStart, Instant.now()));

        // Mirror config
        state.setShareLocation(config.shareLocation());

        // Ensure a couple of skills exist
        if (state.skills.isEmpty()) {
            state.skills.put("DEFENCE", 1);
            state.skills.put("HITPOINTS", 10);
        }

        return state;
    }

    private void applyConfig(Map<String, Object> m) {
        if (m.containsKey("shareLocation")) {
            boolean on = Boolean.TRUE.equals(m.get("shareLocation"));
            configManager.setConfiguration("skillparty", "shareLocation", on);
            state.setShareLocation(on);
        }
    }

    private static String fmtDuration(Duration d) {
        long days = d.toDays();
        long hours = d.minusDays(days).toHours();
        long mins = d.minusDays(days).minusHours(hours).toMinutes();
        if (days  > 0) return days + "d " + hours + "h " + mins + "m";
        if (hours > 0) return hours + "h " + mins + "m";
        return mins + "m";
    }

    @Provides
    SkillPartyConfig provideConfig(ConfigManager cm) {
        return cm.getConfig(SkillPartyConfig.class);
    }
}
