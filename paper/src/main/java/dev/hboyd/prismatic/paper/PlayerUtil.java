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

package dev.hboyd.prismatic.paper;

import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * Utilities for {@link Player}s.
 */
public final class PlayerUtil {
    private PlayerUtil() {
        /* This utility class should not be instantiated */
    }

    /**
     * Builds a component that represents the given player. If the player is online, the component is made up of their
     * display name with a hover event of the player converted into a hover event. If the player is offline the
     * component is made up of their username showing their uuid on hover. If they have no username cached the component
     * is made up of only their uuid.
     *
     * @param offlinePlayer a player
     * @return a component representing the player
     */
    public static Component asComponent(final OfflinePlayer offlinePlayer) {
        final Player player = offlinePlayer.getPlayer();

        if (player != null) {
            return player.displayName().hoverEvent(player.asHoverEvent());
        } else if (offlinePlayer.getName() != null) {
            return Component.text(offlinePlayer.getName()).hoverEvent(Component.text(offlinePlayer.getUniqueId().toString()));
        }

        return Component.text(offlinePlayer.getUniqueId().toString());
    }

    /**
     * Builds a string that represents the given player profile. If the profile is incomplete, it is attempted to be
     * completed via cache. If the profile has a username cached, the string is made up of their username and UUID in
     * parentheses. If they have no username cached but have an id, it returns only the UUID; otherwise, it returns
     * "unknown".
     *
     * <p>
     * Examples:
     * {@snippet lang=java :
     * PlayerUtil.asString(Bukkit.createProfile(UUID.fromString("c3bf4402-aaa4-4ac9-ac2e-404272f730e6"))); // c3bf4402-aaa4-4ac9-ac2e-404272f730e6
     * PlayerUtil.asString(Bukkit.createProfile("playername")); // playername
     * PlayerUtil.asString(Bukkit.createProfile(UUID.fromString("c3bf4402-aaa4-4ac9-ac2e-404272f730e6"),"playername")); // playername (c3bf4402-aaa4-4ac9-ac2e-404272f730e6)
     * }
     * </p>
     *
     * @param playerProfile the player profile to convert
     * @return a string representation of the player profile
     */
    public static String asString(final PlayerProfile playerProfile) {
        if (!playerProfile.isComplete()) playerProfile.completeFromCache();
        if (playerProfile.getName() == null && playerProfile.getId() == null) return "unknown";

        if (playerProfile.getName() != null) {
            return playerProfile.getName() + Optional.ofNullable(playerProfile.getId())
                    .map(uuid -> " (" + uuid + ")")
                    .orElse("");
        }

        return playerProfile.getId().toString();
    }

    /**
     * Builds a string that represents the given player. If the player has a username cached, the string is made up of
     * their username and UUID in parentheses. If they have no username cached the string is simply their UUID.
     *
     * @param offlinePlayer a player
     * @return a string representing the player
     */
    public static String asString(final OfflinePlayer offlinePlayer) {
        final String username = Optional.ofNullable(offlinePlayer.getPlayer())
                .map(Player::getName)
                .orElse(offlinePlayer.getName());

        if (username != null) {
            return username + "(" + offlinePlayer.getUniqueId() + ")";
        }

        return offlinePlayer.getUniqueId().toString();
    }
}
