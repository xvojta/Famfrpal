package com.xvojta.famfrpal;

import org.bukkit.scoreboard.Team;

import java.util.HashMap;

public class FPTeamManager {
    public Team team;
    public String[] playerNames;
    public String teamName;

    public FPTeam(String teamName, Team team, String[] playerNames)
    {
        this.team = team;
        this.playerNames = playerNames;
        this.teamName = teamName;
    }

    public Team getTeamByPlayer(String playerName)
    {

    }
}
