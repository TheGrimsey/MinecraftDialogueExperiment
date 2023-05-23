package net.thegrimsey.dialogueexperiment.dialogue;

import com.google.gson.JsonObject;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.entity.Entity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.thegrimsey.dialogueexperiment.DialogueExperiment;
import net.thegrimsey.dialogueexperiment.dialogue.Dialogue;
import net.thegrimsey.dialogueexperiment.dialogue.DialogueNode;
import net.thegrimsey.dialogueexperiment.dialogue.DialogueResponse;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class DialogueDataLoader implements SimpleSynchronousResourceReloadListener {
    public static final HashMap<Identifier, Dialogue> dialogues = new HashMap<>();
    static final String EXTENSION = ".json";
    static final String STARTING_PATH = "dialogues";

    @Override
    public Identifier getFabricId() {
        return new Identifier(DialogueExperiment.MODID, STARTING_PATH);
    }

    @Override
    public void reload(ResourceManager manager) {
        dialogues.clear();


        for(Identifier id: manager.findResources(STARTING_PATH, path -> path.endsWith(EXTENSION))) {
            try(InputStream stream = manager.getResource(id).getInputStream()) {
                InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);

                JsonObject jsonObject = JsonHelper.deserialize(reader);

                var entries = jsonObject.getAsJsonObject("entries");
                HashMap<String, DialogueNode> nodes = new HashMap<>(entries.size());
                entries.entrySet().forEach(stringJsonElementEntry -> {
                    var key = stringJsonElementEntry.getKey();
                    var value = stringJsonElementEntry.getValue().getAsJsonObject();

                    var jsonPages = value.getAsJsonArray("pages");
                    List<DialoguePage> pages = new ArrayList<>(jsonPages.size());

                    jsonPages.forEach(jsonElement -> {
                        var asObject = jsonElement.getAsJsonObject();

                        var jsonTextLines = asObject.getAsJsonArray("lines");

                        String speaker = asObject.get("speaker").getAsString();
                        List<String> textLines = new ArrayList<>(jsonTextLines.size());

                        jsonTextLines.forEach(textLine -> textLines.add(textLine.getAsString()));

                        pages.add(new DialoguePage(speaker, textLines));
                    });

                    List<DialogueResponse> responses = new ArrayList<>();

                    value.getAsJsonArray("responses").forEach(jsonElement -> {
                        var asObject = jsonElement.getAsJsonObject();

                        String responseText = asObject.get("text").getAsString();
                        String targetNode = asObject.get("targetNode").getAsString();

                        Optional<Predicate<Entity>> condition = Optional.empty();
                        if(asObject.has("condition")) {
                            var conditionJson = asObject.getAsJsonObject("condition");

                            var conditionFactory = ApoliRegistries.ENTITY_CONDITION.get(new Identifier(conditionJson.get("type").getAsString()));

                            if(conditionFactory != null) {
                                condition = Optional.of(conditionFactory.read(conditionJson));
                            } else {
                                DialogueExperiment.LOGGER.error("Couldn't get condition factory :(");
                            }
                        }

                        Optional<List<String>> commands = Optional.empty();
                        if(asObject.has("commands")) {
                            var commandsJson = asObject.getAsJsonArray("commands");

                            List<String> commandsList = new ArrayList<>(commandsJson.size());
                            commandsJson.forEach(commandElement -> commandsList.add(commandElement.getAsString()));

                            commands = Optional.of(commandsList);
                        }

                        responses.add(new DialogueResponse(responseText, targetNode, condition, commands));
                    });

                    nodes.put(key, new DialogueNode(pages, responses));
                });

                Identifier shortenedId = new Identifier(id.getNamespace(), id.getPath().substring(STARTING_PATH.length() + 1, id.getPath().length() - EXTENSION.length()));

                dialogues.put(shortenedId, new Dialogue(nodes));
            } catch (Exception e) {
                DialogueExperiment.LOGGER.error("Error occured whilst loading dialogue: " + id.toString(), e);
            }
        }
    }
}
