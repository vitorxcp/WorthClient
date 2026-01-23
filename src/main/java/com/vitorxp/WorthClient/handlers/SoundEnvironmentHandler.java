package com.vitorxp.WorthClient.handlers;

import com.vitorxp.WorthClient.WorthClient;
import com.vitorxp.WorthClient.utils.sound.OpenALReverbHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;

public class SoundEnvironmentHandler {

    private final Minecraft mc = Minecraft.getMinecraft();
    private final OpenALReverbHandler reverbHandler = new OpenALReverbHandler();
    private int frameCounter = 0;
    private float currentDensity = 0.0f;
    private float targetDensity = 0.0f;
    private boolean firstRun = true;

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if(!WorthClient.somReverbAnb) return;
        if (mc.theWorld == null || mc.isGamePaused()) return;

        if (firstRun) {
            reverbHandler.init();
            firstRun = false;
        }

        if (frameCounter++ > 10) {
            calculateSmartEnvironment();
            frameCounter = 0;
        }

        if (Math.abs(currentDensity - targetDensity) > 0.001f) {
            currentDensity += (targetDensity - currentDensity) * 0.1f;
        }

        reverbHandler.updateReverbEnvironment(currentDensity);
    }

    private void calculateSmartEnvironment() {
        if (mc.thePlayer == null) return;

        BlockPos playerPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY, mc.thePlayer.posZ);
        float baseDensity = analyzeRoomGeometry(playerPos);

        if (baseDensity > 0.1f) {
            float absorption = calculateMaterialAbsorption(playerPos);
            baseDensity *= (1.0f - (absorption * 0.8f));
        }

        float activityDensity = scanNearbyEntitiesSafe(playerPos);
        this.targetDensity = Math.max(baseDensity, activityDensity);
    }

    private float calculateMaterialAbsorption(BlockPos center) {
        int radius = 4;
        int height = 3;

        float totalAbsorb = 0;
        int blocksChecked = 0;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -1; y <= height; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if ((x + y + z) % 2 != 0) continue;
                    BlockPos pos = center.add(x, y, z);
                    if (mc.theWorld.isAirBlock(pos)) continue;
                    Block block = mc.theWorld.getBlockState(pos).getBlock();
                    Material mat = block.getMaterial();
                    if (mat == Material.cloth || mat == Material.carpet || mat == Material.sponge) {
                        totalAbsorb += 1.0f;
                    } else if (mat == Material.wood || mat == Material.gourd || mat == Material.leaves ||
                            mat == Material.grass || mat == Material.ground || mat == Material.sand ||
                            block == Blocks.bookshelf || block == Blocks.hay_block) {
                        totalAbsorb += 0.5f;
                    }
                    blocksChecked++;
                }
            }
        }

        if (blocksChecked == 0) return 0.0f;
        return Math.min(1.0f, (totalAbsorb / (float) blocksChecked) * 2.0f);
    }

    private float analyzeRoomGeometry(BlockPos pos) {
        if (mc.theWorld.canBlockSeeSky(pos)) {
            if (pos.getY() < 50 && checkWalls(pos) >= 3) return 0.4f;
            return 0.0f;
        }

        int distUp = getDistanceToSolid(pos, EnumFacing.UP, 15);
        boolean hasRoof = distUp < 15;
        if (!hasRoof) return 0.0f;
        int walls = checkWalls(pos);
        int skyLight = mc.theWorld.getLightFor(EnumSkyBlock.SKY, pos);
        if (walls < 2 && skyLight > 10) return 0.0f;
        if (skyLight < 3) return 0.95f;
        int distNorth = getDistanceToSolid(pos, EnumFacing.NORTH, 12);
        int distSouth = getDistanceToSolid(pos, EnumFacing.SOUTH, 12);
        int distEast = getDistanceToSolid(pos, EnumFacing.EAST, 12);
        int distWest = getDistanceToSolid(pos, EnumFacing.WEST, 12);
        int volumeScore = distNorth + distSouth + distEast + distWest + (distUp * 2);
        if (volumeScore < 15) return 0.15f;
        if (volumeScore < 40) return 0.35f;
        if (volumeScore < 70) return 0.65f;
        return 0.9f;
    }

    private float scanNearbyEntitiesSafe(BlockPos playerPos) {
        try {
            List<Entity> entities = mc.theWorld.loadedEntityList;
            if (entities == null || entities.isEmpty()) return 0.0f;

            float max = 0.0f;
            for (int i = 0; i < entities.size(); i++) {
                try {
                    Entity e = entities.get(i);
                    if (e == null || e == mc.thePlayer || e.isDead) continue;

                    if (e.getDistanceSq(playerPos) < 144) {
                        if (e.lastTickPosX != e.posX || e.lastTickPosZ != e.posZ) {
                            float eGeo = analyzeRoomGeometry(new BlockPos(e));
                            if (eGeo > 0.3f) max = Math.max(max, eGeo * 0.5f);
                        }
                    }
                } catch (Exception ignored) {
                }
            }
            return max;
        } catch (Exception e) {
            return 0.0f;
        }
    }

    private int getDistanceToSolid(BlockPos start, EnumFacing facing, int max) {
        for (int i = 1; i <= max; i++) {
            if (mc.theWorld.isBlockNormalCube(start.offset(facing, i), false)) return i;
        }
        return max;
    }

    private int checkWalls(BlockPos pos) {
        int c = 0;
        if (scanWall(pos, EnumFacing.NORTH)) c++;
        if (scanWall(pos, EnumFacing.SOUTH)) c++;
        if (scanWall(pos, EnumFacing.EAST)) c++;
        if (scanWall(pos, EnumFacing.WEST)) c++;
        return c;
    }

    private boolean scanWall(BlockPos center, EnumFacing dir) {
        for (int i = 1; i <= 8; i++) {
            if (mc.theWorld.isBlockNormalCube(center.offset(dir, i), false)) return true;
        }
        return false;
    }
}