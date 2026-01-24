package com.vitorxp.WorthClient.mixin;

import net.minecraft.client.settings.GameSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.lang.reflect.Field;

@Mixin(GameSettings.class)
public class MixinGameSettings {

    @Inject(method = "loadOptions", at = @At("RETURN"))
    public void onLoadOptions(CallbackInfo ci) {
        disableFastRender();
    }

    @Inject(method = "saveOptions", at = @At("HEAD"))
    public void onSaveOptions(CallbackInfo ci) {
        disableFastRender();
    }

    private void disableFastRender() {
        try {
            GameSettings settings = (GameSettings) (Object) this;

            Field fastRenderField = GameSettings.class.getDeclaredField("ofFastRender");
            fastRenderField.setAccessible(true);

            if (fastRenderField.getBoolean(settings)) {
                fastRenderField.setBoolean(settings, false);
            }
        } catch (NoSuchFieldException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}