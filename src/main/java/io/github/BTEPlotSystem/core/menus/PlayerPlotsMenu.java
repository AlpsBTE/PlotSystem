package github.BTEPlotSystem.core.menus;

import github.BTEPlotSystem.core.system.plot.Plot;
import github.BTEPlotSystem.core.system.plot.PlotManager;
import github.BTEPlotSystem.core.system.Builder;
import github.BTEPlotSystem.utils.ItemBuilder;
import github.BTEPlotSystem.utils.LoreBuilder;
import github.BTEPlotSystem.utils.Utils;
import github.BTEPlotSystem.utils.enums.Category;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.ipvp.canvas.mask.BinaryMask;
import org.ipvp.canvas.mask.Mask;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class PlayerPlotsMenu extends AbstractMenu {

    private final Builder builder;
    private final List<Plot> plots;

    private int plotDisplayCount = 0;

    public PlayerPlotsMenu(Player menuPlayer, Builder showPlotsBuilder) throws SQLException {
        super(6, showPlotsBuilder.getName() + "'s Plots", menuPlayer);
        this.builder = showPlotsBuilder;

        Mask mask = BinaryMask.builder(getMenu())
                .item(new ItemBuilder(Material.STAINED_GLASS_PANE, 1, (byte) 7).setName(" ").build())
                .pattern("111101111")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("000000000")
                .pattern("111101111")
                .build();
        mask.apply(getMenu());

        plots = PlotManager.getPlots(builder);

        addMenuItems();
        setItemClickEvents();

        getMenu().open(getMenuPlayer());
    }

    @Override
    protected void addMenuItems() {
        // Add player stats item
        try {
            getMenu().getSlot(4)
                    .setItem(new ItemBuilder(Material.SKULL_ITEM, 1, (byte) 3) // TODO: Get players head
                            .setName("§6§l" + builder.getName()).setLore(new LoreBuilder()
                                    .addLines("Points: §f" + builder.getScore(),
                                            "§7Completed Buildings: §f" + builder.getCompletedBuilds())
                                    .build())
                            .build());
        } catch (SQLException ex) {
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
            getMenu().getSlot(4).setItem(errorItem());
        }

        // Add player plots items
        plotDisplayCount = Math.min(plots.size(), 36);
        for (int i = 0; i < plotDisplayCount; i++) {
            try {
                Plot plot = plots.get(i);
                switch (plot.getStatus()) {
                    case unfinished:
                        getMenu().getSlot(9 + i)
                                .setItem(new ItemBuilder(Material.WOOL, 1, (byte) 1)
                                        .setName("§b§l" + plot.getCity().getName() + " | Plot #" + plot.getID())
                                        .setLore(getDescription(plot))
                                        .build());
                        break;
                    case unreviewed:
                        getMenu().getSlot(9 + i)
                                .setItem(new ItemBuilder(Material.MAP, 1)
                                        .setName("§b§l" + plot.getCity().getName() + " | Plot #" + plot.getID())
                                        .setLore(getDescription(plot))
                                        .build());
                        break;
                    case complete:
                        getMenu().getSlot(9 + i)
                                .setItem(new ItemBuilder(Material.WOOL, 1, (byte) 13)
                                        .setName("§b§l" + plot.getCity().getName() + " | Plot #" + plot.getID())
                                        .setLore(getDescription(plot))
                                        .build());
                        break;
                }
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                getMenu().getSlot(9 + i).setItem(errorItem());
            }
        }

        // Add Back Button Item
        getMenu().getSlot(49).setItem(backMenuItem());
    }

    @Override
    protected void setItemClickEvents() {
        // Set click event for plots
        for(int i = 0; i < plotDisplayCount; i++) {
            int itemSlot = i;
            getMenu().getSlot(9 + i).setClickHandler((clickPlayer, clickInformation) -> {
                try {
                    new PlotActionsMenu(clickPlayer, plots.get(itemSlot));
                } catch (SQLException ex) {
                    Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
                }
            });
        }

        // Set click event for back button
        getMenu().getSlot(49).setClickHandler((clickPlayer, clickInformation) -> {
            clickPlayer.closeInventory();
            clickPlayer.performCommand("companion");
        });
    }

    public static ItemStack getMenuItem() {
        return new ItemBuilder(Utils.getItemHead("9282"))
                .setName("§b§lShow Plots")
                .setLore(new LoreBuilder()
                        .addLine("Show all your plots").build())
                .build();
    }

    private List<String> getDescription(Plot plot) throws SQLException {
        List<String> lines = new ArrayList<>();
        lines.add("§7Total Points: §f" + (plot.getScore() == -1 ? 0 : plot.getScore()));
        if (plot.isReviewed() || plot.wasRejected()) {
            lines.add("");
            lines.add("§7Accuracy: " + Utils.getPointsByColor(plot.getReview().getRating(Category.ACCURACY)) + "§8/§a5");
            lines.add("§7Block Palette: " + Utils.getPointsByColor(plot.getReview().getRating(Category.BLOCKPALETTE)) + "§8/§a5");
            lines.add("§7Detailing: " + Utils.getPointsByColor(plot.getReview().getRating(Category.DETAILING)) + "§8/§a5");
            lines.add("§7Technique: " + Utils.getPointsByColor(plot.getReview().getRating(Category.TECHNIQUE)) + "§8/§a5");
            lines.add("");
            lines.add("§7Feedback:§f");
            lines.addAll(Utils.splitText(plot.getReview().getFeedback()));
        }
        lines.add("");
        lines.add("§6§lStatus: §7§l" + plot.getStatus().name().substring(0, 1).toUpperCase() + plot.getStatus().name().substring(1));
        return lines;
    }
}
