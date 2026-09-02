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

package dev.hboyd.prismatic.paper.scheduler;

import io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import io.papermc.paper.util.Tick;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.checkerframework.common.value.qual.IntRange;

import java.time.Duration;
import java.util.function.Consumer;

/**
 * A wrapper for an {@link GlobalRegionScheduler} that holds and provides the {@link Plugin} instance for it.
 */
public final class GlobalRegionPluginScheduler extends AbstractPluginScheduler {
    /**
     * Create a global region plugin scheduler for the given plugin.
     *
     * @param plugin a plugin
     */
    public GlobalRegionPluginScheduler(final Plugin plugin) {
        super(plugin);
    }

    /**
     * Schedule a task to be executed on the global region.
     *
     * @param run the task to execute
     */
    public void run(final Runnable run) {
        this.run(_ -> run.run());
    }

    /**
     * Schedule a task to be executed on the global region on the next tick.
     *
     * @param task the task to execute
     * @return the {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask run(final Consumer<ScheduledTask> task) {
        return this.runDelayed(task, 1L);
    }

    /**
     * Schedule a task to be executed on the global region after the specified delay in ticks.
     *
     * @param task       the task to execute
     * @param delayTicks the time to pass before the task should be executed, in ticks
     * @return the {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask runDelayed(final Consumer<ScheduledTask> task, @IntRange(from = 1) final long delayTicks) {
        if (this.closed) throw SCHEDULER_CLOSED_EXCEPTION;

        return Bukkit.getGlobalRegionScheduler().runDelayed(this.plugin, task, delayTicks);
    }

    /**
     * Schedule a task to be executed on the global region after the specified delay in ticks.
     *
     * @param task  the task to execute
     * @param delay the time to pass before the task should be executed
     * @return the {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask runDelayed(final Consumer<ScheduledTask> task, final Duration delay) {
        return this.runDelayed(task, Tick.tick().fromDuration(delay));
    }

    /**
     * Schedule a task to be executed on the global region after the initial delay and repeating with the specified
     * period.
     *
     * @param task              the task to execute
     * @param initialDelayTicks the time to pass before the first execution of the task, in ticks
     * @param periodTicks       the time between each execution of the task, in ticks
     * @return The {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask runAtFixedRate(final Consumer<ScheduledTask> task,
                                        @IntRange(from = 1) final long initialDelayTicks,
                                        @IntRange(from = 1) final long periodTicks) {
        if (this.closed) throw SCHEDULER_CLOSED_EXCEPTION;

        return Bukkit.getGlobalRegionScheduler().runAtFixedRate(this.plugin, task, initialDelayTicks, periodTicks);
    }

    /**
     * Schedule a task to be executed on the global region starting on the next tick and repeating with the specified
     * period.
     *
     * @param task        the task to execute
     * @param periodTicks the time between each execution, in ticks
     * @return The {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask runAtFixedRate(final Consumer<ScheduledTask> task,
                                        @IntRange(from = 1) final long periodTicks) {
        return this.runAtFixedRate(task, 1L, periodTicks);
    }

    /**
     * Schedule a task to be executed on the global region after the initial delay and repeating with the specified
     * period.
     *
     * @param task         the task to execute
     * @param initialDelay the time to pass before the first execution of the task
     * @param period       the period between each execution of the task
     * @return The {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask runAtFixedRate(final Consumer<ScheduledTask> task,
                                        final Duration initialDelay,
                                        final Duration period) {
        return this.runAtFixedRate(task, Tick.tick().fromDuration(initialDelay), Tick.tick().fromDuration(period));
    }

    /**
     * Schedule a task to be executed on the global region starting on the next tick and repeating with the specified
     * period.
     *
     * @param task   the task to execute
     * @param period the time between each execution
     * @return The {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask runAtFixedRate(final Consumer<ScheduledTask> task, final Duration period) {
        return this.runAtFixedRate(task, Tick.of(1), period);
    }

    @Override
    void cancelTasks() {
        Bukkit.getGlobalRegionScheduler().cancelTasks(this.plugin);
    }
}
