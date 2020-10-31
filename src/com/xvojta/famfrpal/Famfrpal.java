package com.xvojta.famfrpal;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.util.logging.Logger;

public class Famfrpal extends JavaPlugin implements Listener
{
    public Logger LOGGER = Bukkit.getLogger();

    @Override
    public void onEnable()
    {
        Bukkit.getLogger().info("Famfrpal plugin funguje jako vzdy");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments)
    {
        Player player = (Player) sender;
        World world = player.getWorld();
        Location spot = player.getLocation();

        if (label.equalsIgnoreCase("fp"))
        {
            LOGGER.info("Famfrpal commmand");
            return true;
        }
        return false;
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        LOGGER.info("player joined FP");
        // Your code here...
    }

    private void craeteScoreBoard(World world) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective objective = board.registerNewObjective("Slaparoo", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName("Slaparoo Score");
        Team team = board.registerNewTeam("Slaparoo Players");
        for (Player pl:world.getPlayers()) {
            team.addPlayer(pl);
            pl.setScoreboard(board);
            Score score = objective.getScore(pl);
            score.setScore(0);
        }
    }
}
