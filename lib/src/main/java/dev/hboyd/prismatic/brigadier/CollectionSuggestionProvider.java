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
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.kyori.adventure.builder.AbstractBuilder;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A suggestion provider which generates suggestions based off of a given collection of objects.
 *
 * <p>Some examples of possible usage:</p>
 * <ul>
 *   <li>CollectionSuggestionProvider.builder().collection(items).nameProvider(Item::name).build().getSuggestions(context, builder)</li>
 *   <li>CollectionSuggestionProvider.builder().collection(items).nameProvider(Item::name).build().suggest(builder)</li>
 *   <li>CollectionSuggestionProvider.builder().collectionProvider(registry::values).build().suggest(builder)</li>
 * </ul>
 *
 * @param <C> the command context type
 * @param <T> the type of elements suggested
 */
public sealed interface CollectionSuggestionProvider<C, T> extends SuggestionProvider<C> permits CollectionSuggestionProviderImpl {

    /**
     * Get the provider of the collection suggested from.
     *
     * @return the collection provider
     */
    @Contract(pure = true)
    Supplier<Collection<T>> collectionProvider();

    /**
     * Get the provider of the suggested name for an element.
     *
     * @return the name provider
     */
    @Contract(pure = true)
    Function<? super T, String> nameProvider();

    /**
     * Get the provider of the tooltip for the suggestion.
     *
     * @return the tooltip provider
     */
    @Contract(pure = true)
    @Nullable Function<? super T, @Nullable Message> tooltipProvider();

    /**
     * Get the predicate filter which elements must pass to be suggested.
     *
     * @return the filter
     */
    @Contract(pure = true)
    @Nullable Predicate<? super T> filter();

    /**
     * Get whether suggestions should be surrounded by quotes when it has a character which isn't allowed in an unquoted
     * string.
     *
     * @return the add quotes value
     */
    @Contract(pure = true)
    boolean addQuotes();

    @Override
    default CompletableFuture<Suggestions> getSuggestions(final @Nullable CommandContext<C> context,
                                                          final SuggestionsBuilder builder) {
        this.suggest(builder);

        return builder.buildFuture();
    }

    /**
     * Apply every matching suggestion to the given suggestions builder.
     *
     * <p>Elements are matched against the current input, ignoring case, using the name given by the
     * {@link #nameProvider()}.</p>
     *
     * @param suggestionsBuilder the suggestions builder
     * @return the suggestions builder
     */
    @Contract(value = "_ -> param1", mutates = "param")
    SuggestionsBuilder suggest(final SuggestionsBuilder suggestionsBuilder);

    /**
     * Create a builder from this collection suggestion provider.
     *
     * @return a builder
     */
    @Contract(value = " -> new", pure = true)
    Builder<C, T> toBuilder();

    /**
     * Create a new builder for a collection suggester.
     *
     * @param <C> the command context type
     * @param <T> the type of elements to suggest
     * @return a new builder
     */
    @Contract(value = " -> new", pure = true)
    static <C, T> Builder<C, T> builder() {
        return CollectionSuggestionProviderImpl.<C, T>empty().toBuilder();
    }

    /**
     * A builder for {@link CollectionSuggestionProvider}s.
     *
     * @param <C> the command context type
     * @param <T> the type of elements to suggest
     */
    sealed interface Builder<C, T> extends AbstractBuilder<CollectionSuggestionProvider<C, T>> permits CollectionSuggestionProviderImpl.BuilderImpl {

        /**
         * Set the provider of the collection to suggest from.
         *
         * @param collectionProvider the collection provider
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder<C, T> collectionProvider(final Supplier<Collection<T>> collectionProvider);

        /**
         * Set the collection provider to always use a copy of the given collection.
         *
         * @param collection a collection
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder<C, T> collection(final Collection<T> collection);

        /**
         * Set the provider of the suggested name for an element.
         *
         * @param nameProvider the name provider
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder<C, T> nameProvider(final Function<? super T, String> nameProvider);

        /**
         * Set the provider of the tooltip for a suggested element.
         *
         * @param tooltipProvider the tooltip provider
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder<C, T> tooltipProvider(final @Nullable Function<? super T, @Nullable Message> tooltipProvider);

        /**
         * Set the predicate element filter which elements must pass to be suggested.
         *
         * @param filter the filter
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder<C, T> filter(final @Nullable Predicate<? super T> filter);

        /**
         * Set whether suggestions should be surrounded by quotes when it has a character which isn't allowed in an
         * unquoted string.
         *
         * @param addQuotes the add quotes value
         * @return this builder
         */
        @Contract(value = "_ -> this", mutates = "this")
        Builder<C, T> addQuotes(final boolean addQuotes);
    }

}
