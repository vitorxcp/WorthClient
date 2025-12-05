package com.vitorxp.WorthClient.core;

import com.vitorxp.WorthClient.gui.VXLoadingScreen;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.8.9")
public class VXCoreMod implements IFMLLoadingPlugin {

    public VXCoreMod() {
        System.setProperty("fml.noSplash", "true");

        new Thread(() -> {
            while (!Display.isCreated()) {
                try { Thread.sleep(10); } catch (Exception ignored) {}
            }

            while (true) {
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

                VXLoadingScreen.updateProgress();
                VXLoadingScreen.draw();

                Display.update();
                Display.sync(60);
            }

        }, "VX-Loading-Screen").start();
    }

    @Override public String[] getASMTransformerClass() { return new String[0]; }
    @Override public String getModContainerClass() { return null; }
    @Override public String getSetupClass() { return null; }
    @Override public void injectData(Map<String, Object> data) {}
    @Override public String getAccessTransformerClass() { return null; }
}
