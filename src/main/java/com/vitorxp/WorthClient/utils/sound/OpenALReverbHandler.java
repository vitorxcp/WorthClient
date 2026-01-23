package com.vitorxp.WorthClient.utils.sound;

import com.vitorxp.WorthClient.handlers.SoundEnvironmentHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.util.BlockPos;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.EFX10;
import paulscode.sound.Library;
import paulscode.sound.SoundSystem;
import paulscode.sound.libraries.ChannelLWJGLOpenAL;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;
import paulscode.sound.libraries.SourceLWJGLOpenAL;

import java.lang.reflect.Field;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

public class OpenALReverbHandler {

    private int auxEffectSlot = -1;
    private int reverbEffect = -1;
    private boolean initialized = false;
    private boolean supported = false;
    private Field sndManagerField, sndSystemField, libField, sourceMapField, channelField, alSourceField;
    private Field sourcePositionField;
    private SoundSystem currentSoundSystem;
    private HashMap<String, ?> cachedSourceMap;

    public boolean isInitialized() {
        return initialized;
    }
    public void init() {
        try {
            AL10.alGetError();
            if (auxEffectSlot == -1) {
                auxEffectSlot = EFX10.alGenAuxiliaryEffectSlots();
                if (AL10.alGetError() != AL10.AL_NO_ERROR || auxEffectSlot == 0) {
                    if (AL10.alIsExtensionPresent("ALC_EXT_EFX")) {
                        auxEffectSlot = EFX10.alGenAuxiliaryEffectSlots();
                    }
                }
            }
            if (auxEffectSlot == 0) {
                System.out.println("[WorthClient] Reverb não suportado (Sem Slot Auxiliar).");
                supported = false;
                return;
            }
            EFX10.alAuxiliaryEffectSloti(auxEffectSlot, EFX10.AL_EFFECTSLOT_AUXILIARY_SEND_AUTO, AL10.AL_TRUE);
            if (reverbEffect == -1) reverbEffect = EFX10.alGenEffects();
            EFX10.alEffecti(reverbEffect, EFX10.AL_EFFECT_TYPE, EFX10.AL_EFFECT_REVERB);
            EFX10.alAuxiliaryEffectSloti(auxEffectSlot, EFX10.AL_EFFECTSLOT_EFFECT, reverbEffect);
            prepareReflectionFields();
            supported = true;
        } catch (Exception e) {
            e.printStackTrace();
            supported = false;
        }
    }

    public void updateReverbEnvironment(SoundEnvironmentHandler envHandler) {
        if (!supported) return;
        checkSoundSystemValidity();
        if (!initialized || cachedSourceMap == null) return;

        try {
            Minecraft mc = Minecraft.getMinecraft();
            BlockPos playerPos = new BlockPos(mc.thePlayer);

            float playerDensity = envHandler.getDensityAtPosition(playerPos);
            float maxDensityDetected = playerDensity;
            Object[] sources = cachedSourceMap.values().toArray();
            float[] sourcesDensities = new float[sources.length];

            for (int i = 0; i < sources.length; i++) {
                Object sourceObj = sources[i];
                if (sourceObj instanceof SourceLWJGLOpenAL) {
                    SourceLWJGLOpenAL source = (SourceLWJGLOpenAL) sourceObj;
                    if (!source.playing()) {
                        sourcesDensities[i] = -1f;
                        continue;
                    }

                    FloatBuffer posBuffer = (FloatBuffer) sourcePositionField.get(source);
                    if (posBuffer != null && posBuffer.capacity() >= 3) {
                        float x = posBuffer.get(0);
                        float y = posBuffer.get(1);
                        float z = posBuffer.get(2);

                        double distSq = mc.thePlayer.getDistanceSq(x, y, z);
                        float density;

                        if (distSq < 25) {
                            density = playerDensity;
                        } else {
                            density = envHandler.getDensityAtPosition(new BlockPos(x, y, z));
                        }

                        sourcesDensities[i] = density;
                        if (density > maxDensityDetected) {
                            maxDensityDetected = density;
                        }
                    } else {
                        sourcesDensities[i] = playerDensity;
                    }
                }
            }

            configureReverbEffect(maxDensityDetected);

            for (int i = 0; i < sources.length; i++) {
                if (sourcesDensities[i] == -1f) continue;

                Object sourceObj = sources[i];
                SourceLWJGLOpenAL source = (SourceLWJGLOpenAL) sourceObj;
                Object channelObj = channelField.get(source);
                if (channelObj == null) continue;
                ChannelLWJGLOpenAL channel = (ChannelLWJGLOpenAL) channelObj;

                int sourceId;
                Object rawId = alSourceField.get(channel);
                if (rawId instanceof IntBuffer) sourceId = ((IntBuffer) rawId).get(0);
                else sourceId = (Integer) rawId;

                if (sourceId > 0 && AL10.alIsSource(sourceId)) {
                    float myDensity = sourcesDensities[i];
                    int targetSlot = (myDensity > 0.1f) ? auxEffectSlot : 0;
                    AL11.alSource3i(sourceId, EFX10.AL_AUXILIARY_SEND_FILTER, targetSlot, 0, EFX10.AL_FILTER_NULL);
                }
            }

        } catch (Exception e) { }
    }

    private void configureReverbEffect(float density) {
        try {
            if (density <= 0.1f) {
                EFX10.alEffectf(reverbEffect, EFX10.AL_REVERB_DECAY_TIME, 0.1f);
                EFX10.alEffectf(reverbEffect, EFX10.AL_REVERB_GAIN, 0.0f);
            } else {
                float gain, decay, diffusion;

                if (density < 0.4f) {
                    gain = 0.5f;
                    decay = 1.2f;
                    diffusion = 0.8f;
                } else if (density < 0.7f) {
                    gain = 0.8f;
                    decay = 2.5f;
                    diffusion = 0.7f;
                } else {
                    gain = 1.0f;
                    decay = 5.0f + (density * 2.0f);
                    diffusion = 0.6f;
                }

                EFX10.alEffectf(reverbEffect, EFX10.AL_REVERB_DENSITY, 1.0f);
                EFX10.alEffectf(reverbEffect, EFX10.AL_REVERB_DIFFUSION, diffusion);
                EFX10.alEffectf(reverbEffect, EFX10.AL_REVERB_GAIN, gain);
                EFX10.alEffectf(reverbEffect, EFX10.AL_REVERB_DECAY_TIME, decay);
                EFX10.alEffectf(reverbEffect, EFX10.AL_REVERB_GAINHF, 0.9f);
            }

            EFX10.alAuxiliaryEffectSloti(auxEffectSlot, EFX10.AL_EFFECTSLOT_EFFECT, reverbEffect);
            AL10.alGetError();
        } catch (Exception ignored) {}
    }

    private void checkSoundSystemValidity() {
        try {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.getSoundHandler() == null) return;
            SoundManager sndManager = (SoundManager) sndManagerField.get(mc.getSoundHandler());
            SoundSystem activeSystem = (SoundSystem) sndSystemField.get(sndManager);

            if (activeSystem != currentSoundSystem || cachedSourceMap == null) {
                Library library = (Library) libField.get(activeSystem);
                if (library instanceof LibraryLWJGLOpenAL) {
                    this.cachedSourceMap = (HashMap<String, ?>) sourceMapField.get(library);
                    this.currentSoundSystem = activeSystem;
                    this.initialized = true;
                } else {
                    this.initialized = false;
                }
            }
        } catch (Exception e) {
            this.initialized = false;
        }
    }

    private void prepareReflectionFields() throws Exception {
        sndManagerField = getField(SoundHandler.class, "sndManager", "field_147694_f");
        sndSystemField = getField(SoundManager.class, "sndSystem", "field_148620_e");
        libField = getField(SoundSystem.class, "soundLibrary");
        sourceMapField = getField(Library.class, "sourceMap");
        sourcePositionField = getField(SourceLWJGLOpenAL.class, "sourcePosition");
        channelField = getField(SourceLWJGLOpenAL.class, "channelOpenAL");
        try {
            alSourceField = getField(ChannelLWJGLOpenAL.class, "ALSource", "alSource");
        } catch (Exception e) {
            for (Field f : ChannelLWJGLOpenAL.class.getDeclaredFields()) {
                if (f.getType() == int.class || f.getType() == IntBuffer.class) {
                    f.setAccessible(true);
                    alSourceField = f;
                    break;
                }
            }
        }
    }

    private Field getField(Class<?> clazz, String... names) throws NoSuchFieldException {
        for (String name : names) {
            try {
                Field f = clazz.getDeclaredField(name);
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException ignored) {
            }
        }
        throw new NoSuchFieldException(names[0]);
    }
}