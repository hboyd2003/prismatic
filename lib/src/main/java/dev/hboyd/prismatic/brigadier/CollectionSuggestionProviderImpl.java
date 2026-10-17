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

package dev.hboyd.prismatic.brigadier;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default {@link CollectionSuggestionProvider} implementation.
 *
 */
final class CollectionSuggestionProviderImpl<C, T> implements CollectionSuggestionProvider<C, T> {
    private static final Pattern GROUPED_QUOTES = Pattern.compile("^(\"?)(.*?)(\"?)$");

    private final Supplier<Collection<T>> collectionProvider;
    private final Function<? super T, String> nameProvider;
    private final @Nullable Function<? super T, @Nullable Message> tooltipProvider;
    private final @Nullable Predicate<? super T> filter;
    private final boolean addQuotes;

    private CollectionSuggestionProviderImpl(final Supplier<Collection<T>> collectionProvider,
                                             final Function<? super T, String> nameProvider,
                                             @Nullable final Function<? super T, @Nullable Message> tooltipProvider,
                                             @Nullable final Predicate<? super T> filter,
                                             final boolean addQuotes) {
        this.collectionProvider = collectionProvider;
        this.nameProvider = nameProvider;
        this.tooltipProvider = tooltipProvider;
        this.filter = filter;
        this.addQuotes = addQuotes;
    }

    static <C, T> CollectionSuggestionProvider<C, T> empty() {
        return new CollectionSuggestionProviderImpl<>(Collections::emptyList,
                String::valueOf,
                null,
                null,
                true);
    }

    @Override
    public SuggestionsBuilder suggest(final SuggestionsBuilder suggestionsBuilder) {
        Objects.requireNonNull(suggestionsBuilder);

        final Matcher inputMatcher = GROUPED_QUOTES.matcher(suggestionsBuilder.getRemaining());
        inputMatcher.matches();
        final String prefix = inputMatcher.group(1);
        final String input = inputMatcher.group(2);
        final String suffix = inputMatcher.group(3);

        for (final T object : this.collectionProvider.get()) {
            if (object == null) continue;

            String name = this.nameProvider.apply(object);
            if (name == null) continue;

            final String nameLowercase = name.toLowerCase(Locale.ROOT);
            if (!nameLowercase.startsWith(input.toLowerCase(Locale.ROOT)) || (this.filter != null && !this.filter.test(object))) {
                continue;
            }

            if (this.addQuotes && name.chars().anyMatch(integer -> !StringReader.isAllowedInUnquotedString((char) integer))) {
                name = "\"" + name + "\"";
            } else {
                name = prefix + name + suffix;
            }

            if (this.tooltipProvider != null)
                suggestionsBuilder.suggest(name, this.tooltipProvider.apply(object));
            else suggestionsBuilder.suggest(name);
        }

        return suggestionsBuilder;
    }

    @Override
    public Supplier<Collection<T>> collectionProvider() {
        return this.collectionProvider;
    }

    @Override
    public Function<? super T, String> nameProvider() {
        return this.nameProvider;
    }

    @Override
    public @Nullable Function<? super T, @Nullable Message> tooltipProvider() {
        return this.tooltipProvider;
    }

    @Override
    public @Nullable Predicate<? super T> filter() {
        return this.filter;
    }

    @Override
    public boolean addQuotes() {
        return this.addQuotes;
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        final CollectionSuggestionProviderImpl<?, ?> that = (CollectionSuggestionProviderImpl<?, ?>) obj;
        return Objects.equals(this.collectionProvider, that.collectionProvider) &&
                Objects.equals(this.nameProvider, that.nameProvider) &&
                Objects.equals(this.tooltipProvider, that.tooltipProvider) &&
                Objects.equals(this.filter, that.filter) &&
                this.addQuotes == that.addQuotes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.collectionProvider,
                this.nameProvider,
                this.tooltipProvider,
                this.filter,
                this.addQuotes);
    }

    @Override
    public String toString() {
        return "CollectionSuggestionProviderImpl[" +
                "collectionProvider=" + this.collectionProvider + ", " +
                "nameProvider=" + this.nameProvider + ", " +
                "tooltipProvider=" + this.tooltipProvider + ", " +
                "filter=" + this.filter + ", " +
                "addQuotes=" + this.addQuotes + ']';
    }

    @Override
    public Builder<C, T> toBuilder() {
        return new BuilderImpl<>(this);
    }

    /**
     * Default {@link Builder} implementation.
     */
    static final class BuilderImpl<C, T> implements Builder<C, T> {
        private Supplier<Collection<T>> collectionProvider;
        private Function<? super T, String> nameProvider;
        private @Nullable Function<? super T, @Nullable Message> tooltipProvider;
        private @Nullable Predicate<? super T> filter;
        private boolean addQuotes;

        private BuilderImpl(final CollectionSuggestionProviderImpl<C, T> provider) {
            this.collectionProvider = provider.collectionProvider;
            this.nameProvider = provider.nameProvider;
            this.tooltipProvider = provider.tooltipProvider;
            this.filter = provider.filter;
            this.addQuotes = provider.addQuotes;
        }

        @Override
        public Builder<C, T> collectionProvider(final Supplier<Collection<T>> collectionProvider) {
            this.collectionProvider = Objects.requireNonNull(collectionProvider, "collectionProvider");
            return this;
        }

        @Override
        public Builder<C, T> collection(final Collection<T> collection) {
            final List<T> copy = List.copyOf(collection);
            this.collectionProvider = () -> copy;
            return this;
        }

        @Override
        public Builder<C, T> nameProvider(final Function<? super T, String> nameProvider) {
            this.nameProvider = Objects.requireNonNull(nameProvider);
            return this;
        }

        @Override
        public Builder<C, T> tooltipProvider(final @Nullable Function<? super T, @Nullable Message> tooltipProvider) {
            this.tooltipProvider = tooltipProvider;
            return this;
        }

        @Override
        public Builder<C, T> filter(final @Nullable Predicate<? super T> filter) {
            this.filter = filter;
            return this;
        }

        @Override
        public Builder<C, T> addQuotes(final boolean addQuotes) {
            this.addQuotes = addQuotes;
            return this;
        }

        @Override
        public CollectionSuggestionProvider<C, T> build() {
            return new CollectionSuggestionProviderImpl<>(this.collectionProvider,
                    this.nameProvider,
                    this.tooltipProvider,
                    this.filter,
                    this.addQuotes);
        }

    }

}
