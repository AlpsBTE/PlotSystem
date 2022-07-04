package com.alpsbte.plotsystem.core.system.plot.world;

import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotHandler;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.io.language.LangPaths;
import com.alpsbte.plotsystem.utils.io.language.LangUtil;
import com.google.common.annotations.Beta;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class CityPlotWorld extends PlotWorld {
    public CityPlotWorld(@NotNull Plot plot) throws SQLException {
        super("C-" + plot.getCity().getID(), plot);
    }

    @Override
    public boolean teleportPlayer(@NotNull Player player) {
        if (super.teleportPlayer(player)) {
            try {
                player.playSound(player.getLocation(), Utils.TeleportSound, 1, 1);
                player.setAllowFlight(true);
                player.setFlying(true);

                if (getPlot() != null) {
                    player.sendMessage(Utils.getInfoMessageFormat(LangUtil.get(player, LangPaths.Message.Info.TELEPORTING_PLOT, String.valueOf(getPlot().getID()))));

                    Utils.updatePlayerInventorySlots(player);
                    PlotHandler.sendLinkMessages(getPlot(), player);

                    if(getPlot().getPlotOwner().getUUID().equals(player.getUniqueId())) {
                        getPlot().setLastActivity(false);
                    }
                }

                return true;
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            }
        }
        return false;
    }

    @Override
    public String getRegionName() {
        return super.getRegionName() + "-" + getPlot().getID();
    }


    @Beta
    @Override
    public int getPlotHeight() throws IOException {
        try {
            return MIN_WORLD_HEIGHT + getWorldHeight() + (int) Math.round((super.getPlotHeight() / 2d));
        } catch (SQLException ex) { Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex); }
        return MIN_WORLD_HEIGHT;
    }

    /**
     * Calculate additional height for the plot
     * @return additional height
     * @throws IOException - if an I/O error occurs
     * @throws SQLException - if an SQL error occurs
     */
    @Beta
    public int getWorldHeight() throws IOException, SQLException {
        int plotHeight = ClipboardFormat.SCHEMATIC.getReader(Files.newInputStream(getPlot().getOutlinesSchematic().toPath())).read(null).getMinimumPoint().getBlockY();

        // Plots created below min world height are not supported
        if (plotHeight < MIN_WORLD_HEIGHT) throw new IOException("Plot height is not supported");

        // Move Y height to a usable value below 256 blocks
        while (plotHeight >= 150) {
            plotHeight -= 150;
        }
        return plotHeight;
    }

    /**
     * Gets all players located on the plot in the city plot world
     * @return - a list of players located on the plot
     */
    public List<Player> getPlayersOnPlot() {
        List<Player> players = new ArrayList<>();
        if (getPlot() != null && getPlot().getWorld().isWorldLoaded() && !getPlot().getWorld().getBukkitWorld().getPlayers().isEmpty()) {
            for (Player player : getPlot().getWorld().getBukkitWorld().getPlayers()) {
                if (PlotManager.isPlayerOnPlot(getPlot(), player)) players.add(player);
            }
            return players;
        }
        return players;
    }
}