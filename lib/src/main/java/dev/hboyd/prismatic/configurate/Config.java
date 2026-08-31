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

import dev.hboyd.configurateNBT.serializer.BinaryTagSerializer;
import dev.hboyd.prismatic.configurate.constraint.CollectionConstraints;
import dev.hboyd.prismatic.configurate.constraint.NumberConstraints;
import dev.hboyd.prismatic.configurate.constraint.StringConstraints;
import dev.hboyd.prismatic.configurate.serializer.BigDecimalSerializer;
import dev.hboyd.prismatic.configurate.serializer.InetSocketAddressSerializer;
import net.kyori.adventure.serializer.configurate4.ConfigurateComponentSerializer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.checkerframework.checker.nullness.qual.EnsuresNonNull;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.configurate.hocon.internal.typesafeconfig.ConfigFactory;
import org.spongepowered.configurate.hocon.internal.typesafeconfig.ConfigRenderOptions;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.loader.HeaderMode;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.ObjectMapper;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;
import org.spongepowered.configurate.transformation.ConfigurationTransformation;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Objects;

/**
 * An abstract self-contained simplified Configurate config.
 *
 * <p>Concrete extending classes <b>must</b> have be annotated with {@link ConfigSerializable}</p>
 *
 * <p>{@link Config#initialize()} <b>MUST</b> be called at the end of the implementing classes constructor.</p>
 */
@SuppressWarnings({"AbstractClassWithoutAbstractMethods"})
public abstract class Config {
    private static final ObjectMapper.Factory OBJECT_MAPPER_FACTORY = ObjectMapper.factoryBuilder()
            .addConstraint(CollectionConstraints.NonEmpty.class,
                    Collection.class,
                    new CollectionConstraints.NonEmpty.Factory())
            .addConstraint(CollectionConstraints.Size.class, Collection.class, new CollectionConstraints.Size.Factory())
            .addConstraint(NumberConstraints.Positive.class, Number.class, new NumberConstraints.Positive.Factory())
            .addConstraint(NumberConstraints.NonPositive.class, Number.class, new NumberConstraints.NonPositive.Factory())
            .addConstraint(NumberConstraints.Negative.class, Number.class, new NumberConstraints.Negative.Factory())
            .addConstraint(NumberConstraints.NonNegative.class, Number.class, new NumberConstraints.NonNegative.Factory())
            .addConstraint(NumberConstraints.Bound.class, Number.class, new NumberConstraints.Bound.Factory())
            .addConstraint(StringConstraints.NonEmpty.class, String.class, new StringConstraints.NonEmpty.Factory())
            .addConstraint(StringConstraints.NonBlank.class, String.class, new StringConstraints.NonBlank.Factory())
            .addConstraint(StringConstraints.Length.class, String.class, new StringConstraints.Length.Factory())
            .build();

    private static final TypeSerializerCollection DEFAULT_TYPE_SERIALIZERS = TypeSerializerCollection.defaults()
            .childBuilder()
            .registerAnnotatedObjects(OBJECT_MAPPER_FACTORY)
            .registerAll(ConfigurateComponentSerializer.builder()
                    .scalarSerializer(MiniMessage.miniMessage())
                    .outputStringComponents(true)
                    .build().serializers()) // Includes all Adventure serializers and some extras (not just Component)
            .register(BigDecimal.class, BigDecimalSerializer.INSTANCE)
            .register(InetSocketAddress.class, InetSocketAddressSerializer.INSTANCE)
            .registerAll(BinaryTagSerializer.TYPE_SAFE_SERIALIZERS)
            .build();

    private final transient Path filePath;
    private final transient int latestVersion;
    private final transient TypeSerializerCollection typeSerializers;
    private final transient ConfigurationLoader<?> loader;
    private final transient ObjectMapper.Mutable<Config> objectMapper;
    private final transient @Nullable ConfigurationTransformation transformer;

    private transient boolean initialized;

    private transient @MonotonicNonNull ConfigurationNode defaultConfigNode;
    private transient @Nullable ConfigurationNode lastConfigNode;
    private transient @MonotonicNonNull ConfigurationNode configNode;

    @SuppressWarnings("FieldMayBeFinal")
    @Comment("DO NOT TOUCH! Changing this value may cause the config to become corrupt!")
    @NumberConstraints.Bound(min = 0)
    private int version;

    /**
     * Construct a new config.
     *
     * @param filePath                  the config path
     * @param latestVersion             the latest config version
     * @param header                    the config header
     * @param additionalTypeSerializers type serializers to use in addition to the default
     * @param transformer               configuration transformation to run against any loaded configuration
     */
    // TODO: Explain in javadoc that changes made by the transformer will only be saved to the config if the version changes after passing through the config.
    protected Config(final Path filePath,
                     final int latestVersion,
                     @Nullable final String header,
                     @Nullable final TypeSerializerCollection additionalTypeSerializers,
                     @Nullable final ConfigurationTransformation transformer) {
        if (latestVersion < 0) throw new IllegalArgumentException("latestVersion must be >= 0");

        this.filePath = Objects.requireNonNull(filePath, "filePath");
        this.latestVersion = latestVersion;
        this.version = latestVersion;
        this.transformer = transformer;
        this.initialized = false;

        TypeSerializerCollection typeSerializerCollection = DEFAULT_TYPE_SERIALIZERS;
        if (additionalTypeSerializers != null) typeSerializerCollection = typeSerializerCollection.childBuilder()
                .registerAll(additionalTypeSerializers)
                .build();

        this.typeSerializers = typeSerializerCollection;

        this.loader = HoconConfigurationLoader.builder()
                .defaultOptions(configurationOptions ->
                        configurationOptions
                                .serializers(this.typeSerializers())
                                .shouldCopyDefaults(true)
                                .implicitInitialization(false)
                                .header(header))
                .path(this.filePath)
                .prettyPrinting(true)
                .headerMode(HeaderMode.PRESET)
                .build();

        try {
            //noinspection unchecked
            this.objectMapper = (ObjectMapper.Mutable<Config>) OBJECT_MAPPER_FACTORY.get(this.getClass());
        } catch (final SerializationException e) {
            // Only thrown if the mapper cannot map to this, which we can expect to not happen.
            throw new RuntimeException(e);
        }
    }

    /**
     * Construct a new config.
     *
     * @param filePath      the config path
     * @param latestVersion the latest config version
     */
    protected Config(final Path filePath, final int latestVersion) {
        this(filePath, latestVersion, null, null, null);
    }

    /**
     * Initialize the config. Must be called after all config fields have been instantiated.
     *
     * @throws IOException            when the config file directory doesn't exist
     * @throws ConfigurateException   when an issue occurs writing the file
     * @throws SerializationException when unable to map values to a node
     */
    @EnsuresNonNull({"filePath", "loader", "objectMapper", "defaultConfigNode"})
    protected void initialize() throws IOException, ConfigurateException, SerializationException {
        if (this.initialized) throw new IllegalStateException("Config has already been initialized");

        this.defaultConfigNode = this.loader.createNode();
        this.objectMapper.save(this, this.defaultConfigNode);

        this.initialized = true;

        if (Files.exists(this.filePath)) this.load();
        else this.save();
    }

    /**
     * Get the version.
     *
     * @return the version
     */
    public int version() {
        return this.version;
    }

    /**
     * Get the latest config version. May differ from the current config's version.
     *
     * @return the latest version
     */
    public int latestVersion() {
        return this.latestVersion;
    }

    /**
     * Get the type serializers used by this config.
     *
     * @return a type serializer collection
     */
    public final TypeSerializerCollection typeSerializers() {
        return this.typeSerializers;
    }

    /**
     * Revert the config to the last loaded config.
     */
    public final void revert() {
        if (!this.initialized) throw new IllegalStateException("Config is not initialized");
        if (this.lastConfigNode != null) {
            this.configNode = this.lastConfigNode;
            this.lastConfigNode = null;
            try {
                this.objectMapper.load(this, this.configNode);
            } catch (final SerializationException e) {
                throw new RuntimeException(e); // We expect the last config to be valid
            }
        }
    }

    /**
     * Load the config from the file. If an issue occurs while loading the current config will not change.
     *
     * @throws IOException            when the config file directory doesn't exist
     * @throws ConfigurateException   when an issue occurs writing the file
     * @throws SerializationException when unable to map values to a node
     */
    @EnsuresNonNull({"configNode"})
    public final void load() throws IOException, ConfigurateException, SerializationException {
        this.load(true);
    }

    /**
     * Load the config from the file. If an issue occurs while loading the current config will not change.
     *
     * @param format whether to re-format the file or not
     * @throws IOException            when the config file directory doesn't exist
     * @throws ConfigurateException   when an issue occurs writing the file
     * @throws SerializationException when unable to map values to a node
     */
    @EnsuresNonNull({"configNode"})
    public final void load(final boolean format) throws IOException, ConfigurateException, SerializationException {
        if (!this.initialized) throw new IllegalStateException("Config is not initialized");
        if (Files.notExists(this.filePath.toAbsolutePath().getParent()))
            throw new FileNotFoundException("Config file directory doesn't exist");

        // TODO: We want to always show all possible arguments, even if the set value is null. Currently configurate just ignores null values.

        final ConfigurationNode loadedNode = this.loader.load();
        final int originalVersion = loadedNode.node("version").getInt();
        if (this.transformer != null) this.transformer.apply(loadedNode);
        this.objectMapper.load(this, loadedNode);
        this.lastConfigNode = this.configNode;
        this.configNode = loadedNode;

        if (originalVersion != this.version) {
            this.save();
        } else if (format) {
            final String renderedConfig = ConfigFactory.parseFile(this.filePath.toFile()).root()
                    .render(ConfigRenderOptions.defaults().setJson(false).setOriginComments(false));
            try (final BufferedWriter bufferedWriter = Files.newBufferedWriter(this.filePath)) {
                bufferedWriter.write(renderedConfig);
            }
        }
    }

    /**
     * Saves the config file. If no config file exists, the defaults will be saved.
     *
     * @throws ConfigurateException   when unable to map values to a node
     * @throws SerializationException when an issue occurs, writing the file
     */
    @EnsuresNonNull({"configNode"})
    public final void save() throws ConfigurateException, SerializationException {
        if (!this.initialized) throw new IllegalStateException("Config is not initialized");

        final boolean unloaded = this.configNode == null;
        this.configNode = this.defaultConfigNode.copy(); // never reuse original config node to enforce field order
        if (!unloaded) this.objectMapper.save(this, this.configNode);
        this.loader.save(this.configNode);
    }

}
