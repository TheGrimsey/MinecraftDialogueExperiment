package net.thegrimsey.dialogueexperiment;

import com.google.gson.JsonObject;
import io.github.apace100.apoli.power.factory.condition.EntityConditions;
import io.github.apace100.apoli.registry.ApoliRegistries;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.entity.Entity;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import javax.swing.text.html.Option;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class DialogueDataLoader implements SimpleSynchronousResourceReloadListener {
    public static final HashMap<Identifier, Dialogue> dialogues = new HashMap<>();

    @Override
    public Identifier getFabricId() {
        return new Identifier(DialogueExperiment.MODID, "dialogues");
    }

    @Override
    public void reload(ResourceManager manager) {
        dialogues.clear();

        for(Identifier id: manager.findResources("dialogues", path -> path.endsWith(".json"))) {
            try(InputStream stream = manager.getResource(id).getInputStream()) {
                InputStreamReader reader = new InputStreamReader(stream);

                JsonObject jsonObject = JsonHelper.deserialize(reader);

                HashMap<String, DialogueNode> nodes = new HashMap<>();
                jsonObject.getAsJsonObject("entries").entrySet().forEach(stringJsonElementEntry -> {
                    var key = stringJsonElementEntry.getKey();
                    var value = stringJsonElementEntry.getValue().getAsJsonObject();

                    String speaker = value.get("speaker").getAsString();
                    String text = value.get("text").getAsString();

                    List<DialogueResponse> responses = new ArrayList<>();

                    value.getAsJsonArray("responses").forEach(jsonElement -> {
                        var asObject = jsonElement.getAsJsonObject();

                        String responseText = asObject.get("text").getAsString();
                        String targetNode = asObject.get("targetNode").getAsString();

                        Optional<Predicate<Entity>> condition = Optional.empty();
                        if(asObject.has("condition")) {
                            var conditionJson = asObject.get("condition").getAsJsonObject();

                            var conditionFactory = ApoliRegistries.ENTITY_CONDITION.get(new Identifier(conditionJson.get("type").getAsString()));

                            if(conditionFactory != null) {
                                condition = Optional.of(conditionFactory.read(conditionJson));
                            } else {
                                DialogueExperiment.LOGGER.error("Couldn't get condition factory :(");
                            }
                        }

                        responses.add(new DialogueResponse(responseText, targetNode, condition));
                    });

                    nodes.put(key, new DialogueNode(speaker, text, responses));
                });

                dialogues.put(id, new Dialogue(nodes));
            } catch (Exception e) {
                DialogueExperiment.LOGGER.error("Error occured whilst loading dialogue: " + id.toString(), e);
            }
        }
    }
}
