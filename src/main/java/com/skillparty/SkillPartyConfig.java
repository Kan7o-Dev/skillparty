package com.skillparty;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup("skillparty")
public interface SkillPartyConfig extends Config
{
    @ConfigItem(
            keyName = "shareLocation",
            name = "Share my location",
            description = "If OFF, your card shows Hidden",
            position = 1
    )
    default boolean shareLocation() { return false; }
}
