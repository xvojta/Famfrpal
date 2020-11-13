package com.xvojta.famfrpal;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import org.bukkit.*;
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
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.*;
import java.util.logging.Logger;

public class Famfrpal extends JavaPlugin implements Listener
{
    private static final int ENDERDRAGONSCORE = 100;
    private static final int PLAYERBOUNTY = 5;
    private static final int ELDERGUARDIANSCORE = 50;
    private static final int WITHERSCORE = 50;
    Map<String, Integer> ADVANCEMENTSSCORES;
    public static Famfrpal Instance;
    public boolean started = false;
    public Logger LOGGER = Bukkit.getLogger();
    public Scoreboard scoreboard;
    public Objective objective;
    public FPTeamManager teamManager;

    @Override
    public void onEnable()
    {
        //Load advancement score rewards from yaml file
        Yaml yaml = new Yaml();
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("achievements.yml");
        ADVANCEMENTSSCORES = yaml.load(inputStream);
        LOGGER.info(ADVANCEMENTSSCORES.toString());

        Instance = this;

        LOGGER.info("Famfrpal plugin funguje jako vzdy");

        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] arguments)
    {
        World world = getServer().getWorlds().get(0);


        if (label.equalsIgnoreCase("fpstart")) {
            if (!(sender instanceof Player)) {
                start(world);
            }
            else
            {
                Player player = (Player) sender;
                if (/*player.isOp()*/ player.hasPermission("fp.admin"))
                {
                    start(world);
                }
                else
                {
                    player.sendMessage("You don't have permissions to this command!");
                }
            }
        }
        else if (label.equalsIgnoreCase("fpend")) {
            if (!(sender instanceof Player)) {
                end();
            }
            else
            {
                Player player = (Player) sender;
                if (/*player.isOp()*/ player.hasPermission("fp.admin"))
                {
                    end();
                }
                else
                {
                    player.sendMessage("You don't have permissions to this command!");
                }
            }
        }
        else if (label.equalsIgnoreCase("fpend")) {
            if (!(sender instanceof Player)) {
                if(arguments[0] != null && arguments[1] != null)
                {

                }
            }
            else
            {
                Player player = (Player) sender;
                if (/*player.isOp()*/ player.hasPermission("fp.admin"))
                {
                    end();
                }
                else
                {
                    player.sendMessage("You don't have permissions to this command!");
                }
            }
        }
        return false;
    }

    @EventHandler (priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        if (started)
        {
            Bukkit.getScheduler().runTaskLater(this, new Runnable() {
                @Override
                public void run() {
                    player.setScoreboard(scoreboard);
                }
            }, 20L);
        }
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
                end();
                //end game
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
            addScore(player, ADVANCEMENTSSCORES.get(advancementKey));
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
            //teamManager.teams.get(s).setColor(ChatColor.values()[i]);
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
            for (Player p : getServer().getOnlinePlayers())
            {
                if (p.hasPermission("fp.team" + i) && !p.isOp())
                {
                    teamPlayers.add(p);
                }
            }
            teams.put("team " + i, teamPlayers);
        }

        teamManager = new FPTeamManager(teams);

        craeteScoreBoard(world);

        getServer().getOnlinePlayers().forEach(player -> {setScoreboardToPlayer(player);});

        getServer().getOnlinePlayers().forEach(player -> {player.sendMessage("Speedrun has started");});

        started = true;
    }

    private void end()
    {
        ListMultimap<Integer, Set<OfflinePlayer>> teamsScoresPlayers = ArrayListMultimap.create();
        LOGGER.info(teamManager.teams.toString());
        teamManager.teams.values().forEach(team -> {
            LOGGER.info(team.getName());
            int score = 0;
            for (OfflinePlayer player : team.getPlayers())
            {
                score += objective.getScore(player.getName()).getScore();
                LOGGER.info(player.getName() + " " + objective.getScore(player.getName()).getScore());
            }
            teamsScoresPlayers.put(score, team.getPlayers());
        });
        List<Integer> scoresSorted = new ArrayList<Integer>(teamsScoresPlayers.keySet());
        Collections.sort(scoresSorted);
        for(Integer i : scoresSorted)
        {
            for (Player player : getServer().getOnlinePlayers())
            {
                for(Set<OfflinePlayer> offlinePlayers: teamsScoresPlayers.get(i))
                {
                    offlinePlayers.forEach(offlinePlayer ->
                    {
                        player.sendMessage(offlinePlayer.getPlayer().getDisplayName() + ": " + objective.getScore(offlinePlayer.getName()).getScore());
                    });
                    player.sendMessage("Total team score: " + i.toString());
                }
            }
        }
        World overworld = getServer().getWorlds().get(0);
        for (Player player : getServer().getOnlinePlayers())
        {
            player.teleport(overworld.getSpawnLocation());
        }

        getServer().getOnlinePlayers().forEach(player -> {player.sendMessage("Speedrun has ended");});

        started = false;
    }
}