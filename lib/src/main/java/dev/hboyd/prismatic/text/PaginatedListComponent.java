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
public sealed interface PaginatedListComponent extends ComponentLike permits PaginatedListComponentImpl {
    /**
     * Create a paginated list component with the given title style and item factory.
     *
     * @param title       the title
     * @param style       the style
     * @param itemFactory the factory
     * @return a paginated list component
     */
    @Contract("_, _, _ -> new")
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
    @Contract("_, _, _ -> new")
    static PaginatedListComponent of(final Component title,
                                     final PaginatedListStyle style,
                                     final List<? extends ComponentLike> items) {
        return of(title, style, IPaginatedListComponentItemFactory.of(List.copyOf(items)));
    }

    /**
     * Get an empty paginated list component.
     *
     * @return an empty component
     */
    @Contract(pure = true)
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
    @Contract(value = "_ -> new", pure = true)
    PaginatedListComponent title(final Component title);

    /**
     * Get the title.
     *
     * @return the title
     */
    @Contract(pure = true)
    Component title();

    /**
     * Get a paginated list component that is the same as this but with the given style.
     *
     * <p>If the style is the same, this will be returned</p>
     *
     * @param style the style
     * @return a paginated list component
     */
    @Contract(value = "_ -> new", pure = true)
    PaginatedListComponent style(final PaginatedListStyle style);

    /**
     * Get the style.
     *
     * @return the style
     */
    @Contract(pure = true)
    PaginatedListStyle style();

    /**
     * Get a paginated list component that is the same as this but with the given item factory.
     *
     * <p>If the item factory is the same, this will be returned</p>
     *
     * @param itemFactory the factory
     * @return a paginated list component
     */
    @Contract(value = "_ -> new", pure = true)
    PaginatedListComponent itemFactory(IPaginatedListComponentItemFactory itemFactory);

    /**
     * Get the item factory.
     *
     * @return the factory
     */
    @Contract(pure = true)
    IPaginatedListComponentItemFactory itemFactory();

    /**
     * Send the given page as a system chat message to the given audience.
     *
     * <p>Pages are indexed from one.</p>
     *
     * <p>If the given page does not exist (i.e., there are no items at that index) then an empty page is sent.</p>
     *
     * @param page     the page
     * @param audience an audience
     */
    void sendAsMessage(final int page, final Audience audience);

    /**
     * Send the first page as a system chat message to the given audience.
     *
     * <p>If no items are available then an empty page is sent.</p>
     *
     * @param audience an audience
     */
    default void sendAsMessage(final Audience audience) {
        this.sendAsMessage(1, audience);
    }

    /**
     * Render the given page for the given audience.
     *
     * <p>Pages are indexed from one.</p>
     *
     * <p>If the given page does not exist (i.e., there are no items at that index) then an empty page is returned.</p>
     *
     * @param page     the page
     * @param audience an audience
     * @return a component
     */
    @Contract(value = "_, _ -> new", pure = true)
    Component render(final int page, final Audience audience);

    /**
     * Render the first page for the given audience.
     *
     * <p>If no items are available then an empty page is returned.</p>
     *
     * @param audience an audience
     * @return a component
     */
    @Contract(value = "_ -> new", pure = true)
    default Component render(final Audience audience) {
        return this.render(1, audience);
    }

    /**
     * Render the given page.
     *
     * <p>Pages are indexed from one.</p>
     *
     * <p>If the given page does not exist (i.e., there are no items at that index) then an empty page is returned.</p>
     *
     * @param page the page
     * @return a component
     */
    @Contract(value = "_ -> new", pure = true)
    default Component render(final int page) {
        return this.render(page, Audience.empty());
    }

    /**
     * Render the first page with an empty audience.
     *
     * <p>If no items are available then an empty page is returned.</p>
     *
     * @return a component
     */
    @Contract(pure = true)
    default Component render() {
        return this.render(Audience.empty());
    }

    @Override
    default Component asComponent() {
        return this.render();
    }

    /**
     * Create a builder from this component.
     *
     * @return a builder
     */
    @Contract(value = " -> new", pure = true)
    Builder toBuilder();

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
    sealed interface Builder extends AbstractBuilder<PaginatedListComponent> permits PaginatedListComponentImpl.BuilderImpl {

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
