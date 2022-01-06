package com.alpsbte.plotsystem.core.system.plot.world;

import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.generator.AbstractPlotGenerator;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

public interface IPlotWorld {
    /**
     * Generates a new plot world with the required settings and plot schematic
     * @param plotOwner - plow owner of the plot
     * @param generator - generator type as class
     * @return - true if world was generated successfully
     */
    <T extends AbstractPlotGenerator> boolean generate(@NotNull Builder plotOwner, @NotNull Class<T> generator);

    /**
     * Deletes the world file and entry in the config file
     * @return - true if world was deleted successfully
     */
    boolean delete();

    /**
     * Loads the plot world to memory to be used. Plot has to be generated.
     * @return - true if world was loaded successfully
     */
    boolean load();

    /**
     * Unloads the plot world from memory. Plot cannot be used anymore. Plot has to be generated.
     * @param movePlayers - if true, players will get teleported to the spawn location. Otherwise, plot will not get unloaded.
     * @return - true if world was loaded successfully
     */
    boolean unload(boolean movePlayers);

    /**
     * @return - Bukkit plot world
     */
    World getBukkitWorld();

    /**
     * Returns plot world name in the format (for example: P-23)
     * @return - world name of the plot
     */
    String getName();

    /**
     * Loads the protected plot world region from WorldGuard config
     * @return - protected WorldGuard region
     */
    ProtectedRegion getProtectedRegion();

    /**
     * Checks if the plot world is loaded to memory. If the plot world has not yet been generated, it will return false.
     * @return - true if world is loaded
     */
    boolean isLoaded();

    /**
     * Checks if the plot world is generated.
     * @return - true if world is generated
     */
    boolean isGenerated();
}
