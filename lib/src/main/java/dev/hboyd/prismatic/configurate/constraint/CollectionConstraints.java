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
import java.util.Collection;

/**
 * Configurate constraints for {@link java.util.Collection}s.
 *
 * @see Constraint
 */
@SuppressWarnings({"rawtypes", "unused"})
public final class CollectionConstraints {

    private CollectionConstraints() {
        /* This utility class should not be instantiated */
    }

    /**
     * Constrains the annotated {@link Collection} to have at-least one element when being loaded and saved by an
     * {@link ObjectMapper}.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface NonEmpty {
        @SuppressWarnings("checkstyle:MissingJavadocType")
        final class Factory implements Constraint.Factory<NonEmpty, Collection> {
            @Override
            public Constraint<Collection> make(final NonEmpty data, final Type type) {
                return value -> {
                    if (value != null && value.isEmpty())
                        throw new SerializationException("The collection must not be empty");
                };
            }
        }
    }

    /**
     * Constrains the annotated collection's size to be within the given inclusive {@code min} and {@code max} bounds
     * when being loaded and saved by an {@link ObjectMapper}.
     */
    @Documented
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface Size {
        /**
         * Get the minimum inclusive size of the annotated collection.
         *
         * @return the minimum size
         */
        int min() default 0;

        /**
         * Get the maximum inclusive size of the annotated collection.
         *
         * @return the maximum size
         */
        int max() default Integer.MAX_VALUE;

        @SuppressWarnings("checkstyle:MissingJavadocType")
        final class Factory implements Constraint.Factory<Size, Collection> {
            @Override
            public Constraint<Collection> make(final Size data, final Type type) {
                return value -> {
                    if (value != null && (value.size() < data.min() || value.size() > data.max()))
                        throw new SerializationException(value.size() + " is outside the collection bounds of (" + data.min() + ", " + data.max() + ")");
                };
            }
        }
    }
}
