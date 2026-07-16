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

import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataType;

import java.util.function.Function;

/**
 * A {@link PersistentDataType} which serializes into a String using the configured serialization functions.
 *
 * @param <T> the complex type being mapped to a string
 */
public class SimplePersistentDataType<T> implements PersistentDataType<String, T> {
    private final Class<T> type;
    private final Function<T, String> toPrimitiveFunction;
    private final Function<String, T> fromPrimitiveFunction;

    /**
     * Constructs a new SimplePersistentDataType.
     *
     * @param type                  the class of the complex type being mapped
     * @param toPrimitiveFunction   the function used to serialize the complex type to a string
     * @param fromPrimitiveFunction the function used to deserialize a string back into the complex type
     */
    public SimplePersistentDataType(final Class<T> type,
                                    final Function<T, String> toPrimitiveFunction,
                                    final Function<String, T> fromPrimitiveFunction) {
        this.type = type;
        this.toPrimitiveFunction = toPrimitiveFunction;
        this.fromPrimitiveFunction = fromPrimitiveFunction;
    }

    /**
     * Creates a new SimplePersistentDataType and registers it in the {@link PersistentDataTypeRegistry}.
     *
     * @param type                  the class of the complex type being mapped
     * @param toPrimitiveFunction   the function used to serialize the complex type to a string
     * @param fromPrimitiveFunction the function used to deserialize a string back into the complex type
     * @param <T>                   the complex type being mapped to a string
     * @return the created SimplePersistentDataType instance
     */
    public static <T> SimplePersistentDataType<T> createAndRegister(final Class<T> type,
                                                                    final Function<T, String> toPrimitiveFunction,
                                                                    final Function<String, T> fromPrimitiveFunction) {
        final SimplePersistentDataType<T> simpleType = new SimplePersistentDataType<>(type,
                toPrimitiveFunction,
                fromPrimitiveFunction);
        PersistentDataTypeRegistry.register(simpleType);
        return simpleType;
    }

    @Override
    public Class<String> getPrimitiveType() {
        return String.class;
    }

    @Override
    public Class<T> getComplexType() {
        return this.type;
    }

    @Override
    public String toPrimitive(final T complex, final PersistentDataAdapterContext context) {
        return this.toPrimitiveFunction.apply(complex);
    }

    @Override
    public T fromPrimitive(final String primitive, final PersistentDataAdapterContext context) {
        return this.fromPrimitiveFunction.apply(primitive);
    }
}
