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

package dev.hboyd.prismatic.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import org.jetbrains.annotations.ApiStatus;

import java.lang.reflect.Method;

/**
 * {@link Component} related utilities.
 */
public final class ComponentUtil {

    private static final Method COMPONENT_BUILDER_BUILD_METHOD; // Used to workaround for differences between Adventure 4 and 5 "build" declarations differences

    static {
        try {
            COMPONENT_BUILDER_BUILD_METHOD = ComponentBuilder.class.getMethod("build");
        } catch (final NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private ComponentUtil() {
    }

    /**
     * Remove color from a component and its children.
     *
     * @param component the component to strip
     * @return a new component with no color styling
     */
    public static Component stripColor(final Component component) {
        return component.color(null).children(component.children().stream()
                .map(ComponentUtil::stripColor)
                .toList());
    }

    /**
     * Builds the given builder reflectively. Used to avoid issues when running with Adventure 4.
     *
     * @param builder the builder
     * @param <C>     the component type
     * @param <B>     the builder type
     * @return a component
     */
    @ApiStatus.Experimental
    public static <C extends Component, B extends ComponentBuilder<C, B>> C build(final ComponentBuilder<C, B> builder) {
        try {
            //noinspection unchecked
            return (C) COMPONENT_BUILDER_BUILD_METHOD.invoke(builder);
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }
}
