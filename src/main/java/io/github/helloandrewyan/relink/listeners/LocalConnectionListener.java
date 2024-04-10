package io.github.helloandrewyan.relink.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import io.github.helloandrewyan.relink.Relink;

import java.util.UUID;

public class LocalConnectionListener {
    @Subscribe
    private void validateConnection(PostLoginEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        String connection = Relink.getLocalDataExecutor().getUserConnection(uuid);

        if (connection == null || !Relink.getLinked().contains(connection)) {
            Relink.getLocalDataExecutor().insertUserConnection(uuid, "");
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    @Subscribe
    private void updateConnection(ServerPostConnectEvent event) {
        if (event.getPlayer().getCurrentServer().isEmpty()) {
            return;
        }
        String connection = event.getPlayer().getCurrentServer().get().getServerInfo().getName();
        if (!Relink.getLinked().contains(connection)) {
            return;
        }
        UUID uuid = event.getPlayer().getUniqueId();
        Relink.getLocalDataExecutor().insertUserConnection(uuid, connection);
    }
}
