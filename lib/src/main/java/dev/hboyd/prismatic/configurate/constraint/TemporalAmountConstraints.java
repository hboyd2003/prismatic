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
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;

/**
 * Configurate constraints for {@link java.time.Duration}s.
 *
 * @see Constraint
 */
public final class TemporalAmountConstraints {
    private TemporalAmountConstraints() {
        /* This utility class should not be instantiated */
    }

    /**
     * Constrains the annotated {@link TemporalAmount} to a positive amount ({@code N > 0}) when being loaded by an
     * {@link ObjectMapper}.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Positive {
        @SuppressWarnings("checkstyle:MissingJavadocType")
        final class Factory implements Constraint.Factory<TemporalAmountConstraints.Positive, TemporalAmount> {
            @Override
            public Constraint<TemporalAmount> make(final TemporalAmountConstraints.Positive data, final Type type) {
                return value -> {
                    if (value == null) return;

                    final Duration duration;
                    if (value instanceof Duration) duration = (Duration) value;
                    else duration = Duration.from(value);

                    if (!duration.isPositive()) throw new SerializationException(value + " must be positive (N > 0)");
                };
            }
        }
    }

    /**
     * Constrains the annotated {@link TemporalAmount} to a positive amount ({@code N <= 0}) when being loaded by an
     * {@link ObjectMapper}.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface NonPositive {
        @SuppressWarnings("checkstyle:MissingJavadocType")
        final class Factory implements Constraint.Factory<TemporalAmountConstraints.NonPositive, TemporalAmount> {
            @Override
            public Constraint<TemporalAmount> make(final TemporalAmountConstraints.NonPositive data, final Type type) {
                return value -> {
                    if (value == null) return;

                    final Duration duration;
                    if (value instanceof Duration) duration = (Duration) value;
                    else duration = Duration.from(value);

                    if (duration.isPositive())
                        throw new SerializationException(value + " must be less than or equal to zero (N <= 0)");
                };
            }
        }
    }

    /**
     * Constrains the annotated {@link TemporalAmount} to a negative amount ({@code N < 0}) when being loaded by an
     * {@link ObjectMapper}.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Negative {
        @SuppressWarnings("checkstyle:MissingJavadocType")
        final class Factory implements Constraint.Factory<TemporalAmountConstraints.Negative, TemporalAmount> {
            @Override
            public Constraint<TemporalAmount> make(final TemporalAmountConstraints.Negative data, final Type type) {
                return value -> {
                    if (value == null) return;

                    final Duration duration;
                    if (value instanceof Duration) duration = (Duration) value;
                    else duration = Duration.from(value);

                    if (!duration.isNegative()) throw new SerializationException(value + " must be negative (N < 0)");
                };
            }
        }
    }

    /**
     * Constrains the annotated {@link TemporalAmount} to a non-negative amount ({@code N >= 0}) when being loaded by an
     * {@link ObjectMapper}.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface NonNegative {
        @SuppressWarnings("checkstyle:MissingJavadocType")
        final class Factory implements Constraint.Factory<TemporalAmountConstraints.NonNegative, TemporalAmount> {
            @Override
            public Constraint<TemporalAmount> make(final TemporalAmountConstraints.NonNegative data, final Type type) {
                return value -> {
                    if (value == null) return;

                    final Duration duration;
                    if (value instanceof Duration) duration = (Duration) value;
                    else duration = Duration.from(value);

                    if (duration.isNegative())
                        throw new SerializationException(value + " must be greater than or equal to zero (N >= 0)");
                };
            }
        }
    }

    /**
     * Constrains the annotated {@link TemporalAmount} to within the given inclusive {@code min} and {@code max} bounds
     * when being loaded by an {@link ObjectMapper}.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Bound {
        /**
         * Get the inclusive minimum amount that the constrained {@link TemporalAmount} can be.
         *
         * @return the minimum amount
         */
        long min() default Long.MIN_VALUE;

        /**
         * Get the inclusive maximum amount that the constrained {@link TemporalAmount} can be.
         *
         * @return the maximum amount
         */
        long max() default Long.MAX_VALUE;

        /**
         * Get the chrono unit that the {@code min} and {@code max} are in.
         *
         * @return the unit
         */
        ChronoUnit unit() default ChronoUnit.SECONDS;

        @SuppressWarnings("checkstyle:MissingJavadocType")
        final class Factory implements Constraint.Factory<TemporalAmountConstraints.Bound, TemporalAmount> {
            @Override
            public Constraint<TemporalAmount> make(final TemporalAmountConstraints.Bound data, final Type type) {
                return value -> {
                    if (value == null) return;

                    final Duration duration = Duration.from(value);
                    if (duration.compareTo(Duration.of(data.min(), data.unit())) < 0
                            || duration.compareTo(Duration.of(data.max(), data.unit())) > 0)
                        throw new SerializationException(value + " is outside the bounds of (" + data.min() + ", " + data.max() + ")");
                };
            }
        }
    }
}
