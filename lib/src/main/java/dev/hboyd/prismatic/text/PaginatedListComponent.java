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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.util.Buildable;
import org.jetbrains.annotations.Contract;

import java.util.List;
import java.util.Objects;

/**
 * A component with a title, items and page selector.
 *
 * @see PaginatedListStyle
 * @see IPaginatedListComponentItemFactory
 * @see PaginatedListRedisplayClickCallbackBuilder
 */
public sealed interface PaginatedListComponent extends ComponentLike, Buildable<PaginatedListComponent, PaginatedListComponent.Builder> permits PaginatedListComponentImpl {
    /**
     * Create a paginated list component with the given title style and item factory.
     *
     * @param title       the title
     * @param style       the style
     * @param itemFactory the factory
     * @return a paginated list component
     */
    static PaginatedListComponent of(final Component title,
                                     final PaginatedListStyle style,
                                     final IPaginatedListComponentItemFactory itemFactory) {
        return new PaginatedListComponentImpl(Objects.requireNonNull(title, "title"),
                Objects.requireNonNull(style, "style"),
                Objects.requireNonNull(itemFactory, "itemFactory"));
    }

    /**
     * Create a paginated list component with the given title style and item factory.
     *
     * @param title the title
     * @param style the style
     * @param items the items
     * @return a paginated list component
     */
    static PaginatedListComponent of(final Component title,
                                     final PaginatedListStyle style,
                                     final List<? extends ComponentLike> items) {
        return of(title, style, IPaginatedListComponentItemFactory.of(List.copyOf(items)));
    }

    /**
     * Get an empty grouped component.
     *
     * @return an empty component
     */
    static PaginatedListComponent empty() {
        return PaginatedListComponentImpl.EMPTY;
    }

    /**
     * Get a paginated list component that is the same as this but with the given title.
     *
     * <p>If the title is the same, this will be returned</p>
     *
     * @param title the title
     * @return a paginated list component
     */
    PaginatedListComponent title(final Component title);

    /**
     * Get the title.
     *
     * @return the title
     */
    Component title();

    /**
     * Get a paginated list component that is the same as this but with the given style.
     *
     * <p>If the style is the same, this will be returned</p>
     *
     * @param style the style
     * @return a paginated list component
     */
    PaginatedListComponent style(final PaginatedListStyle style);

    /**
     * Get the style.
     *
     * @return the style
     */
    PaginatedListStyle style();

    /**
     * Get a paginated list component that is the same as this but with the given item factory.
     *
     * <p>If the item factory is the same, this will be returned</p>
     *
     * @param itemFactory the factory
     * @return a paginated list component
     */
    PaginatedListComponent itemFactory(IPaginatedListComponentItemFactory itemFactory);

    /**
     * Get the item factory.
     *
     * @return the factory
     */
    IPaginatedListComponentItemFactory itemFactory();

    /**
     * Renders the first page with an empty audience.
     *
     * @return a component
     */
    Component render();

    /**
     * Renders the first page for the given audience.
     *
     * @param audience an audience
     * @return a component
     */
    Component render(final Audience audience);

    /**
     * Renders the given page with an audience.
     *
     * @param page the page
     * @return a component
     */
    Component render(final int page);

    /**
     * Renders the given page for the given audience.
     *
     * @param page     the page
     * @param audience an audience
     * @return a component
     */
    Component render(final int page, final Audience audience);

    /**
     * Creates a builder for a {@link PaginatedListComponent}.
     *
     * @return a builder
     */
    static Builder builder() {
        return empty().toBuilder();
    }

    /**
     * A builder for {@link PaginatedListComponent}s.
     */
    sealed interface Builder extends AbstractBuilder<PaginatedListComponent>, Buildable.Builder<PaginatedListComponent> permits PaginatedListComponentImpl.BuilderImpl {

        /**
         * Set the title.
         *
         * @param title the title
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder title(final Component title);

        /**
         * Set the style.
         *
         * @param style the style
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder style(final PaginatedListStyle style);

        /**
         * Set the paginated list component item factory used to create the items displayed.
         *
         * @param itemFactory the factory
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder itemFactory(final IPaginatedListComponentItemFactory itemFactory);
    }
}
