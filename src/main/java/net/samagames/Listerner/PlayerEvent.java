package net.samagames.Listerner;

import net.samagames.Wasteland;
import net.samagames.WastelandItem;
import net.samagames.player.WastelandPlayer;
import net.samagames.tools.chat.ActionBarAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;

/**
 * Created by werter on 08.03.2017.
 */
public class PlayerEvent implements Listener {
    Wasteland wasteland;

    public PlayerEvent (Wasteland wasteland){
        this.wasteland = wasteland;
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if (event.getItem().getItemStack().getType().equals(Material.WHEAT)) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            WastelandPlayer wastelandPlayer = wasteland.getWastelandPlayer(player);
            int wheat = wastelandPlayer.getWheat();
            int amount = event.getItem().getItemStack().getAmount();
            if (wheat < 50) {
                event.getItem().remove();
                if (wheat + amount > 50) {
                    wastelandPlayer.setWheat(50);
                    ItemStack itemStack = event.getItem().getItemStack();
                    itemStack.setAmount(wheat + amount - 50);
                    player.getWorld().dropItem(event.getItem().getLocation(), itemStack);
                    player.sendMessage("Vous ne pouvez plus rammaser de blés");
                    player.playSound(player.getLocation(),Sound.ENTITY_PLAYER_LEVELUP,(float) 1 , (float) 1);
                } else {
                    wastelandPlayer.addWheat(amount);
                    player.playSound(player.getLocation(),Sound.ITEM_HOE_TILL,(float) 0.5,(float) 0.5);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){
        Player player = event.getPlayer();
        WastelandPlayer wastelandPlayer = wasteland.getWastelandPlayer(player);
        event.setRespawnLocation(wastelandPlayer.getTeam().getSpawn());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        if(event.getEntity() instanceof Player){
            Player player = event.getEntity();
            WastelandPlayer wastelandPlayer = wasteland.getWastelandPlayer(player);
            event.setKeepInventory(true);
            if(wastelandPlayer.getWheat() > 0)
                event.setDeathMessage(player.getName()+ " est mort avec :" + wastelandPlayer.getWheat() + " blés sur lui");
            if(wastelandPlayer.getWheat() > 0){
                event.getDrops().clear();
                event.getDrops().add(new ItemStack(Material.WHEAT,wastelandPlayer.getWheat()));
                wastelandPlayer.setWheat(0);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        WastelandPlayer wastelandPlayer = wasteland.getWastelandPlayer(player);
        if(event.hasBlock())
            if(wastelandPlayer.hasTeam()) {
                if(wastelandPlayer.getTeam().getEnnemies().getChestLocation().equals(event.getClickedBlock().getLocation())){
                    //TODO ADD COOLDOWN AND RANDOM
                    event.setCancelled(true);
                    if(wastelandPlayer.getTeam().getEnnemies().getWheat() < 16){
                        player.sendMessage("L'équipe adverse n'as pas assez de ressources pour être volé");
                        return;
                    }
                    if(wastelandPlayer.getWheat() == 50){
                        player.sendMessage("Vous ne pouvez pas voler de ressource car vous êtes full");
                        return;
                    }
                    int capacity = 50 - wastelandPlayer.getWheat();
                    if(capacity > 15)
                        capacity = 15;
                    wastelandPlayer.getTeam().getEnnemies().removeWheat(capacity);
                    wastelandPlayer.addWheat(capacity);
                    player.sendMessage("Vous avez volé " + capacity + " blés");
                }
                if (wastelandPlayer.getTeam().getChestLocation().equals(event.getClickedBlock().getLocation())) {
                    wastelandPlayer.getTeam().addWheat(wastelandPlayer.getWheat());
                    wastelandPlayer.setWheat(0);
                    event.setCancelled(true);
                }
            }
        if(!wasteland.isStarted() && event.hasItem()){
            event.setCancelled(true);
            ItemStack item = event.getItem();
            if(item.equals(WastelandItem.JOIN_TEAM_BLUE.getItemStack())){
                wasteland.setTeamBlue(player);
            }
            if(item.equals(WastelandItem.JOIN_TEAM_RED.getItemStack())) {
                wasteland.setTeamRed(player);
            }
        }
    }
}
