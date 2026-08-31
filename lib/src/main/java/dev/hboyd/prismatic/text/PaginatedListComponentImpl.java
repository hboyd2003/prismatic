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

import dev.hboyd.chasm.UIContainer;
import dev.hboyd.chasm.text.ComponentSpacer;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.VirtualComponent;
import net.kyori.adventure.text.VirtualComponentRenderer;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public final class PaginatedListComponentImpl implements PaginatedListComponent {
    static final PaginatedListComponentImpl EMPTY = new PaginatedListComponentImpl(Component.empty(),
            PaginatedListStyleImpl.DEFAULT,
            IPaginatedListComponentItemFactory.empty());

    private final Component title;
    private final PaginatedListStyle style;
    private final IPaginatedListComponentItemFactory itemFactory;

    PaginatedListComponentImpl(final Component title,
                               final PaginatedListStyle style,
                               final IPaginatedListComponentItemFactory itemFactory) {
        this.title = title;
        this.style = style;
        this.itemFactory = itemFactory;
    }

    @Override
    public PaginatedListComponent title(final Component title) {
        if (this.title.equals(title)) return this;

        return new PaginatedListComponentImpl(Objects.requireNonNull(title, "itemFactory"),
                this.style,
                this.itemFactory);
    }

    @Override
    public Component title() {
        return this.title;
    }

    @Override
    public PaginatedListComponent style(final PaginatedListStyle style) {
        if (style == this.style) return this;

        return new PaginatedListComponentImpl(this.title, Objects.requireNonNull(style, "style"), this.itemFactory);
    }

    @Override
    public PaginatedListStyle style() {
        return this.style;
    }

    @Override
    public PaginatedListComponent itemFactory(final IPaginatedListComponentItemFactory itemFactory) {
        if (this.itemFactory.equals(itemFactory)) return this;

        return new PaginatedListComponentImpl(this.title,
                this.style,
                Objects.requireNonNull(itemFactory, "itemFactory"));
    }

    @Override
    public IPaginatedListComponentItemFactory itemFactory() {
        return this.itemFactory;
    }

    @Override
    public void sendAsMessage(final Audience audience) {
        this.sendAsMessage(0, audience);
    }

    @Override
    public void sendAsMessage(final int page, final Audience audience) {
        audience.sendMessage(this.render(page, audience));
    }

    @Override
    public Component asComponent() {
        return this.render();
    }

    @Override
    public Component render() {
        return this.render(Audience.empty());
    }

    @Override
    public Component render(final Audience audience) {
        return this.render(0, audience);
    }

    @Override
    public Component render(final int page) {
        return this.render(page, Audience.empty());
    }

    @Override
    public Builder toBuilder() {
        return new BuilderImpl(this);
    }

    @Override
    public Component render(final int page, final Audience audience) {
        if (page < 0) throw new IllegalArgumentException("Page number must be greater than zero");

        final ComponentBuilder<TextComponent, TextComponent.Builder> builder = Component.text();

        builder.append(ComponentSpacer.alignCenter(this.style.titlePrefix()
                        .append(this.title)
                        .append(this.style.titleSuffix()),
                this.style.spacingGlyph(),
                this.style.widthProvider(),
                UIContainer.CHAT.width(),
                Locale.getDefault())); // TODO: derive locale from audience
        final int firstItem = this.style.itemsPerPage() * page;

        int lastItem = firstItem + this.style.itemsPerPage();
        while (!this.itemFactory.isAvailable(lastItem, audience))
            lastItem--;

        // Add items
        for (int i = firstItem; i <= lastItem; i++) {
            final Component item = this.itemFactory.get(i, audience).asComponent();
            builder.appendNewline().append(this.transformVirtualComponentCallback(item, page));
        }

        if (this.itemFactory.isAvailable(this.style.itemsPerPage() + 1, audience))
            builder.appendNewline().append(this.buildPageSelector(page, audience));
        else // If we have more items than can fit on one page
            builder.appendNewline()
                    .append(ComponentSpacer.alignCenter(Component.empty(),
                            this.style.spacingGlyph(),
                            this.style.widthProvider(),
                            UIContainer.CHAT.width(),
                            Locale.getDefault()));

        return builder.build();
    }

    private Component transformVirtualComponentCallback(Component component, final int page) {
        if (component instanceof final VirtualComponent virtualComponent
                && virtualComponent.contextType() == PaginatedListRedisplayClickCallbackBuilderImpl.RedisplayContext.class) {
            //noinspection unchecked
            component = ((VirtualComponentRenderer<PaginatedListRedisplayClickCallbackBuilderImpl.RedisplayContext>) virtualComponent.renderer())
                    .apply(new PaginatedListRedisplayClickCallbackBuilderImpl.RedisplayContext(this, page))
                    .asComponent()
                    .children(component.children());
        }

        final List<Component> newChildren = component.children().stream()
                .map(child -> this.transformVirtualComponentCallback(child, page))
                .toList();

        return component.children(newChildren);
    }

    private Component buildPageSelector(final int page, final Audience audience) {
        int lastPage = page;
        for (int i = page + this.style.pageSelectorCount(); i > page; i--) {
            if (this.itemFactory.isAvailable(this.style.itemsPerPage() * i, audience)) {
                lastPage = i;
                break;
            }
        }

        final int minPage = Math.max(Math.min(page - (this.style.pageSelectorCount() / 2),
                lastPage - this.style.pageSelectorCount()), 0);
        final int maxPage = Math.min(lastPage, minPage + this.style.pageSelectorCount());

        final List<Component> pageSelectors = new ArrayList<>();
        for (int i = minPage; i <= maxPage; i++) {
            TextComponent pageComponent = Component.text(i);

            if (i == page)
                pageComponent = pageComponent.decorate(TextDecoration.BOLD); // Selector for current page is always bold
            else {
                final int finalI = i;
                pageComponent = pageComponent.clickEvent(ClickEvent.callback(callbackAudience -> callbackAudience.sendMessage(
                        this.render(finalI, callbackAudience))));
            }

            pageSelectors.add(pageComponent);
        }

        return ComponentSpacer.alignCenter(Component.join(this.style.pageSelectorStyle(), pageSelectors),
                this.style.spacingGlyph(),
                this.style.widthProvider(),
                UIContainer.CHAT.width(),
                Locale.getDefault());
    }

    static final class BuilderImpl implements PaginatedListComponent.Builder {
        private Component title;
        private PaginatedListStyle style;
        private IPaginatedListComponentItemFactory itemFactory;

        private BuilderImpl(final PaginatedListComponentImpl paginatedListComponent) {
            this.title = paginatedListComponent.title;
            this.style = paginatedListComponent.style;
            this.itemFactory = paginatedListComponent.itemFactory;
        }

        @Override
        public BuilderImpl title(final Component title) {
            this.title = Objects.requireNonNull(title, "title");
            return this;
        }

        @Override
        public BuilderImpl style(final PaginatedListStyle style) {
            this.style = Objects.requireNonNull(style, "style");
            return this;
        }

        @Override
        public Builder itemFactory(final IPaginatedListComponentItemFactory itemFactory) {
            this.itemFactory = Objects.requireNonNull(itemFactory, "itemFactory");
            return this;
        }

        @Override
        public PaginatedListComponentImpl build() {
            return new PaginatedListComponentImpl(this.title, this.style, this.itemFactory);
        }
    }
}
