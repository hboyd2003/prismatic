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

package dev.hboyd.prismatic.configurate.constraint;

import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.meta.Constraint;
import org.spongepowered.configurate.serialize.SerializationException;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

/**
 * Configurate constraints for {@link Number}s.
 *
 * @see Constraint
 */
@SuppressWarnings({"unused"})
public final class NumberConstraints {

    private NumberConstraints() {
        /* This utility class should not be instantiated */
    }

    private static int compare(final Number number, final long bound) {
        return switch (number) {
            case final BigDecimal bigDecimal -> bigDecimal.compareTo(BigDecimal.valueOf(bound));
            case final BigInteger bigInteger -> bigInteger.compareTo(BigInteger.valueOf(bound));
            case Byte _, Short _, Integer _, Long _, AtomicInteger _, AtomicLong _, LongAdder _, LongAccumulator _ ->
                Long.compare(number.longValue(), bound);
            default -> Double.compare(number.doubleValue(), bound);
        };
    }

    /**
     * Constrains the annotated {@link Number} to a positive value ({@code N > 0}) when being loaded by an
     * {@link ObjectMapper}.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Positive {
        @SuppressWarnings("checkstyle:MissingJavadocType")
        final class Factory implements Constraint.Factory<Positive, Number> {
            @Override
            public Constraint<Number> make(final Positive data, final Type type) {
                return value -> {
                    if (value != null && compare(value, 0L) <= 0)
                        throw new SerializationException(value + " must be positive (> 0)");
                };
            }
        }
    }

    /**
     * Constrains the annotated {@link Number} to a non-positive value ({@code N <= 0}) when being loaded by an
     * {@link ObjectMapper}.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface NonPositive {
        @SuppressWarnings("checkstyle:MissingJavadocType")
        final class Factory implements Constraint.Factory<NonPositive, Number> {
            @Override
            public Constraint<Number> make(final NonPositive data, final Type type) {
                return value -> {
                    if (value != null && compare(value, 0L) > 0)
                        throw new SerializationException(value + " must be non-positive");
                };
            }
        }
    }


    /**
     * Constrains the annotated {@link Number} to a negative value ({@code N < 0}) when being loaded by an
     * {@link ObjectMapper}.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Negative {
        @SuppressWarnings("checkstyle:MissingJavadocType")
        final class Factory implements Constraint.Factory<Negative, Number> {
            @Override
            public Constraint<Number> make(final Negative data, final Type type) {
                return value -> {
                    if (value != null && compare(value, 0L) >= 0)
                        throw new SerializationException(value + " must be negative (< 0)");
                };
            }
        }
    }

    /**
     * Constrains the annotated {@link Number} to a non-negative value ({@code N >= 0}) when being loaded by an
     * {@link ObjectMapper}.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface NonNegative {
        @SuppressWarnings("checkstyle:MissingJavadocType")
        final class Factory implements Constraint.Factory<NonNegative, Number> {
            @Override
            public Constraint<Number> make(final NonNegative data, final Type type) {
                return value -> {
                    if (value != null && compare(value, 0L) < 0)
                        throw new SerializationException(value + " must be non-negative (>= 0)");
                };
            }
        }
    }

    /**
     * Constrains the annotated {@link Number} to within the given inclusive {@code min} and {@code max} bounds when
     * being loaded by an {@link ObjectMapper}.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Bound {
        /**
         * Get inclusive minimum value that the constrained {@link Number} can be.
         *
         * @return the minimum value
         */
        long min() default Long.MIN_VALUE;

        /**
         * Get inclusive maximum value that the constrained {@link Number} can be.
         *
         * @return the maximum value
         */
        long max() default Long.MAX_VALUE;

        @SuppressWarnings("checkstyle:MissingJavadocType")
        final class Factory implements Constraint.Factory<NumberConstraints.Bound, Number> {
            @Override
            public Constraint<Number> make(final NumberConstraints.Bound data, final Type type) {
                return value -> {
                    if (value != null && (compare(value, data.min()) < 0 || compare(value, data.max()) > 0))
                        throw new SerializationException(value.intValue() + " is outside the bounds of (" + data.min() + ", " + data.max() + ")");
                };
            }
        }
    }
}
