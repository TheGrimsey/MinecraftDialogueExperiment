package net.thegrimsey.dialogueexperiment.dialogue;

import java.util.HashMap;

public record Dialogue(HashMap<String, DialogueNode> nodes) {}
