package com.xvojta.famfrpal;

import net.minecraft.server.v1_16_R2.PlayerConnection;
import org.apache.logging.log4j.core.net.Priority;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.logging.Logger;

public class Famfrpal extends JavaPlugin implements Listener
{
    public Logger LOGGER = Bukkit.getLogger();
    public Scoreboard scoreboard;
    public Team team;
    public Objective objective;

    @Override
    public void onEnable()
    {
        Bukkit.getLogger().info("Famfrpal plugin funguje jako vzdy");

        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(this, this);

        craeteScoreBoard(getServer().getWorld("world"));
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

    @EventHandler (priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        LOGGER.info("player joined FP");

        //here the error appers. I need to add something like callLater
        team.addEntry(player.getName());
        player.setScoreboard(scoreboard);
        Score score = objective.getScore(player.getName());
        score.setScore(0);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        Player player = entity.getKiller();

        if(player != null) {
            if (event.getEntityType() == EntityType.ENDER_DRAGON)
            {

            }
        }
    }

    private void craeteScoreBoard(World world) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        scoreboard = manager.getNewScoreboard();
        objective = scoreboard.registerNewObjective("test", "dummy");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
        objective.setDisplayName("Score");
        team = scoreboard.registerNewTeam("Players");
    }
}
