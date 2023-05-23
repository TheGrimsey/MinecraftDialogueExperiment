package net.thegrimsey.dialogueexperiment;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.thegrimsey.dialogueexperiment.networking.ChooseResponseChannelHandler;
import net.thegrimsey.dialogueexperiment.screen.DialogueScreen;
import net.thegrimsey.dialogueexperiment.screen.DialogueScreenHandler;

public class DialogueNetworking {
    public static final Identifier SEND_DIALOGUE = new Identifier(DialogueExperiment.MODID, "send_dialogue");

    private static final Identifier CHOOSE_RESPONSE = new Identifier(DialogueExperiment.MODID, "choose_response");
    private static final ChooseResponseChannelHandler CHOOSE_RESPONSE_CHANNEL_HANDLER = new ChooseResponseChannelHandler();

    public static void registerNetworking() {
        ServerPlayNetworking.registerGlobalReceiver(CHOOSE_RESPONSE, CHOOSE_RESPONSE_CHANNEL_HANDLER);


    }

    @Environment(EnvType.CLIENT)
    public static void sendResponsePacket(int response) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());

        buf.writeVarInt(response);

        ClientPlayNetworking.send(CHOOSE_RESPONSE, buf);
    }
}
