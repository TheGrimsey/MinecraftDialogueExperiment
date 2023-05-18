package net.thegrimsey.dialogueexperiment.screen;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.thegrimsey.dialogueexperiment.*;
import net.thegrimsey.dialogueexperiment.dialogue.Dialogue;
import net.thegrimsey.dialogueexperiment.dialogue.DialogueNode;
import net.thegrimsey.dialogueexperiment.dialogue.DialogueResponse;
import net.thegrimsey.dialogueexperiment.dialogue.DialogueTextFormatter;

import java.util.ArrayList;
import java.util.List;

public class DialogueScreenHandler extends ScreenHandler {

    @Environment(EnvType.CLIENT)
    String speaker;
    @Environment(EnvType.CLIENT)
    Text text;
    @Environment(EnvType.CLIENT)
    List<Text> responses = new ArrayList<>();

    // Server side.
    ServerPlayerEntity player;

    Dialogue dialogue;

    DialogueNode activeNode;

    List<DialogueResponse> possibleResponses = new ArrayList<>();

    // Server side.
    public DialogueScreenHandler(int syncId, PlayerInventory playerInventory, Dialogue dialogue) {
        super(DialogueExperiment.DIALOGUE_SCREENHANDLER, syncId);

        player = (ServerPlayerEntity) playerInventory.player;

        this.dialogue = dialogue;
    }

    public DialogueScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        super(DialogueExperiment.DIALOGUE_SCREENHANDLER, syncId);

        readFromBuffer(buf);
    }

    @Environment(EnvType.CLIENT)
    public void readFromBuffer(PacketByteBuf buf) {
        this.speaker = buf.readString();
        this.text = buf.readText();

        this.responses.clear();
        final int count = buf.readVarInt();
        for(int i = 0; i < count; i++) {
            responses.add(buf.readText());
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }

    // SERVER SIDE.
    void switchToNode(DialogueNode node, boolean send) {
        activeNode = node;

        possibleResponses.clear();

        for(int i = 0; i < activeNode.responses().size(); i++) {
            DialogueResponse response = activeNode.responses().get(i);

            if(response.condition().isPresent()) {
                var condition = response.condition().get();

                if(condition.test(player)) {
                    possibleResponses.add(response);
                }
            } else {
                possibleResponses.add(response);
            }
        }

        if(send) {
            PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
            writeActiveNode(buf);
            ServerPlayNetworking.send(this.player, DialogueNetworking.SEND_DIALOGUE, buf);
        }
    }

    public void writeActiveNode(PacketByteBuf buf) {
        buf.writeString(activeNode.speaker());
        buf.writeText(DialogueTextFormatter.formatText(activeNode.text(), player));
        buf.writeVarInt(possibleResponses.size());
        for (DialogueResponse possibleResponse : possibleResponses) {
            buf.writeText(DialogueTextFormatter.formatText(possibleResponse.text(), player));
        }
    }

    public void selectResponse(int chosenResponse) {
        if(chosenResponse >= 0 && chosenResponse < possibleResponses.size()) {
            DialogueResponse response = possibleResponses.get(chosenResponse);

            if(response.commands().isPresent()) {
                List<String> commands = response.commands().get();

                commands.forEach(command -> {
                    String formattedCommand = command.replace("--player--", player.getGameProfile().getName());
                    player.server.getCommandManager().execute(player.server.getCommandSource().withWorld(player.getWorld()).withPosition(player.getPos()).withRotation(player.getRotationClient()), formattedCommand);
                });
            }
            String targetNode = response.targetNode();
            if(targetNode.isEmpty()){
                player.closeHandledScreen();
                return;
            }

            DialogueNode newNode = dialogue.nodes().get(targetNode);

            if(newNode != null) {
                switchToNode(newNode, true);
            } else {
                player.sendMessage(Text.of("Invalid dialogue node."), false);
            }
        } else {
            player.sendMessage(Text.of("Invalid choice."), false);
        }
    }
}
