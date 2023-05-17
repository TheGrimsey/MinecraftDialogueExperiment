package net.thegrimsey.dialogueexperiment;

import java.util.HashMap;

public record Dialogue(HashMap<String, DialogueNode> nodes) {}
