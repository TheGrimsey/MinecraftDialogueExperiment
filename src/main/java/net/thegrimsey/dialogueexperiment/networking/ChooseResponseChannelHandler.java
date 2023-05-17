package net.thegrimsey.dialogueexperiment.networking;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.thegrimsey.dialogueexperiment.screen.DialogueScreenHandler;

public class ChooseResponseChannelHandler implements ServerPlayNetworking.PlayChannelHandler {
    @Override
    public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {

        try {
            int chosenResponse = buf.readVarInt();

            if(player.currentScreenHandler instanceof DialogueScreenHandler dialogueScreenHandler) {
                dialogueScreenHandler.selectResponse(chosenResponse);
            }
        } catch (IndexOutOfBoundsException exception) {
            return;
        }

        // TODO: Handle choice.
    }
}
