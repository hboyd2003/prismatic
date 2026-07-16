/*
 * prismatic
 * Copyright (c) 2026 Harrison Boyd
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package dev.hboyd.prismatic.paper.actionBar;

import dev.hboyd.prismatic.paper.scheduler.PluginScheduler;
import io.papermc.paper.util.Tick;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import org.bukkit.scheduler.BukkitTask;
import org.jspecify.annotations.Nullable;

import java.time.Duration;

/**
 * Displays a component in the action bar for a fixed duration.
 */
public class TimedActionBar implements ActionBar {
    protected final PluginScheduler pluginScheduler;
    protected final Audience audience;
    protected final Component actionBarComponent;

    protected long displayTimeTicksLeft;
    protected @Nullable BukkitTask task;

    /**
     * Creates a timed action bar.
     *
     * @param pluginScheduler the plugin scheduler used for timing
     * @param audience        the audience to show the action bar to
     * @param component       the component to display
     * @param displayTime     how long to display the component for
     */
    public TimedActionBar(final PluginScheduler pluginScheduler,
                          final Audience audience,
                          final ComponentLike component,
                          final Duration displayTime) {
        if (displayTime.compareTo(Tick.of(20)) < 0)
            throw new IllegalArgumentException("Display time cannot be less than 20 ticks (1 second)");

        this.pluginScheduler = pluginScheduler;
        this.audience = audience;
        this.actionBarComponent = component.asComponent();
        this.displayTimeTicksLeft = Tick.tick().fromDuration(displayTime) - 20; // Action bar fades out for 20 ticks

        this.task = null;
    }

    /**
     * Starts the action bar.
     *
     * @throws IllegalStateException when the action bar is running or is finished
     */
    @Override
    public void start() {
        if (this.task != null) throw new IllegalStateException("Timed action bar has already started");

        this.task = this.pluginScheduler.runTaskTimer(this::tick, 0L, 0L);
    }

    /**
     * Stops the action bar.
     *
     * @throws IllegalStateException when the action bar has not been started or is already stopped
     */
    @Override
    public void stop() {
        if (this.task == null || this.task.isCancelled()) throw new IllegalStateException("Timed action bar is not running");

        this.task.cancel();
    }

    /**
     * Decrements the remaining time and sends the action bar to the audience.
     */
    protected void tick() {
        if (this.displayTimeTicksLeft <= 0) {
            this.task.cancel();
            return;
        }

        this.displayTimeTicksLeft--;
        this.audience.sendActionBar(this.actionBarComponent);
    }
}
