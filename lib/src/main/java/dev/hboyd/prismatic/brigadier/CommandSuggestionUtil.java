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
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities for command suggestions.
 */
public final class CommandSuggestionUtil {
    private CommandSuggestionUtil() {
    }

    /**
     * Pattern that matches any non-alphanumeric ASCII character.
     */
    public static final Pattern NON_ASCII_ALPHANUMERIC = Pattern.compile("[^A-z|0-9]");

    /**
     * Matches text possibly surrounded by quotes.
     * Groups:
     * <ol>
     *     <li>Starting double-quote (if-any)</li>
     *     <li>Text</li>
     *     <li>Ending double-quote (if-any)</li>
     * </ol>
     */
    public static final Pattern GROUPED_QUOTES = Pattern.compile("^(\"?)(.*?)(\"?)$");

    /**
     * Convenience method. Same as
     * {@link #suggestFromList(SuggestionsBuilder, Collection, Function, Function, Predicate)} with the tooltip function
     * and filter predicate as {@code null}
     *
     * @param builder the {@link SuggestionsBuilder} used to accumulate suggestions and get user input
     * @param list    the collection of objects to suggest from
     * @param getName a function mapping each object to its string representation (used for matching and display)
     * @param <T>     the type of the elements in the provided collection
     * @see #suggestFromList(SuggestionsBuilder, Collection, Function, Function, Predicate)
     */
    public static <T> void suggestFromList(final SuggestionsBuilder builder,
                                           final Collection<T> list,
                                           final Function<T, String> getName) {
        suggestFromList(builder, list, getName, null, null);
    }

    /**
     * Convenience method. Same as
     * {@link #suggestFromList(SuggestionsBuilder, Collection, Function, Function, Predicate)} with the filter predicate
     * as {@code null}
     *
     * @param builder         the {@link SuggestionsBuilder} used to accumulate suggestions and get user input
     * @param list            the collection of objects to suggest from
     * @param nameFunction    a function mapping each object to its string representation (used for matching and
     *                        display)
     * @param tooltipFunction an optional function mapping each object to a {@link Message} used as the tooltip of the
     *                        suggestion; may be {@code null} if no tooltip should be shown
     * @param <T>             the type of the elements in the provided collection
     * @see #suggestFromList(SuggestionsBuilder, Collection, Function, Function, Predicate)
     */
    public static <T> void suggestFromList(final SuggestionsBuilder builder,
                                           final Collection<T> list,
                                           final Function<T, String> nameFunction,
                                           @Nullable final Function<T, Message> tooltipFunction) {
        suggestFromList(builder, list, nameFunction, tooltipFunction, null);
    }

    /**
     * Generate and add suggestions to a {@link SuggestionsBuilder} from a given collection of objects.
     *
     * <p>This method filters a list based on user input from {@link SuggestionsBuilder} and an optional filter.
     * It builds the suggestion entry based on the input quotes and if quotes are needed for a given result. An optional
     * tooltip function can be provided.</p>
     *
     * <p>Usage:
     * {@snippet lang = java:
     * suggestFromList(builder, List.of("player", "player 1"),
     *     s -> s,
     *     null,
     *     s -> true);
     *
     * // If the user has typed: pla
     * // Suggestions: [player, "player 1"]
     *
     * // If the user has already started with a quote: "pla
     * // Suggestions: ["player, "player 1"]
     * }
     * </p>
     *
     * @param builder         the {@link SuggestionsBuilder} used to accumulate suggestion results and get user input
     * @param list            the collection of objects to suggest from
     * @param nameFunction    a function mapping each object to its string representation (used for matching and
     *                        display)
     * @param tooltipFunction an optional function mapping each object to a {@link Message} used as the tooltip of the
     *                        suggestion; may be {@code null} if no tooltip should be shown
     * @param filter          an optional predicate used to determine whether an object should be included in the
     *                        suggestions
     * @param <T>             the type of the elements in the provided collection
     */
    public static <T> void suggestFromList(final SuggestionsBuilder builder,
                                           final Collection<T> list,
                                           final Function<T, String> nameFunction,
                                           @Nullable final Function<T, Message> tooltipFunction,
                                           @Nullable final Predicate<T> filter) {
        final Matcher inputMatcher = GROUPED_QUOTES.matcher(builder.getRemaining());
        inputMatcher.find();
        final String prefix = inputMatcher.group(1);
        final String input = inputMatcher.group(2);
        final String suffix = inputMatcher.group(3);

        for (final T object : list) {
            final String name = nameFunction.apply(object);
            final boolean filterResult = filter == null || filter.test(object);
            if (name.startsWith(input) && filterResult) {
                String tempPrefix = prefix;
                String tempSuffix = suffix;
                if (NON_ASCII_ALPHANUMERIC.matcher(name).find()) {
                    tempPrefix = "\"";
                    tempSuffix = "\"";
                }

                if (tooltipFunction != null)
                    builder.suggest(tempPrefix + name + tempSuffix, tooltipFunction.apply(object));
                else builder.suggest(tempPrefix + name + tempSuffix);
            }
        }
    }
}
