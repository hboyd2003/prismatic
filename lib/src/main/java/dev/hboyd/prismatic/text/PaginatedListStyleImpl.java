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
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.checkerframework.checker.index.qual.Positive;
import org.checkerframework.common.value.qual.IntRange;

import java.util.Map;
import java.util.Objects;

/**
 * The style of a paginated list.
 *
 * @see PaginatedListComponent
 */
final class PaginatedListStyleImpl implements PaginatedListStyle {
    static final PaginatedListStyle DEFAULT = new PaginatedListStyleImpl(TextWidthProvider.DEFAULT,
            new StyledGlyph(
                    MinecraftFont.BUILTIN.getSpaceCodepoints().entrySet().stream()
                            .filter(entry -> entry.getValue() > 0)
                            .min(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse(32),
                    Style.style(TextDecoration.STRIKETHROUGH)),
            Component.text("=[ "),
            Component.text(" ]="),
            9,
            8,
            JoinConfiguration.builder()
                    .prefix(Component.text("<"))
                    .suffix(Component.text(">"))
                    .separator(Component.text(", "))
                    .convertor(componentLike -> componentLike.asComponent().colorIfAbsent(NamedTextColor.AQUA))
                    .build());

    private final TextWidthProvider widthProvider;
    private final StyledGlyph spacingGlyph;
    private final Component titlePrefix;
    private final Component titleSuffix;
    private final int itemsPerPage;
    private final int pageSelectorCount;
    private final JoinConfiguration pageSelectorStyle;

    private PaginatedListStyleImpl(final TextWidthProvider widthProvider,
                                   final StyledGlyph spacingGlyph,
                                   final Component titlePrefix,
                                   final Component titleSuffix,
                                   @Positive final int itemsPerPage,
                                   @Positive final int pageSelectorCount,
                                   final JoinConfiguration pageSelectorStyle) {
        this.widthProvider = widthProvider;
        this.spacingGlyph = spacingGlyph;
        this.titlePrefix = titlePrefix;
        this.titleSuffix = titleSuffix;
        this.itemsPerPage = itemsPerPage;
        this.pageSelectorCount = pageSelectorCount;
        this.pageSelectorStyle = pageSelectorStyle;
    }

    @Override
    public TextWidthProvider widthProvider() {
        return this.widthProvider;
    }

    @Override
    public StyledGlyph spacingGlyph() {
        return this.spacingGlyph;
    }

    @Override
    public Component titlePrefix() {
        return this.titlePrefix;
    }

    @Override
    public Component titleSuffix() {
        return this.titleSuffix;
    }

    @Override
    public int itemsPerPage() {
        return this.itemsPerPage;
    }

    @Override
    public int pageSelectorCount() {
        return this.pageSelectorCount;
    }

    @Override
    public JoinConfiguration pageSelectorStyle() {
        return this.pageSelectorStyle;
    }

    @Override
    public BuilderImpl toBuilder() {
        return new BuilderImpl(this);
    }

    static final class BuilderImpl implements PaginatedListStyle.Builder {
        private TextWidthProvider widthProvider;
        private StyledGlyph spacingGlyph;
        private Component titlePrefix;
        private Component titleSuffix;
        private int itemsPerPage;
        private int pageSelectorCount;
        private JoinConfiguration pageSelectorStyle;

        private BuilderImpl(final PaginatedListStyle style) {
            this.widthProvider = style.widthProvider();
            this.spacingGlyph = style.spacingGlyph();
            this.titlePrefix = style.titlePrefix();
            this.titleSuffix = style.titleSuffix();
            this.itemsPerPage = style.itemsPerPage();
            this.pageSelectorCount = style.pageSelectorCount();
            this.pageSelectorStyle = style.pageSelectorStyle();
        }

        @Override
        public BuilderImpl widthProvider(final TextWidthProvider widthProvider) {
            Objects.requireNonNull(widthProvider, "widthProvider");
            this.widthProvider = widthProvider;
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
        public Builder itemsPerPage(@Positive final int itemsPerPage) {
            if (itemsPerPage < 1) throw new IllegalArgumentException("itemsPerPage must be greater than zero");
            this.itemsPerPage = itemsPerPage;
            return this;
        }

        @Override
        public Builder pageSelectorCount(@IntRange(from = 2) final int pageSelectorCount) {
            if (pageSelectorCount < 2) throw new IllegalArgumentException("pageSelectorCount must be greater than one");
            this.pageSelectorCount = pageSelectorCount;
            return this;
        }

        @Override
        public Builder pageSelectorStyle(final JoinConfiguration pageSelectorStyle) {
            this.pageSelectorStyle = Objects.requireNonNull(pageSelectorStyle, "pageSelectorStyle");
            return this;
        }

        @Override
        public PaginatedListStyleImpl build() {
            return new PaginatedListStyleImpl(
                    this.widthProvider,
                    this.spacingGlyph,
                    this.titlePrefix,
                    this.titleSuffix,
                    this.itemsPerPage,
                    this.pageSelectorCount,
                    this.pageSelectorStyle);
        }
    }
}
