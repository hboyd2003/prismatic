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

import org.bukkit.NamespacedKey;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

/**
 * Configurate serializer for {@link NamespacedKey}s.
 */
public class NamespacedKeySerializer implements TypeSerializer<NamespacedKey> {
    public static final NamespacedKeySerializer INSTANCE = new NamespacedKeySerializer();

    @Override
    public NamespacedKey deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
        final String namespacedKeyString = node.getString();
        if (namespacedKeyString == null || namespacedKeyString.isEmpty())
            throw new SerializationException(type, "Key cannot be empty");

        final NamespacedKey namespacedKey = NamespacedKey.fromString(namespacedKeyString);
        if (namespacedKey == null) throw new SerializationException(type, "Invalid key");
        return namespacedKey;
    }

    @Override
    public void serialize(final Type type, @Nullable final NamespacedKey namespacedKey, final ConfigurationNode node) throws SerializationException {
        if (namespacedKey == null) node.set("");
        else node.set(namespacedKey.asString());
    }
}
