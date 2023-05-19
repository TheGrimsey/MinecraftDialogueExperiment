package net.thegrimsey.dialogueexperiment.dialogue;

import net.minecraft.text.Text;

import java.util.List;

public record FormattedDialoguePage(Text speaker, List<Text> lines) {
}
