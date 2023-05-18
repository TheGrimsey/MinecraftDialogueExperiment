package net.thegrimsey.dialogueexperiment.dialogue;

import eu.pb4.placeholders.PlaceholderAPI;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class DialogueTextFormatter {
    public static Text formatText(String text, ServerPlayerEntity player) {
        return PlaceholderAPI.parseText(Text.of(text), player);
    }
}
