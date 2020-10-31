package com.javaminecraft;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Thread.sleep;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import java.net.URL;
import org.bukkit.ChatColor;
import org.bukkit.command.ConsoleCommandSender;




public class Slaparoo extends JavaPlugin 
    implements Listener {

    public static final Logger LOG = Logger.getLogger("Minecraft"); 

    private static final String SLAPAROO_SIGN = "slaparooSign";    
    
    // config values
    private static String LOBBY_WORLD_NAME;
    private static int WINNER_SCORE = 20;
    private static String SLAPAROO_WORLD_NAME;
    public static int MAX_PLAYER_COUNT = 10;
    public static int MIN_PLAYER_COUNT = 2;
    public static int KNOCKBACK_LEVEL = 3;
    
    boolean on = false;
    Sign sign;
    boolean gameIsRuning = false;
    HashMap<Player, Player> lastdamager = new HashMap<>();
    Scoreboard board;
    Objective objective;
    private boolean signOn = false;
    Player me;
    Server server = getServer();
    ConsoleCommandSender console = server.getConsoleSender();

    
    @Override
    public boolean onCommand(CommandSender sender, 
        Command command, String label, String[] arguments) {
         
        me = (Player) sender;
        World world = me.getWorld();
        Location spot = me.getLocation();
        
         
            if (label.equalsIgnoreCase("sl")) { 
                if (sender instanceof Player && me.isOp()) {
                    if (arguments.length > 1) {
                        if (arguments[0].equals("join")) {
                            if (server.getPlayer(arguments[1]) != null && 
                                    playerJoin(server.getPlayer(arguments[1]))) 
                            {                                
                                me.sendMessage(arguments[1] + " joined Slaparoo!");
                            } else {
                                me.sendMessage(arguments[1] + " did NOT joined Slaparoo!");
                            }
                            return true;
                        }
                    }
                } else {
                    me.sendMessage("You don't have permissions to this command!");
                }
            }
            if (label.equalsIgnoreCase("slaparoosign")) { 
                if (sender instanceof Player && me.isOp()) {
                    if (arguments.length > 0) {
                        if (arguments[0].equals("on")) {
                            signOn = true;
                            me.sendMessage("slaparoo sign mode on !");
                        } else {
                            signOn = false;
                            me.sendMessage("slaparoo sign mode off !");
                        }
                        return true;
                    }
                } else {
                    me.sendMessage("You don't have permissions to this command!");
                }
            }
        return false;
    }
    
      // make this class listen to events
    @Override
    public void onEnable() {
        Server server = getServer();
        PluginManager manager = server.getPluginManager();
        manager.registerEvents(this, this);
        
        console.sendMessage(ChatColor.GREEN + "          +---------+");
        console.sendMessage(ChatColor.GREEN + "          |" + ChatColor.AQUA + " Slaproo " + ChatColor.GREEN + "|");
        console.sendMessage(ChatColor.GREEN + "          +---------+");
        console.sendMessage(" ");
        console.sendMessage(ChatColor.YELLOW + "       Slaparoo is enabled");
        if (!isLastVersion()) {
            console.sendMessage(ChatColor.RED + "+--------------------------------------------------------------------------------------+");
            console.sendMessage(ChatColor.RED + "| New version of Slaparoo available on: https://dev.bukkit.org/projects/slaparoo/files |");
            console.sendMessage(ChatColor.RED + "+--------------------------------------------------------------------------------------+");
        }
        if (!new File(getDataFolder(),"config.yml").exists()) {
          saveDefaultConfig();
            
        }
        FileConfiguration config = getConfig();
        LOBBY_WORLD_NAME = config.getString("lobby-world-name");
        SLAPAROO_WORLD_NAME = config.getString("slaparoo-world-name");
        WINNER_SCORE = config.getInt("winner-score");
        MIN_PLAYER_COUNT = config.getInt("minimal-player");
        MAX_PLAYER_COUNT = config.getInt("maximal-player");
        KNOCKBACK_LEVEL = config.getInt("knockback-level");
    }
    
    private void dejSusenkuHraci(Player pl) {
        ItemStack cookie = new ItemStack(Material.COOKIE);
        cookie.addUnsafeEnchantment(Enchantment.KNOCKBACK, KNOCKBACK_LEVEL);
        pl.getInventory().addItem(cookie);        
    }
    
    public void gameStart (List<Player> players, World world) {
        if(sign != null) {
            sign.setLine(2, "GAME RUN");
            sign.update();            
        }
        for (Player pl:players) {
            dejSusenkuHraci(pl);
            TitleAPI.sendTitle(pl, 1*20, 2*20, 1*20, "GAME STARTED", "Kick them all!");
            pl.sendMessage("You must get " + WINNER_SCORE + " points to win.");
        }
    }
    
    @EventHandler
    public void onSignChange (SignChangeEvent event) {
        if(signOn && event.getPlayer().equals(me)) {
            event.setLine(0, "Slaparoo COOKIE");
            event.setLine(3, "<left click>");
        }
    }        
    
    @EventHandler
    public void onPlayerRespawn (PlayerRespawnEvent event) {
        if(event.getRespawnLocation().getWorld().getName().equals(SLAPAROO_WORLD_NAME)) {
            if(!gameIsRuning){
                event.setRespawnLocation(Bukkit.getWorld(LOBBY_WORLD_NAME).getSpawnLocation());
            } else {
                dejSusenkuHraci(event.getPlayer());
            }
        }
    }
    
    

    
    @EventHandler
    public void onPlayerInteract (PlayerInteractEvent event) {
        if(event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if(block.getType() == Material.WALL_SIGN) {
                sign = (Sign) block.getState();
                if(sign.getLine(0).equals("Slaparoo COOKIE")) {
                    playerJoin(event.getPlayer());
               }
            }
        }
    }        

    
    @EventHandler
    public void onPlayerChangedWorld (PlayerChangedWorldEvent event) {
        Player newPlayer = event.getPlayer();
        World world = newPlayer.getWorld();
        int activePlayerCount = world.getPlayers().size();

        // player has left
        if(event.getFrom().getName().equals(SLAPAROO_WORLD_NAME)){
            int pocetHracu = event.getFrom().getPlayers().size();
            if(board != null){
                board.resetScores(newPlayer);
            }
            if(sign!=null){
                sign.setLine(1,pocetHracu +"/"+MAX_PLAYER_COUNT);
                sign.update();                
            }
            if(pocetHracu <= 1 && gameIsRuning){
                GameOver(newPlayer);
                return;
            }    
        }
        
        // player joined Slaparoo        
        if(world.getName().equals(SLAPAROO_WORLD_NAME)) {
            if(sign != null) {
                sign.setLine(1, activePlayerCount+"/"+MAX_PLAYER_COUNT);
                sign.update();                
            }
            for (Player pl:world.getPlayers()) {
                pl.sendMessage("There is " + activePlayerCount + " players in the game");            
            }
            if(activePlayerCount >= MIN_PLAYER_COUNT && !gameIsRuning) {
                gameIsRuning = true;
                Thread slaparooStarterThread = new Thread(new SlaparooStarter(world, this, world.getPlayers()));
                slaparooStarterThread.start();
                craeteScoreBoard(world);
            }
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntityEvent  (EntityDamageByEntityEvent event) {
       
        if ((event.getEntity() instanceof Player) && (event.getDamager() instanceof Player)) {
            lastdamager.put((Player) event.getEntity(),  (Player) event.getDamager());            
        }
    }



    @EventHandler
    public void onPlayerDeath (PlayerDeathEvent event) {
        Player killedPlayer = event.getEntity();
        Player killer = lastdamager.get(killedPlayer);
        EntityDamageEvent kpDamageEvent = killedPlayer.getLastDamageCause();
        if ((objective != null) && (killer != null) && kpDamageEvent.getCause().equals(DamageCause.VOID) && 
             killedPlayer.getWorld().getName().equals(SLAPAROO_WORLD_NAME) && gameIsRuning) {
            Score score = objective.getScore(killer);
            lastdamager.remove(killedPlayer);
            int newScore = score.getScore() + 1;
            score.setScore(newScore);
            if (newScore >= WINNER_SCORE) {
                GameOver(killer);
            }
            
        }
        
    }

    public boolean playerJoin(Player pl){
        World world = Bukkit.getWorld(SLAPAROO_WORLD_NAME);
        if(world == null) {
           console.sendMessage(ChatColor.RED + "World " + SLAPAROO_WORLD_NAME + " does not exist. Check slaparoo config file.");
        } else {
            int playerCount = world.getPlayers().size();        
            if(playerCount < MAX_PLAYER_COUNT && !gameIsRuning) {
               pl.teleport(Bukkit.getWorld(SLAPAROO_WORLD_NAME).getSpawnLocation());
               return true;
           }
        }   
        return false;
    }
        
        
    private void craeteScoreBoard(World world) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        board = manager.getNewScoreboard();
        objective = board.registerNewObjective("Slaparoo", "dummy");
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

    private void GameOver(Player winner) {
        World world = winner.getWorld();
        for (Player pl:world.getPlayers()) {
            pl.getInventory().remove(Material.COOKIE);
            if(world.getPlayers().size() <= 1 && gameIsRuning) {
                TitleAPI.sendTitle(pl, 1*20, 3*20, 1*20, "Your opponents left the game", "");                
            } else {
                TitleAPI.sendTitle(pl, 1*20, 3*20, 1*20, winner.getName() + " IS THE WINNER", "");                
            }            
            board.resetScores(pl);
            pl.teleport(Bukkit.getWorld(LOBBY_WORLD_NAME).getSpawnLocation());            
        }
        if(sign != null) {
            sign.setLine(2, " ");
            sign.update();
            gameIsRuning = false;
        }
    }

    private boolean isLastVersion() {
        BufferedReader in = null;
        try {
            URL slaparooPage = new URL("https://dev.bukkit.org/projects/slaparoo");
            in = new BufferedReader(
                    new InputStreamReader(slaparooPage.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                int verIndex = inputLine.indexOf("(Version:");
                if (verIndex != -1) {
                    String version = inputLine.substring(verIndex+10, verIndex+10+3);                    
                    return version.equals(Bukkit.getServer().getPluginManager().getPlugin("Slaparoo").getDescription().getVersion());
                }
            }
            in.close();
            return false;
        } catch (IOException ex) {
            LOG.info(ex.toString());
            return true;
        } finally {
            try {
                in.close();
            } catch (IOException ex) {
                LOG.info(ex.toString());
            }
        }         
    } 
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        World world = Bukkit.getWorld(SLAPAROO_WORLD_NAME);
        int activePlayerCount = world.getPlayers().size();
        if(activePlayerCount <= 1 && gameIsRuning){
            GameOver(event.getPlayer());
        }
    }

}

class SlaparooStarter implements Runnable {
    List<Player> players;
    World world;
    Slaparoo slaparoo;
    public static int START_COUNTDOWN = 20; //pisu presne co me tatka diktuje

    SlaparooStarter(World world, Slaparoo slaparoo, List<Player> players) {
        this.slaparoo = slaparoo;
        this.world = world;
        this.players = players;
    }
    
    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void run() {
        Slaparoo.LOG.info("RUN");
        try {
          int pocetHrajicichHracu = world.getPlayers().size();
          for(int i = 0; i<START_COUNTDOWN; i++) {
            Slaparoo.LOG.info("FOR " + i);
            sleep(1000);
            if(pocetHrajicichHracu < Slaparoo.MIN_PLAYER_COUNT) {
                return;
            }    
            if(i%5 == 0 || i > (START_COUNTDOWN-5)) {
                for (Player pl:world.getPlayers()) {
                    TitleAPI.sendTitle(pl, 10, 20, 10, START_COUNTDOWN-i + "", "");
                    pl.sendMessage("Slaparoo starts in " + (START_COUNTDOWN-i));
                }
            }

          }
          slaparoo.gameStart(players, world);
        } catch (InterruptedException e) {
          // doresit !!!  
        }
    }
    
}
