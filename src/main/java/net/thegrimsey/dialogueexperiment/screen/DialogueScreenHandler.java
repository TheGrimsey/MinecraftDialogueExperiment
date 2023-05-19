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
import net.thegrimsey.dialogueexperiment.dialogue.*;

import java.util.ArrayList;
import java.util.List;

public class DialogueScreenHandler extends ScreenHandler {

    @Environment(EnvType.CLIENT)
    List<FormattedDialoguePage> pages = new ArrayList<>();
    @Environment(EnvType.CLIENT)
    int currentPage = 0;

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
    public boolean isFinalPage() {
        return currentPage == (this.pages.size() - 1);
    }

    @Environment(EnvType.CLIENT)
    public FormattedDialoguePage getCurrentPage() {
        return this.pages.get(this.currentPage);
    }

    @Environment(EnvType.CLIENT)
    public void readFromBuffer(PacketByteBuf buf) {
        this.currentPage = 0;
        this.pages.clear();

        final int pageCount = buf.readVarInt();
        for(int i = 0; i < pageCount; i++) {
            Text speaker = buf.readText();
            final int lineCount = buf.readVarInt();
            List<Text> lines = new ArrayList<>(lineCount);

            for(int j = 0; j < lineCount; j++) {
                lines.add(buf.readText());
            }

            this.pages.add(new FormattedDialoguePage(speaker, lines));
        }

        this.responses.clear();
        final int responsesCount = buf.readVarInt();
        for(int i = 0; i < responsesCount; i++) {
            responses.add(buf.readText());
        }
    }

    // SERVER SIDE.
    public void writeActiveNode(PacketByteBuf buf) {
        buf.writeVarInt(activeNode.pages().size());
        for(DialoguePage page : activeNode.pages()) {
            buf.writeText(DialogueTextFormatter.formatText(page.speaker(), player));
            buf.writeVarInt(page.lines().size());
            for (String text : page.lines()) {
                buf.writeText(DialogueTextFormatter.formatText(text, player));
            }
        }

        buf.writeVarInt(possibleResponses.size());
        for (DialogueResponse possibleResponse : possibleResponses) {
            buf.writeText(DialogueTextFormatter.formatText(possibleResponse.text(), player));
        }
    }

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


    @Override
    public boolean canUse(PlayerEntity player) {
        return true;
    }
}
