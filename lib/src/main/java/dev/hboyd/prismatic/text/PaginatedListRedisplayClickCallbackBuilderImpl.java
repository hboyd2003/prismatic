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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.VirtualComponent;
import net.kyori.adventure.text.VirtualComponentRenderer;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.event.ClickEvent;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public final class PaginatedListRedisplayClickCallbackBuilderImpl implements PaginatedListRedisplayClickCallbackBuilder {
    private Component component;
    private String fallback;
    private @Nullable ClickCallback<Audience> clickCallback;
    private ClickCallback.Options callbackOptions;

    PaginatedListRedisplayClickCallbackBuilderImpl() {
        this.component = Component.empty();
        this.fallback = "";
        this.clickCallback = null;
        this.callbackOptions = ClickCallback.Options.builder().build();
    }

    @Override
    public PaginatedListRedisplayClickCallbackBuilder component(final ComponentLike component) {
        this.component = Objects.requireNonNull(component, "component").asComponent().compact();
        return this;
    }

    @Override
    public PaginatedListRedisplayClickCallbackBuilder fallback(final String fallback) {
        this.fallback = Objects.requireNonNull(fallback, "fallback");
        return this;
    }

    @Override
    public PaginatedListRedisplayClickCallbackBuilder clickCallback(final @Nullable ClickCallback<Audience> clickCallback) {
        this.clickCallback = clickCallback;
        return this;
    }

    @Override
    public PaginatedListRedisplayClickCallbackBuilder callbackOptions(final ClickCallback.Options options) {
        this.callbackOptions = Objects.requireNonNull(options, "callbackOptions");
        return this;
    }

    @Override
    public VirtualComponent build() {
        final RedisplayRenderer redisplayRenderer = new RedisplayRenderer(this.component,
                this.fallback,
                this.clickCallback,
                this.callbackOptions);
        return Component.virtual(RedisplayContext.class, redisplayRenderer, this.component.style());
    }

    static final class RedisplayRenderer implements VirtualComponentRenderer<RedisplayContext> {
        private final Component component;
        private final String fallback;
        private final @Nullable ClickCallback<Audience> clickCallback;
        private final ClickCallback.Options callbackOptions;

        RedisplayRenderer(final Component component,
                          final String fallback,
                          @Nullable final ClickCallback<Audience> clickCallback,
                          final ClickCallback.Options callbackOptions) {

            this.component = component;
            this.fallback = fallback;
            this.clickCallback = clickCallback;
            this.callbackOptions = callbackOptions;
        }

        @Override
        public @UnknownNullability ComponentLike apply(final RedisplayContext context) {
            return this.component.clickEvent(
                    ClickEvent.callback(callbackAudience -> {
                        if (this.clickCallback != null) this.clickCallback.accept(callbackAudience);

                        callbackAudience.sendMessage(context.component.render(context.page));
                    }, this.callbackOptions));
        }

        @Override
        public String fallbackString() {
            return this.fallback;
        }
    }


    record RedisplayContext(PaginatedListComponentImpl component, int page) {}
}
