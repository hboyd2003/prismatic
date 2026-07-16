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

package dev.hboyd.prismatic.paper.configurate;

import dev.hboyd.prismatic.configurate.Config;
import dev.hboyd.prismatic.paper.configurate.serializer.BukkitVectorSerializer;
import dev.hboyd.prismatic.paper.configurate.serializer.ItemStackSerializer;
import dev.hboyd.prismatic.paper.configurate.serializer.LocationSerializer;
import dev.hboyd.prismatic.paper.configurate.serializer.NamespacedKeySerializer;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;

import java.nio.file.Path;

/**
 * An abstract self-contained Configurate config with Paper serializers by default.
 *
 * <p>Concrete extending classes must be annotated with
 * {@link org.spongepowered.configurate.objectmapping.ConfigSerializable}</p>
 *
 * <p>{@link Config#initialize()} <b>MUST</b> be called at the end of the implementing classes constructor.</p>
 */
@SuppressWarnings("AbstractClassWithoutAbstractMethods")
public abstract class PaperConfig extends Config {
    private static final TypeSerializerCollection PAPER_TYPE_SERIALIZERS = TypeSerializerCollection.defaults().childBuilder()
            .register(Vector.class, BukkitVectorSerializer.INSTANCE)
            .register(ItemStack.class, ItemStackSerializer.INSTANCE)
            .register(Location.class, LocationSerializer.INSTANCE)
            .register(NamespacedKey.class, NamespacedKeySerializer.INSTANCE)
            .build();

    protected PaperConfig(final Path filePath,
                          final int latestVersion,
                          @Nullable final String header,
                          @Nullable final TypeSerializerCollection additionalTypeSerializers,
                          @Nullable final ConfigurationTransformation transformer) {
        TypeSerializerCollection typeSerializerCollection = PAPER_TYPE_SERIALIZERS;
        if (additionalTypeSerializers != null) typeSerializerCollection = typeSerializerCollection.childBuilder()
                    .registerAll(additionalTypeSerializers)
                    .build();

        super(filePath, latestVersion, header, typeSerializerCollection, transformer);
    }

    protected PaperConfig(final Path filePath,
                          final int latestVersion) {
        this(filePath, latestVersion, null, null, null);
    }
}
