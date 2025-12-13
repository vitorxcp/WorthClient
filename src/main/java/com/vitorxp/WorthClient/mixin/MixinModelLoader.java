package com.vitorxp.WorthClient.mixin;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.apache.logging.log4j.Logger;

@Mixin(ModelLoader.class)
public class MixinModelLoader {

    @Redirect(
            method = "onPostBakeEvent",
            at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;error(Ljava/lang/String;Ljava/lang/Throwable;)V"),
            remap = false
    )
    private void silenceModelErrors(Logger logger, String message, Throwable t) {
        if (message != null && message.contains("Exception loading model")) {
            return;
        }
        logger.error(message, t);
    }
}