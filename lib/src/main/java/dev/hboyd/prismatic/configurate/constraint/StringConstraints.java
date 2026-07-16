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

/**
 * Configurate constraints for {@link String}s.
 *
 * @see Constraint
 */
@SuppressWarnings({"unused"})
public final class StringConstraints {
    private StringConstraints() {
        /* This utility class should not be instantiated */
    }

    /**
     * Constrains the annotated {@link String} to a non-empty value when being loaded by an {@link ObjectMapper}.
     *
     * @see String#isEmpty()
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface NonEmpty {
        @SuppressWarnings("checkstyle:MissingJavadocType")
        final class Factory implements Constraint.Factory<NonEmpty, String> {
            @Override
            public Constraint<String> make(final NonEmpty data, final Type type) {
                return value -> {
                    if (value != null && value.isEmpty())
                        throw new SerializationException("The string must not be empty");
                };
            }
        }
    }

    /**
     * Constrains the annotated {@link String} to a value which does not contain solely whitespace when being loaded by
     * an {@link ObjectMapper}.
     *
     * @see String#isBlank()
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface NonBlank {
        @SuppressWarnings("checkstyle:MissingJavadocType")
        final class Factory implements Constraint.Factory<NonBlank, String> {
            @Override
            public Constraint<String> make(final NonBlank data, final Type type) {
                return value -> {
                    if (value != null && value.isBlank())
                        throw new SerializationException("The string must not be blank");
                };
            }
        }
    }

    /**
     * Constrains the annotated {@link String} to a value whose length fits in the inclusive {@code min} and {@code max}
     * bounds when being loaded by an {@link ObjectMapper}.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Length {
        /**
         * Get the minimum length that the constrained {@link String} can be.
         *
         * @return the minimum value
         */
        int min() default 0;

        /**
         * Get the maximum length that the constrained {@link String} can be.
         *
         * @return the maximum value
         */
        int max() default Integer.MAX_VALUE;

        @SuppressWarnings("checkstyle:MissingJavadocType")
        final class Factory implements Constraint.Factory<StringConstraints.Length, String> {
            @Override
            public Constraint<String> make(final StringConstraints.Length data, final Type type) {
                return value -> {
                    if (value != null && value.length() < data.min() && value.length() > data.max())
                        throw new SerializationException(value.length() + " is outside the length bounds of (" + data.min() + ", " + data.max() + ")");
                };
            }
        }
    }
}
