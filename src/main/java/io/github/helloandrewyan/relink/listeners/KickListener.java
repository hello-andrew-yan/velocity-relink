package io.github.helloandrewyan.relink.listeners;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;

public class KickListener {
    @Subscribe
    private void onServerKick(KickedFromServerEvent event)  {
        if (event.getServerKickReason().isPresent()) {
            event.getPlayer().disconnect(event.getServerKickReason().get());
        }
    }
}
