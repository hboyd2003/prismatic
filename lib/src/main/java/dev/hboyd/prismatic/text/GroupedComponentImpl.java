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

import dev.hboyd.chasm.text.ComponentSpacer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;

import java.util.Objects;

public final class GroupedComponentImpl implements GroupedComponent {
    static final GroupedComponent EMPTY = new GroupedComponentImpl(Component.empty(),
            Component.empty(),
            GroupedComponentStyle.style());

    private final ComponentLike title;
    private final ComponentLike content;
    private final GroupedComponentStyle style;

    GroupedComponentImpl(final ComponentLike title, final ComponentLike content, final GroupedComponentStyle style) {
        this.title = title;
        this.content = content;
        this.style = style;
    }

    @Override
    public ComponentLike title() {
        return this.title;
    }

    @Override
    public GroupedComponent title(final ComponentLike title) {
        if (this.title.equals(title)) return this;

        return new GroupedComponentImpl(title, this.content, this.style);
    }

    @Override
    public ComponentLike content() {
        return this.content;
    }

    @Override
    public GroupedComponent content(final ComponentLike content) {
        if (this.content.equals(content)) return this;

        return new GroupedComponentImpl(this.title, content, this.style);
    }

    @Override
    public GroupedComponentStyle style() {
        return this.style;
    }

    @Override
    public GroupedComponent style(final GroupedComponentStyle style) {
        if (this.style.equals(style)) return this;

        return new GroupedComponentImpl(this.title, this.content, style);
    }

    @Override
    public Component asComponent() {
        return Component.text()
                .append(ComponentSpacer.alignCenter(Component.text()
                                .append(this.style.titlePrefix())
                                .append(this.title)
                                .append(this.style.titleSuffix())
                                .build(),
                        this.style.spacingGlyph(),
                        this.style.widthProvider()))
                .appendNewline()
                .append(this.content)
                .appendNewline()
                .append(ComponentSpacer.alignCenter(Component.empty(),
                        this.style.spacingGlyph(),
                        this.style.widthProvider()))
                .build();
    }

    @Override
    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    static final class BuilderImpl implements GroupedComponent.Builder {
        private ComponentLike title;
        private ComponentLike content;
        private GroupedComponentStyle style;

        private BuilderImpl(final GroupedComponentImpl groupedComponent) {
            this.title = groupedComponent.title;
            this.content = groupedComponent.content;
            this.style = groupedComponent.style;
        }

        @Override
        public Builder title(final ComponentLike title) {
            this.title = Objects.requireNonNull(title, "title");
            return this;
        }

        @Override
        public Builder content(final ComponentLike content) {
            this.content = Objects.requireNonNull(content, "content");
            return this;
        }

        @Override
        public Builder style(final GroupedComponentStyle style) {
            this.style = Objects.requireNonNull(style, "style");
            return this;
        }

        @Override
        public GroupedComponent build() {
            return new GroupedComponentImpl(this.title, this.content, this.style);
        }
    }
}
