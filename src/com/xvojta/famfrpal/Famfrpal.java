package com.xvojta.famfrpal;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.advancement.Advancement;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.*;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class Famfrpal extends JavaPlugin implements Listener
{
    private static final int ENDERDRAGONSCORE = 100;
    private static final int PLAYERBOUNTY = 5;
    private static final int ELDERGUARDIANSCORE = 50;
    private static final int WITHERSCORE = 50;
    private static final int ADVANCEMENTSCORE = 5;
    public static Famfrpal Instance;
    public boolean started = false;
    public Logger LOGGER = Bukkit.getLogger();
    public Scoreboard scoreboard;
    public Objective objective;
    public FPTeamManager teamManager;

    @Override
    public void onEnable()
    {
        Instance = this;

        Bukkit.getLogger().info("Famfrpal plugin funguje jako vzdy");

        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments)
    {
        Player player = (Player) sender;
        World world = player.getWorld();
        Location spot = player.getLocation();


        if (label.equalsIgnoreCase("fpstart")) {
            if (sender instanceof Player && player.isOp()) {
                world.getPlayers().forEach(player1 -> {player.sendMessage("Speedrun has started");});
                start(world);
            }
            else
            {
                player.sendMessage("You don't have permissions to this command!");
            }
        }
        return false;
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        LOGGER.info("player joined FP");
    }
    //here the error appers. I need to add something like callLater

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        Player killedPlayer = event.getEntity();
        Player killer = killedPlayer.getKiller();

        if (started) {
            if (killer != null) {
                addScore(killer, PLAYERBOUNTY);
            }

            addScore(killedPlayer, -PLAYERBOUNTY);
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        Player player = entity.getKiller();

        if(player != null && started)
        {
            if (event.getEntityType() == EntityType.ENDER_DRAGON)
            {
                addScore(player, ENDERDRAGONSCORE);
            }
            if (event.getEntityType() == EntityType.ELDER_GUARDIAN)
            {
                addScore(player, ELDERGUARDIANSCORE);
            }
            if (event.getEntityType() == EntityType.WITHER)
            {
                addScore(player, WITHERSCORE);
            }
        }
    }

    @EventHandler
    public void onPlayerDoneAdvancement(PlayerAdvancementDoneEvent event)
    {
        Player player = event.getPlayer();
        String advancementKey = event.getAdvancement().getKey().toString();
        if (!advancementKey.startsWith("minecraft:recipes/") && started)
        {
            player.sendMessage("Advancement done " + event.getAdvancement().getKey().toString());
            addScore(player, ADVANCEMENTSCORE);
        }
    }

    public void addScore(Player player, int amount)
    {
        Score score = objective.getScore(player.getName());
        int newScore = score.getScore() + amount;
        score.setScore(newScore);
    }

    public void setScore(Player player, int amount)
    {
        Score score = objective.getScore(player.getName());
        score.setScore(amount);
    }

    public void setScoreboardToPlayer(Player player)
    {
        teamManager.getTeamByPlayer(player).addEntry(player.getName());
        player.setScoreboard(scoreboard);
        Score score = objective.getScore(player.getName());
        score.setScore(0);
    }

    private void craeteScoreBoard(World world)
    {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        scoreboard = manager.getNewScoreboard();
        objective = scoreboard.registerNewObjective("test", "dummy", "Score");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        int i = 0;
        for (String s : teamManager.getTeamsNames()) {
            teamManager.teams.put(s, scoreboard.registerNewTeam(s));
            teamManager.teams.get(s).setAllowFriendlyFire(false);
            teamManager.teams.get(s).setColor(ChatColor.values()[i]);
            //teamManager.teams.get(s).setSuffix(" - " + s);
            i++;
        }
    }

    private void start(World world)
    {
        //fill teams
        HashMap<String, ArrayList<Player>> teams = new HashMap<String, ArrayList<Player>>();

        for (int i = 1; i < 9; i++)
        {
            ArrayList<Player> teamPlayers = new ArrayList<Player>();
            for (Player p : world.getPlayers())
            {
                if (p.hasPermission("team" + i))
                {
                    teamPlayers.add(p);
                }
            }
            teams.put("team " + i, teamPlayers);
        }

        teamManager = new FPTeamManager(teams);

        craeteScoreBoard(world);

        teams.values().forEach(players -> {players.forEach(player -> {setScoreboardToPlayer(player);});});

        started = true;
    }
}