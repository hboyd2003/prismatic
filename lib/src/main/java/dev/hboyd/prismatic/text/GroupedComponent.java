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
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.util.Buildable;
import org.jetbrains.annotations.Contract;

import java.util.Objects;

/**
 * A component with a title, content and a footer.
 *
 * @see GroupedComponentStyle
 */
public sealed interface GroupedComponent extends ComponentLike, Buildable<GroupedComponent, GroupedComponent.Builder> permits GroupedComponentImpl {
    /**
     * Create a grouped component with the given title, content and formated with the given style.
     *
     * @param title   the title
     * @param content the content
     * @param style   the style
     * @return a grouped component
     */
    static GroupedComponent of(final ComponentLike title,
                               final ComponentLike content,
                               final GroupedComponentStyle style) {
        return new GroupedComponentImpl(Objects.requireNonNull(title, "title"),
                Objects.requireNonNull(content, "content"),
                Objects.requireNonNull(style, "style"));
    }

    /**
     * Create a grouped component with the given title and content.
     *
     * @param title   the title
     * @param content the content
     * @return a grouped component
     */
    static GroupedComponent of(final Component title, final Component content) {
        return of(title, content, GroupedComponentStyle.style());
    }

    /**
     * Get an empty grouped component.
     *
     * @return an empty component
     */
    static GroupedComponent empty() {
        return GroupedComponentImpl.EMPTY;
    }

    /**
     * Get the title.
     *
     * @return the title
     */
    ComponentLike title();

    /**
     * Get a grouped component that is the same as this but with the given title.
     *
     * <p>If the title is the same, this will be returned</p>
     *
     * @param title the style
     * @return a grouped component
     */
    GroupedComponent title(final ComponentLike title);

    /**
     * Get the content.
     *
     * @return the content
     */
    ComponentLike content();

    /**
     * Get a grouped component that is the same as this but with the given content.
     *
     * <p>If the content is the same, this will be returned</p>
     *
     * @param content the content
     * @return a grouped component
     */
    GroupedComponent content(final ComponentLike content);

    /**
     * Get the style.
     *
     * @return the style
     */
    GroupedComponentStyle style();

    /**
     * Get a grouped component that is the same as this but with the given style.
     *
     * <p>If the style is the same, this will be returned</p>
     *
     * @param style the style
     * @return a grouped component
     */
    GroupedComponent style(final GroupedComponentStyle style);

    /**
     * Create an empty builder.
     *
     * @return a builder
     */
    static Builder builder() {
        return empty().toBuilder();
    }

    /**
     * A builder for a {@link GroupedComponent}.
     */
    sealed interface Builder extends Buildable.Builder<GroupedComponent> permits GroupedComponentImpl.BuilderImpl {

        /**
         * Set the title.
         *
         * @param title the title
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder title(final ComponentLike title);

        /**
         * Set the content.
         *
         * @param content the content
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder content(final ComponentLike content);

        /**
         * Set the style.
         *
         * @param style the style
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder style(final GroupedComponentStyle style);
    }
}
