package com.xvojta.famfrpal;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionType;
import org.bukkit.scoreboard.*;

import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

public class Famfrpal extends JavaPlugin implements Listener
{
    HashMap<String, String[]> DropsOverrite = new HashMap<>();
    HashMap<String, String[]> Teams = new HashMap<>();
    HashMap<UUID, ArrayList<UUID>> PearlDrops = new HashMap<>();
    //HashMap <Killer, list<KilledPlayers>>
    public static Famfrpal Instance;
    public boolean started = false;
    public Logger LOGGER = Bukkit.getLogger();
    public Scoreboard scoreboard;
    public Compass compass;
    private static final PotionType[] hamfulPotions = {PotionType.WEAKNESS, PotionType.POISON, PotionType.SLOWNESS, PotionType.INSTANT_DAMAGE};
    private static final PotionType[] friendlyPotions = {PotionType.SPEED, PotionType.JUMP, PotionType.STRENGTH, PotionType.INSTANT_HEAL, PotionType.REGEN};

    private static Gson gson = new GsonBuilder().enableComplexMapKeySerialization().create();

    @Override
    public void onEnable()
    {
        compass = new Compass();

        Instance = this;

        LOGGER.info("Famfrpal plugin funguje jako vzdy");

        PluginManager manager = getServer().getPluginManager();
        manager.registerEvents(this, this);

        // Examle of creating the drops.json
        /*DropsOverrite.put(Material.DIRT.name(), new String[]{ Material.DIAMOND.name(), Material.MILK_BUCKET.name()});
        DropsOverrite.put(Material.GRASS_BLOCK.name(), new String[] { Material.GOLDEN_HOE.name()} );
        String json = gson.toJson(DropsOverrite);

        try (PrintWriter out = new PrintWriter(new FileWriter("drops.json"))) {
            out.write(json);
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        String dropsJson = "";
        String teamsJson = "";
        try
        {
            dropsJson = new String(Files.readAllBytes(Paths.get("drops.json")));
            teamsJson = new String(Files.readAllBytes(Paths.get("teams.json")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Type type = new TypeToken<HashMap<String, String[]>>(){}.getType();
        DropsOverrite = (HashMap<String, String[]>) gson.fromJson(dropsJson,type);
        Teams = (HashMap<String, String[]>) gson.fromJson(teamsJson,type);

        Iterator<String> iterator = Teams.keySet().iterator();
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        for (int i = 0; i < Teams.keySet().size(); i++) {
            String teamName = iterator.next();
            Team Team = scoreboard.registerNewTeam(teamName);
            Team.setAllowFriendlyFire(false);
            Team.setCanSeeFriendlyInvisibles(true);
            Team.setPrefix(teamName + " ");
            Team.setColor(ChatColor.values()[i + 1]); //0 is black, not really visible
            for (String player : Teams.get(teamName)) {
                Team.addPlayer(Bukkit.getOfflinePlayer(player));
            }
        }
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

    @EventHandler(priority=EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event){
        Block block = event.getBlock();
        Player player = event.getPlayer();
        if (DropsOverrite.containsKey(block.getType().name())) {
            for (String mat : DropsOverrite.get(block.getType().name())) {
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.getMaterial(mat), 1));
            }
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
        }
        if (block.getType() == Material.EMERALD_ORE) {
            ((ExperienceOrb) block.getWorld().spawn(block.getLocation(), ExperienceOrb.class)).setExperience(7);
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);
        }
        if (block.getType() == Material.REDSTONE_ORE) {

            //get Potion
            Potion potion;
            if(Math.random() > 0.5)
            {
                potion = new Potion(getRandomPotion(hamfulPotions));
                potion.setSplash(true);
            }
            else {
                potion = new Potion(getRandomPotion(friendlyPotions));
                potion.setSplash(false);
            }

            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), potion.toItemStack(1));
            event.setCancelled(true);
            event.getBlock().setType(Material.AIR);

            //get Upgrade
            if(Math.random() < 0.3)
            {
                if (Math.random() > 0.5)
                {
                    event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.REDSTONE, 1));
                }
                else
                {
                    event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.GLOWSTONE_DUST, 1));
                }
            }
        }
    }

    @EventHandler
    public  void onPlayerQuit(PlayerQuitEvent event)
    {
        compass.removePlayersOnQuit(event.getPlayer());
    }

    @EventHandler
    public  void OnPlayerJoin(PlayerJoinEvent event)
    {
        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                event.getPlayer().setScoreboard(scoreboard);
            }
        }, 20L);
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event)
    {
        if (event.getAction().equals(Action.LEFT_CLICK_AIR) || event.getAction().equals(Action.LEFT_CLICK_BLOCK)) return;
        compass.onRightButtonPress(event.getPlayer(), this);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        Player killedPlayer = event.getEntity();
        Player killer = killedPlayer.getKiller();

        if (started) {
            if (killer != null) {
                if(PearlDrops.containsKey(killer.getUniqueId())) {
                    if(PearlDrops.get(killer.getUniqueId()).contains(killedPlayer.getUniqueId())) {
                        return;
                    }
                }
                killedPlayer.getWorld().dropItemNaturally(killedPlayer.getLocation(), new ItemStack(Material.ENDER_PEARL, 1));
                if(PearlDrops.containsKey(killer.getUniqueId())) {
                    PearlDrops.get(killer.getUniqueId()).add(killedPlayer.getUniqueId());
                }
                else {
                    PearlDrops.put(killer.getUniqueId(), new ArrayList<UUID>() {{ add(killedPlayer.getUniqueId()); }});
                }
            }
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event)
    {
        LivingEntity entity = event.getEntity();
        Player player = entity.getKiller();

        if(player != null)
        {
            if(!player.isOp()) {
                if (event.getEntityType() == EntityType.ENDER_DRAGON) {
                    LOGGER.info(player.getDisplayName());
                    end();
                    return;
                    //end game
                }
            }
            if(DropsOverrite.containsKey(event.getEntityType().name()))
            {
                event.getDrops().clear();

                for(String mat : DropsOverrite.get(event.getEntityType().name())) {
                    ItemStack stack = new ItemStack(Material.getMaterial(mat), 1);
                    event.getDrops().add(stack);
                }
            }
        }
    }

    private void start(World world)
    {
        HashMap<String, ArrayList<Player>> teams = new HashMap<String, ArrayList<Player>>();

        getServer().getOnlinePlayers().forEach(player -> {player.sendMessage(ChatColor.RED + "Speedrun has started");});

        started = true;
    }

    private void end()
    {

        World overworld = getServer().getWorlds().get(0);
        for (Player player : getServer().getOnlinePlayers())
        {
            player.teleport(overworld.getSpawnLocation());
        }

        getServer().getOnlinePlayers().forEach(player -> {player.sendMessage(ChatColor.RED + "Speedrun has ended");});

        started = false;
    }

    public static PotionType getRandomPotion(PotionType[] array) {
        int rnd = new Random().nextInt(array.length);
        return array[rnd];
    }
}