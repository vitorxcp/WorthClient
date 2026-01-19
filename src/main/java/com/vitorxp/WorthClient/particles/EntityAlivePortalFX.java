package com.vitorxp.WorthClient.particles;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

public class EntityAlivePortalFX extends EntityFX {

    private final double centerX;
    private final double centerY;
    private final double centerZ;
    private final int type;
    private final boolean isEmber;
    private float orbitAngle;
    private float orbitRadius;
    private float orbitSpeed;
    private final float startupScale;

    public EntityAlivePortalFX(World worldIn, double x, double y, double z,
                               double targetX, double targetY, double targetZ,
                               float r, float g, float b, int type) {
        super(worldIn, x, y, z, 0, 0, 0);
        this.centerX = targetX;
        this.centerY = targetY;
        this.centerZ = targetZ;
        this.type = type;

        this.isEmber = (type == 0) && (rand.nextFloat() < 0.2F);

        if (type == 0) {
            if (isEmber) {
                this.particleRed = 1.0F;
                this.particleGreen = 0.6F + rand.nextFloat() * 0.4F;
                this.particleBlue = 0.1F;
                this.particleScale = 0.4F;

                double ang = rand.nextDouble() * Math.PI * 2.0;
                double spd = 0.06;
                this.motionX = Math.cos(ang) * spd;
                this.motionZ = Math.sin(ang) * spd;
                this.motionY = 0.03 + rand.nextDouble() * 0.05;
            } else {
                this.particleRed = 0.7F + rand.nextFloat() * 0.3F;
                this.particleGreen = 0.0F;
                this.particleBlue = 1.0F;

                this.particleScale = 0.8F + rand.nextFloat();

                double dx = x - targetX;
                double dz = z - targetZ;
                this.orbitRadius = (float)Math.sqrt(dx * dx + dz * dz);
                this.orbitAngle = (float)Math.atan2(dz, dx);
                this.orbitSpeed = 0.12F + rand.nextFloat() * 0.05F;
            }
        } else {
            this.particleRed = 0.1F;
            this.particleGreen = 0.8F;
            this.particleBlue = 0.9F;
            this.particleScale = 1.5F;
        }

        this.startupScale = this.particleScale;
        this.particleMaxAge = 40 + rand.nextInt(40);
        this.particleAlpha = 0.0F;
        this.noClip = true;
    }

    @Override
    public int getFXLayer() {
        return 3;
    }

    @Override
    public void renderParticle(WorldRenderer wr, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.depthMask(false);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

        float x = (float)(this.prevPosX + (this.posX - this.prevPosX) * (double)partialTicks - interpPosX);
        float y = (float)(this.prevPosY + (this.posY - this.prevPosY) * (double)partialTicks - interpPosY);
        float z = (float)(this.prevPosZ + (this.posZ - this.prevPosZ) * (double)partialTicks - interpPosZ);
        float ageFactor = (float)this.particleAge / (float)this.particleMaxAge;
        float alpha = 1.0F;
        if (ageFactor < 0.1F) alpha = ageFactor * 10.0F;
        else alpha = 1.0F - ((ageFactor - 0.1F) / 0.9F);

        if (type == 1) alpha *= 0.6F;

        float speedX = (float)(this.posX - this.prevPosX);
        float speedY = (float)(this.posY - this.prevPosY);
        float speedZ = (float)(this.posZ - this.prevPosZ);
        float speedLen = MathHelper.sqrt_float(speedX*speedX + speedY*speedY + speedZ*speedZ);
        float stretch = (type == 0) ? 1.0F + (speedLen * 2.0F) : 1.0F + (speedLen * 8.0F);
        float width = this.startupScale * 0.08F;

        wr.begin(7, DefaultVertexFormats.POSITION_COLOR);

        float coreR = 1.0F, coreG = 1.0F, coreB = 1.0F;
        float edgeR = this.particleRed, edgeG = this.particleGreen, edgeB = this.particleBlue;
        float rX = rotationX * width;
        float rXY = rotationXY * width;
        float rZ = rotationZ * width;
        float rYZ = rotationYZ * width;
        float rXZ = rotationXZ * width;
        float sX = rXY * stretch;
        float sY = rZ * stretch;
        float sZ = rXZ * stretch;

        if (type == 0) {
            wr.pos(x - rX + sX, y + sY, z - rYZ + sZ).color(edgeR, edgeG, edgeB, 0.0F).endVertex();
            wr.pos(x + rXY, y + rZ, z + rXZ).color(edgeR, edgeG, edgeB, 0.0F).endVertex();
            wr.pos(x + rX - sX, y - sY, z + rYZ - sZ).color(edgeR, edgeG, edgeB, 0.0F).endVertex();
            wr.pos(x - rXY, y - rZ, z - rXZ).color(edgeR, edgeG, edgeB, 0.0F).endVertex();

            float coreSize = 0.3F;
            wr.pos(x - rX*coreSize + sX*coreSize, y + sY*coreSize, z - rYZ*coreSize + sZ*coreSize).color(coreR, coreG, coreB, alpha).endVertex();
            wr.pos(x + rXY*coreSize, y + rZ*coreSize, z + rXZ*coreSize).color(coreR, coreG, coreB, alpha).endVertex();
            wr.pos(x + rX*coreSize - sX*coreSize, y - sY*coreSize, z + rYZ*coreSize - sZ*coreSize).color(coreR, coreG, coreB, alpha).endVertex();
            wr.pos(x - rXY*coreSize, y - rZ*coreSize, z - rXZ*coreSize).color(coreR, coreG, coreB, alpha).endVertex();

        } else {
            wr.pos(x + (rotationX * width), y + (rotationZ * width), z + (rotationYZ * width)).color(coreR, coreG, coreB, alpha).endVertex();
            wr.pos(x - (rotationX * width), y - (rotationZ * width), z - (rotationYZ * width)).color(coreR, coreG, coreB, alpha).endVertex();

            float trailX = -speedX * 10.0F;
            float trailY = -speedY * 10.0F;
            float trailZ = -speedZ * 10.0F;

            wr.pos(x - (rotationX * width) + trailX, y - (rotationZ * width) + trailY, z - (rotationYZ * width) + trailZ).color(edgeR, edgeG, edgeB, 0.0F).endVertex();
            wr.pos(x + (rotationX * width) + trailX, y + (rotationZ * width) + trailY, z + (rotationYZ * width) + trailZ).color(edgeR, edgeG, edgeB, 0.0F).endVertex();
        }

        Tessellator.getInstance().draw();
        GlStateManager.enableTexture2D();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.depthMask(true);

        Minecraft.getMinecraft().getTextureManager().bindTexture(new ResourceLocation("textures/particle/particles.png"));
    }

    @Override
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setDead();
        }

        if (type == 0) {
            if (isEmber) {
                this.motionY -= 0.003D;
                this.moveEntity(this.motionX, this.motionY, this.motionZ);
                this.motionX *= 0.92; this.motionY *= 0.92; this.motionZ *= 0.92;
            } else {
                this.orbitAngle += this.orbitSpeed;
                this.orbitRadius -= 0.02F;

                if (orbitRadius < 0.2F) {
                    orbitRadius = 0.2F;
                    this.motionY += 0.02;
                }

                double nX = centerX + Math.cos(orbitAngle) * orbitRadius;
                double nZ = centerZ + Math.sin(orbitAngle) * orbitRadius;
                double wave = Math.sin(this.particleAge * 0.3) * 0.05;
                this.setPosition(nX, this.posY + this.motionY + (wave * 0.1), nZ);
            }
        } else {
            double dx = centerX - posX;
            double dy = (centerY + 1.0) - posY;
            double dz = centerZ - posZ;
            double dist = Math.sqrt(dx*dx + dy*dy + dz*dz);

            if (dist < 0.2) {
                this.setDead();
            } else {
                double force = 0.35 / (dist + 0.1);
                this.motionX += dz * 0.4;
                this.motionZ -= dx * 0.4;
                this.motionX += (dx / dist) * force;
                this.motionY += (dy / dist) * force;
                this.motionZ += (dz / dist) * force;
                this.motionX *= 0.85;
                this.motionY *= 0.85;
                this.motionZ *= 0.85;
                this.moveEntity(this.motionX, this.motionY, this.motionZ);
            }
        }
    }
}