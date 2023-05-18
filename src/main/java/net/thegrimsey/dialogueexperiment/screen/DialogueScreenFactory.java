package net.thegrimsey.dialogueexperiment.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.thegrimsey.dialogueexperiment.dialogue.Dialogue;
import org.jetbrains.annotations.Nullable;

public class DialogueScreenFactory implements ExtendedScreenHandlerFactory {
    Dialogue dialogue;

    DialogueScreenHandler screenHandler;
    public DialogueScreenFactory(Dialogue dialogue) {
        this.dialogue = dialogue;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        screenHandler.writeActiveNode(buf);
    }

    @Override
    public Text getDisplayName() {
        return Text.of("Dialogue");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        screenHandler = new DialogueScreenHandler(syncId, inv, dialogue);

        screenHandler.switchToNode(dialogue.nodes().get("entry"), false);

        return screenHandler;
    }
}
