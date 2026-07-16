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

import dev.hboyd.chasm.Localed;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.audience.ForwardingAudience;
import org.bukkit.entity.Player;

import java.util.Locale;

/**
 * {@link Audience} related utilities.
 */
public final class AudienceUtil {
    private AudienceUtil() {}

    // TODO: Figure out how to move this to main lib
    /**
     * Find the given audience's locale falling back to the default Locale
     * when the audience cannot have a locale or no single locale is applicable to it.
     *
     * @param audience the audience
     * @return a locale
     */
    @SuppressWarnings("OverrideOnly")
    public static Locale findLocale(final Audience audience) {
        return switch (audience) {
            case final Localed localed -> localed.locale();
            case final Player player -> player.locale();
            case final ForwardingAudience.Single single -> findLocale(single.audience());
            default -> Locale.getDefault();
        };
    }
}
