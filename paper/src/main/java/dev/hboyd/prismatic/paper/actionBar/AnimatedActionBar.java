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
import java.time.Instant;

/**
 * Displays a {@link Component} in the action bar with an animated text prefix.
 */
public class AnimatedActionBar implements ActionBar {
    private final PluginScheduler pluginScheduler;
    private final Audience audience;
    private final String[] frames;
    private final int frameDurationTicks;
    private final Duration timeout;

    private Component actionBarComponent;
    private long ticksDisplayedFor;
    private @Nullable BukkitTask task;
    private int currentFrame;
    private Instant lastComponentChange;

    /**
     * Creates an animated action bar that shows a component with a changing prefix.
     *
     * @param pluginScheduler  the plugin scheduler to schedule the action bar with
     * @param audience         the audience to display the action bar to
     * @param initialComponent the component that will be displayed initially
     * @param frames           the frames to animate with
     * @param frameDuration    the duration each frame should be displayed for
     * @param timeout          how long to wait since the last component change to stop the action bar
     */
    public AnimatedActionBar(final PluginScheduler pluginScheduler,
                             final Audience audience,
                             final ComponentLike initialComponent,
                             final String[] frames,
                             final Duration frameDuration,
                             final Duration timeout) {
        if (!frameDuration.isPositive())
            throw new IllegalArgumentException("Frame duration cannot be negative or zero");

        this.pluginScheduler = pluginScheduler;
        this.audience = audience;
        this.frames = frames;
        this.frameDurationTicks = Tick.tick().fromDuration(frameDuration);
        this.timeout = timeout;

        this.actionBarComponent = initialComponent.asComponent();
        this.ticksDisplayedFor = 0;
        this.task = null;
        this.currentFrame = 0;
        this.lastComponentChange = Instant.now();
    }

    /**
     * Updates the component shown after the animated prefix and resets the timeout timer.
     *
     * @param component the new component to show
     */
    public void component(final ComponentLike component) {
        this.actionBarComponent = component.asComponent();
        this.lastComponentChange = Instant.now();
    }

    @Override
    public void start() throws IllegalStateException {
        if (this.task != null) throw new IllegalStateException("Throbber action bar has already started");

        this.task = this.pluginScheduler.runTaskTimer(this::tick, 0L, 0L);
    }

    @Override
    public void stop() throws IllegalStateException {
        if (this.task == null || this.task.isCancelled()) throw new IllegalStateException("Throbber action bar is not running");

        this.task.cancel();
    }

    /**
     * Advances the animation by one tick, updating the displayed frame.
     */
    private void tick() {
        final Component currentText = Component.text(this.frames[this.currentFrame])
                .append(Component.text(" "))
                .append(this.actionBarComponent);
        this.audience.sendActionBar(currentText);
        this.currentFrame = (int) Math.floor(((double) this.ticksDisplayedFor / this.frameDurationTicks) % this.frames.length );
        this.ticksDisplayedFor++;

        if (Instant.now().isAfter(this.lastComponentChange.plus(this.timeout))) this.stop();
    }

    // TODO: Rework
    /**
     * Commonly used animation frames for action bar loaders.
     */
    public static final class ActionBarLoaderFrames {

        private ActionBarLoaderFrames() {
            /* This utility class should not be instantiated */
        }

        /**
         * Eight-frame dot loader sequence.
         */
        public static final String[] EIGHT_DOT = {"⡆", "⠇", "⠋", "⠙", "⠸", "⢰", "⣠", "⣄"};
    }
}
