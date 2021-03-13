package com.xvojta.famfrpal;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Iterator;

public class Compass
{
    HashMap<Player, Iterator> trackingPlayers = new HashMap<Player, Iterator>();

    public Compass()
    {

    }

    public void onRightButtonPress(CommandSender sender, Plugin plugin)
    {
        if (!(sender instanceof Player)) return;
        if (Bukkit.getOnlinePlayers().size() < 2) {
            sender.sendMessage("Not enough players on server");
            return;
        }

        final Player player = (Player)sender;


        //First check if player is holding a compass
        if (!(player.getInventory().getItemInMainHand().getType().equals(Material.COMPASS) || player.getInventory().getItemInOffHand().getType().equals(Material.COMPASS)))
        {
            return;
        }

        Iterator itr;

        if (trackingPlayers.containsKey(player))
        {
            itr = trackingPlayers.get(player);
        }
        else
        {
            itr = Bukkit.getOnlinePlayers().iterator();
        }

        Player newTarget;
        newTarget = (Player) itr.next();

        //Don't allow tracking self
        while (newTarget == player || newTarget == null || !player.canSee(newTarget) || !newTarget.hasPermission("fp.admin"))
        {
            if (itr.hasNext()) {
                newTarget = (Player) itr.next();
            }
            else
            {
                itr = Bukkit.getOnlinePlayers().iterator();
            }
        }

        final Player target = newTarget;

        trackingPlayers.put(player, itr);
        player.sendMessage(ChatColor.GREEN + "Your compass is now tracking " + target.getName() + ".");

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
        }.runTaskTimer(plugin, 5L, 300L);
    }

    public void removePlayersOnQuit(Player player)
    {
        trackingPlayers.remove(player);
    }
}
