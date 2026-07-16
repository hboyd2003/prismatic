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

import com.destroystokyo.paper.profile.PlayerProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import dev.hboyd.prismatic.paper.MessageUtil;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.ArgumentResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.PlayerProfileListResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.SelectorArgumentResolver;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * An {@link ArgumentResolver} that's capable of resolving
 * a player selector argument value using a {@link CommandSourceStack}.
 */
public class OfflinePlayerArgumentTypeResolver implements SelectorArgumentResolver<Collection<OfflinePlayer>> {
    private static final SimpleCommandExceptionType UNKNOWN_PLAYER_FOUND_EXCEPTION =
            new SimpleCommandExceptionType(MessageUtil.translatableMessage("argument.player.unknown"));
    private static final SimpleCommandExceptionType TOO_MANY_SELECTED_EXCEPTION =
            new SimpleCommandExceptionType(MessageUtil.translatableMessage("argument.player.toomany"));

    private final PlayerProfileListResolver playerProfileListResolver;
    private final Collection<? super OfflinePlayer> filteredOfflinePlayers;
    private final boolean singleSelector;
    private final @Nullable SimpleCommandExceptionType notMatchedException;

    @Contract(pure = true)
    OfflinePlayerArgumentTypeResolver(final PlayerProfileListResolver playerProfileListResolver,
                                             final Collection<? super OfflinePlayer> filteredOfflinePlayers,
                                             final boolean singleSelector,
                                             @Nullable final SimpleCommandExceptionType notMatchedException) {
        this.playerProfileListResolver = playerProfileListResolver;
        this.filteredOfflinePlayers = filteredOfflinePlayers;
        this.singleSelector = singleSelector;
        this.notMatchedException = notMatchedException;
    }

    @Override
    public Collection<OfflinePlayer> resolve(final CommandSourceStack sourceStack) throws CommandSyntaxException {
        final Set<UUID> offlinePlayerUUIDs = this.filteredOfflinePlayers.stream()
                .map(OfflinePlayer.class::cast)
                .map(OfflinePlayer::getUniqueId)
                .collect(Collectors.toSet());

        final Collection<PlayerProfile> selectedPlayerProfiles = this.playerProfileListResolver.resolve(sourceStack);
        if (this.singleSelector && selectedPlayerProfiles.size() > 1) throw TOO_MANY_SELECTED_EXCEPTION.create();

        final List<OfflinePlayer> selectedPlayers = new ArrayList<>();
        int notMatchedCount = 0;
        for (final PlayerProfile profile : selectedPlayerProfiles) {
            profile.complete(false);
            final OfflinePlayer selectedOfflinePlayer = Bukkit.getOfflinePlayer(Objects.requireNonNull(profile.getId()));

            if (offlinePlayerUUIDs.contains(selectedOfflinePlayer.getUniqueId())) {
                selectedPlayers.add(selectedOfflinePlayer);
            } else if (selectedOfflinePlayer.hasPlayedBefore()) notMatchedCount++;
        }

        if (selectedPlayerProfiles.size() == notMatchedCount)
            throw Optional.ofNullable(this.notMatchedException).orElse(UNKNOWN_PLAYER_FOUND_EXCEPTION).create();
        else if (selectedPlayers.isEmpty())
            throw UNKNOWN_PLAYER_FOUND_EXCEPTION.create();

        return selectedPlayers;
    }
}
