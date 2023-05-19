package net.thegrimsey.dialogueexperiment.screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.thegrimsey.dialogueexperiment.DialogueNetworking;
import net.thegrimsey.dialogueexperiment.dialogue.FormattedDialoguePage;

import java.util.ArrayList;
import java.util.List;

@Environment(EnvType.CLIENT)
public class DialogueScreen extends HandledScreen<DialogueScreenHandler> {
    List<OrderedText> wrappedDialogue = new ArrayList<>();

    int textWidth = 300;
    float textX = 0;

    int buttonsHeightOffset = 0;

    final int BACKGROUND_PADDING = 5;

    static final Text CONTINUE_POINTER = Text.of("►");

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
        final float textY = (float)(this.height - buttonsHeightOffset - ((textRenderer.fontHeight + 2) * (wrappedDialogue.size() + 1)));

        final int textHeight = (textRenderer.fontHeight + 2) * wrappedDialogue.size();

        this.fillGradient(matrices, (int) textX - BACKGROUND_PADDING, (int)textY - BACKGROUND_PADDING, (int) textX + textWidth + BACKGROUND_PADDING, (int)textY + textHeight + BACKGROUND_PADDING, -1072689136, -1072689136);

        // Speaker name.
        final Text speaker = handler.getCurrentPage().speaker();
        final int speakerWidth = textRenderer.getWidth(speaker);
        final float speakerY = textY - (float)(8 + textRenderer.fontHeight) - BACKGROUND_PADDING;

        this.fillGradient(matrices, (int) textX - BACKGROUND_PADDING, (int) speakerY - BACKGROUND_PADDING, (int) textX + speakerWidth + BACKGROUND_PADDING, (int)speakerY + textRenderer.fontHeight + BACKGROUND_PADDING, -1072689136, -1072689136);
        textRenderer.draw(matrices, speaker, textX, speakerY, 16777215);

        // Text.
        for(int i = 0; i < wrappedDialogue.size(); i++) {
            textRenderer.draw(matrices, wrappedDialogue.get(i), textX, textY + (float)((textRenderer.fontHeight + 2) * i), 16777215);
        }

        if(!handler.isFinalPage()) {
            int width = textRenderer.getWidth(CONTINUE_POINTER);
            int offset = (int)(Math.sin(client.world.getTime() / 3.5) * 3);
            textRenderer.draw(matrices, CONTINUE_POINTER, textX + textWidth - width - offset, textY + textHeight - textRenderer.fontHeight / 2,16777215);
        }

        // Black background. Text top left.
    }

    @Override
    protected void drawForeground(MatrixStack matrices, int mouseX, int mouseY) {

    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if(!handler.isFinalPage()) {
            handler.currentPage++;
            this.reinit();
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    public void reinit() {
        textWidth = (int)(this.width * 0.7f);
        textX = (float)(this.width - textWidth) / 2.0f;

        wrappedDialogue.clear();
        FormattedDialoguePage page = handler.getCurrentPage();
        for(Text line : page.lines()) {
            if(line.asString().isEmpty()) {
                wrappedDialogue.add(OrderedText.EMPTY);
            } else {
                wrappedDialogue.addAll(textRenderer.wrapLines(line, textWidth));
            }
        }

        this.clearChildren();

        if(handler.isFinalPage()) {
            final int buttonHeight = 20;
            final int buttonMargin = 2;

            buttonsHeightOffset = handler.responses.size() * (buttonHeight + buttonMargin) + 10;

            for(int i = 0; i < handler.responses.size(); i++) {
                int x = (int)textX;
                int y = this.height - buttonsHeightOffset + i * (buttonHeight + buttonMargin);

                final int finalI = i;
                addDrawableChild(new ButtonWidget(x, y, textWidth, buttonHeight, handler.responses.get(i), button -> {
                    onPressResponse(finalI);
                }));
            }
        } else {
            buttonsHeightOffset = 0;
        }
    }
}
