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

package dev.hboyd.prismatic.paper.actionBar;

import dev.hboyd.prismatic.text.ComponentUtil;
import dev.hboyd.prismatic.paper.scheduler.PluginScheduler;
import io.papermc.paper.util.Tick;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.util.HSVLike;

import java.time.Duration;

/**
 * Displays a timed action bar that hue-shifts its text over time.
 */
public class HueShiftingActionBar extends TimedActionBar {
    private final long displayTimeTicks;

    private long displayTimeTicksLeft;

    /**
     * Creates a hue-shifting action bar.
     *
     * @param pluginScheduler the plugin scheduler used for timing
     * @param audience        the audience to show the action bar to
     * @param component       the component to display
     * @param displayTime     how long to display the component for
     */
    public HueShiftingActionBar(final PluginScheduler pluginScheduler,
                                final Audience audience,
                                final ComponentLike component,
                                final Duration displayTime) {
        super(pluginScheduler, audience, ComponentUtil.stripColor(component.asComponent()), displayTime);

        this.displayTimeTicks = Tick.tick().fromDuration(displayTime);
    }

    /**
     * Decrements the remaining time and sends a hue-shifted action bar message.
     */
    @Override
    protected void tick() {
        if (this.displayTimeTicksLeft <= 0) {
            this.audience.sendActionBar(Component.text()); // Since we cannot control fade just "reset" the action bar to hide it
            this.task.cancel();
            return;
        }

        this.displayTimeTicksLeft--;
        final TextColor textColor = TextColor.color(HSVLike.hsvLike((float) this.displayTimeTicksLeft / this.displayTimeTicks, 0.7f, 0.6f));
        this.audience.sendActionBar(this.actionBarComponent.color(textColor).asComponent());
    }
}
