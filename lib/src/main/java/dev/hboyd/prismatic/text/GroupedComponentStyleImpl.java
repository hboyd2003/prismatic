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

import dev.hboyd.chasm.font.MinecraftFont;
import dev.hboyd.chasm.font.StyledGlyph;
import dev.hboyd.chasm.text.TextWidthProvider;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.jetbrains.annotations.Contract;

import java.util.Map;
import java.util.Objects;

final class GroupedComponentStyleImpl implements GroupedComponentStyle {
    static GroupedComponentStyleImpl DEFAULT = new GroupedComponentStyleImpl(TextWidthProvider.DEFAULT,
            new StyledGlyph(
                    MinecraftFont.BUILTIN.getSpaceCodepoints().entrySet().stream()
                            .filter(entry -> entry.getValue() > 0)
                            .min(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse(32),
                    Style.style(TextDecoration.STRIKETHROUGH)),
            Component.text("=[ "),
            Component.text(" ]="));

    private final TextWidthProvider widthProvider;
    private final StyledGlyph spacingGlyph;
    private final Component titlePrefix;
    private final Component titleSuffix;

    private GroupedComponentStyleImpl(final TextWidthProvider widthProvider,
                                      final StyledGlyph spacingGlyph,
                                      final Component titlePrefix,
                                      final Component titleSuffix) {
        this.widthProvider = widthProvider;
        this.spacingGlyph = spacingGlyph;
        this.titlePrefix = titlePrefix;
        this.titleSuffix = titleSuffix;
    }

    @Contract(pure = true)
    @Override
    public TextWidthProvider widthProvider() {
        return this.widthProvider;
    }

    @Contract(pure = true)
    @Override
    public StyledGlyph spacingGlyph() {
        return this.spacingGlyph;
    }

    @Contract(pure = true)
    @Override
    public Component titlePrefix() {
        return this.titlePrefix;
    }

    @Override
    public Component titleSuffix() {
        return this.titleSuffix;
    }

    @Override
    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    @Override
    public String toString() {
        return "GroupedMessageStyle{" + "widthProvider=" + this.widthProvider +
                ", spacingGlyph=" + this.spacingGlyph +
                ", titlePrefix=" + this.titlePrefix +
                ", titleSuffix=" + this.titleSuffix +
                '}';
    }

    static final class BuilderImpl implements GroupedComponentStyle.Builder {
        private TextWidthProvider widthProvider;
        private StyledGlyph spacingGlyph;
        private Component titlePrefix;
        private Component titleSuffix;

        private BuilderImpl(final GroupedComponentStyle style) {
            this.widthProvider = style.widthProvider();
            this.spacingGlyph = style.spacingGlyph();
            this.titlePrefix = style.titlePrefix();
            this.titleSuffix = style.titleSuffix();
        }

        @Override
        public BuilderImpl widthProvider(final TextWidthProvider widthProvider) {
            this.widthProvider = Objects.requireNonNull(widthProvider, "widthProvider");
            return this;
        }

        @Override
        public BuilderImpl spacingGlyph(final StyledGlyph spacingGlyph) {
            this.spacingGlyph = Objects.requireNonNull(spacingGlyph, "spacingGlyph");
            return this;
        }

        @Override
        public BuilderImpl titlePrefix(final Component titlePrefix) {
            this.titlePrefix = Objects.requireNonNull(titlePrefix, "titlePrefix");
            return this;
        }

        @Override
        public BuilderImpl titleSuffix(final Component titleSuffix) {
            this.titleSuffix = Objects.requireNonNull(titleSuffix, "titleSuffix");
            return this;
        }

        @Override
        public GroupedComponentStyleImpl build() {
            return new GroupedComponentStyleImpl(
                    this.widthProvider,
                    this.spacingGlyph,
                    this.titlePrefix,
                    this.titleSuffix);
        }
    }
}
