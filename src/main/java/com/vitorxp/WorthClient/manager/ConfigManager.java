package com.vitorxp.WorthClient.manager;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.vitorxp.WorthClient.WorthClient;

import java.io.*;

public class ConfigManager {

    private static final File FILE = new File("config/WorthClient/settings.json");

    public static void save() {
        JsonObject obj = new JsonObject();
        obj.addProperty("petBlock", WorthClient.blockPetMessages);
        obj.addProperty("inventoryBlock", WorthClient.blockInventoryMessages);
        obj.addProperty("announceZealot", WorthClient.announceZealot);
        obj.addProperty("MsgBlockDestroyBlock", WorthClient.MsgBlockDestroyBlock);
        obj.addProperty("petOverlay", WorthClient.petOverlay);
        obj.addProperty("pingOverlay", WorthClient.pingOverlay);
        obj.addProperty("fpsOverlay", WorthClient.fpsOverlay);
        obj.addProperty("mainHandHUDOverlay", WorthClient.mainHandHUDOverlay);
        obj.addProperty("helmetHUDOverlay", WorthClient.helmetHUDOverlay);
        obj.addProperty("chestplateHUDOverlay", WorthClient.chestplateHUDOverlay);
        obj.addProperty("leggingsHUDOverlay", WorthClient.leggingsHUDOverlay);
        obj.addProperty("bootsHUDOverlay", WorthClient.bootsHUDOverlay);
        obj.addProperty("petDisplayViewOff", WorthClient.petDisplayViewOff);
        obj.addProperty("viewsPetAll", WorthClient.viewsPetAll);
        obj.addProperty("showTime", WorthClient.showTime);
        obj.addProperty("enableCopy", WorthClient.enableCopy);
        obj.addProperty("keystrokesOverlay", WorthClient.keystrokesOverlay);
        obj.addProperty("PerspectiveModToggle", WorthClient.PerspectiveModToggle);
        obj.addProperty("ArmorsOverlays", WorthClient.ArmorsOverlays);
        obj.addProperty("RadarOverlay", WorthClient.RadarOverlay);
        obj.addProperty("KeyPerspective", WorthClient.KeyPerspective);
        obj.addProperty("WailaMod", WorthClient.WailaMod);
        obj.addProperty("KeyZoom", WorthClient.KeyZoom);
        obj.addProperty("enableToggleZoom", WorthClient.enableToggleZoom);
        obj.addProperty("PerspectiveStartFront", WorthClient.PerspectiveStartFront);
        obj.addProperty(("AutoLoginEnabled"), WorthClient.AutoLoginEnabled);

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
            if (obj.has("petBlock")) WorthClient.blockPetMessages = obj.get("petBlock").getAsBoolean();
            if (obj.has("inventoryBlock")) WorthClient.blockInventoryMessages = obj.get("inventoryBlock").getAsBoolean();
            if (obj.has("announceZealot")) WorthClient.announceZealot = obj.get("announceZealot").getAsBoolean();
            if (obj.has("MsgBlockDestroyBlock")) WorthClient.MsgBlockDestroyBlock = obj.get("MsgBlockDestroyBlock").getAsBoolean();
            if (obj.has("petOverlay")) WorthClient.petOverlay = obj.get("petOverlay").getAsBoolean();
            if (obj.has("pingOverlay")) WorthClient.pingOverlay = obj.get("pingOverlay").getAsBoolean();
            if (obj.has("fpsOverlay")) WorthClient.fpsOverlay = obj.get("fpsOverlay").getAsBoolean();
            if (obj.has("mainHandHUDOverlay")) WorthClient.mainHandHUDOverlay = obj.get("mainHandHUDOverlay").getAsBoolean();
            if (obj.has("helmetHUDOverlay")) WorthClient.helmetHUDOverlay = obj.get("helmetHUDOverlay").getAsBoolean();
            if (obj.has("chestplateHUDOverlay")) WorthClient.chestplateHUDOverlay = obj.get("chestplateHUDOverlay").getAsBoolean();
            if (obj.has("leggingsHUDOverlay")) WorthClient.leggingsHUDOverlay = obj.get("leggingsHUDOverlay").getAsBoolean();
            if (obj.has("bootsHUDOverlay")) WorthClient.bootsHUDOverlay = obj.get("bootsHUDOverlay").getAsBoolean();
            if (obj.has("petDisplayViewOff")) WorthClient.petDisplayViewOff = obj.get("petDisplayViewOff").getAsBoolean();
            if (obj.has("viewsPetAll")) WorthClient.viewsPetAll = obj.get("viewsPetAll").getAsBoolean();
            if (obj.has("showTime")) WorthClient.showTime = obj.get("showTime").getAsBoolean();
            if (obj.has("enableCopy")) WorthClient.enableCopy = obj.get("enableCopy").getAsBoolean();
            if (obj.has("keystrokesOverlay")) WorthClient.keystrokesOverlay = obj.get("keystrokesOverlay").getAsBoolean();
            if (obj.has("PerspectiveModToggle")) WorthClient.PerspectiveModToggle = obj.get("PerspectiveModToggle").getAsBoolean();
            if (obj.has("RadarOverlay")) WorthClient.RadarOverlay = obj.get("RadarOverlay").getAsBoolean();
            if (obj.has("ArmorsOverlays")) WorthClient.ArmorsOverlays = obj.get("ArmorsOverlays").getAsBoolean();
            if (obj.has("KeyPerspective")) WorthClient.KeyPerspective = obj.get("KeyPerspective").getAsInt();
            if (obj.has("WailaMod")) WorthClient.WailaMod = obj.get("WailaMod").getAsBoolean();
            if (obj.has("KeyZoom")) WorthClient.KeyZoom = obj.get("KeyZoom").getAsInt();
            if (obj.has("enableToggleZoom")) WorthClient.enableToggleZoom = obj.get("enableToggleZoom").getAsBoolean();
            if (obj.has("PerspectiveStartFront")) WorthClient.PerspectiveStartFront = obj.get("PerspectiveStartFront").getAsBoolean();
            if(obj.has("AutoLoginEnabled")) WorthClient.AutoLoginEnabled = obj.get("AutoLoginEnabled").getAsBoolean();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}