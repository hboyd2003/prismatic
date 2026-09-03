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

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.concurrent.ThreadLocalRandom;

import static org.junit.jupiter.api.Assertions.*;

class ComponentUtilTest {

    private static TextColor randomTextColor() {
        return TextColor.color(ThreadLocalRandom.current().nextInt(256),
                ThreadLocalRandom.current().nextInt(256),
                ThreadLocalRandom.current().nextInt(256));
    }

    @ParameterizedTest
    @EnumSource(TextDecoration.class)
    void stripColorOnlyStripsColor(final TextDecoration textDecoration) {
        final TextColor textColor = randomTextColor();
        final TextComponent component = Component.text(textDecoration.name().toLowerCase(),
                Style.style()
                        .color(textColor)
                        .decorate(textDecoration).build());

        final Component strippedComponent = ComponentUtil.stripColor(component);

        assertEquals(0, strippedComponent.children().size());
        assertEquals(TextDecoration.State.TRUE, strippedComponent.decoration(textDecoration));
        assertTrue(strippedComponent.hasDecoration(textDecoration), "Expected stripped component to have " + textDecoration);
        assertNull(strippedComponent.color());
    }
}
