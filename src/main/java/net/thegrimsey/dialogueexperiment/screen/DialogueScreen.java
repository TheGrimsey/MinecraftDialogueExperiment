package net.thegrimsey.dialogueexperiment.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.thegrimsey.dialogueexperiment.DialogueExperiment;
import net.thegrimsey.dialogueexperiment.DialogueNetworking;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class DialogueScreen extends HandledScreen<DialogueScreenHandler> {
    List<OrderedText> wrappedDialogue = new ArrayList<>();

    int textWidth = 300;
    float textX = 0;

    int buttonsHeightOffset = 0;

    final int BACKGROUND_PADDING = 5;

    public DialogueScreen(DialogueScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init() {
        super.init();

        reinit();
    }

    private void onPressResponse(int i) {
        DialogueNetworking.sendResponsePacket(i);
    }

    @Override
    protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
        float textY = (float)(this.height - buttonsHeightOffset - ((textRenderer.fontHeight + 2) * (wrappedDialogue.size() + 1)));

        this.fillGradient(matrices, (int) textX - BACKGROUND_PADDING, (int)textY - BACKGROUND_PADDING, (int) textX + textWidth + BACKGROUND_PADDING, (int)textY + (textRenderer.fontHeight + 2) * wrappedDialogue.size() + BACKGROUND_PADDING, -1072689136, -1072689136);

        // Speaker name.
        int speakerWidth = textRenderer.getWidth(handler.speaker);
        float speakerY = textY - (float)(8 + textRenderer.fontHeight) - BACKGROUND_PADDING;

        this.fillGradient(matrices, (int) textX - BACKGROUND_PADDING, (int) speakerY - BACKGROUND_PADDING, (int) textX + speakerWidth + BACKGROUND_PADDING, (int)speakerY + textRenderer.fontHeight + BACKGROUND_PADDING, -1072689136, -1072689136);
        textRenderer.draw(matrices, handler.speaker, textX, speakerY, 16777215);

        // Text.
        for(int i = 0; i < wrappedDialogue.size(); i++) {
            textRenderer.draw(matrices, wrappedDialogue.get(i), textX, textY + (float)((textRenderer.fontHeight + 2) * i), 16777215);
        }

        // Black background. Text top left.
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {

    }

    public void reinit() {
        textWidth = (int)(this.width * 0.7f);
        textX = (float)(this.width - textWidth) / 2.0f;

        wrappedDialogue = textRenderer.wrapLines(Text.of(handler.text), textWidth);

        this.clearChildren();

        final int buttonHeight = 20;
        final int buttonMargin = 2;

        buttonsHeightOffset = handler.responses.size() * (buttonHeight + buttonMargin) + 10;

        for(int i = 0; i < handler.responses.size(); i++) {
            int x = (int)textX;
            int y = this.height - buttonsHeightOffset + i * (buttonHeight + buttonMargin);

            final int finalI = i;
            addDrawableChild(new ButtonWidget(x, y, textWidth, buttonHeight, Text.of(handler.responses.get(i)), button -> {
                onPressResponse(finalI);
            }));
        }
    }
}
