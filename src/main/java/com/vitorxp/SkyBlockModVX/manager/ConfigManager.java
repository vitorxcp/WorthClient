package com.vitorxp.SkyBlockModVX.manager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vitorxp.SkyBlockModVX.SkyBlockMod;

import java.io.*;

public class ConfigManager {

    private static final File FILE = new File("config/SkyBlockModVX/settings.json");

    public static void save() {
        JsonObject obj = new JsonObject();
        obj.addProperty("petBlock", SkyBlockMod.blockPetMessages);
        obj.addProperty("inventoryBlock", SkyBlockMod.blockInventoryMessages);
        obj.addProperty("announceZealot", SkyBlockMod.announceZealot);
        obj.addProperty("MsgBlockDestroyBlock", SkyBlockMod.MsgBlockDestroyBlock);
        obj.addProperty("petOverlay", SkyBlockMod.petOverlay);
        obj.addProperty("pingOverlay", SkyBlockMod.pingOverlay);
        obj.addProperty("fpsOverlay", SkyBlockMod.fpsOverlay);
        obj.addProperty("mainHandHUDOverlay", SkyBlockMod.mainHandHUDOverlay);
        obj.addProperty("helmetHUDOverlay", SkyBlockMod.helmetHUDOverlay);
        obj.addProperty("chestplateHUDOverlay", SkyBlockMod.chestplateHUDOverlay);
        obj.addProperty("leggingsHUDOverlay", SkyBlockMod.leggingsHUDOverlay);
        obj.addProperty("bootsHUDOverlay", SkyBlockMod.bootsHUDOverlay);
        obj.addProperty("petDisplayViewOff", SkyBlockMod.petDisplayViewOff);
        obj.addProperty("viewsPetAll", SkyBlockMod.viewsPetAll);
        obj.addProperty("showTime", SkyBlockMod.showTime);
        obj.addProperty("enableCopy", SkyBlockMod.enableCopy);
        obj.addProperty("keystrokesOverlay", SkyBlockMod.keystrokesOverlay);

        try (FileWriter writer = new FileWriter(FILE)) {
            new Gson().toJson(obj, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void load() {
        if (!FILE.exists()) return;

        try (FileReader reader = new FileReader(FILE)) {
            JsonObject obj = new Gson().fromJson(reader, JsonObject.class);
            if (obj.has("petBlock")) SkyBlockMod.blockPetMessages = obj.get("petBlock").getAsBoolean();
            if (obj.has("inventoryBlock")) SkyBlockMod.blockInventoryMessages = obj.get("inventoryBlock").getAsBoolean();
            if (obj.has("announceZealot")) SkyBlockMod.announceZealot = obj.get("announceZealot").getAsBoolean();
            if (obj.has("MsgBlockDestroyBlock")) SkyBlockMod.MsgBlockDestroyBlock = obj.get("MsgBlockDestroyBlock").getAsBoolean();
            if (obj.has("petOverlay")) SkyBlockMod.petOverlay = obj.get("petOverlay").getAsBoolean();
            if (obj.has("pingOverlay")) SkyBlockMod.pingOverlay = obj.get("pingOverlay").getAsBoolean();
            if (obj.has("fpsOverlay")) SkyBlockMod.fpsOverlay = obj.get("fpsOverlay").getAsBoolean();
            if (obj.has("mainHandHUDOverlay")) SkyBlockMod.mainHandHUDOverlay = obj.get("mainHandHUDOverlay").getAsBoolean();
            if (obj.has("helmetHUDOverlay")) SkyBlockMod.helmetHUDOverlay = obj.get("helmetHUDOverlay").getAsBoolean();
            if (obj.has("chestplateHUDOverlay")) SkyBlockMod.chestplateHUDOverlay = obj.get("chestplateHUDOverlay").getAsBoolean();
            if (obj.has("leggingsHUDOverlay")) SkyBlockMod.leggingsHUDOverlay = obj.get("leggingsHUDOverlay").getAsBoolean();
            if (obj.has("bootsHUDOverlay")) SkyBlockMod.bootsHUDOverlay = obj.get("bootsHUDOverlay").getAsBoolean();
            if (obj.has("petDisplayViewOff")) SkyBlockMod.petDisplayViewOff = obj.get("petDisplayViewOff").getAsBoolean();
            if (obj.has("viewsPetAll")) SkyBlockMod.viewsPetAll = obj.get("viewsPetAll").getAsBoolean();
            if (obj.has("showTime")) SkyBlockMod.showTime = obj.get("showTime").getAsBoolean();
            if (obj.has("enableCopy")) SkyBlockMod.enableCopy = obj.get("enableCopy").getAsBoolean();
            if (obj.has("keystrokesOverlay")) SkyBlockMod.keystrokesOverlay = obj.get("keystrokesOverlay").getAsBoolean();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}