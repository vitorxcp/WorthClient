package com.vitorxp.WorthClient.mixin;

import com.vitorxp.WorthClient.interfaces.IResourcePackRepository;
import com.vitorxp.WorthClient.utils.FolderResourcePack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.resources.IResourcePack;
import net.minecraft.client.resources.ResourcePackRepository;
import net.minecraft.client.resources.data.PackMetadataSection;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Mixin(ResourcePackRepository.class)
public abstract class MixinResourcePackRepository implements IResourcePackRepository {

    @Shadow @Final private File dirResourcepacks;
    @Shadow @Final public List<ResourcePackRepository.Entry> repositoryEntriesAll;

    @Unique private File currentDirectory = null;

    @Override
    public void navigateTo(File newDir) {
        this.currentDirectory = newDir;
    }

    @Override
    public File getCurrentDirectory() {
        return this.currentDirectory;
    }

    @Inject(method = "updateRepositoryEntriesAll", at = @At("HEAD"), cancellable = true)
    public void onUpdateRepositoryEntriesAll(CallbackInfo ci) {
        this.repositoryEntriesAll.clear();

        if (this.currentDirectory == null) {
            this.currentDirectory = this.dirResourcepacks;
        }

        List<File> listFolders = new ArrayList<>();
        List<File> listPacks = new ArrayList<>();

        if (!this.currentDirectory.equals(this.dirResourcepacks)) {
            File parent = this.currentDirectory.getParentFile();
            if (parent != null) {
                addFolderEntry(parent, true);
            } else {
                this.currentDirectory = this.dirResourcepacks;
            }
        }

        if (this.currentDirectory.exists() && this.currentDirectory.isDirectory()) {
            File[] files = this.currentDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.getName().equals(".DS_Store") || file.getName().equals("thumbs.db")) continue;

                    if (isValidPack(file)) {
                        listPacks.add(file);
                    } else if (file.isDirectory()) {
                        listFolders.add(file);
                    }
                }
            }
        }

        for (File folder : listFolders) {
            addFolderEntry(folder, false);
        }

        for (File pack : listPacks) {
            addPackEntry(pack);
        }

        ci.cancel();
    }

    @Unique
    private boolean isValidPack(File file) {
        if (file.isFile() && file.getName().toLowerCase().endsWith(".zip")) return true;
        if (file.isDirectory() && new File(file, "pack.mcmeta").exists()) return true;
        return false;
    }

    @Unique
    private void addPackEntry(File file) {
        try {
            ResourcePackRepository repo = (ResourcePackRepository) (Object) this;
            Constructor<ResourcePackRepository.Entry> ctor = ResourcePackRepository.Entry.class.getDeclaredConstructor(ResourcePackRepository.class, File.class);
            ctor.setAccessible(true);
            ResourcePackRepository.Entry entry = ctor.newInstance(repo, file);
            entry.updateResourcePack();
            this.repositoryEntriesAll.add(entry);
        } catch (Exception e) { }
    }

    @Unique
    private void addFolderEntry(File folder, boolean isBack) {
        try {
            FolderResourcePack folderPack = new FolderResourcePack(folder, isBack);
            ResourcePackRepository repo = (ResourcePackRepository) (Object) this;

            Constructor<ResourcePackRepository.Entry> ctor = ResourcePackRepository.Entry.class.getDeclaredConstructor(ResourcePackRepository.class, File.class);
            ctor.setAccessible(true);
            ResourcePackRepository.Entry entry = ctor.newInstance(repo, folder);

            injectFakeData(entry, folderPack);

            DynamicTexture dynTex = new DynamicTexture(folderPack.getPackImage());
            ResourceLocation loc = Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation("texturepackicon", dynTex);
            injectTextureLocation(entry, loc);

            this.repositoryEntriesAll.add(entry);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Unique
    private void injectFakeData(ResourcePackRepository.Entry entry, FolderResourcePack folderPack) throws IllegalAccessException {
        Field packField = null;
        Field metaField = null;

        for (Field f : ResourcePackRepository.Entry.class.getDeclaredFields()) {
            f.setAccessible(true);
            if (f.getType().equals(IResourcePack.class)) {
                packField = f;
            } else if (f.getType().equals(PackMetadataSection.class)) {
                metaField = f;
            }
        }

        if (packField != null) packField.set(entry, folderPack);

        if (metaField != null) {
            String desc = folderPack.getPackName().contains("Voltar") ? "§7Clique para voltar" : "§7Clique para abrir a pasta";
            PackMetadataSection fakeMeta = new PackMetadataSection(new ChatComponentText(desc), 1);
            metaField.set(entry, fakeMeta);
        }
    }

    @Unique
    private void injectTextureLocation(ResourcePackRepository.Entry entry, ResourceLocation location) throws IllegalAccessException {
        for (Field f : ResourcePackRepository.Entry.class.getDeclaredFields()) {
            f.setAccessible(true);
            if (f.getType().equals(ResourceLocation.class)) {
                f.set(entry, location);
                return;
            }
        }
    }
}