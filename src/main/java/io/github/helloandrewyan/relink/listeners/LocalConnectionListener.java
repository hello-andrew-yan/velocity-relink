package io.github.helloandrewyan.relink.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
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
    @Subscribe
    private void updateConnection(ServerConnectedEvent event) {
        if (event.getPlayer().getCurrentServer().isEmpty()) {
            return;
        }
        String connection = event.getServer().getServerInfo().getName();
        if (!Relink.getLinked().contains(connection)) {
            return;
        }
        UUID uuid = event.getPlayer().getUniqueId();
        Relink.getLocalDataExecutor().insertUserConnection(uuid, connection);
    }
}
