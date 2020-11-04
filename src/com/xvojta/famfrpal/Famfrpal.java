package com.xvojta.famfrpal;

import org.bukkit.Bukkit;
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

import java.util.HashMap;
import java.util.logging.Logger;

public class Famfrpal extends JavaPlugin implements Listener
{
    private static final int ENDERDRAGONSCORE = 100;
    private static final int PLAYERBOUNTY = 5;
    private static final int ELDERGUARDIANSCORE = 75;
    private static final int WITHERSCORE = 75;
    public static Famfrpal Instance;
    public Logger LOGGER = Bukkit.getLogger();
    public Scoreboard scoreboard;
    public Objective objective;
    public FPTeamManager teamManager;

    @Override
    public void onEnable()
    {
        //fill teams
        HashMap<String, String[]> teams = new HashMap<String, String[]>();
        teams.put("TestTeam", new String[]{"xvojta", "NEMO_CZ"});
        teamManager = new FPTeamManager(teams);

        Instance = this;

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
            if (arguments[0] != null)
            {
                if (arguments[0] == "addScore")
                {
                    if (arguments[1] != null && arguments[2] != null)
                    {
                        addScore(getServer().getPlayer(arguments[1]), Integer.parseInt(arguments[2]));
                    }
                }
            }
            return true;
        }
        return false;
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        LOGGER.info("player joined FP");

        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                Famfrpal.Instance.setScoreboardToPlayer(player);
            }
        }, 20L);
    }
    //here the error appers. I need to add something like callLater

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        Player killedPlayer = event.getEntity();
        Player killer = killedPlayer.getKiller();

        if (killer != null)
        {
            addScore(killer, PLAYERBOUNTY);
        }
        
        addScore(killedPlayer, -PLAYERBOUNTY);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        Player player = entity.getKiller();

        if(player != null)
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
        event.getAdvancement().getCriteria().forEach((a) -> player.sendMessage(a));
    }

    public void addScore(Player player, int amount)
    {
        Score score = objective.getScore(player.getName());
        int newScore = score.getScore() + amount;
        score.setScore(newScore);
    }

    public void setScoreboardToPlayer(Player player)
    {
        teamManager.getTeamByPlayer(player.getName()).addEntry(player.getName());
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

        for (String i : teamManager.getTeamsNames()) {
            teamManager.teams.put(i, scoreboard.registerNewTeam(i));
        }
    }
}
