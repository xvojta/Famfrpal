package com.xvojta.famfrpal;

import org.bukkit.scoreboard.Team;

import java.util.HashMap;

public class FPTeamManager {
    public HashMap<String, String[]> teamsNames;
    public HashMap<String, Team> teams;

    public FPTeamManager(HashMap<String, String[]> teams)
    {
        this.teamsNames = teams;
    }

    public String[] getTeamsNames()
    {
        return (String[]) teamsNames.keySet().toArray();
    }

    public Team getTeamByPlayer(String playerName)
    {
        for (String[] s : teamsNames.values())
        {
            for (int i = 0; i < s.length; i++)
            {
                if (s[i] == playerName)
                {
                    return teams.get(i);
                }
            }
        }
        return null;
    }
}
