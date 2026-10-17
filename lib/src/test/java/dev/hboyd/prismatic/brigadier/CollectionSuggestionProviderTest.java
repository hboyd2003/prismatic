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

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

class CollectionSuggestionProviderTest {

    private static final Message TOOLTIP = new TestMessage("A tooltip");

    @Nested
    class Builder {

        @Test
        void defaultsUseCollectionContentAndToStringNames() {
            final CollectionSuggestionProvider<Object, String> provider = CollectionSuggestionProvider.<Object, String>builder()
                    .collection(List.of("player", "mob"))
                    .build();

            assertNull(provider.tooltipProvider());
            assertNull(provider.filter());
            assertTrue(provider.addQuotes());
            assertEquals(List.of("mob", "player"),
                    suggestedTexts(provider.suggest(new SuggestionsBuilder("", 0))));
        }

        @Test
        void collectionIsDefensivelyCopied() {
            final List<String> source = new ArrayList<>(List.of("player"));
            final CollectionSuggestionProvider<Object, String> provider = CollectionSuggestionProvider.<Object, String>builder()
                    .collection(source)
                    .build();

            source.add("mob");
            assertEquals(List.of("player"),
                    suggestedTexts(provider.suggest(new SuggestionsBuilder("", 0))));
        }

        @Test
        void collectionProviderIsPassedThroughAndReadLazily() {
            final List<String> source = new ArrayList<>(List.of("player"));
            final Supplier<Collection<String>> listProvider = () -> source;
            final CollectionSuggestionProvider<Object, String> provider = CollectionSuggestionProvider.<Object, String>builder()
                    .collectionProvider(listProvider)
                    .build();

            assertSame(listProvider, provider.collectionProvider());

            source.add("mob");
            assertEquals(List.of("mob", "player"),
                    suggestedTexts(provider.suggest(new SuggestionsBuilder("", 0))));
        }

        @Test
        void accessorsReflectConfiguration() {
            final Function<Object, String> nameProvider = Object::toString;
            final Function<Object, Message> tooltipProvider = o -> TOOLTIP;
            final Predicate<String> filter = name -> true;

            final CollectionSuggestionProvider<Object, String> provider = CollectionSuggestionProvider.<Object, String>builder()
                    .collection(List.of("player"))
                    .nameProvider(nameProvider)
                    .tooltipProvider(tooltipProvider)
                    .filter(filter)
                    .addQuotes(false)
                    .build();

            assertSame(nameProvider, provider.nameProvider());
            assertSame(tooltipProvider, provider.tooltipProvider());
            assertSame(filter, provider.filter());
            assertFalse(provider.addQuotes());
        }

        @Test
        void fluentMethodsReturnSameBuilder() {
            final CollectionSuggestionProvider.Builder<Object, String> builder = CollectionSuggestionProvider.builder();

            assertSame(builder, builder.collection(List.of()));
            assertSame(builder, builder.collectionProvider(Collections::emptyList));
            assertSame(builder, builder.nameProvider(String::valueOf));
            assertSame(builder, builder.tooltipProvider(null));
            assertSame(builder, builder.filter(null));
            assertSame(builder, builder.addQuotes(false));
        }
    }

    @Nested
    class Suggest {

        @Test
        void suggestionsMatchInputPrefix() {
            final CollectionSuggestionProvider<Object, String> provider = providerWith("player", "player 1", "mob");

            assertEquals(List.of("\"player 1\"", "player"),
                    suggestedTexts(provider.suggest(new SuggestionsBuilder("pla", 0))));
        }

        @Test
        void matchingIsCaseInsensitive() {
            final CollectionSuggestionProvider<Object, String> provider = providerWith("player 1", "PLAyer");

            assertEquals(List.of("\"player 1\"", "PLAyer"),
                    suggestedTexts(provider.suggest(new SuggestionsBuilder("PLAy", 0))));
        }

        @Test
        void exactMatchIsProposedToTheBuilder() {
            final CollectionSuggestionProvider<Object, String> provider = providerWith("player", "mob");
            final TestSuggestionsBuilder builder = new TestSuggestionsBuilder("player");

            provider.suggest(builder);

            assertEquals(List.of("player"), builder.proposed);
        }

        @Test
        void dispatcherDropsSuggestionIdenticalToRemaining() {
            final CommandDispatcher<Object> dispatcher = dispatcher(providerWith("player", "mob"));

            assertEquals(List.of(), dispatchedTexts(dispatcher, "teleport player"));
        }

        @Test
        void suggestionsFlowThroughCommandDispatcher() {
            final CommandDispatcher<Object> dispatcher =
                    dispatcher(providerWith("player 1", "player 2", "world"));

            assertEquals(List.of("\"player 1\"", "\"player 2\""),
                    dispatchedTexts(dispatcher, "teleport pla"));
        }

        @Test
        void openQuoteIsPreservedAndNamesAreWrapped() {
            final CollectionSuggestionProvider<Object, String> provider = providerWith("player", "player 1");

            assertEquals(List.of("\"player", "\"player 1\""),
                    suggestedTexts(provider.suggest(new SuggestionsBuilder("\"pla", 0))));
        }

        @Test
        void closedQuoteIsPreserved() {
            final CollectionSuggestionProvider<Object, String> provider = providerWith("player", "player 1");

            assertEquals(List.of("\"player 1\"", "\"player\""),
                    suggestedTexts(provider.suggest(new SuggestionsBuilder("\"pla\"", 0))));
        }

        @Test
        void duplicateSuggestionsAreCollapsed() {
            final CollectionSuggestionProvider<Object, String> provider = providerWith("player", "player", "mob");

            assertEquals(List.of("mob", "player"),
                    suggestedTexts(provider.suggest(new SuggestionsBuilder("", 0))));
        }

        @Test
        void allNamesSuggestedWhenRemainingIsEmpty() {
            final CollectionSuggestionProvider<Object, String> provider = providerWith("player", "mob");

            assertEquals(List.of("mob", "player"),
                    suggestedTexts(provider.suggest(new SuggestionsBuilder("teleport ", 9))));
        }

        @Test
        void customNameProviderIsUsed() {
            final CollectionSuggestionProvider<Object, String> provider = CollectionSuggestionProvider.<Object, String>builder()
                    .collection(List.of("a", "b"))
                    .nameProvider(name -> "prefix-" + name)
                    .build();

            assertEquals(List.of("prefix-a", "prefix-b"),
                    suggestedTexts(provider.suggest(new SuggestionsBuilder("", 0))));
        }

        @Test
        void filterExcludesElements() {
            final CollectionSuggestionProvider<Object, String> provider = CollectionSuggestionProvider.<Object, String>builder()
                    .collection(List.of("player", "mob"))
                    .filter(name -> name.startsWith("m"))
                    .build();

            assertEquals(List.of("mob"),
                    suggestedTexts(provider.suggest(new SuggestionsBuilder("", 0))));
        }

        @Test
        void tooltipsAreIncludedForAllSuggestionsByDefault() {
            final CollectionSuggestionProvider<Object, String> provider = CollectionSuggestionProvider.<Object, String>builder()
                    .collection(List.of("player", "player 1"))
                    .tooltipProvider(o -> TOOLTIP)
                    .build();

            final List<Suggestion> suggestions = provider.suggest(new SuggestionsBuilder("pla", 0))
                    .build()
                    .getList();

            assertSame(TOOLTIP, suggestionByText(suggestions, "player").getTooltip());
            assertSame(TOOLTIP, suggestionByText(suggestions, "\"player 1\"").getTooltip());
        }

        @Test
        void tooltipsAreOmittedWithoutProvider() {
            final CollectionSuggestionProvider<Object, String> provider = providerWith("player 1");

            final List<Suggestion> suggestions = provider.suggest(new SuggestionsBuilder("", 0))
                    .build()
                    .getList();

            assertNull(suggestionByText(suggestions, "\"player 1\"").getTooltip());
        }

        @Test
        void nullTooltipsAreOmittedPerElement() {
            final CollectionSuggestionProvider<Object, String> provider = CollectionSuggestionProvider.<Object, String>builder()
                    .collection(List.of("player 1", "player 2"))
                    .tooltipProvider(o -> "player 1".equals(o) ? TOOLTIP : null)
                    .build();

            final List<Suggestion> suggestions = provider.suggest(new SuggestionsBuilder("", 0))
                    .build()
                    .getList();

            assertSame(TOOLTIP, suggestionByText(suggestions, "\"player 1\"").getTooltip());
            assertNull(suggestionByText(suggestions, "\"player 2\"").getTooltip());
        }

        @Test
        void suggestReturnsTheSameBuilder() {
            final SuggestionsBuilder builder = new SuggestionsBuilder("pla", 0);
            assertSame(builder, providerWith("player").suggest(builder));
        }

        @Test
        void usableAsBrigadierSuggestionProvider() {
            final CollectionSuggestionProvider<Object, String> provider = providerWith("player");

            final SuggestionsBuilder builder = new SuggestionsBuilder("pla", 0);
            provider.getSuggestions(null, builder);
            assertEquals(List.of("player"), suggestedTexts(builder));
        }
    }

    private static CollectionSuggestionProvider<Object, String> providerWith(final String... names) {
        return CollectionSuggestionProvider.<Object, String>builder()
                .collection(List.of(names))
                .build();
    }

    private static CommandDispatcher<Object> dispatcher(final CollectionSuggestionProvider<Object, String> suggester) {
        final CommandDispatcher<Object> dispatcher = new CommandDispatcher<>();
        dispatcher.register(LiteralArgumentBuilder.literal("teleport")
                .then(RequiredArgumentBuilder.argument("target", StringArgumentType.greedyString())
                        .suggests(suggester)));
        return dispatcher;
    }

    private static List<String> dispatchedTexts(final CommandDispatcher<Object> dispatcher, final String input) {
        return dispatcher.getCompletionSuggestions(dispatcher.parse(input, new Object()))
                .join()
                .getList()
                .stream()
                .map(Suggestion::getText)
                .sorted()
                .toList();
    }

    private static List<String> suggestedTexts(final SuggestionsBuilder builder) {
        return builder.build().getList().stream()
                .map(Suggestion::getText)
                .sorted()
                .toList();
    }

    private static Suggestion suggestionByText(final List<Suggestion> suggestions, final String text) {
        return suggestions.stream()
                .filter(suggestion -> suggestion.getText().equals(text))
                .findFirst()
                .orElseThrow();
    }

    /**
     * Suggestion builder which doesn't discard anything unlike the default suggestion builder which discards
     * suggestions that match the remaining text.
     */
    private static final class TestSuggestionsBuilder extends SuggestionsBuilder {

        private final List<String> proposed = new ArrayList<>();

        private TestSuggestionsBuilder(final String input) {
            super(input, 0);
        }

        @Override
        public SuggestionsBuilder suggest(final String text) {
            this.proposed.add(text);
            return this;
        }

        @Override
        public SuggestionsBuilder suggest(final String text, final Message tooltip) {
            this.proposed.add(text);
            return this;
        }
    }

    private record TestMessage(String string) implements Message {

        @Override
        public String getString() {
            return this.string;
        }
    }
}
