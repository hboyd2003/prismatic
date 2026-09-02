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

import net.kyori.adventure.key.KeyPattern;
import net.kyori.adventure.key.Namespaced;
import org.bukkit.plugin.Plugin;

import java.io.Closeable;
import java.util.Objects;

/**
 * A scheduler tied to a specific plugin.
 */
public abstract class AbstractPluginScheduler implements Closeable, Namespaced {
    protected static final IllegalStateException SCHEDULER_CLOSED_EXCEPTION =
            new IllegalStateException("Scheduler is closed");
    protected final Plugin plugin;
    protected boolean closed;

    protected AbstractPluginScheduler(final Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
        this.closed = false;
    }

    /**
     * Get the namespace of the plugin tied to this scheduler.
     *
     * @return the namespace
     */
    @KeyPattern.Namespace
    public final String namespace() {
        return this.plugin.namespace();
    }

    /**
     * Closes the scheduler, preventing anything from being scheduled and canceling any current tasks. Does nothing if
     * the scheduler is already closed.
     */
    @Override
    public final void close() {
        this.closed = true;
        this.cancelTasks();
    }

    /**
     * Get whether the scheduler has been closed or not.
     *
     * @return true if the scheduler is closed
     */
    public final boolean closed() {
        return this.closed;
    }

    /**
     * Cancels all tasks scheduled with this scheduler.
     */
    abstract void cancelTasks();
}
