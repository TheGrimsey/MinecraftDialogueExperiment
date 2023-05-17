package net.thegrimsey.dialogueexperiment;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.thegrimsey.dialogueexperiment.screen.DialogueScreen;

public class DialogueExperimentClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ScreenRegistry.register(DialogueExperiment.DIALOGUE_SCREENHANDLER, DialogueScreen::new);
    }
}
