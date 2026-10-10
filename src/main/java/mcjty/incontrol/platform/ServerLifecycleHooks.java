package mcjty.incontrol.platform;

import net.minecraft.server.MinecraftServer;

public final class ServerLifecycleHooks {
    private static MinecraftServer server;
    private ServerLifecycleHooks() {}
    public static MinecraftServer getCurrentServer() { return server; }
    public static void setCurrentServer(MinecraftServer value) { server = value; }
}
