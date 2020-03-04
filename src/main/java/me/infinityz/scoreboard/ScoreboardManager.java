package me.infinityz.scoreboard;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import me.infinityz.SurvivalUniverse;

/**
 * ScoreboardManager
 */
public class ScoreboardManager {
    SurvivalUniverse instance;
    public Map<UUID, FastBoard> scoreboardHashMap;

    public ScoreboardManager(SurvivalUniverse instance){
        this.instance = instance;
        this.scoreboardHashMap = new HashMap<>();
        updateBoard();
    }

    void updateBoard(){
        //TODO: Add a hashSet of pending updates to be processed and process them here.
    }
}