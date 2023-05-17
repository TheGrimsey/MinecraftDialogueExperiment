package net.thegrimsey.dialogueexperiment;

import java.util.List;

public record DialogueNode(String speaker, String text, List<DialogueResponse> responses) {
}
