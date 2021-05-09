package ch.mixin.eventListener.eventListenerList;

import ch.mixin.MetaData.BlitzardData;
import ch.mixin.MetaData.GreenWellData;
import ch.mixin.MetaData.LighthouseData;
import ch.mixin.MetaData.PlayerData;
import ch.mixin.eventChange.aspect.AspectType;
import ch.mixin.helpInventory.HelpInventoryManager;
import ch.mixin.helperClasses.*;
import ch.mixin.main.MixedCatastrophesPlugin;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

public class ActionListener implements Listener {
    protected final MixedCatastrophesPlugin plugin;

    public ActionListener(MixedCatastrophesPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void openMixIslandDictionary(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        Player player = event.getPlayer();
        World world = player.getWorld();

        if (!plugin.getAffectedWorlds().contains(world))
            return;

        ItemStack itemStack = player.getInventory().getItemInMainHand();
        ItemStack mixIslandDictionary = HelpInventoryManager.HelpBookItem;

        if (itemStack.getType() != mixIslandDictionary.getType())
            return;

        if (!itemStack.getItemMeta().equals(mixIslandDictionary.getItemMeta()))
            return;

        plugin.getHelpInventoryManager().open(player);
    }

    @EventHandler
    public void dream(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (!Constants.Beds.contains(event.getClickedBlock().getType()))
            return;

        Player player = event.getPlayer();
        World world = player.getWorld();

        if (!plugin.getAffectedWorlds().contains(world))
            return;

        plugin.getRootCatastropheManager().getPersonalCatastropheManager().getDreamManager().performDream(player, event.getClickedBlock());
    }

    @EventHandler
    public void rite(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        if (!plugin.getAffectedWorlds().contains(world))
            return;

        if (!Constants.Fires.contains(event.getBlockPlaced().getType()))
            return;

        Location location = event.getBlockPlaced().getLocation();
        Location LocationN1 = Coordinate3D.toCoordinate(location).sum(0, -1, 0).toLocation(world);
        Location LocationN2 = Coordinate3D.toCoordinate(LocationN1).sum(0, -1, 0).toLocation(world);
        Block blockN1 = LocationN1.getBlock();
        Block blockN2 = LocationN2.getBlock();

        plugin.getRootCatastropheManager().getPersonalCatastropheManager().getRiteManager().performRite(player, blockN1, blockN2);
    }

    @EventHandler
    public void makeGreenWell(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (event.getHand() != EquipmentSlot.HAND)
            return;

        Player player = event.getPlayer();
        World world = player.getWorld();

        if (!plugin.getAffectedWorlds().contains(world))
            return;

        ItemStack itemStack = player.getInventory().getItemInMainHand();

        if (itemStack.getType() != Material.ENDER_EYE)
            return;

        Block block = null;
        List<Block> lineOfSight = event.getPlayer().getLineOfSight(null, 5);

        for (Block b : lineOfSight) {
            if (b.getType() == Material.WATER) {
                block = b;
                break;
            }
        }

        if (block == null)
            return;

        Coordinate3D center = Coordinate3D.toCoordinate(block.getLocation());
        List<Coordinate2D> square = Functions.getSquareEdge(center.to2D(), 1);

        for (Coordinate2D field : square) {
            if (!Constants.Logs.contains(field.to3D(center.getY()).toLocation(world).getBlock().getType()))
                return;
        }

        GreenWellData greenWellData = null;

        for (GreenWellData gwd : plugin.getMetaData().getGreenWellDataList()) {
            if (center.equals(gwd.getPosition())) {
                greenWellData = gwd;
                break;
            }
        }

        if (greenWellData == null) {
            greenWellData = new GreenWellData(center, world.getName(), 0);
            plugin.getMetaData().getGreenWellDataList().add(greenWellData);
        } else if (block.getType() != Material.WATER) {
            return;
        }

        PlayerData playerData = plugin.getMetaData().getPlayerDataMap().get(player.getUniqueId());
        int cost = 160 + 80 * greenWellData.getLevel();
        int costEyes = 1 + (int) Math.floor(0.5 * greenWellData.getLevel());
        boolean success = true;

        if (playerData.getAspect(AspectType.Secrets) < cost) {
            plugin.getEventChangeManager()
                    .eventChange(player)
                    .withEventMessage("You need at least " + cost + " Secrets to do this.")
                    .withColor(Constants.AspectThemes.get(AspectType.Secrets))
                    .finish()
                    .execute();
            success = false;
        }

        if (itemStack.getAmount() < costEyes) {
            plugin.getEventChangeManager()
                    .eventChange(player)
                    .withEventMessage("You need at least " + costEyes + " Ender Eyes to do this.")
                    .withColor(Constants.AspectThemes.get(AspectType.Secrets))
                    .finish()
                    .execute();
            success = false;
        }

        if (!success)
            return;

        greenWellData.setLevel(greenWellData.getLevel() + 1);
        itemStack.setAmount(itemStack.getAmount() - costEyes);

        HashMap<AspectType, Integer> changeMap = new HashMap<>();
        changeMap.put(AspectType.Secrets, -cost);

        plugin.getEventChangeManager()
                .eventChange(player)
                .withAspectChange(changeMap)
                .withEventSound(Sound.AMBIENT_CAVE)
                .withEventMessage("The Green Well has Depth " + greenWellData.getLevel() + ".")
                .withColor(Constants.AspectThemes.get(AspectType.Nature_Conspiracy))
                .withTitle(true)
                .finish()
                .execute();
    }

    @EventHandler
    public void makeBlitzard(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (event.getHand() != EquipmentSlot.HAND)
            return;

        Player player = event.getPlayer();
        World world = player.getWorld();

        if (!plugin.getAffectedWorlds().contains(world))
            return;

        ItemStack itemStack = player.getInventory().getItemInMainHand();

        if (itemStack.getType() != Material.QUARTZ)
            return;

        Block block = event.getClickedBlock();

        if (block == null)
            return;

        if (!Constants.Blitzard.isConstructed(block.getLocation()))
            return;

        Coordinate3D center = Coordinate3D.toCoordinate(block.getLocation());
        BlitzardData blitzardData = null;

        for (BlitzardData bd : plugin.getMetaData().getBlitzardDataList()) {
            if (center.equals(bd.getPosition())) {
                blitzardData = bd;
                break;
            }
        }

        if (blitzardData == null) {
            blitzardData = new BlitzardData(center, world.getName(), 0);
            plugin.getMetaData().getBlitzardDataList().add(blitzardData);
        }

        PlayerData playerData = plugin.getMetaData().getPlayerDataMap().get(player.getUniqueId());
        int multiplier = (int) Math.pow(1 + blitzardData.getLevel(), 2);
        int cost = 100 * multiplier;
        int costItem = multiplier;
        boolean success = true;

        if (playerData.getAspect(AspectType.Secrets) < cost) {
            plugin.getEventChangeManager()
                    .eventChange(player)
                    .withEventMessage("You need at least " + cost + " Secrets to do this.")
                    .withColor(Constants.AspectThemes.get(AspectType.Secrets))
                    .finish()
                    .execute();
            success = false;
        }

        if (itemStack.getAmount() < costItem) {
            plugin.getEventChangeManager()
                    .eventChange(player)
                    .withEventMessage("You need at least " + costItem + " Quartz to do this.")
                    .withColor(Constants.AspectThemes.get(AspectType.Secrets))
                    .finish()
                    .execute();
            success = false;
        }

        if (!success)
            return;

        blitzardData.setLevel(blitzardData.getLevel() + 1);
        itemStack.setAmount(itemStack.getAmount() - costItem);

        HashMap<AspectType, Integer> changeMap = new HashMap<>();
        changeMap.put(AspectType.Secrets, -cost);

        plugin.getEventChangeManager()
                .eventChange(player)
                .withAspectChange(changeMap)
                .withEventSound(Sound.AMBIENT_CAVE)
                .withEventMessage("The Blitzard has Range " + blitzardData.getLevel() * 10 + ".")
                .withColor(ChatColor.DARK_AQUA)
                .withTitle(true)
                .finish()
                .execute();
    }

    @EventHandler
    public void makeLighthouse(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        if (event.getHand() != EquipmentSlot.HAND)
            return;

        Player player = event.getPlayer();
        World world = player.getWorld();

        if (!plugin.getAffectedWorlds().contains(world))
            return;

        ItemStack itemStack = player.getInventory().getItemInMainHand();

        if (itemStack.getType() != Material.GLOWSTONE)
            return;

        Block block = event.getClickedBlock();

        if (block == null)
            return;

        if (!Constants.Lighthouse.isConstructed(block.getLocation()))
            return;

        Coordinate3D center = Coordinate3D.toCoordinate(block.getLocation());
        LighthouseData lighthouseData = null;

        for (LighthouseData ld : plugin.getMetaData().getLightHouseDataList()) {
            if (center.equals(ld.getPosition())) {
                lighthouseData = ld;
                break;
            }
        }

        if (lighthouseData == null) {
            lighthouseData = new LighthouseData(center, world.getName(), 0);
            plugin.getMetaData().getLightHouseDataList().add(lighthouseData);
        }

        PlayerData playerData = plugin.getMetaData().getPlayerDataMap().get(player.getUniqueId());
        double multiplier = Math.pow(1 + lighthouseData.getLevel(), 1.5);
        int cost = (int) Math.round(100 * multiplier);
        int costItem = (int) Math.round(multiplier);
        boolean success = true;

        if (playerData.getAspect(AspectType.Secrets) < cost) {
            plugin.getEventChangeManager()
                    .eventChange(player)
                    .withEventMessage("You need at least " + cost + " Secrets to do this.")
                    .withColor(Constants.AspectThemes.get(AspectType.Secrets))
                    .finish()
                    .execute();
            success = false;
        }

        if (itemStack.getAmount() < costItem) {
            plugin.getEventChangeManager()
                    .eventChange(player)
                    .withEventMessage("You need at least " + costItem + " Glowstone to do this.")
                    .withColor(Constants.AspectThemes.get(AspectType.Secrets))
                    .finish()
                    .execute();
            success = false;
        }

        if (!success)
            return;

        lighthouseData.setLevel(lighthouseData.getLevel() + 1);
        itemStack.setAmount(itemStack.getAmount() - costItem);

        HashMap<AspectType, Integer> changeMap = new HashMap<>();
        changeMap.put(AspectType.Secrets, -cost);

        plugin.getEventChangeManager()
                .eventChange(player)
                .withAspectChange(changeMap)
                .withEventSound(Sound.AMBIENT_CAVE)
                .withEventMessage("The Lighthouse has Range " + lighthouseData.getLevel() * 10 + ".")
                .withColor(ChatColor.GOLD)
                .withTitle(true)
                .finish()
                .execute();
    }
}
