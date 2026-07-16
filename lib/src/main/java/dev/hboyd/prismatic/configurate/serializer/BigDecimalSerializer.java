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

package dev.hboyd.prismatic.configurate.serializer;

import org.spongepowered.configurate.serialize.CoercionFailedException;
import org.spongepowered.configurate.serialize.ScalarSerializer;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.util.function.Predicate;

/**
 * Configurate serializer for {@link BigDecimal}s.
 */
public class BigDecimalSerializer extends ScalarSerializer<BigDecimal> {
    public static final BigDecimalSerializer INSTANCE = new BigDecimalSerializer();

    protected BigDecimalSerializer() {
        super(BigDecimal.class);
    }

    @Override
    public BigDecimal deserialize(final Type type, final Object val) throws CoercionFailedException {
        return switch (val) {
            case final String string -> new BigDecimal(string);
            case final Number number -> new BigDecimal(number.toString());
            default -> throw new CoercionFailedException(type, val, "BigDecimal");
        };
    }

    @Override
    protected Object serialize(final BigDecimal bigDecimal, final Predicate<Class<?>> typeSupported) {
        return bigDecimal.toString();
    }
}
