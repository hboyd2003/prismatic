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

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.builder.AbstractBuilder;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.VirtualComponent;
import net.kyori.adventure.text.event.ClickCallback;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

/**
 * A builder for virtual components with a click callback which when part of a paginated list component, will
 * re-display the current page when clicked.
 *
 * @see PaginatedListComponent
 * @see ClickCallback
 */
public sealed interface PaginatedListRedisplayClickCallbackBuilder extends AbstractBuilder<VirtualComponent> permits PaginatedListRedisplayClickCallbackBuilderImpl {

    /**
     * Create a builder for a paginated list re-display callback.
     *
     * @return a builder
     */
    static PaginatedListRedisplayClickCallbackBuilder builder() {
        return new PaginatedListRedisplayClickCallbackBuilderImpl();
    }

    /**
     * Set the component to display when rendered by a {@link PaginatedListComponent}.
     *
     * @param component a component
     * @return this builder
     */
    @Contract(value = "_ -> this", mutates = "this")
    PaginatedListRedisplayClickCallbackBuilder component(final ComponentLike component);

    /**
     * Set the string to use when the component has been serialized without being rendered.
     *
     * @param fallback the fallback
     * @return this builder
     */
    @Contract(value = "_ -> this", mutates = "this")
    PaginatedListRedisplayClickCallbackBuilder fallback(final String fallback);

    /**
     * Set the function to call before the paginated list is redisplayed.
     *
     * @param clickCallback the function
     * @return this builder
     */
    @Contract(value = "_ -> this", mutates = "this")
    PaginatedListRedisplayClickCallbackBuilder clickCallback(final @Nullable ClickCallback<Audience> clickCallback);

    /**
     * Set the options to control how the callback will be stored on the server.
     *
     * @param options the options
     * @return this builder
     */
    @Contract(value = "_ -> this", mutates = "this")
    PaginatedListRedisplayClickCallbackBuilder callbackOptions(final ClickCallback.Options options);
}
