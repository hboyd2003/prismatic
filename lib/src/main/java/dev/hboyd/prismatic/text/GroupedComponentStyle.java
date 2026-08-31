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
import org.jetbrains.annotations.Contract;

/**
 * The style of a grouped component.
 *
 * @see GroupedComponent
 */
public sealed interface GroupedComponentStyle permits GroupedComponentStyleImpl {

    /**
     * Get the default grouped component style.
     *
     * @return a style
     */
    @Contract(pure = true)
    static GroupedComponentStyle style() {
        return GroupedComponentStyleImpl.DEFAULT;
    }

    /**
     * Creates a builder.
     *
     * @return a builder
     */
    @Contract(value = " -> new", pure = true)
    static Builder builder() {
        return style().toBuilder();
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
    @Contract(pure = true)
    Component titleSuffix();

    /**
     * Create a builder from this style.
     *
     * @return a builder
     */
    @Contract(value = " -> new", pure = true)
    Builder toBuilder();

    /**
     * A builder for {@link GroupedComponentStyle}s.
     */
    sealed interface Builder extends AbstractBuilder<GroupedComponentStyle> permits GroupedComponentStyleImpl.BuilderImpl {
        /**
         * Set the width provider.
         *
         * @param widthProvider the width provider
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder widthProvider(final TextWidthProvider widthProvider);

        /**
         * Set the glyph used to pad the header and footer.
         *
         * @param spacingGlyph the spacing glyph
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder spacingGlyph(final StyledGlyph spacingGlyph);

        /**
         * Set the title prefix.
         *
         * @param titlePrefix the title prefix
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder titlePrefix(final Component titlePrefix);

        /**
         * Set the title suffix.
         *
         * @param titleSuffix the title suffix
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder titleSuffix(final Component titleSuffix);
    }
}
