package net.thegrimsey.dialogueexperiment;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.thegrimsey.dialogueexperiment.screen.DialogueScreen;
import net.thegrimsey.dialogueexperiment.screen.DialogueScreenHandler;

import static net.thegrimsey.dialogueexperiment.DialogueNetworking.SEND_DIALOGUE;

@Environment(EnvType.CLIENT)
public class DialogueNetworkingClient {
    public static void registerNetworking() {
        ClientPlayNetworking.registerGlobalReceiver(SEND_DIALOGUE, (client, handler, buf, responseSender) -> {
            if(client.player.currentScreenHandler instanceof DialogueScreenHandler screenHandler) {
                screenHandler.readFromBuffer(buf);
                if(client.currentScreen instanceof DialogueScreen dialogueScreen) {
                    dialogueScreen.reinit();
                }
            }
        });
    }
}
