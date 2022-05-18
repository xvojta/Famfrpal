package com.xvojta.famfrpal;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class Compass
{
    HashMap<Player, Player> trackingPlayers = new HashMap<Player, Player>();

    public void onRightButtonPress(CommandSender sender, Plugin plugin)
    {
        if (!(sender instanceof Player)) return;

        final Player player = (Player)sender;

        //First check if player is holding a compass
        if (!(player.getInventory().getItemInMainHand().getType().equals(Material.COMPASS) || player.getInventory().getItemInOffHand().getType().equals(Material.COMPASS)))
        {
            return;
        }

        //check if enough valid players are on the server
        int validPlayers = 0;
        for (Player p:Bukkit.getOnlinePlayers())
        {
            if(p != player && player.canSee(p) && !p.hasPermission("fp.admin")) validPlayers++;
        }
        if (validPlayers < 1) {
            sender.sendMessage("Not enough players on server");
            return;
        }

        Player newTarget;

        if (trackingPlayers.containsKey(player))
        {
            newTarget = getNextPlayer(trackingPlayers.get(player));
        }
        else
        {
            newTarget = getNextPlayer(null);
        }

        //Don't allow tracking self, admins and spectators
        while (newTarget == player || !player.canSee(newTarget) || newTarget.hasPermission("fp.admin"))
        {
            newTarget = getNextPlayer(newTarget);
        }

        final Player target = newTarget;

        trackingPlayers.put(player, target);
        player.sendMessage(ChatColor.GREEN + "Your compass is now tracking " + ChatColor.RED + target.getName() + ChatColor.GREEN + ".");

        new BukkitRunnable()
        {
            public void run()
            {
                //Cancel task if player is offline or is no longer tracking target
                if (!player.isOnline() || !trackingPlayers.containsKey(player) || !trackingPlayers.get(player).equals(target))
                    this.cancel();

                    //Cancel task if target is offline
                else
                    player.setCompassTarget(target.getLocation());
            }
        }.runTaskTimer(plugin, 5L, 40L);
    }

    public void removePlayersOnQuit(Player player)
    {
        trackingPlayers.remove(player);
    }

    private Player getNextPlayer(Player player)
    {
        ArrayList<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());
        ArrayList<UUID> playersID = new ArrayList<>();
        for (Player p:players)
        {
            playersID.add(p.getUniqueId());
        }
        Collections.sort(playersID);

        if (player == null) return Bukkit.getPlayer(playersID.get(0));

        int currentIDIndex = playersID.indexOf(player.getUniqueId());
        if (playersID.size() > currentIDIndex+1)
        {
            return Bukkit.getPlayer(playersID.get(currentIDIndex+1));
        }

        return Bukkit.getPlayer(playersID.get(0));
    }
}
