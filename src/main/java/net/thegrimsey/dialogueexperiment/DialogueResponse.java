package net.thegrimsey.dialogueexperiment;

import net.minecraft.entity.Entity;

import java.util.Optional;
import java.util.function.Predicate;

public record DialogueResponse(String text, String targetNode, Optional<Predicate<Entity>> condition) {
}
