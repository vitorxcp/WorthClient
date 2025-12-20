package com.vitorxp.WorthClient.mixin;

import net.minecraft.util.EnumChatFormatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.regex.Pattern;

@Mixin(EnumChatFormatting.class)
public class MixinEnumChatFormatting {

    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)" + String.valueOf('\u00a7') + "[0-9A-FK-OR]");

    /**
     * @author
     * @reason
     */
    @Overwrite
    public static String getTextWithoutFormattingCodes(String text) {
        return text == null ? null : STRIP_COLOR_PATTERN.matcher(text).replaceAll("");
    }

    @Shadow
    public String toString() { return null; }
}