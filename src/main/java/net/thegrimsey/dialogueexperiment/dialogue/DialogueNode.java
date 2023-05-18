package net.thegrimsey.dialogueexperiment.dialogue;

import java.util.List;

public record DialogueNode(String speaker, String text, List<DialogueResponse> responses) {
}
