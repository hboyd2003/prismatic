/**
 * Prismatic is a general purpose library for Minecraft: Java Edition
 */
module dev.hboyd.prismatic.lib {
    requires transitive brigadier;
    requires transitive chasm.chasm.lib.main;
    requires transitive configurate.nbt;
    requires transitive org.checkerframework.checker.qual;
    requires transitive org.jetbrains.annotations;
    requires transitive org.jspecify;
    requires transitive org.spongepowered.configurate;

    requires com.google.common;
    requires com.google.gson;
    requires net.kyori.adventure.serializer.configurate4;
    requires net.kyori.adventure.text.minimessage;
    requires net.kyori.adventure.text.serializer.legacy;
    requires net.kyori.adventure.text.serializer.plain;
    requires org.spongepowered.configurate.hocon;

    exports dev.hboyd.prismatic;
    exports dev.hboyd.prismatic.brigadier;
    exports dev.hboyd.prismatic.configurate;
    exports dev.hboyd.prismatic.configurate.constraint;
    exports dev.hboyd.prismatic.configurate.serializer;
    exports dev.hboyd.prismatic.text;
    exports dev.hboyd.prismatic.key;
}
