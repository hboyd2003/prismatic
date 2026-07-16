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
import net.kyori.adventure.text.format.Style;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link Component} related utilities.
 */
public final class ComponentUtil {
    private ComponentUtil() {}

    /**
     * Remove color from a component and its children.
     *
     * @param component the component to strip
     * @return a new component with no color styling
     */
    public static Component stripColor(Component component) {
        final Style style = Style.style().build();
        style.merge(component.style(), Style.Merge.SHADOW_COLOR, Style.Merge.DECORATIONS, Style.Merge.EVENTS, Style.Merge.INSERTION, Style.Merge.FONT);
        component = component.style(style);

        final List<Component> children = new ArrayList<>();
        for (int i = 0; i < component.children().size(); i++) {
            children.add(ComponentUtil.stripColor(component.children().get(i)));
        }

        return component.children(children);
    }
}
