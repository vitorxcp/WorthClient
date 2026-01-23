package com.vitorxp.WorthClient.utils.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import org.lwjgl.openal.EFX10;
import paulscode.sound.Library;
import paulscode.sound.SoundSystem;
import paulscode.sound.libraries.ChannelLWJGLOpenAL;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;
import paulscode.sound.libraries.SourceLWJGLOpenAL;

import java.lang.reflect.Field;
import java.nio.IntBuffer;
import java.util.HashMap;

public class OpenALReverbHandler {

    private int auxEffectSlot = -1;
    private int reverbEffect = -1;
    private boolean initialized = false;
    private boolean supported = false;
    private Field sndManagerField, sndSystemField, libField, sourceMapField, channelField, alSourceField;
    private SoundSystem currentSoundSystem;
    private HashMap<String, ?> cachedSourceMap;

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
                System.out.println("[WorthClient] Hardware não suporta Reverb (EFX).");
                return;
            }
            EFX10.alAuxiliaryEffectSloti(auxEffectSlot, EFX10.AL_EFFECTSLOT_AUXILIARY_SEND_AUTO, AL10.AL_TRUE);
            if (reverbEffect == -1) reverbEffect = EFX10.alGenEffects();
            EFX10.alEffecti(reverbEffect, EFX10.AL_EFFECT_TYPE, EFX10.AL_EFFECT_REVERB);
            EFX10.alAuxiliaryEffectSloti(auxEffectSlot, EFX10.AL_EFFECTSLOT_EFFECT, reverbEffect);
            prepareReflectionFields();
            supported = true;
            System.out.println("[WorthClient] Sistema de Reverb V2 Inicializado.");
        } catch (Exception e) {
            e.printStackTrace();
            supported = false;
        }
    }

    public void updateReverbEnvironment(float density) {
        if (!supported) return;
        checkSoundSystemValidity();
        if (!initialized || cachedSourceMap == null) return;

        try {
            if (density <= 0.1f) {
                EFX10.alEffectf(reverbEffect, EFX10.AL_REVERB_DECAY_TIME, 0.1f);
                EFX10.alEffectf(reverbEffect, EFX10.AL_REVERB_GAIN, 0.0f);
            } else {
                float gain, decay, diffusion;

                if (density < 0.4f) {
                    gain = 0.4f;
                    decay = 0.6f + (density * 0.5f);
                    diffusion = 0.9f;
                } else if (density < 0.7f) {
                    gain = 0.6f;
                    decay = 1.0f + (density * 1.0f);
                    diffusion = 0.7f;
                } else {
                    gain = 1.0f;
                    decay = 1.5f + (density * 3.5f);
                    diffusion = 0.6f;
                }

                EFX10.alEffectf(reverbEffect, EFX10.AL_REVERB_DENSITY, 1.0f);
                EFX10.alEffectf(reverbEffect, EFX10.AL_REVERB_DIFFUSION, diffusion);
                EFX10.alEffectf(reverbEffect, EFX10.AL_REVERB_GAIN, gain);
                EFX10.alEffectf(reverbEffect, EFX10.AL_REVERB_DECAY_TIME, decay);
                EFX10.alEffectf(reverbEffect, EFX10.AL_REVERB_GAINHF, 0.85f);
            }

            EFX10.alAuxiliaryEffectSloti(auxEffectSlot, EFX10.AL_EFFECTSLOT_EFFECT, reverbEffect);

            Object[] sources = cachedSourceMap.values().toArray();
            for (Object sourceObj : sources) {
                if (sourceObj instanceof SourceLWJGLOpenAL) {
                    try {
                        SourceLWJGLOpenAL source = (SourceLWJGLOpenAL) sourceObj;
                        Object channelObj = channelField.get(source);
                        if (channelObj == null) continue;
                        ChannelLWJGLOpenAL channel = (ChannelLWJGLOpenAL) channelObj;

                        int sourceId;
                        Object rawId = alSourceField.get(channel);
                        if (rawId instanceof IntBuffer) sourceId = ((IntBuffer) rawId).get(0);
                        else sourceId = (Integer) rawId;

                        if (sourceId > 0 && AL10.alIsSource(sourceId)) {
                            int targetSlot = (density > 0.1f) ? auxEffectSlot : 0;
                            AL11.alSource3i(sourceId, EFX10.AL_AUXILIARY_SEND_FILTER, targetSlot, 0, EFX10.AL_FILTER_NULL);
                        }
                    } catch (Exception ignored) {
                    }
                }
            }
            AL10.alGetError();
        } catch (Exception e) {
        }
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