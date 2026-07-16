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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.CleanupMode;
import org.junit.jupiter.api.io.TempDir;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ConfigTest {

    @TempDir(cleanup = CleanupMode.ON_SUCCESS)
    private Path tempDirectory;

    private Path configPath;

    @BeforeEach
    void setUp() {
        this.configPath = this.tempDirectory.resolve(TestConfig.CONFIG_NAME);
    }

    @Test
    void configCreatesNewFileWithDefaultsWhenNoneExist() throws IOException {
        assertFalse(Files.exists(this.configPath));

        new TestConfig(this.tempDirectory); // Throw this away so we can make sure the defaults are actually loaded
        final TestConfig testConfig = new TestConfig(this.tempDirectory);

        assertTrue(Files.exists(this.configPath));
        assertEquals("default", testConfig.stringSetting);
        assertNull(testConfig.stringSettingDefaultless);
        assertNotNull(testConfig.subConfigMap);
        assertEquals(1, testConfig.subConfigMap.size());

        final TestConfig.SubConfig subConfig = testConfig.subConfigMap.get("key");
        assertNotNull(subConfig);
        assertEquals("key", subConfig.nodeKey);
        assertEquals(2, subConfig.integerSetting);
    }

    @Test
    void configAddsMissingOptionsWhenLoaded() throws IOException {
        new TestConfig(this.tempDirectory);

        try (final BufferedReader bufferedReader = Files.newBufferedReader(this.configPath)) {
            assertTrue(bufferedReader.lines().noneMatch(line -> line.equals("new-setting=5372")));
        }

        final TestConfigChanged testConfigChanged = new TestConfigChanged(this.tempDirectory);

        assertEquals(5372, testConfigChanged.newSetting);

        try (final BufferedReader bufferedReader = Files.newBufferedReader(this.configPath)) {
            assertTrue(bufferedReader.lines().anyMatch(line -> line.equals("new-setting=5372")));
        }
    }

    @Test
    void changesAreSaved() throws IOException {
        final TestConfig testConfig = new TestConfig(this.tempDirectory);

        testConfig.stringSetting = "new-string";
        testConfig.subConfigMap.get("key").integerSetting = 4;

        testConfig.save();

        final TestConfig reloadedTestConfig = new TestConfig(this.tempDirectory);
        assertEquals("new-string", reloadedTestConfig.stringSetting);
        assertEquals(4, testConfig.subConfigMap.get("key").integerSetting);
    }

    @Test
    void externalChangedFieldsAreLoadedCorrectly() throws IOException {
        final TestConfig testConfig = new TestConfig(this.tempDirectory);

        String config;
        try (final BufferedReader bufferedReader = Files.newBufferedReader(this.configPath)) {
            config = bufferedReader.readAllAsString();
        }

        config = config.replace("string-setting=default", "string-setting=new");

        try (final BufferedWriter bufferedWriter = Files.newBufferedWriter(this.configPath)) {
            bufferedWriter.write(config);
        }

        assertEquals("default", testConfig.stringSetting);
        testConfig.load();
        assertEquals("new", testConfig.stringSetting);

    }

    @Test
    void defaultsAreNotInsertedIntoMaps() throws IOException {
        final String newSubConfigKey = "different-key";
        final TestConfig testConfig = new TestConfig(this.tempDirectory);
        testConfig.subConfigMap.clear();
        testConfig.subConfigMap.put(newSubConfigKey, new TestConfig.SubConfig(66, newSubConfigKey));

        testConfig.save();

        final TestConfig reloadedTestConfig = new TestConfig(this.tempDirectory);
        assertEquals(1, testConfig.subConfigMap.size());

        final TestConfig.SubConfig newSubConfig = reloadedTestConfig.subConfigMap.get(newSubConfigKey);
        assertNotNull(newSubConfig);
        assertEquals(newSubConfigKey, newSubConfig.nodeKey);
        assertEquals(66, newSubConfig.integerSetting);
    }

    @Test
    void configAppliesVersionTransformation() throws IOException {
        final TestConfig testConfig = new TestConfig(this.tempDirectory);
        final String originalStringSetting = "new string";
        testConfig.stringSetting = originalStringSetting;
        testConfig.save();

        final TestConfigChanged testConfigChanged = new TestConfigChanged(this.tempDirectory);
        assertEquals(testConfigChanged.latestVersion(), testConfigChanged.version());
        assertEquals(originalStringSetting, testConfigChanged.movedStringSetting);

        final CommentedConfigurationNode loadedNode = HoconConfigurationLoader.builder()
                .file(this.tempDirectory.resolve("testConfig.conf").toFile())
                .build()
                .load();
        assertEquals(testConfigChanged.latestVersion(), loadedNode.node("version").getInt());
        assertEquals(originalStringSetting, loadedNode.node("moved-string-setting").getString());
    }

    @Test
    void changedInvalidConfigDoesNotEffectPreviousConfig() throws IOException {
        final TestConfigChanged testConfigChanged = new TestConfigChanged(this.tempDirectory);
        final String originalStringSettingDefaultless = "qwerty";
        testConfigChanged.stringSettingDefaultless = originalStringSettingDefaultless;
        final int originalRestrictedIntegerSetting = 8;
        testConfigChanged.restrictedIntegerSetting = originalRestrictedIntegerSetting;

        final TestConfig testConfig = new TestConfig(this.tempDirectory);
        testConfig.restrictedIntegerSetting = 1;
        testConfig.save();

        assertThrows(ConfigurateException.class, testConfigChanged::load);
        assertEquals(originalStringSettingDefaultless, testConfigChanged.stringSettingDefaultless);
        assertEquals(originalRestrictedIntegerSetting, testConfigChanged.restrictedIntegerSetting);
    }
}
