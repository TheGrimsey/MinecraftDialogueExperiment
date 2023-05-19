package net.thegrimsey.dialogueexperiment.dialogue;

import java.util.List;

// Text is each "page" of the node and each line of the page.
public record DialogueNode(List<DialoguePage> pages, List<DialogueResponse> responses) {
}
