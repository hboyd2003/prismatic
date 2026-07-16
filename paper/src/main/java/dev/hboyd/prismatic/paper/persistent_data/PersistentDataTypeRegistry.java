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

package dev.hboyd.prismatic.paper.persistent_data;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.ShadowColor;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Unmodifiable;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.ServiceLoader;

/**
 * A central registry for {@link PersistentDataType}s. Datatypes are shared globally.
 */
public final class PersistentDataTypeRegistry {
    private static final Map<Type, PersistentDataType<?, ?>> PERSISTENT_DATA_TYPE_MAP = new HashMap<>();

    static {
        // Primitives
        register(PersistentDataType.BYTE);
        register(PersistentDataType.FLOAT);
        register(PersistentDataType.DOUBLE);
        register(PersistentDataType.INTEGER);
        register(PersistentDataType.LONG);
        register(PersistentDataType.BOOLEAN);
        register(PersistentDataType.STRING);

        // Primitive Arrays
        register(PersistentDataType.BYTE_ARRAY);
        register(PersistentDataType.INTEGER_ARRAY);
        register(PersistentDataType.LONG_ARRAY);

        // "Simple" types
        register(new SimplePersistentDataType<>(Key.class, Key::asString, Key::key));
        register(new SimplePersistentDataType<>(ShadowColor.class,
                ShadowColor::asHexString,
                ShadowColor::fromHexString));
        register(new SimplePersistentDataType<>(NamespacedKey.class,
                NamespacedKey::asString,
                NamespacedKey::fromString));

        @SuppressWarnings("rawtypes") final ServiceLoader<PersistentDataType> persistentDataTypeServiceLoader =
                ServiceLoader.load(PersistentDataType.class);
        persistentDataTypeServiceLoader.stream()
                .forEach(type -> register((PersistentDataType<?, ?>) type));
    }

    private PersistentDataTypeRegistry() {}

    /**
     * Registers the specified {@link PersistentDataType}.
     *
     * @param persistentDataType the persistent data type to register
     * @throws IllegalStateException if a persistent data type is already registered for the specified complex type
     */
    public static void register(final PersistentDataType<?, ?> persistentDataType) {
        if (PERSISTENT_DATA_TYPE_MAP.containsKey(persistentDataType.getComplexType()))
            throw new IllegalStateException("Duplicate persistent data type " + persistentDataType.getComplexType()
                    .getTypeName());

        PERSISTENT_DATA_TYPE_MAP.put(persistentDataType.getComplexType(), persistentDataType);
    }

    /**
     * Checks if a {@link PersistentDataType} is registered for the specified type.
     *
     * @param type the class to check
     * @return {@code true} if a persistent data type is registered for the specified type, false otherwise
     */
    public static boolean has(final Class<?> type) {
        return PERSISTENT_DATA_TYPE_MAP.containsKey(type);
    }

    /**
     * Retrieves the registered {@link PersistentDataType} for the specified class.
     *
     * @param type the class to get for
     * @param <P> the primitive type of the persistent data type
     * @param <C> the complex type of the persistent data type
     * @return the persistent data type associated with the specified class
     * @throws NoSuchElementException if no persistent data type is registered for the specified class
     */
    public static <P, C> PersistentDataType<P, C> get(final Class<C> type) throws NoSuchElementException {
        //noinspection unchecked
        return (PersistentDataType<P, C>) Optional.ofNullable(PERSISTENT_DATA_TYPE_MAP.get(type))
                .orElseThrow(() -> new NoSuchElementException("No persistent data type for " + type.getTypeName() + " present."));
    }

    /**
     * Returns an unmodifiable map of registered types and their corresponding {@link PersistentDataType}.
     *
     * @return a map of registered persistent data types
     */
    public static @Unmodifiable Map<Type, PersistentDataType<?, ?>> getPersistentDataTypeMap() {
        return Map.copyOf(PERSISTENT_DATA_TYPE_MAP);
    }
}
