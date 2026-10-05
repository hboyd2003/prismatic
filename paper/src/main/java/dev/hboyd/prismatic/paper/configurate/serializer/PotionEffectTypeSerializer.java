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

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffectType;
import org.spongepowered.configurate.serialize.ScalarSerializer;
import org.spongepowered.configurate.serialize.SerializationException;

import java.lang.reflect.Type;
import java.util.function.Predicate;

/**
 * Configurate serializer for {@link PotionEffectType}s.
 */
public class PotionEffectTypeSerializer extends ScalarSerializer<PotionEffectType> {
    public static final PotionEffectTypeSerializer INSTANCE = new PotionEffectTypeSerializer();

    protected PotionEffectTypeSerializer() {
        super(PotionEffectType.class);
    }

    @Override
    public PotionEffectType deserialize(final Type type, final Object obj) throws SerializationException {
        final String raw = obj.toString().toLowerCase();

        final Key key = NamespacedKey.fromString(raw.toLowerCase());
        if (key == null) throw new SerializationException(type, "Invalid potion effect id: " + raw);

        final PotionEffectType potionEffectType = RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.MOB_EFFECT)
                .get(key);

        if (potionEffectType == null) throw new SerializationException(type, "Unknown or unloaded potion effect type: " + raw);

        return potionEffectType;
    }

    @Override
    public Object serialize(final PotionEffectType potionEffectType, final Predicate<Class<?>> typeSupported) {
        return potionEffectType.getKey().asString();
    }
}
