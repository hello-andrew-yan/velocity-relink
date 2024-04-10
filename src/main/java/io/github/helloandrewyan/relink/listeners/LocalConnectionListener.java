package io.github.helloandrewyan.relink.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import io.github.helloandrewyan.relink.Relink;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class LocalConnectionListener {

    // TODO - EXPERIMENTAL METHOD
    @Subscribe
    private void onProxyConnect(LoginEvent event) {
        String connection = Relink.getLocalDataExecutor().getUserConnection(event.getPlayer().getUniqueId());
        RegisteredServer relink = Relink.getServer(connection);

        event.getPlayer().disconnect(Component.empty());
        event.getPlayer().createConnectionRequest(relink);
    }

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
