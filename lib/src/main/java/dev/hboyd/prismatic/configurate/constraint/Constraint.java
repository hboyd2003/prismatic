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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

// Taken from https://github.com/PaperMC/Paper/blob/main/paper-server/src/main/java/io/papermc/paper/configuration/constraint/Constraint.java

/**
 * Provides a Constraint factory for constraints without a value.
 *
 * @see dev.hboyd.prismatic.configurate.constraint.StringConstraints.NonBlank
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER})
public @interface Constraint {
    @SuppressWarnings("checkstyle:MissingJavadocMethod")
    Class<? extends org.spongepowered.configurate.objectmapping.meta.Constraint<?>> value();

    @SuppressWarnings("checkstyle:MissingJavadocType")
    class Factory implements org.spongepowered.configurate.objectmapping.meta.Constraint.Factory<Constraint, Object> {
        @SuppressWarnings("unchecked")
        @Override
        public org.spongepowered.configurate.objectmapping.meta.Constraint<Object> make(final Constraint data, final Type type) {
            try {
                final Constructor<? extends org.spongepowered.configurate.objectmapping.meta.Constraint<?>> constructor = data.value().getDeclaredConstructor();
                constructor.trySetAccessible();
                return (org.spongepowered.configurate.objectmapping.meta.Constraint<Object>) constructor.newInstance();
            } catch (final ReflectiveOperationException e) {
                throw new RuntimeException("Could not create constraint", e);
            }
        }
    }
}
