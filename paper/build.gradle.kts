plugins {
    id("prismatic.java-conventions")
}


dependencies {
    api(projects.prismaticLib)
    api(libs.paperAPI)

    testRuntimeOnly(libs.paperAPI)
    testRuntimeOnly(projects.prismaticLib)
}

extraJavaModuleInfo {
    automaticModule("net.md-5:bungeecord-chat", "net.md5.bungeecord.chat")

}
