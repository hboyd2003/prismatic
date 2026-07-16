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

package dev.hboyd.prismatic.paper.brigadier.argument;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.argument.CustomArgumentType;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * An argument type that allows for repeating an argument.
 *
 * @param <T> argument type
 */
public class RepeatingArgumentType<T> implements CustomArgumentType<List<T>, String> {
    ArgumentType<T> type;

    /**
     * Construct a repeating argument type with the given argument type.
     *
     * @param type repeated argument type
     */
    public RepeatingArgumentType(final ArgumentType<T> type) {
        this.type = type;
    }

    @Override
    public List<T> parse(final StringReader reader) throws CommandSyntaxException {
        return this.parseAll(reader);
    }

    @Override
    public <S> List<T> parse(final StringReader reader, final S source) throws CommandSyntaxException {
        return this.parseAll(reader, source);
    }

    @Override
    public ArgumentType<String> getNativeType() {
        return StringArgumentType.greedyString();
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(final CommandContext<S> context,
                                                              final SuggestionsBuilder builder) {
        final String remaining = builder.getRemaining();
        final StringReader reader = new StringReader(remaining);

        int lastCursor = reader.getCursor();
        final List<String> args = new ArrayList<>();
        try {
            while (reader.canRead()) {
                lastCursor = reader.getCursor();
                this.type.parse(reader);

                if (CommandDispatcher.ARGUMENT_SEPARATOR_CHAR != reader.read())
                    break;

                args.add(remaining.substring(lastCursor, reader.getCursor()));
            }
        } catch (final CommandSyntaxException _) {
        }

        if (!reader.canRead()) return Suggestions.empty();

        final SuggestionsBuilder offsetBuilder = builder.createOffset(lastCursor);
        return this.type.listSuggestions(context, offsetBuilder.restart())
                .thenApply(suggestions -> {
                    suggestions.getList().stream()
                            .map(Suggestion::getText)
                            .filter(suggestion -> !args.contains(suggestion))
                            .forEach(offsetBuilder::suggest);

                    return offsetBuilder.build();
                });
    }

    private <S> List<T> parseAll(final StringReader reader, final S source) throws CommandSyntaxException {
        final List<T> parsed = new ArrayList<>();
        while (reader.canRead()) {
            parsed.add(this.type.parse(reader, source));
        }
        return parsed;
    }

    private List<T> parseAll(final StringReader reader) throws CommandSyntaxException {
        final List<T> parsed = new ArrayList<>();
        while (reader.canRead()) {
            parsed.add(this.type.parse(reader));
        }
        return parsed;
    }
}
