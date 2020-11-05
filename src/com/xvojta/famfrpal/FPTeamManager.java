package com.xvojta.famfrpal;

import com.mysql.fabric.xmlrpc.base.Array;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class FPTeamManager {
    public HashMap<String, String[]> teamsNames;
    public HashMap<String, Team> teams;

    public FPTeamManager(HashMap<String, String[]> teams)
    {
        this.teamsNames = teams;
        this.teams = new HashMap<String, Team>();
    }

    public Set<String> getTeamsNames()
    {
        return teamsNames.keySet();
    }

    public Team getTeamByPlayer(String playerName)
    {
        for (String[] s : teamsNames.values())
        {
            for (int i = 0; i < s.length; i++)
            {
                if (s[i].equalsIgnoreCase(playerName))
                {
                    return teams.get(getKey(teamsNames, s));
                }
            }
        }
        return null;
    }

    public <K, V> K getKey(Map<K, V> map, V value) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
