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

package dev.hboyd.prismatic.paper.persistent_data.type;

import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Persistent data type for {@link ItemStack}s.
 *
 * @see org.bukkit.persistence.PersistentDataType
 * @see PersistentDataContainer
 */
public final class ItemStackPersistentDataType implements PersistentDataType<PersistentDataContainer, ItemStack> {
    public static final ItemStackPersistentDataType INSTANCE = new ItemStackPersistentDataType();

    private ItemStackPersistentDataType() {
    }

    @Override
    public Class<PersistentDataContainer> getPrimitiveType() {
        return PersistentDataContainer.class;
    }

    @Override
    public Class<ItemStack> getComplexType() {
        return ItemStack.class;
    }

    @Override
    public PersistentDataContainer toPrimitive(final @Nullable ItemStack itemStack, final PersistentDataAdapterContext context) {
        final PersistentDataContainer itemStackPDC = context.newPersistentDataContainer();
        if (itemStack == null || itemStack.isEmpty()) return itemStackPDC;
        final byte[] itemStackCompressedNBTBytes = itemStack.serializeAsBytes();

        // PDC expects uncompressed NBT while ItemStack expects GZIP compressed NBT
        try {
            try (final ByteArrayInputStream bais = new ByteArrayInputStream(itemStackCompressedNBTBytes);
                 final GZIPInputStream gzis = new GZIPInputStream(bais)) {
                itemStackPDC.readFromBytes(gzis.readAllBytes());
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        return itemStackPDC;
    }

    @Override
    public ItemStack fromPrimitive(final PersistentDataContainer itemStackPDC,
                                   final PersistentDataAdapterContext context) {
        if (itemStackPDC.isEmpty()) return ItemStack.empty();

        // PDC expects uncompressed NBT while ItemStack expects GZIP compressed NBT
        try {
            byte[] itemStackBytes = itemStackPDC.serializeToBytes();
            try (final ByteArrayOutputStream baos = new ByteArrayOutputStream(itemStackBytes.length);
                 final GZIPOutputStream gzos = new GZIPOutputStream(baos)) {

                gzos.write(itemStackBytes);
                gzos.finish();
                itemStackBytes = baos.toByteArray();
            }
            return ItemStack.deserializeBytes(itemStackBytes);
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
