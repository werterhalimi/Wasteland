package net.samagames.Listerner;

import net.samagames.Wasteland;
import net.samagames.WastelandItem;
import net.samagames.entity.Plant;
import net.samagames.entity.PlantType;
import net.samagames.player.Kit;
import net.samagames.player.WastelandPlayer;
import net.samagames.tools.chat.ActionBarAPI;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

/*
 * This file is part of Wasteland.
 *
 * Wasteland is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Wasteland is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Wasteland.  If not, see <http://www.gnu.org/licenses/>.
 */
public class PlayerEvent implements Listener {
    Wasteland wasteland;

    public PlayerEvent (Wasteland wasteland){
        this.wasteland = wasteland;
    }

    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event){
        if(!wasteland.hasPlayer(event.getPlayer()) || wasteland.isGameStarted())
            return;
        Player player = event.getPlayer();
        if(player.getInventory().getItem(event.getNewSlot()) == null) {
            ActionBarAPI.removeMessage(player, true);
            return;
        }
        ActionBarAPI.sendPermanentMessage(event.getPlayer(),player.getInventory().getItem(event.getNewSlot()).getItemMeta().getDisplayName());
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event){
        if(!wasteland.hasPlayer(event.getPlayer()) || !wasteland.isGameStarted() )
            return;
        if(event.getBlock().getType().equals(Material.CROPS) && event.getPlayer() != null){
            WastelandPlayer wastelandPlayer = wasteland.getWastelandPlayer(event.getPlayer());
            if(wastelandPlayer.getTeam() == null) return;
            if(!(wastelandPlayer.getWheat() == 50)){
                wastelandPlayer.addWheat(1);
                event.getPlayer().playSound(event.getPlayer().getLocation(),Sound.ITEM_HOE_TILL,(float) 0.5,(float) 0.5);
                wasteland.addLocation(event.getBlock().getLocation());
            }
            event.getBlock().setType(Material.AIR);
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        if(!wasteland.hasPlayer(event.getPlayer()) )
            return;
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
                } else {
                    wastelandPlayer.addWheat(amount);
                    player.playSound(player.getLocation(),Sound.ITEM_HOE_TILL,(float) 0.5,(float) 0.5);
                }
            }
        }else if(event.getItem().getItemStack().getType().equals(Material.RED_ROSE) || event.getItem().getItemStack().getType().equals(Material.DOUBLE_PLANT))
            ActionBarAPI.sendMessage(event.getPlayer(),ChatColor.YELLOW + "Vous avez rammasé une plant.");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event){
        if(event.getCurrentItem() == null && event.getCurrentItem().getType().equals(Material.AIR))
            return;
        if(!wasteland.isGameStarted()) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            WastelandPlayer wastelandPlayer = wasteland.getWastelandPlayer(player);
            player.closeInventory();
            ItemStack item = event.getCurrentItem();
            if(player.getInventory().getItem(0).equals(item)){
                Inventory inventory = Bukkit.createInventory(null, InventoryType.HOPPER, item.getItemMeta().getDisplayName());
                inventory.setItem(WastelandItem.JOIN_TEAM_BLUE.getSlot(),WastelandItem.JOIN_TEAM_BLUE.getItemStack());
                inventory.setItem(WastelandItem.JOIN_TEAM_RED.getSlot(),WastelandItem.JOIN_TEAM_RED.getItemStack());
                player.openInventory(inventory);

            }
            if(item.equals(WastelandItem.JOIN_TEAM_BLUE.getItemStack())) {
                wasteland.setTeamBlue(player);
                wastelandPlayer.setBannerColor(DyeColor.BLUE);
                return;
            }
            if(item.equals(WastelandItem.JOIN_TEAM_RED.getItemStack())) {
                wasteland.setTeamRed(player);
                wastelandPlayer.setBannerColor(DyeColor.RED);
                return;
            }
            if(item.equals(WastelandItem.KIT_SELECTOR.getItemStack())){
                wastelandPlayer.openKitSelector();
                return;
            }
            if(item.equals(WastelandItem.CHOOSE_KIT_DEFENDER.getItemStack())){
                wastelandPlayer.setKit(wasteland.getKitDefender(),true);
                return;
            }
            if(item.equals(WastelandItem.CHOOSE_KIT_DEMOLISHER.getItemStack())) {
                wastelandPlayer.setKit(wasteland.getKitDemolisher(),true);
                return;

            }
            if(item.equals(WastelandItem.CHOOSE_KIT_HERBALIST.getItemStack())) {
                wastelandPlayer.setKit(wasteland.getKitHerbelist(),true);
                return;
            }
            if(item.equals(WastelandItem.CHOOSE_KIT_ROBBER.getItemStack())) {
                wastelandPlayer.setKit(wasteland.getKitRobber(),true);
                return;
            }
            if(item.equals(WastelandItem.CHOOSE_KIT_TRAPPER.getItemStack())) {
                wastelandPlayer.setKit(wasteland.getKitTrapper(),true);
                return;
            }
        }
    }


    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event){
        if(!wasteland.hasPlayer(event.getPlayer()) )
            return;
        Player player = event.getPlayer();
        WastelandPlayer wastelandPlayer = wasteland.getWastelandPlayer(player);
        event.setRespawnLocation(wastelandPlayer.getTeam().getSpawn());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event){
        if(!wasteland.hasPlayer(event.getEntity()))
            return;
        if(event.getEntity() instanceof Player) {
            Player player = event.getEntity();
            WastelandPlayer wastelandPlayer = wasteland.getWastelandPlayer(player);
            player.getInventory().clear();
            wastelandPlayer.getKit().equip(wastelandPlayer);
            event.setKeepInventory(true);
            event.setDroppedExp(0);
            event.setDeathMessage(null);
            if (!(wastelandPlayer.getKit().equals(Kit.HERBALIST))){
                if (new Random().nextInt(50) == 3)
                    new Plant(event.getEntity().getLocation()).spawn();
            }else
            if(new Random().nextInt(wastelandPlayer.getAmplifier()) == 3)
                new Plant(event.getEntity().getLocation()).spawn();
            event.setDeathMessage(ChatColor.GRAY + player.getName() + ChatColor.YELLOW + " a été tué par " + ChatColor.GRAY + event.getEntity().getKiller().getName());
            if(wastelandPlayer.getWheat() > 0){
                event.setDeathMessage(event.getDeathMessage() + ChatColor.YELLOW + " droppant " +ChatColor.GRAY + wastelandPlayer.getWheat() + " blées");
                event.getDrops().clear();
                event.getEntity().getWorld().dropItem(event.getEntity().getLocation(),new ItemStack(Material.WHEAT,wastelandPlayer.getWheat()));
                wastelandPlayer.setWheat(0);
            }
        }
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event){
        if(!wasteland.hasPlayer(event.getPlayer()))
            return;
        if(!wasteland.isGameStarted())
            event.setCancelled(true);
        Player player = event.getPlayer();
        WastelandPlayer wastelandPlayer = wasteland.getWastelandPlayer(player);

        //plant
        if(wasteland.isGameStarted() && event.hasItem())
            if(event.getItem().getType().equals(Material.DOUBLE_PLANT) || event.getItem().getType().equals(Material.RED_ROSE)){
                for(PlantType plantType : PlantType.values())
                    if(plantType.getItemStack().getItemMeta().equals(event.getItem().getItemMeta())) {
                        if(plantType.isBonus())
                            wasteland.playEffect(player,wastelandPlayer.getTeam(),plantType);
                        else
                            wasteland.playEffect(player,wastelandPlayer.getTeam().getEnnemies(), plantType);
                        break;
                    }
                if (event.getItem().getAmount() == 1)
                    player.getInventory().remove(event.getItem());
                event.getItem().setAmount(event.getItem().getAmount() - 1);
            }

        // Harvest
        if(event.hasBlock())
            if(wastelandPlayer.hasTeam()) {
                if(wastelandPlayer.getTeam().getEnnemies().getChestLocation().equals(event.getClickedBlock().getLocation())){
                    event.setCancelled(true);
                    if(wastelandPlayer.getTeam().getEnnemies().getWheat() < 16){
                        player.sendMessage("L'équipe adverse n'as pas assez de ressources pour être volé");
                        return;


                    }
                    if(wastelandPlayer.getWheat() == 50){
                        player.sendMessage("Vous ne pouvez pas voler de ressource car vous êtes full");
                        return;
                    }
                    if(player.getWalkSpeed() == 0) return;
                    ActionBarAPI.sendMessage(player,"Vous avez volé " + ChatColor.MAGIC + "15" + ChatColor.RESET + " blés");
                    player.setWalkSpeed(0);
                    new BukkitRunnable(){
                        @Override
                        public void run() {
                            int capacity = 50 - wastelandPlayer.getWheat();
                            if(capacity > 15)
                                capacity = new Random().nextInt(15);
                            if(wastelandPlayer.getKit().equals(Kit.TRAPPER))
                                if(new Random().nextInt(wastelandPlayer.getAmplifier()) == 3) {
                                    capacity = (15 - capacity) + capacity;
                                }
                            wastelandPlayer.getTeam().getEnnemies().removeWheat(capacity);
                            wastelandPlayer.addWheat(capacity);
                            wastelandPlayer.resetWalkSpeed();
                            ActionBarAPI.sendMessage(player,"Vous avez volé " + capacity + " blés");
                            player.sendMessage("Vous avez volé " + capacity + " blés");
                        }
                    }.runTaskLater(wasteland.getMain(),20*5);

                }
                if (wastelandPlayer.getTeam().getChestLocation().equals(event.getClickedBlock().getLocation())) {
                    wastelandPlayer.getTeam().addWheat(wastelandPlayer.getWheat());
                    player.playSound(player.getLocation(),Sound.BLOCK_COMPARATOR_CLICK,1,1);
                    wastelandPlayer.setWheat(0);
                    event.setCancelled(true);
                }
            }

        //team ands kit selector
        if(!wasteland.isGameStarted() && event.hasItem()){
            event.setCancelled(true);
            ItemStack item = event.getItem();
            if(item.equals(WastelandItem.KIT_SELECTOR.getItemStack())){
                wastelandPlayer.openKitSelector();
            }
            if(item.getType().equals(Material.BANNER)){
                Inventory inventory = Bukkit.createInventory(null, InventoryType.HOPPER, item.getItemMeta().getDisplayName());
                inventory.setItem(WastelandItem.JOIN_TEAM_BLUE.getSlot(),WastelandItem.JOIN_TEAM_BLUE.getItemStack());
                inventory.setItem(WastelandItem.JOIN_TEAM_RED.getSlot(),WastelandItem.JOIN_TEAM_RED.getItemStack());
                player.openInventory(inventory);
            }
        }
    }
}
