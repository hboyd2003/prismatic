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

package dev.hboyd.prismatic.configurate;

import dev.hboyd.prismatic.configurate.constraint.NumberConstraints;
import org.spongepowered.configurate.NodePath;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.NodeKey;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;
import org.spongepowered.configurate.transformation.TransformAction;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigSerializable
public class TestConfigChanged extends Config {
    public static final String CONFIG_NAME = "testConfig.conf";
    private static final ConfigurationTransformation VERSION_TRANSFORMER = ConfigurationTransformation.versionedBuilder()
            .versionKey("version")
            .makeVersion(2, builder -> builder.addAction(NodePath.of(List.of("string-setting")), TransformAction.rename("moved-string-setting")))
            .build();

    public String movedStringSetting = "default";
    public String stringSettingDefaultless;
    public int newSetting = 5372;
    @NumberConstraints.Bound(min = 2)
    public int restrictedIntegerSetting = 3;

    public Map<String, SubConfig> subConfigMap = new HashMap<>(Map.of("key", new SubConfig()));

    @ConfigSerializable
    public static class SubConfig {
        @NodeKey
        public String nodeKey;

        public int integerSetting = 2;

        public SubConfig() {}

        public SubConfig(final int integerSetting, final String nodeKey) {
            this.nodeKey = nodeKey;
            this.integerSetting = integerSetting;
        }
    }

    public TestConfigChanged(final Path configDirectory) throws IOException {
        super(configDirectory.resolve(CONFIG_NAME), 2, null, null, VERSION_TRANSFORMER);
        super.initialize();
    }
}
