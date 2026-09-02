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

import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import io.papermc.paper.util.Tick;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.checkerframework.common.value.qual.IntRange;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A wrapper for an {@link RegionScheduler} that holds and provides the {@link Plugin} instance for it.
 */
public final class RegionPluginScheduler extends AbstractPluginScheduler {
    private final List<ScheduledTask> tasks = new ArrayList<>();

    /**
     * Create a region plugin scheduler for the given plugin.
     *
     * @param plugin a plugin
     */
    public RegionPluginScheduler(final Plugin plugin) {
        super(plugin);
    }

    /**
     * Schedule a task to be executed on the region which owns the location.
     *
     * @param location the location at which the region executing should own
     * @param run      the task to execute
     */
    public void run(final Location location, final Runnable run) {
        this.run(location, _ -> run.run());
    }

    /**
     * Schedule a task to be executed on the region which owns the chunk on the next tick.
     *
     * @param world  the world of the region that owns the task
     * @param chunkX the chunk X coordinate of the region that owns the task
     * @param chunkZ the chunk Z coordinate of the region that owns the task
     * @param run    the task to execute
     */
    public void run(final World world, final int chunkX, final int chunkZ, final Runnable run) {
        this.run(world, chunkX, chunkZ, _ -> run.run());
    }

    /**
     * Schedule a task to be executed on the region which owns the location on the next tick.
     *
     * @param location the location at which the region executing should own
     * @param task     the task to execute
     * @return the {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask run(final Location location, final Consumer<ScheduledTask> task) {
        if (this.closed) throw SCHEDULER_CLOSED_EXCEPTION;

        return this.runDelayed(location.getWorld(),
                location.getBlockX() >> 4,
                location.getBlockZ() >> 4,
                task,
                1L);
    }

    /**
     * Schedule a task to be executed on the region which owns the chunk on the next tick.
     *
     * @param world  the world of the region that owns the task
     * @param chunkX the chunk X coordinate of the region that owns the task
     * @param chunkZ the chunk Z coordinate of the region that owns the task
     * @param task   the task to execute
     * @return the {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask run(final World world,
                             final int chunkX,
                             final int chunkZ,
                             final Consumer<ScheduledTask> task) {
        return this.runDelayed(world, chunkX, chunkZ, task, 1L);
    }

    /**
     * Schedule a task to be executed on the region which owns the chunk after the specified delay in ticks.
     *
     * @param world      the world of the region that owns the task
     * @param chunkX     the chunk X coordinate of the region that owns the task
     * @param chunkZ     the chunk Z coordinate of the region that owns the task
     * @param task       the task to execute
     * @param delayTicks the time to pass before the first execution of the task, in ticks
     * @return the {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask runDelayed(final World world,
                                    final int chunkX,
                                    final int chunkZ,
                                    final Consumer<ScheduledTask> task,
                                    @IntRange(from = 1) final long delayTicks) {
        if (this.closed) throw SCHEDULER_CLOSED_EXCEPTION;

        final ScheduledTask scheduledTask = Bukkit.getRegionScheduler()
                .runDelayed(this.plugin, world, chunkX, chunkZ, task, delayTicks);
        this.tasks.add(scheduledTask);

        return scheduledTask;
    }

    /**
     * Schedule a task to be executed on the region which owns the location after the specified delay in ticks.
     *
     * @param location   the location at which the region executing should own
     * @param task       the task to execute
     * @param delayTicks the time to pass before the task should be executed, in ticks
     * @return the {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask runDelayed(final Location location,
                                    final Consumer<ScheduledTask> task,
                                    @IntRange(from = 1) final long delayTicks) {
        return this.runDelayed(location.getWorld(),
                location.getBlockX() >> 4,
                location.getBlockZ() >> 4,
                task,
                delayTicks);
    }

    /**
     * Schedule a task to be executed on the region which owns the chunk after the specified delay.
     *
     * @param world  the world of the region that owns the task
     * @param chunkX the chunk X coordinate of the region that owns the task
     * @param chunkZ the chunk Z coordinate of the region that owns the task
     * @param task   the task to execute
     * @param delay  the time to pass before the task should be executed
     * @return the {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask runDelayed(final World world,
                                    final int chunkX,
                                    final int chunkZ,
                                    final Consumer<ScheduledTask> task,
                                    final Duration delay) {
        return this.runDelayed(world, chunkX, chunkZ, task, Tick.tick().fromDuration(delay));
    }

    /**
     * Schedule a task to be executed on the region which owns the location starting after the initial delay and
     * repeating with the specified period.
     *
     * @param world             the world of the region that owns the task
     * @param chunkX            the chunk X coordinate of the region that owns the task
     * @param chunkZ            the chunk Z coordinate of the region that owns the task
     * @param task              the task to execute
     * @param initialDelayTicks the time to pass before the first execution of the task, in ticks
     * @param periodTicks       the time between each execution of the task, in ticks
     * @return the {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask runAtFixedRate(final World world,
                                        final int chunkX,
                                        final int chunkZ,
                                        final Consumer<ScheduledTask> task,
                                        @IntRange(from = 1) final long initialDelayTicks,
                                        @IntRange(from = 1) final long periodTicks) {
        if (this.closed) throw SCHEDULER_CLOSED_EXCEPTION;

        final ScheduledTask scheduledTask = Bukkit.getRegionScheduler()
                .runAtFixedRate(this.plugin, world, chunkX, chunkZ, task, initialDelayTicks, periodTicks);
        this.tasks.add(scheduledTask);

        return scheduledTask;
    }

    /**
     * Schedule a task to be executed on the region which owns the chunk starting on the next tick and repeating with
     * the specified period.
     *
     * @param world       the world of the region that owns the task
     * @param chunkX      the chunk X coordinate of the region that owns the task
     * @param chunkZ      the chunk Z coordinate of the region that owns the task
     * @param task        the task to execute
     * @param periodTicks the time between each execution of the task, in ticks
     * @return the {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask runAtFixedRate(final World world,
                                        final int chunkX,
                                        final int chunkZ,
                                        final Consumer<ScheduledTask> task,
                                        @IntRange(from = 1) final long periodTicks) {
        return this.runAtFixedRate(world,
                chunkX,
                chunkZ,
                task,
                1L,
                periodTicks);
    }

    /**
     * Schedule a task to be executed on the region which owns the location starting after the initial delay and
     * repeating with the specified period.
     *
     * @param world        the world of the region that owns the task
     * @param chunkX       the chunk X coordinate of the region that owns the task
     * @param chunkZ       the chunk Z coordinate of the region that owns the task
     * @param task         the task to execute
     * @param initialDelay the time to pass before the first execution of the task
     * @param period       the time between task executions after the first execution of the task
     * @return the {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask runAtFixedRate(final World world,
                                        final int chunkX,
                                        final int chunkZ,
                                        final Consumer<ScheduledTask> task,
                                        final Duration initialDelay,
                                        final Duration period) {
        return this.runAtFixedRate(world,
                chunkX,
                chunkZ,
                task,
                Tick.tick().fromDuration(initialDelay),
                Tick.tick().fromDuration(period));
    }

    /**
     * Schedule a task to be executed on the region which owns the chunk starting on the next tick and repeating with
     * the specified period.
     *
     * @param world  the world of the region that owns the task
     * @param chunkX the chunk X coordinate of the region that owns the task
     * @param chunkZ the chunk Z coordinate of the region that owns the task
     * @param task   the task to execute
     * @param period the time between task executions after the first execution of the task
     * @return the {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask runAtFixedRate(final World world,
                                        final int chunkX,
                                        final int chunkZ,
                                        final Consumer<ScheduledTask> task,
                                        final Duration period) {
        return this.runAtFixedRate(world,
                chunkX,
                chunkZ,
                task,
                Tick.of(1),
                period);
    }

    /**
     * Schedule a task to be executed on the region which owns the location starting after the initial delay and
     * repeating with the specified period.
     *
     * @param location          the location at which the region executing should own
     * @param task              the task to execute
     * @param initialDelayTicks the time to pass before the first execution of the task, in ticks
     * @param periodTicks       the time between each execution of the task, in ticks
     * @return the {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask runAtFixedRate(final Location location,
                                        final Consumer<ScheduledTask> task,
                                        @IntRange(from = 1) final long initialDelayTicks,
                                        @IntRange(from = 1) final long periodTicks) {
        return this.runAtFixedRate(location.getWorld(),
                location.getBlockX() >> 4,
                location.getBlockZ() >> 4,
                task,
                initialDelayTicks,
                periodTicks);
    }

    /**
     * Schedule a task to be executed on the region which owns the location starting on the next tick and repeating with
     * the specified period.
     *
     * @param location    the location at which the region executing should own
     * @param task        the task to execute
     * @param periodTicks the time between each execution of the task, in ticks
     * @return the {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask runAtFixedRate(final Location location,
                                        final Consumer<ScheduledTask> task,
                                        @IntRange(from = 1) final long periodTicks) {
        return this.runAtFixedRate(location,
                task,
                1L,
                periodTicks);
    }

    /**
     * Schedule a task to be executed on the region which owns the location starting after the initial delay and
     * repeating with the specified period.
     *
     * @param location     the location at which the region executing should own
     * @param task         the task to execute
     * @param initialDelay the time to pass before the first execution of the task
     * @param period       the time between task executions after the first execution of the task
     * @return the {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask runAtFixedRate(final Location location,
                                        final Consumer<ScheduledTask> task,
                                        final Duration initialDelay,
                                        final Duration period) {
        return this.runAtFixedRate(location.getWorld(),
                location.getBlockX() >> 4,
                location.getBlockZ() >> 4,
                task,
                Tick.tick().fromDuration(initialDelay),
                Tick.tick().fromDuration(period));
    }

    /**
     * Schedule a task to be executed on the region which owns the location starting on the next tick and repeating with
     * the specified period.
     *
     * @param location the location at which the region executing should own
     * @param task     the task to execute
     * @param period   the time between task executions after the first execution of the task
     * @return the {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask runAtFixedRate(final Location location,
                                        final Consumer<ScheduledTask> task,
                                        final Duration period) {
        return this.runAtFixedRate(location,
                task,
                Tick.of(1),
                period);
    }

    @Override
    void cancelTasks() {
        this.tasks.forEach(ScheduledTask::cancel);
    }
}
