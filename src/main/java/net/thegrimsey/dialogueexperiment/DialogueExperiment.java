package net.thegrimsey.dialogueexperiment;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.thegrimsey.dialogueexperiment.screen.DialogueScreenFactory;
import net.thegrimsey.dialogueexperiment.screen.DialogueScreenHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

public class DialogueExperiment implements ModInitializer {
	public static final String MODID = "dialogueexperiment";

	public static final ScreenHandlerType<DialogueScreenHandler> DIALOGUE_SCREENHANDLER = ScreenHandlerRegistry.registerExtended(new Identifier(MODID, "dialogue"), DialogueScreenHandler::new);
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	@Override
	public void onInitialize() {
		DialogueNetworking.registerNetworking();

		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new DialogueDataLoader());

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(CommandManager.literal("dialogue").then(CommandManager.argument("dialogue", IdentifierArgumentType.identifier()).executes(context -> {
			Identifier dialogue = IdentifierArgumentType.getIdentifier(context, "dialogue");

			Dialogue dialogueObj = DialogueDataLoader.dialogues.get(dialogue);

			if(dialogueObj != null) {
				context.getSource().getPlayer().openHandledScreen(new DialogueScreenFactory(dialogueObj));
			}

			return 1;
		}))));

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(CommandManager.literal("dialogues").executes(context -> {
			StringBuilder dialogues = new StringBuilder();
			dialogues.append("Available dialogues: ");

			DialogueDataLoader.dialogues.keySet().forEach(identifier -> {
				dialogues.append(identifier.toString()).append(", ");
			});

			context.getSource().sendFeedback(Text.of(dialogues.toString()), false);
			return 1;
		})));

		LOGGER.info("Hello Fabric world!");
	}
}
