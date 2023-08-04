/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */


package com.alpsbte.plotsystem.core.system.tutorial;

import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.TutorialPlot;
import com.alpsbte.plotsystem.core.system.plot.generator.TutorialPlotGenerator;
import com.alpsbte.plotsystem.core.system.tutorial.stage.AbstractPlotStage;
import com.alpsbte.plotsystem.core.system.tutorial.stage.AbstractStage;
import com.sk89q.worldedit.WorldEditException;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.logging.Level;

public abstract class AbstractPlotTutorial extends AbstractTutorial {
    protected final int tutorialId;

    protected final TutorialPlot plot;
    protected TutorialPlotGenerator plotGenerator;
    protected AbstractPlotTutorial(Player player, int tutorialId) throws SQLException {
        super(player);
        this.tutorialId = tutorialId;

        // Get tutorial plot
        Builder builder = Builder.byUUID(player.getUniqueId());
        if (TutorialPlot.getPlot(builder.getUUID().toString(), tutorialId) == null) {
            plot = TutorialPlot.addTutorialPlot(builder.getUUID().toString(), tutorialId);
        } else plot = TutorialPlot.getPlot(builder.getUUID().toString(), tutorialId);

        // Check if tutorial plot is null
        if (plot == null) {
            Bukkit.getLogger().log(Level.SEVERE, "Could not load tutorial. Plot is null.");
            return;
        }

        // Initialize tutorial worlds and stages
        initTutorialStage();

        // Set the current stage from the player
        setStage(plot.getStage());
    }

    @Override
    protected AbstractStage getStage() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        return getStages().get(activeStageIndex + 1).getDeclaredConstructor(Player.class, TutorialPlot.class).newInstance(player, plot);
    }

    @Override
    protected void StopTutorial(boolean isCompleted) {
        Bukkit.getScheduler().runTask(PlotSystem.getPlugin(), () -> {
            try {
                plot.getWorld().unloadWorld(true);
            } catch (SQLException ex) {
                Bukkit.getLogger().log(Level.SEVERE, "An error occurred while unloading tutorial world!", ex);
            }
            super.StopTutorial(isCompleted);
        });
    }

    @Override
    public void onSwitchWorld(Player player, int tutorialWorldIndex) {
        if (!player.getUniqueId().toString().equals(this.player.getUniqueId().toString())) return;
        try {
            if (tutorialWorldIndex == 1 && plotGenerator == null) {
                plotGenerator = new TutorialPlotGenerator(plot, Builder.byUUID(player.getUniqueId()));
                plotGenerator.generateOutlines(((AbstractPlotStage) currentStage).getInitSchematicId());
            }
            super.onSwitchWorld(player, tutorialWorldIndex);
        } catch (SQLException | IOException | WorldEditException e) {
            throw new RuntimeException(e);
        }
    }
}
