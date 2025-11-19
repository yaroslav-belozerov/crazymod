package tech.tarakoshka.mymod.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import tech.tarakoshka.mymod.util.CustomStringPayload;

public class ClientNetworkHandler {
    public static void sendCustomPacket(String message) {
        ClientPlayNetworking.send(new CustomStringPayload(message));
    }
}
