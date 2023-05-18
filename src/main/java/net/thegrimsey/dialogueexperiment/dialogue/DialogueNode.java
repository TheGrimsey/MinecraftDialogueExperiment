package net.thegrimsey.dialogueexperiment.dialogue;

import java.util.List;

public record DialogueNode(String speaker, List<String> text, List<DialogueResponse> responses) {
}
