package com.xvojta.famfrpal;

import com.mysql.fabric.xmlrpc.base.Array;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class FPTeamManager {
    public HashMap<String, ArrayList<Player>> teamsNames;
    public HashMap<String, Team> teams;

    public FPTeamManager(HashMap<String, ArrayList<Player>> teams)
    {
        this.teamsNames = teams;
        this.teams = new HashMap<String, Team>();
    }

    public Set<String> getTeamsNames()
    {
        return teamsNames.keySet();
    }

    public Team getTeamByPlayer(Player player)
    {
        for (ArrayList<Player> s : teamsNames.values())
        {
            for (int i = 0; i < s.stream().count(); i++)
            {
                if (s.get(i) == player)
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
