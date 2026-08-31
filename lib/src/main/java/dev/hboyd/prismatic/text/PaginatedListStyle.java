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

import dev.hboyd.chasm.font.StyledGlyph;
import dev.hboyd.chasm.text.TextWidthProvider;
import net.kyori.adventure.builder.AbstractBuilder;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.jetbrains.annotations.Contract;

/**
 * A style of a paginated list component.
 *
 * @see PaginatedListComponent
 */
public interface PaginatedListStyle {
    /**
     * Get the default style.
     *
     * @return a style
     */
    static PaginatedListStyle style() {
        return PaginatedListStyleImpl.DEFAULT;
    }

    /**
     * Create a new builder for a {@link PaginatedListStyle}.
     *
     * @return a builder
     */
    @Contract(value = " -> new", pure = true)
    static Builder builder() {
        return PaginatedListStyleImpl.DEFAULT.toBuilder();
    }


    /**
     * Get the width provider.
     *
     * @return the width provider
     */
    @Contract(pure = true)
    TextWidthProvider widthProvider();

    /**
     * Get the glyph used to pad the header and footer.
     *
     * @return the spacing glyph
     */
    @Contract(pure = true)
    StyledGlyph spacingGlyph();

    /**
     * Get the title prefix.
     *
     * @return the title prefix
     */
    @Contract(pure = true)
    Component titlePrefix();

    /**
     * Get the title suffix.
     *
     * @return the title suffix
     */
    Component titleSuffix();

    /**
     * Get the items per page.
     *
     * @return items per page
     */
    int itemsPerPage();

    /**
     * Get the maximum number of page selectors to show at once.
     *
     * @return the count
     */
    int pageSelectorCount();

    /**
     * Get the page selector style.
     *
     * @return a style
     */
    JoinConfiguration pageSelectorStyle();

    /**
     * Create a builder from this style.
     *
     * @return a builder
     */
    Builder toBuilder();

    /**
     * A builder for {@link PaginatedListStyle}s.
     */
    sealed interface Builder extends AbstractBuilder<PaginatedListStyle> permits PaginatedListStyleImpl.BuilderImpl {
        /**
         * Set the width provider.
         *
         * @param widthProvider the width provider
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder widthProvider(TextWidthProvider widthProvider);

        /**
         * Set the glyph used to pad the header and footer.
         *
         * @param spacingGlyph the spacing glyph
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder spacingGlyph(StyledGlyph spacingGlyph);

        /**
         * Set the title prefix.
         *
         * @param titlePrefix the title prefix
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder titlePrefix(Component titlePrefix);

        /**
         * Set the title suffix.
         *
         * @param titleSuffix the title suffix
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder titleSuffix(Component titleSuffix);

        /**
         * Set the items shown per page.
         *
         * @param itemsPerPage the items per page
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder itemsPerPage(int itemsPerPage);

        /**
         * Set the maximum number of page selectors to show at once.
         *
         * @param pageSelectorCount the count
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder pageSelectorCount(int pageSelectorCount);

        /**
         * Set the style of the page selector.
         *
         * @param pageSelectorStyle the style
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder pageSelectorStyle(JoinConfiguration pageSelectorStyle);
    }
}
