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

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scheduler.BukkitWorker;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * A wrapper for {@link BukkitScheduler} that holds and provides the {@link Plugin} instance for it.
 *
 * @deprecated Incompatible with <a href="https://github.com/papermc/folia">Folia</a>; use the appropriate Folia
 *         scheduler instead: {@link AsyncPluginScheduler}, {@link GlobalRegionPluginScheduler},
 *         {@link RegionPluginScheduler} {@link EntityPluginScheduler}
 */
@Deprecated
public final class PluginScheduler extends AbstractPluginScheduler {
    private final BukkitScheduler scheduler;

    /**
     * Construct a plugin scheduler for the given plugin and scheduler.
     *
     * @param plugin    the plugin
     * @param scheduler the scheduler
     */
    public PluginScheduler(final Plugin plugin, final BukkitScheduler scheduler) {
        super(plugin);
        this.scheduler = Objects.requireNonNull(scheduler);
    }

    /**
     * Schedule a once-off task to occur after a delay.
     *
     * <p>This task will be executed by the main server thread.</p>
     *
     * @param task  Task to be executed
     * @param delay Delay in server ticks before executing the task
     * @return Task id number (-1 if scheduling failed)
     */
    public int scheduleSyncDelayedTask(final Runnable task, final long delay) {
        return this.scheduler.scheduleSyncDelayedTask(this.plugin, task, delay);
    }

    /**
     * Schedule a repeating task.
     *
     * <p>This task will be executed by the main server thread.</p>
     *
     * @param task   Task to be executed
     * @param delay  Delay in server ticks before executing the first repeat
     * @param period Period in server ticks of the task
     * @return Task id number (-1 if scheduling failed)
     */
    public int scheduleSyncRepeatingTask(final Runnable task, final long delay, final long period) {
        return this.scheduler.scheduleSyncRepeatingTask(this.plugin, task, delay, period);
    }

    /**
     * Call a method on the main thread and returns a Future object. This task will be executed by the main server
     * thread.
     * <ul>
     * <li>Note: The Future.get() methods must NOT be called from the main
     *     thread.</li>
     * <li>Note2: There is at least an average of 10ms latency until the
     *     isDone() method returns true.</li>
     * </ul>
     *
     * @param task Task to be executed
     * @param <T>  the result type of execution
     * @return Future object related to the task
     */
    public <T> Future<T> callSyncMethod(final Callable<T> task) {
        return this.scheduler.callSyncMethod(this.plugin, task);
    }

    /**
     * Return a task that will run after the specified number of server ticks.
     *
     * @param task  the task to be run
     * @param delay the ticks to wait before running the task
     * @return a BukkitTask that contains the id number
     * @throws IllegalArgumentException if the task is null
     */
    public BukkitTask runTaskLater(final Runnable task, final long delay) throws IllegalArgumentException {
        return this.scheduler.runTaskLater(this.plugin, task, delay);
    }

    /**
     * Return a task that will run on the next server tick.
     *
     * @param task the task to be run
     * @return a BukkitTask that contains the id number
     * @throws IllegalArgumentException if the task is null
     */
    public BukkitTask runTask(final Runnable task) throws IllegalArgumentException {
        return this.scheduler.runTask(this.plugin, task);
    }

    /**
     * Return an executor that will run tasks on the next server tick.
     *
     * @param plugin the reference to the plugin scheduling tasks
     * @return an executor associated with the given plugin
     */
    public Executor getMainThreadExecutor(final Plugin plugin) {
        return this.scheduler.getMainThreadExecutor(plugin);
    }

    /**
     * <b>Asynchronous tasks should never access any API in Bukkit.</b> <b>Great care
     * should be taken to assure the thread-safety of asynchronous tasks.</b>
     *
     * <p>Returns a task that will run asynchronously after the specified number
     * of server ticks.</p>
     *
     * @param task  the task to be run
     * @param delay the ticks to wait before running the task
     * @throws IllegalArgumentException if the task is null
     */
    public void runTaskLaterAsynchronously(final Consumer<? super BukkitTask> task,
                                           final long delay) throws IllegalArgumentException {
        this.scheduler.runTaskLaterAsynchronously(this.plugin, task, delay);
    }

    /**
     * Return a task that will run after the specified number of server ticks.
     *
     * @param task  the task to be run
     * @param delay the ticks to wait before running the task
     * @throws IllegalArgumentException if the task is null
     */
    public void runTaskLater(final Consumer<? super BukkitTask> task,
                             final long delay) throws IllegalArgumentException {
        this.scheduler.runTaskLater(this.plugin, task, delay);
    }

    /**
     * Remove task from scheduler.
     *
     * @param taskId Id number of task to be removed
     */
    public void cancelTask(final int taskId) {
        this.scheduler.cancelTask(taskId);
    }

    /**
     * Return a task that will run on the next server tick.
     *
     * @param task the task to be run
     * @throws IllegalArgumentException if the task is null
     */
    public void runTask(final Consumer<? super BukkitTask> task) throws IllegalArgumentException {
        this.scheduler.runTask(this.plugin, task);
    }

    /**
     * <b>Asynchronous tasks should never access any API in Bukkit.</b> <b>Great care
     * should be taken to assure the thread-safety of asynchronous tasks.</b>
     *
     * <p>Returns a task that will repeatedly run asynchronously until cancelled,
     * starting after the specified number of server ticks.</p>
     *
     * @param task   the task to be run
     * @param delay  the ticks to wait before running the task for the first time
     * @param period the ticks to wait between runs
     * @return a BukkitTask that contains the id number
     * @throws IllegalArgumentException if the task is null
     */
    public BukkitTask runTaskTimerAsynchronously(final Runnable task,
                                                 final long delay,
                                                 final long period) throws IllegalArgumentException {
        return this.scheduler.runTaskTimerAsynchronously(this.plugin, task, delay, period);
    }

    /**
     * <b>Asynchronous tasks should never access any API in Bukkit.</b> <b>Great care
     * should be taken to assure the thread-safety of asynchronous tasks.</b>
     *
     * <p>Returns a task that will run asynchronously.</p>
     *
     * @param task the task to be run
     * @return a BukkitTask that contains the id number
     * @throws IllegalArgumentException if the task is null
     */
    public BukkitTask runTaskAsynchronously(final Runnable task) throws IllegalArgumentException {
        return this.scheduler.runTaskAsynchronously(this.plugin, task);
    }

    /**
     * Check if the task currently running.
     *
     * <p>A repeating task might not be running currently, but will be running in
     * the future. A task that has finished, and does not repeat, will not be running ever again.</p>
     *
     * <p>Explicitly, a task is running if there exists a thread for it, and that
     * thread is alive.</p>
     *
     * @param taskId The task to check.
     * @return If the task is currently running.
     */
    public boolean isCurrentlyRunning(final int taskId) {
        return this.scheduler.isCurrentlyRunning(taskId);
    }

    /**
     * Schedule a once off task to execute as soon as possible.
     *
     * <p>This task will be executed by the main server thread.</p>
     *
     * @param task Task to be executed
     * @return Task id number (-1 if scheduling failed)
     */
    public int scheduleSyncDelayedTask(final Runnable task) {
        return this.scheduler.scheduleSyncDelayedTask(this.plugin, task);
    }

    /**
     * Removes all tasks associated with a particular plugin from the scheduler.
     *
     * @param plugin Owner of tasks to be removed
     */
    public void cancelTasks(final Plugin plugin) {
        this.scheduler.cancelTasks(plugin);
    }

    /**
     * Returns a task that will repeatedly run until cancelled, starting after the specified number of server ticks.
     *
     * @param task   the task to be run
     * @param delay  the ticks to wait before running the task
     * @param period the ticks to wait between runs
     * @return a BukkitTask that contains the id number
     * @throws IllegalArgumentException if the task is null
     */
    public BukkitTask runTaskTimer(final Runnable task,
                                   final long delay,
                                   final long period) throws IllegalArgumentException {
        return this.scheduler.runTaskTimer(this.plugin, task, delay, period);
    }

    /**
     * <b>Asynchronous tasks should never access any API in Bukkit.</b> <b>Great care
     * should be taken to assure the thread-safety of asynchronous tasks.</b>
     *
     * <p>Returns a task that will repeatedly run asynchronously until cancelled,
     * starting after the specified number of server ticks.</p>
     *
     * @param task   the task to be run
     * @param delay  the ticks to wait before running the task for the first time
     * @param period the ticks to wait between runs
     * @throws IllegalArgumentException if the task is null
     */
    public void runTaskTimerAsynchronously(final Consumer<? super BukkitTask> task,
                                           final long delay,
                                           final long period) throws IllegalArgumentException {
        this.scheduler.runTaskTimerAsynchronously(this.plugin, task, delay, period);
    }

    /**
     * Returns a list of active workers owned by the scheduler's owning plugin.
     *
     * <p>This list contains async tasks that are being executed by separate
     * threads.</p>
     *
     * @return Active workers
     */
    public @Unmodifiable List<BukkitWorker> getActiveWorkers() {
        return this.scheduler.getActiveWorkers().stream()
                .filter(worker -> worker.getOwner().equals(this.plugin))
                .toList();
    }

    /**
     * Returns a list of pending tasks owned by the scheduler's owning plugin. The ordering of the tasks is not related
     * to their order of execution.
     *
     * @return Active workers
     */
    public @Unmodifiable List<BukkitTask> getPendingTasks() {
        return this.scheduler.getPendingTasks().stream()
                .filter(task -> task.getOwner().equals(this.plugin))
                .toList();
    }

    /**
     * Check if the task queued to be run later.
     *
     * <p>If a repeating task is currently running, it might not be queued now
     * but could be in the future. A task that is not queued, and not running, will not be queued again.</p>
     *
     * @param taskId The task to check.
     * @return If the task is queued to be run.
     */
    public boolean isQueued(final int taskId) {
        return this.scheduler.isQueued(taskId);
    }

    /**
     * Returns a task that will repeatedly run until canceled, starting after the specified number of server ticks.
     *
     * @param task   the task to be run
     * @param delay  the ticks to wait before running the task
     * @param period the ticks to wait between runs
     * @throws IllegalArgumentException if the task is null
     */
    public void runTaskTimer(final Consumer<? super BukkitTask> task,
                             final long delay,
                             final long period) throws IllegalArgumentException {
        this.scheduler.runTaskTimer(this.plugin, task, delay, period);
    }

    /**
     * <b>Asynchronous tasks should never access any API in Bukkit.</b> <b>Great care
     * should be taken to ensure the thread-safety of asynchronous tasks.</b>
     *
     * <p>Returns a task that will run asynchronously after the specified number</p>
     * of server ticks.
     *
     * @param task  the task to be run
     * @param delay the ticks to wait before running the task
     * @return a BukkitTask that contains the id number
     * @throws IllegalArgumentException if the task is null
     */
    public BukkitTask runTaskLaterAsynchronously(final Runnable task,
                                                 final long delay) throws IllegalArgumentException {
        return this.scheduler.runTaskLaterAsynchronously(this.plugin, task, delay);
    }

    /**
     * <b>Asynchronous tasks should never access any API in Bukkit.</b> <b>Great care
     * should be taken to ensure the thread-safety of asynchronous tasks.</b>
     *
     * <p>Returns a task that will run asynchronously.</p>
     *
     * @param task the task to be run
     * @throws IllegalArgumentException if the task is null
     */
    public void runTaskAsynchronously(final Consumer<? super BukkitTask> task) throws IllegalArgumentException {
        this.scheduler.runTaskAsynchronously(this.plugin, task);
    }

    @Override
    void cancelTasks() {

    }
}
