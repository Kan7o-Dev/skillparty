package com.skillparty;

import java.util.*;

final class State {
    public String  name = "Player";
    public Integer world = 301;
    public boolean members = false;

    public boolean shareLocation = false;
    public String  location = "";

    public Integer xp = 0;              // progress bar xp for the shown skill
    public String  skill = "DEFENCE";   // main skilling label

    public Activity activity = new Activity();        // { type, target, boss }
    public Map<String, Integer> skills = new HashMap<>(); // all skill levels
    public String  type = "NORMAL";     // NORMAL/HCIM/UIM/GIM/IRON
    public Long    lastMove = System.currentTimeMillis();

    public List<Message> messages = new ArrayList<>();
    public List<Death>   deaths   = new ArrayList<>();
    public List<Drop>    drops    = new ArrayList<>();
    public List<Quest>   quests   = new ArrayList<>();
    public List<Pet>     pets     = new ArrayList<>();

    // gear: name -> { slot -> item } (only filled slots)
    public Map<String, Map<String, String>> gear = new HashMap<>();

    // footer stats
    public String  timePlayed = "";
    public Integer combatLvl = null;
    public Integer totalLevel = null;
    public Long    totalXp = null;
    public Integer questsCompleted = null;

    // optional per-friend stats
    public Map<String, Stats> statsByName = new HashMap<>();

    // nested data shapes
    static final class Activity { public String type=""; public String target=""; public boolean boss=false; }
    static final class Message  { public String from; public String text; }
    static final class Death    { public String name; public String cause; }
    static final class Drop     { public String name; public String item; }
    static final class Quest    { public String name; public String quest; }
    static final class Pet      { public String name; public String pet; }
    static final class Stats    { public String timePlayed; public Integer combatLvl; public Integer totalLevel; public Long totalXp; public Integer questsCompleted; }

    public void setShareLocation(boolean on) {
        this.shareLocation = on;
        if (!on) this.location = "";
    }
}
