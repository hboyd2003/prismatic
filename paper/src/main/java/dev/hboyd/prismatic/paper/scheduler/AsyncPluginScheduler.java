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

import io.papermc.paper.threadedregions.scheduler.AsyncScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import io.papermc.paper.util.Tick;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.checkerframework.common.value.qual.IntRange;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * A wrapper for an {@link AsyncScheduler} that provides the {@link Plugin} instance for it.
 */
public final class AsyncPluginScheduler extends AbstractPluginScheduler {
    /**
     * Construct an async plugin scheduler for the given plugin.
     *
     * @param plugin a plugin
     */
    public AsyncPluginScheduler(final Plugin plugin) {
        super(plugin);
    }

    /**
     * Schedule the specified task to be executed asynchronously immediately.
     *
     * @param task specified task
     * @return The {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask runNow(final Consumer<ScheduledTask> task) {
        this.checkClosed();

        return Bukkit.getAsyncScheduler().runNow(this.plugin, task);
    }

    /**
     * Schedule the specified task to be executed asynchronously after the time delay has passed.
     *
     * @param task  specified task
     * @param delay the time to pass before the task should be executed
     * @param unit  the time unit for the time delay
     * @return the {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask runDelayed(final Consumer<ScheduledTask> task,
                                    @IntRange(from = 1) final long delay,
                                    final TimeUnit unit) {
        this.checkClosed();

        return Bukkit.getAsyncScheduler().runDelayed(this.plugin, task, delay, unit);
    }

    /**
     * Schedule the specified task to be executed asynchronously after the time delay has passed.
     *
     * @param task  specified task
     * @param delay the time to pass before the task should be executed
     * @return the {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask runDelayed(final Consumer<ScheduledTask> task, final Duration delay) {
        return this.runDelayed(task, delay.toNanos(), TimeUnit.NANOSECONDS);
    }

    /**
     * Schedule the specified task to be executed asynchronously, starting after the initial delay and repeating with
     * the specified period.
     *
     * @param task         specified task
     * @param initialDelay the time to pass before the first execution of the task
     * @param period       the time between task executions after the first execution of the task
     * @param unit         the time unit for the initial delay and period
     * @return the {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask runAtFixedRate(final Consumer<ScheduledTask> task,
                                        @IntRange(from = 1) final long initialDelay,
                                        @IntRange(from = 1) final long period,
                                        final TimeUnit unit) {
        this.checkClosed();

        return Bukkit.getAsyncScheduler().runAtFixedRate(this.plugin, task, initialDelay, period, unit);
    }

    /**
     * Schedule the specified task to be executed asynchronously, with the specified period.
     *
     * @param task   specified task
     * @param period the time between task executions after the first execution of the task
     * @param unit   the time unit for the period
     * @return the {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask runAtFixedRate(final Consumer<ScheduledTask> task,
                                        @IntRange(from = 1) final long period,
                                        final TimeUnit unit) {
        this.checkClosed();

        return Bukkit.getAsyncScheduler().runAtFixedRate(this.plugin, task, unit.convert(Tick.of(1L)), period, unit);
    }

    /**
     * Schedule the specified task to be executed asynchronously, starting after the initial delay and repeating with
     * the specified period.
     *
     * @param task         specified task
     * @param initialDelay the time to pass before the first execution of the task
     * @param period       the time between task executions after the first execution of the task
     * @return the {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask runAtFixedRate(final Consumer<ScheduledTask> task,
                                        final Duration initialDelay,
                                        final Duration period) {
        return this.runAtFixedRate(task, initialDelay.toNanos(), period.toNanos(), TimeUnit.NANOSECONDS);
    }

    /**
     * Schedule the specified task to be executed asynchronously, repeating with the specified period.
     *
     * @param task   specified task
     * @param period the time between task executions after the first execution of the task
     * @return the {@link ScheduledTask} that represents the scheduled task
     */
    public ScheduledTask runAtFixedRate(final Consumer<ScheduledTask> task,
                                        final Duration period) {
        return this.runAtFixedRate(task, Tick.of(1L).toNanos(), period.toNanos(), TimeUnit.NANOSECONDS);
    }

    @Override
    void cancelTasks() {
        Bukkit.getAsyncScheduler().cancelTasks(this.plugin);
    }
}
