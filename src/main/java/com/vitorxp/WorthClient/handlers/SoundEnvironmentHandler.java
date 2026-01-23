package com.vitorxp.WorthClient.handlers;

import com.vitorxp.WorthClient.WorthClient;
import com.vitorxp.WorthClient.utils.sound.OpenALReverbHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;

public class SoundEnvironmentHandler {

    private final Minecraft mc = Minecraft.getMinecraft();
    public final OpenALReverbHandler reverbHandler = new OpenALReverbHandler(); // Public para acesso
    private int frameCounter = 0;
    private final Map<Long, Float> densityCache = new HashMap<>();
    private int cacheCleanTimer = 0;

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!WorthClient.somReverbAnb) return;
        if (mc.theWorld == null || mc.isGamePaused()) return;

        if (!reverbHandler.isInitialized()) {
            reverbHandler.init();
        }

        if (cacheCleanTimer++ > 20) {
            densityCache.clear();
            cacheCleanTimer = 0;
        }

        reverbHandler.updateReverbEnvironment(this);
    }

    public float getDensityAtPosition(BlockPos pos) {
        long posKey = pos.toLong();
        if (densityCache.containsKey(posKey)) {
            return densityCache.get(posKey);
        }

        float density = calculateGeometryDensity(pos);
        densityCache.put(posKey, density);
        return density;
    }

    private float calculateGeometryDensity(BlockPos pos) {
        if (mc.theWorld.canBlockSeeSky(pos)) {
            if (pos.getY() < 50 && checkWalls(pos, 10) >= 3) return 0.4f;
            return 0.0f;
        }

        int distUp = getDistanceToSolidOptimized(pos, EnumFacing.UP, 60);
        boolean hasRoof = distUp < 60;

        if (!hasRoof) return 0.0f;

        int distNorth = getDistanceToSolidOptimized(pos, EnumFacing.NORTH, 40);
        int distSouth = getDistanceToSolidOptimized(pos, EnumFacing.SOUTH, 40);
        int distEast = getDistanceToSolidOptimized(pos, EnumFacing.EAST, 40);
        int distWest = getDistanceToSolidOptimized(pos, EnumFacing.WEST, 40);

        int totalSpace = distNorth + distSouth + distEast + distWest + (distUp * 2);

        float baseVal;
        if (totalSpace < 15) baseVal = 0.20f;
        else if (totalSpace < 60) baseVal = 0.45f;
        else if (totalSpace < 120) baseVal = 0.75f;
        else baseVal = 1.0f;

        float absorption = calculateMaterialAbsorption(pos);
        return Math.max(0.0f, baseVal - absorption);
    }

    private int getDistanceToSolidOptimized(BlockPos start, EnumFacing facing, int max) {
        int check = 1;
        int step = 1;

        while (check < max) {
            BlockPos p = start.offset(facing, check);
            if (mc.theWorld.isBlockNormalCube(p, false)) {
                return check;
            }

            if (check > 10) step = 3;
            else if (check > 5) step = 2;

            check += step;
        }
        return max;
    }

    private int checkWalls(BlockPos pos, int range) {
        int c = 0;
        if (scanWall(pos, EnumFacing.NORTH, range)) c++;
        if (scanWall(pos, EnumFacing.SOUTH, range)) c++;
        if (scanWall(pos, EnumFacing.EAST, range)) c++;
        if (scanWall(pos, EnumFacing.WEST, range)) c++;
        return c;
    }

    private boolean scanWall(BlockPos center, EnumFacing dir, int range) {
        return getDistanceToSolidOptimized(center, dir, range) < range;
    }

    private float calculateMaterialAbsorption(BlockPos center) {
        int radius = 2;
        float totalAbsorb = 0;
        int blocksChecked = 0;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -1; y <= 2; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (blocksChecked > 20) break;

                    BlockPos pos = center.add(x, y, z);
                    if (mc.theWorld.isAirBlock(pos)) continue;

                    Block block = mc.theWorld.getBlockState(pos).getBlock();
                    Material mat = block.getMaterial();

                    if (mat == Material.cloth || mat == Material.carpet || mat == Material.sponge) {
                        totalAbsorb += 0.8f;
                    } else if (mat == Material.wood || block == Blocks.bookshelf || block == Blocks.hay_block) {
                        totalAbsorb += 0.2f;
                    }
                    blocksChecked++;
                }
            }
        }
        return (blocksChecked == 0) ? 0.0f : Math.min(0.5f, totalAbsorb / 5.0f);
    }
}