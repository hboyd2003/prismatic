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

import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;

/**
 * Configurate serializer for {@link Vector}s.
 */
public class BukkitVectorSerializer implements TypeSerializer<Vector> {
    public static final BukkitVectorSerializer INSTANCE = new BukkitVectorSerializer();

    private static final String X_KEY = "x";
    private static final String Y_KEY = "y";
    private static final String Z_KEY = "z";

    protected BukkitVectorSerializer() {}

    @Override
    public Vector deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
        return new Vector(node.node(X_KEY).getDouble(),
                node.node(Y_KEY).getDouble(),
                node.node(Z_KEY).getDouble());
    }

    @Override
    public void serialize(final Type type, @Nullable final Vector vector, final ConfigurationNode node) throws SerializationException {
        if (vector == null) return;

        node.node(X_KEY).set(double.class, vector.getX());
        node.node(Y_KEY).set(double.class, vector.getY());
        node.node(Z_KEY).set(double.class, vector.getZ());
    }
}
