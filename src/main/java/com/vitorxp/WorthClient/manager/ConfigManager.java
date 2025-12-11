package com.vitorxp.WorthClient.manager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.*;

public class ConfigManager {

    private static final File FILE = new File("config/WorthClient/settings.json");

    public static void save() {
        JsonObject obj = new JsonObject();
        obj.addProperty("petBlock", com.vitorxp.WorthClient.WorthClient.blockPetMessages);
        obj.addProperty("inventoryBlock", com.vitorxp.WorthClient.WorthClient.blockInventoryMessages);
        obj.addProperty("announceZealot", com.vitorxp.WorthClient.WorthClient.announceZealot);
        obj.addProperty("MsgBlockDestroyBlock", com.vitorxp.WorthClient.WorthClient.MsgBlockDestroyBlock);
        obj.addProperty("petOverlay", com.vitorxp.WorthClient.WorthClient.petOverlay);
        obj.addProperty("pingOverlay", com.vitorxp.WorthClient.WorthClient.pingOverlay);
        obj.addProperty("fpsOverlay", com.vitorxp.WorthClient.WorthClient.fpsOverlay);
        obj.addProperty("mainHandHUDOverlay", com.vitorxp.WorthClient.WorthClient.mainHandHUDOverlay);
        obj.addProperty("helmetHUDOverlay", com.vitorxp.WorthClient.WorthClient.helmetHUDOverlay);
        obj.addProperty("chestplateHUDOverlay", com.vitorxp.WorthClient.WorthClient.chestplateHUDOverlay);
        obj.addProperty("leggingsHUDOverlay", com.vitorxp.WorthClient.WorthClient.leggingsHUDOverlay);
        obj.addProperty("bootsHUDOverlay", com.vitorxp.WorthClient.WorthClient.bootsHUDOverlay);
        obj.addProperty("petDisplayViewOff", com.vitorxp.WorthClient.WorthClient.petDisplayViewOff);
        obj.addProperty("viewsPetAll", com.vitorxp.WorthClient.WorthClient.viewsPetAll);
        obj.addProperty("showTime", com.vitorxp.WorthClient.WorthClient.showTime);
        obj.addProperty("enableCopy", com.vitorxp.WorthClient.WorthClient.enableCopy);
        obj.addProperty("keystrokesOverlay", com.vitorxp.WorthClient.WorthClient.keystrokesOverlay);
        obj.addProperty("PerspectiveModToggle", com.vitorxp.WorthClient.WorthClient.PerspectiveModToggle);
        obj.addProperty("ArmorsOverlays", com.vitorxp.WorthClient.WorthClient.ArmorsOverlays);
        obj.addProperty("RadarOverlay", com.vitorxp.WorthClient.WorthClient.RadarOverlay);

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
            if (obj.has("petBlock")) com.vitorxp.WorthClient.WorthClient.blockPetMessages = obj.get("petBlock").getAsBoolean();
            if (obj.has("inventoryBlock")) com.vitorxp.WorthClient.WorthClient.blockInventoryMessages = obj.get("inventoryBlock").getAsBoolean();
            if (obj.has("announceZealot")) com.vitorxp.WorthClient.WorthClient.announceZealot = obj.get("announceZealot").getAsBoolean();
            if (obj.has("MsgBlockDestroyBlock")) com.vitorxp.WorthClient.WorthClient.MsgBlockDestroyBlock = obj.get("MsgBlockDestroyBlock").getAsBoolean();
            if (obj.has("petOverlay")) com.vitorxp.WorthClient.WorthClient.petOverlay = obj.get("petOverlay").getAsBoolean();
            if (obj.has("pingOverlay")) com.vitorxp.WorthClient.WorthClient.pingOverlay = obj.get("pingOverlay").getAsBoolean();
            if (obj.has("fpsOverlay")) com.vitorxp.WorthClient.WorthClient.fpsOverlay = obj.get("fpsOverlay").getAsBoolean();
            if (obj.has("mainHandHUDOverlay")) com.vitorxp.WorthClient.WorthClient.mainHandHUDOverlay = obj.get("mainHandHUDOverlay").getAsBoolean();
            if (obj.has("helmetHUDOverlay")) com.vitorxp.WorthClient.WorthClient.helmetHUDOverlay = obj.get("helmetHUDOverlay").getAsBoolean();
            if (obj.has("chestplateHUDOverlay")) com.vitorxp.WorthClient.WorthClient.chestplateHUDOverlay = obj.get("chestplateHUDOverlay").getAsBoolean();
            if (obj.has("leggingsHUDOverlay")) com.vitorxp.WorthClient.WorthClient.leggingsHUDOverlay = obj.get("leggingsHUDOverlay").getAsBoolean();
            if (obj.has("bootsHUDOverlay")) com.vitorxp.WorthClient.WorthClient.bootsHUDOverlay = obj.get("bootsHUDOverlay").getAsBoolean();
            if (obj.has("petDisplayViewOff")) com.vitorxp.WorthClient.WorthClient.petDisplayViewOff = obj.get("petDisplayViewOff").getAsBoolean();
            if (obj.has("viewsPetAll")) com.vitorxp.WorthClient.WorthClient.viewsPetAll = obj.get("viewsPetAll").getAsBoolean();
            if (obj.has("showTime")) com.vitorxp.WorthClient.WorthClient.showTime = obj.get("showTime").getAsBoolean();
            if (obj.has("enableCopy")) com.vitorxp.WorthClient.WorthClient.enableCopy = obj.get("enableCopy").getAsBoolean();
            if (obj.has("keystrokesOverlay")) com.vitorxp.WorthClient.WorthClient.keystrokesOverlay = obj.get("keystrokesOverlay").getAsBoolean();
            if (obj.has("PerspectiveModToggle")) com.vitorxp.WorthClient.WorthClient.PerspectiveModToggle = obj.get("PerspectiveModToggle").getAsBoolean();
            if (obj.has("RadarOverlay")) com.vitorxp.WorthClient.WorthClient.RadarOverlay = obj.get("RadarOverlay").getAsBoolean();
            if (obj.has("ArmorsOverlays")) com.vitorxp.WorthClient.WorthClient.ArmorsOverlays = obj.get("ArmorsOverlays").getAsBoolean();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}