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

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import java.lang.reflect.Type;
import java.util.UUID;

/**
 * Configurate serializer for {@link Location}s.
 */
public class LocationSerializer implements TypeSerializer<Location> {
    public static final LocationSerializer INSTANCE = new LocationSerializer();

    private static final String WORLD_KEY = "world";
    private static final String X_KEY = "x";
    private static final String Y_KEY = "y";
    private static final String Z_KEY = "z";
    private static final String PITCH_KEY = "pitch";
    private static final String YAW_KEY = "yaw";

    protected LocationSerializer() {}

    @Override
    public Location deserialize(final Type type, final ConfigurationNode node) throws SerializationException {
        final World world = node.node(WORLD_KEY).isNull() ? null : Bukkit.getWorld(node.node(WORLD_KEY).get(UUID.class));

        return new Location(world,
                node.node(X_KEY).getDouble(),
                node.node(Y_KEY).getDouble(),
                node.node(Z_KEY).getDouble(),
                node.node(YAW_KEY).getFloat(),
                node.node(PITCH_KEY).getFloat());
    }

    @Override
    public void serialize(final Type type, @Nullable final Location location, final ConfigurationNode node) throws SerializationException {
        if (location == null) return;

        if (location.getWorld() != null)
            node.node(WORLD_KEY).set(UUID.class, location.getWorld().getUID());

        node.node(X_KEY).set(Double.class, location.getX());
        node.node(Y_KEY).set(Double.class, location.getY());
        node.node(Z_KEY).set(Double.class, location.getZ());
        node.node(YAW_KEY).set(Float.class, location.getYaw());
        node.node(PITCH_KEY).set(Float.class, location.getPitch());
    }
}
