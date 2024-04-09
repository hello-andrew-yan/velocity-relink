package io.github.helloandrewyan.relink.temp;

import io.github.helloandrewyan.relink.Relink;

import java.util.UUID;

public class Test {
    public void testExecutor() {
        UUID uuid = UUID.randomUUID();
        Relink.getSqlExecutor().insertUserConnection(uuid, "hub");
        Relink.getLogger().info(Relink.getSqlExecutor().getUserConnection(uuid));
        Relink.getSqlExecutor().insertUserConnection(uuid, "factions");
        Relink.getLogger().info(Relink.getSqlExecutor().getUserConnection(uuid));
        Relink.getSqlExecutor().insertUserConnection(uuid, "minigames");
        Relink.getLogger().info(Relink.getSqlExecutor().getUserConnection(uuid));
        Relink.getSqlExecutor().deleteUserConnection(uuid);
    }
}
