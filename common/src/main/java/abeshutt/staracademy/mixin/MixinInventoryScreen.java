package abeshutt.staracademy.mixin;

import abeshutt.staracademy.screen.WardrobeScreen;
import abeshutt.staracademy.screen.WardrobeWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookProvider;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = InventoryScreen.class, priority = 1000)
public abstract class MixinInventoryScreen extends AbstractInventoryScreen<PlayerScreenHandler> implements RecipeBookProvider {

    @Unique private WardrobeWidget wardrobeButton;

    public MixinInventoryScreen(PlayerScreenHandler screenHandler, PlayerInventory playerInventory, Text text) {
        super(screenHandler, playerInventory, text);
    }

    @Inject(method = "init", at = @At("HEAD"))
    protected void init(CallbackInfo ci) {
        if(this.client != null && this.client.interactionManager != null && this.client.interactionManager.hasCreativeInventory()) {
            return;
        }

        this.wardrobeButton = this.addDrawableChild(new WardrobeWidget(this, this.x + 160 - 14 - 120 + 1, this.y + 5 + 14 + 48, widget -> {
            MinecraftClient.getInstance().setScreen(new WardrobeScreen());
        }));

        this.addSelectableChild(this.wardrobeButton);
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if(this.wardrobeButton != null) {
            this.wardrobeButton.setPosition(this.x + 160 - 14 - 120 + 1, this.y + 5 + 14 + 48);
        }
    }

}
