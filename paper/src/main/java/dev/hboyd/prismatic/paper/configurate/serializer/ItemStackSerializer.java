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

package dev.hboyd.prismatic.paper.configurate.serializer;

import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * Configurate serializer for {@link ItemStack}s.
 */
public class ItemStackSerializer implements TypeSerializer<ItemStack> {
    public static final ItemStackSerializer INSTANCE = new ItemStackSerializer();

    protected ItemStackSerializer() {}

    @Override
    public ItemStack deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
        final CompoundBinaryTag itemStackTag = node.options().serializers().get(CompoundBinaryTag.class)
                .deserialize(CompoundBinaryTag.class, node);

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            BinaryTagIO.writer().write(itemStackTag, outputStream, BinaryTagIO.Compression.GZIP);
        } catch (final IOException e) {
            throw new SerializationException(e);
        }

        return ItemStack.deserializeBytes(outputStream.toByteArray());
    }

    @Override
    public void serialize(final Type type, @Nullable final ItemStack itemStack, final ConfigurationNode node) throws SerializationException {
        if (itemStack == null) return;

        final CompoundBinaryTag itemStackTag;
        try {
            itemStackTag = BinaryTagIO.reader().read(new ByteArrayInputStream(itemStack.serializeAsBytes()), BinaryTagIO.Compression.GZIP);
        } catch (final IOException e) {
            throw new SerializationException(e);
        }

        node.options().serializers().get(CompoundBinaryTag.class).serialize(CompoundBinaryTag.class, itemStackTag, node);
    }

    @Override
    public @Nullable ItemStack emptyValue(final Type specificType, final ConfigurationOptions options) {
        return ItemStack.empty();
    }
}
