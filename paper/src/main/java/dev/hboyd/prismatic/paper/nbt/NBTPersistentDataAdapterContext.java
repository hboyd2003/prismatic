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

package dev.hboyd.prismatic.paper.nbt;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;

/**
 * Implementation of {@link PersistentDataAdapterContext} that handles the creation
 * of persistent data containers using NBT formats.
 */
public final class NBTPersistentDataAdapterContext implements PersistentDataAdapterContext {
    /**
     * The singleton instance of the NBT persistent data adapter context.
     */
    public static final NBTPersistentDataAdapterContext INSTANCE = new NBTPersistentDataAdapterContext();

    private NBTPersistentDataAdapterContext() {}

    /**
     * Creates a new and empty meta container instance.
     *
     * @return the fresh container instance
     */
    @Override
    public PersistentDataContainer newPersistentDataContainer() {
        return new NBTPersistentDataContainer(CompoundBinaryTag.empty());
    }
}
