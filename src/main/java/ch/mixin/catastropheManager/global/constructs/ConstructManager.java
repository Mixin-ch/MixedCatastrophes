package ch.mixin.catastropheManager.global.constructs;

import ch.mixin.MetaData.BlitzardData;
import ch.mixin.MetaData.GreenWellData;
import ch.mixin.MetaData.LighthouseData;
import ch.mixin.catastropheManager.CatastropheManager;
import ch.mixin.catastropheManager.RootCatastropheManager;
import ch.mixin.helperClasses.Constants;
import ch.mixin.helperClasses.Coordinate2D;
import ch.mixin.helperClasses.Coordinate3D;
import ch.mixin.helperClasses.Functions;
import ch.mixin.main.MixedCatastrophesPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ConstructManager extends CatastropheManager {
    public ConstructManager(MixedCatastrophesPlugin plugin, RootCatastropheManager rootCatastropheManager) {
        super(plugin, rootCatastropheManager);
    }

    @Override
    public void initializeMetaData() {
        if (metaData.getGreenWellDataList() == null)
            metaData.setGreenWellDataList(new ArrayList<>());

        if (metaData.getBlitzardDataList() == null)
            metaData.setBlitzardDataList(new ArrayList<>());

        if (metaData.getLightHouseDataList() == null)
            metaData.setLightHouseDataList(new ArrayList<>());
    }

    @Override
    public void updateMetaData() {
    }

    @Override
    public void initializeCauser() {
    }

    @Override
    public void tick() {
        if (plugin.getServer().getOnlinePlayers().size() == 0)
            return;

        tickGreenWell();
        tickBlitzard();
        tickLighthouse();
    }


    private void tickGreenWell() {
        List<GreenWellData> greenWellDataList = metaData.getGreenWellDataList();

        greenWellDataListLoop:
        for (GreenWellData greenWellData : greenWellDataList) {
            World world = plugin.getServer().getWorld(greenWellData.getWorldName());
            Coordinate3D center = greenWellData.getPosition();
            int level = greenWellData.getLevel();
            Location locationCenter = center.toLocation(world);
            Location locationCenterMiddle = center.sum(0.5, 0.5, 0.5).toLocation(world);

            List<Coordinate3D> particles = new ArrayList<>();
            particles.add(center);
            particles.add(center.sum(0, 1, 0));

            plugin.getParticler().spawnParticles(Particle.VILLAGER_HAPPY, particles, world, level * 0.25, 4, 5);

            if (locationCenter.getBlock().getType() != Material.WATER)
                continue;

            List<Coordinate2D> square = Functions.getSquareEdge(center.to2D(), 1);
            List<Material> logs = new ArrayList<>();

            for (Coordinate2D field : square) {
                Material material = field.to3D(center.getY()).toLocation(world).getBlock().getType();

                if (!Constants.Logs.contains(material))
                    continue greenWellDataListLoop;

                logs.add(material);
            }

            double amount = Math.pow(3 + 2 * level, 2) * 0.002;

            while (amount > 0) {
                double probability = Math.min(1, amount);
                amount--;

                if (new Random().nextDouble() < probability) {
                    int range = new Random().nextInt(level + 2);
                    int x = new Random().nextInt(2 * range + 1) - range;
                    int z = new Random().nextInt(2 * range + 1) - range;

                    Coordinate3D spot = center.sum(x, 0, z);
                    Location location = spot.toLocation(world);

                    boolean success = false;

                    if (location.getBlock().getType() == Material.FLOWER_POT) {
                        success = true;
                        location.getBlock().setType(Constants.PottedFlowers.get(new Random().nextInt(Constants.PottedFlowers.size())));
                    } else if (Constants.Airs.contains(location.getBlock().getType())) {
                        Location below = spot.sum(0, -1, 0).toLocation(world);

                        if (below.getBlock().getType() == Material.GRASS_BLOCK) {
                            success = true;
                            location.getBlock().setType(Constants.Flowers.get(new Random().nextInt(Constants.Flowers.size())));
                        } else if (below.getBlock().getType() == Material.DIRT) {
                            below.getBlock().setType(Material.GRASS_BLOCK);
                        }
                    }

                    if (success) {
                        world.dropItem(locationCenterMiddle, new ItemStack(logs.get(new Random().nextInt(logs.size())), 1));
                    }
                }
            }
        }
    }

    private void tickBlitzard() {
        List<BlitzardData> blitzardDataList = metaData.getBlitzardDataList();

        for (BlitzardData blitzardData : blitzardDataList) {
            World world = plugin.getServer().getWorld(blitzardData.getWorldName());
            Coordinate3D center = blitzardData.getPosition();
            int level = blitzardData.getLevel();

            List<Coordinate3D> particles = new ArrayList<>();
            particles.add(center);
            particles.add(center.sum(0, 1, 0));

            plugin.getParticler().spawnParticles(Particle.SPELL_MOB, particles, world, level * 0.25, 4, 5);
        }
    }

    private void tickLighthouse() {
        List<LighthouseData> lighthouseDataList = metaData.getLightHouseDataList();

        for (LighthouseData lighthouseData : lighthouseDataList) {
            World world = plugin.getServer().getWorld(lighthouseData.getWorldName());
            Coordinate3D center = lighthouseData.getPosition();
            int level = lighthouseData.getLevel();

            List<Coordinate3D> particles = new ArrayList<>();
            particles.add(center);
            particles.add(center.sum(0, 1, 0));

            plugin.getParticler().spawnParticles(Particle.LAVA, particles, world, level * 0.25, 4, 5);
        }
    }
}
