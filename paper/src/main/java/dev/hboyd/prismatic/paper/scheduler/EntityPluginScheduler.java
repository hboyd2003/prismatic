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

import io.papermc.paper.threadedregions.scheduler.EntityScheduler;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import io.papermc.paper.util.Tick;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.checkerframework.common.value.qual.IntRange;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A wrapper for an {@link EntityScheduler} that holds and provides the {@link Plugin} instance for it.
 */
public final class EntityPluginScheduler extends AbstractPluginScheduler {
    private final List<ScheduledTask> tasks = new ArrayList<>();

    /**
     * Create am entity plugin scheduler for the given plugin.
     *
     * @param plugin a plugin
     */
    public EntityPluginScheduler(final Plugin plugin) {
        super(plugin);
    }

    /**
     * Schedule a task to be executed on the given entity with the given delay. If the task failed to schedule because
     * the scheduler is retired (entity removed), then returns {@code false}. Otherwise, either the run callback will be
     * invoked after the specified delay, or the retired callback will be invoked if the scheduler is retired. Note that
     * the retired callback is invoked in critical code, so it should not attempt to remove the entity, remove other
     * entities, load chunks, load worlds, modify ticket levels, etc.
     *
     * <p>
     * It is guaranteed that the run and retired callback are invoked on the region which owns the entity.
     * </p>
     *
     * @param entity  the entity to execute on
     * @param run     the callback to run after the specified delay, may not be null
     * @param retired retire callback to run if the entity is retired before the run callback can be invoked, may be
     *                null
     * @param delay   the time to pass before the task should be executed
     * @return {@code true} if the task was scheduled, which means that either the run function or the retired function
     *         will be invoked (but never both), or {@code false} indicating neither the run nor retired function will
     *         be invoked since the scheduler has been retired
     * @throws IllegalStateException if the scheduler is closed
     */
    public boolean execute(final Entity entity,
                           final Runnable run,
                           @Nullable final Runnable retired,
                           @IntRange(from = 1) final long delay) {
        this.checkClosed();

        return entity.getScheduler().execute(this.plugin, run, retired, delay);
    }

    /**
     * Schedule a task to be executed on the given entity on the next tick. If the task failed to schedule because the
     * scheduler is retired (entity removed), then returns {@code null}. Otherwise, either the task callback will be
     * invoked after the specified delay, or the retired callback will be invoked if the scheduler is retired. Note that
     * the retired callback is invoked in critical code, so it should not attempt to remove the entity, remove other
     * entities, load chunks, load worlds, modify ticket levels, etc.
     *
     * <p>
     * It is guaranteed that the task and retired callback are invoked on the region which owns the entity.
     * </p>
     *
     * @param entity  the entity to execute on
     * @param task    the task to execute
     * @param retired retire callback to run if the entity is retired before the run callback can be invoked, may be
     *                null
     * @return the {@link ScheduledTask} that represents the scheduled task, or {@code null} if the entity has been
     *         removed
     * @throws IllegalStateException if the scheduler is closed
     */
    public @Nullable ScheduledTask run(final Entity entity,
                                       final Consumer<ScheduledTask> task,
                                       @Nullable final Runnable retired) {
        return this.runDelayed(entity, task, retired, 1L);
    }

    /**
     * Schedule a task to be executed on the given entity with the given delay. If the task failed to schedule because
     * the scheduler is retired (entity removed), then returns {@code null}. Otherwise, either the task callback will be
     * invoked after the specified delay, or the retired callback will be invoked if the scheduler is retired. Note that
     * the retired callback is invoked in critical code, so it should not attempt to remove the entity, remove other
     * entities, load chunks, load worlds, modify ticket levels, etc.
     *
     * <p>
     * It is guaranteed that the task and retired callback are invoked on the region which owns the entity.
     * </p>
     *
     * @param entity     the entity to execute on
     * @param task       the task to execute
     * @param retired    retire callback to run if the entity is retired before the run callback can be invoked, may be
     *                   null
     * @param delayTicks the time to pass before the first execution of the task, in ticks
     * @return the {@link ScheduledTask} that represents the scheduled task, or {@code null} if the entity has been
     *         removed
     * @throws IllegalStateException if the scheduler is closed
     */
    public @Nullable ScheduledTask runDelayed(final Entity entity,
                                              final Consumer<ScheduledTask> task,
                                              @Nullable final Runnable retired,
                                              @IntRange(from = 1) final long delayTicks) throws IllegalStateException {
        this.checkClosed();

        final ScheduledTask scheduledTask = entity.getScheduler().runDelayed(this.plugin, task, retired, delayTicks);
        if (scheduledTask != null) this.tasks.add(scheduledTask);

        return scheduledTask;
    }

    /**
     * Schedule a task to be executed on the given entity with the given delay. If the task failed to schedule because
     * the scheduler is retired (entity removed), then returns {@code null}. Otherwise, either the task callback will be
     * invoked after the specified delay, or the retired callback will be invoked if the scheduler is retired. Note that
     * the retired callback is invoked in critical code, so it should not attempt to remove the entity, remove other
     * entities, load chunks, load worlds, modify ticket levels, etc.
     *
     * <p>
     * It is guaranteed that the task and retired callback are invoked on the region which owns the entity.
     * </p>
     *
     * @param entity  the entity to execute on
     * @param task    the task to execute
     * @param retired retire callback to run if the entity is retired before the run callback can be invoked, may be
     *                null
     * @param delay   the time to pass before the first execution of the task
     * @return the {@link ScheduledTask} that represents the scheduled task, or {@code null} if the entity has been
     *         removed
     * @throws IllegalStateException if the scheduler is closed
     */
    public @Nullable ScheduledTask runDelayed(final Entity entity,
                                              final Consumer<ScheduledTask> task,
                                              @Nullable final Runnable retired,
                                              final Duration delay) {
        return this.runDelayed(entity, task, retired, Tick.tick().fromDuration(delay));
    }

    /**
     * Schedule a task to be executed on the given entity starting after the initial delay and repeating with the
     * specified period. If the task failed to schedule because the scheduler is retired (entity removed), then returns
     * {@code null}. Otherwise, either the task callback will be invoked after the specified delay, or the retired
     * callback will be invoked if the scheduler is retired. Note that the retired callback is invoked in critical code,
     * so it should not attempt to remove the entity, remove other entities, load chunks, load worlds, modify ticket
     * levels, etc.
     *
     * <p>
     * It is guaranteed that the task and retired callback are invoked on the region which owns the entity.
     * </p>
     *
     * @param entity            the entity to execute on
     * @param task              the task to execute
     * @param retired           retire callback to run if the entity is retired before the run callback can be invoked,
     *                          may be null
     * @param initialDelayTicks the time to pass before the first execution of the task, in ticks
     * @param periodTicks       the time between task executions after the first execution of the task, in ticks
     * @return the {@link ScheduledTask} that represents the scheduled task, or {@code null} if the entity has been
     *         removed
     * @throws IllegalStateException if the scheduler is closed
     */
    public @Nullable ScheduledTask runAtFixedRate(final Entity entity,
                                                  final Consumer<ScheduledTask> task,
                                                  @Nullable final Runnable retired,
                                                  @IntRange(from = 1) final long initialDelayTicks,
                                                  @IntRange(from = 1) final long periodTicks) {
        this.checkClosed();

        final ScheduledTask scheduledTask = entity.getScheduler()
                .runAtFixedRate(this.plugin, task, retired, initialDelayTicks, periodTicks);
        if (scheduledTask != null) this.tasks.add(scheduledTask);

        return scheduledTask;
    }

    /**
     * Schedule a task to be executed on the given entity starting on the next tick and repeating with the specified
     * period. If the task failed to schedule because the scheduler is retired (entity removed), then returns
     * {@code null}. Otherwise, either the task callback will be invoked after the specified delay, or the retired
     * callback will be invoked if the scheduler is retired. Note that the retired callback is invoked in critical code,
     * so it should not attempt to remove the entity, remove other entities, load chunks, load worlds, modify ticket
     * levels, etc.
     *
     * <p>
     * It is guaranteed that the task and retired callback are invoked on the region which owns the entity.
     * </p>
     *
     * @param entity      the entity to execute on
     * @param task        the task to execute
     * @param retired     retire callback to run if the entity is retired before the run callback can be invoked, may be
     *                    null
     * @param periodTicks the time between task executions after the first execution of the task, in ticks
     * @return the {@link ScheduledTask} that represents the scheduled task, or {@code null} if the entity has been
     *         removed
     * @throws IllegalStateException if the scheduler is closed
     */
    public @Nullable ScheduledTask runAtFixedRate(final Entity entity,
                                                  final Consumer<ScheduledTask> task,
                                                  @Nullable final Runnable retired,
                                                  @IntRange(from = 1) final long periodTicks) {
        return this.runAtFixedRate(entity, task, retired, 1L, periodTicks);
    }

    /**
     * Schedule a task to be executed on the given entity starting after the initial delay and repeating with the
     * specified period. If the task failed to schedule because the scheduler is retired (entity removed), then returns
     * {@code null}. Otherwise, either the task callback will be invoked after the specified delay, or the retired
     * callback will be invoked if the scheduler is retired. Note that the retired callback is invoked in critical code,
     * so it should not attempt to remove the entity, remove other entities, load chunks, load worlds, modify ticket
     * levels, etc.
     *
     * <p>
     * It is guaranteed that the task and retired callback are invoked on the region which owns the entity.
     * </p>
     *
     * @param entity       the entity to execute on
     * @param task         the task to execute
     * @param retired      retire callback to run if the entity is retired before the run callback can be invoked, may
     *                     be null
     * @param initialDelay the time to pass before the first execution of the task
     * @param period       the time between task executions after the first execution of the task
     * @return the {@link ScheduledTask} that represents the scheduled task, or {@code null} if the entity has been
     *         removed
     * @throws IllegalStateException if the scheduler is closed
     */
    public @Nullable ScheduledTask runAtFixedRate(final Entity entity,
                                                  final Consumer<ScheduledTask> task,
                                                  @Nullable final Runnable retired,
                                                  final Duration initialDelay,
                                                  final Duration period) {
        return this.runAtFixedRate(entity,
                task,
                retired,
                Tick.tick().fromDuration(initialDelay),
                Tick.tick().fromDuration(period));
    }

    /**
     * Schedule a task to be executed on the given entity starting on the next tick and repeating with the specified
     * period. If the task failed to schedule because the scheduler is retired (entity removed), then returns
     * {@code null}. Otherwise, either the task callback will be invoked after the specified delay, or the retired
     * callback will be invoked if the scheduler is retired. Note that the retired callback is invoked in critical code,
     * so it should not attempt to remove the entity, remove other entities, load chunks, load worlds, modify ticket
     * levels, etc.
     *
     * <p>
     * It is guaranteed that the task and retired callback are invoked on the region which owns the entity.
     * </p>
     *
     * @param entity  the entity to execute on
     * @param task    the task to execute
     * @param retired retire callback to run if the entity is retired before the run callback can be invoked, may be
     *                null
     * @param period  the time between task executions after the first execution of the task
     * @return the {@link ScheduledTask} that represents the scheduled task, or {@code null} if the entity has been
     *         removed
     * @throws IllegalStateException if the scheduler is closed
     */
    public @Nullable ScheduledTask runAtFixedRate(final Entity entity,
                                                  final Consumer<ScheduledTask> task,
                                                  @Nullable final Runnable retired,
                                                  final Duration period) {
        return this.runAtFixedRate(entity,
                task,
                retired,
                Tick.of(1),
                period);
    }

    @Override
    void cancelTasks() {
        this.tasks.forEach(ScheduledTask::cancel);
    }
}
