package net.thegrimsey.dialogueexperiment.dialogue;

import net.minecraft.entity.Entity;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public record DialogueResponse(String text, String targetNode, Optional<Predicate<Entity>> condition, Optional<List<String>> commands) {
}
