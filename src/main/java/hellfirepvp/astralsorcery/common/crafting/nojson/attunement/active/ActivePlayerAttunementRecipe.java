/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.nojson.attunement.active;

import hellfirepvp.astralsorcery.client.ClientScheduler;
import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXMotionController;
import hellfirepvp.astralsorcery.client.effect.function.impl.RenderOffsetNoisePlane;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.util.camera.*;
import hellfirepvp.astralsorcery.client.util.camera.path.CameraPathBuilder;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.crafting.nojson.attunement.AttunePlayerRecipe;
import hellfirepvp.astralsorcery.common.crafting.nojson.attunement.AttunementRecipe;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import hellfirepvp.astralsorcery.common.event.helper.EventHelperInvulnerability;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.lib.SoundsAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.client.PktAttunePlayerConstellation;
import hellfirepvp.astralsorcery.common.tile.TileAttunementAltar;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ActivePlayerAttunementRecipe
 * Created by HellFirePvP
 * Date: 02.12.2019 / 19:43
 */
public class ActivePlayerAttunementRecipe extends AttunementRecipe.Active<AttunePlayerRecipe> {

    private static final int DURATION_PLAYER_ATTUNEMENT = 800; //Duration of the player's camera flight

    private IMajorConstellation constellation;
    private UUID playerUUID;

    //Client camera flight cache, ICameraTransformer
    public Object cameraHack;

    private boolean startedPlayerSound = false;
    private List<Object> playerNoisePlanes = new ArrayList<>();

    public ActivePlayerAttunementRecipe(AttunePlayerRecipe recipe, IMajorConstellation constellation, UUID playerUUID) {
        super(recipe);
        this.constellation = constellation;
        this.playerUUID = playerUUID;
    }

    public ActivePlayerAttunementRecipe(AttunePlayerRecipe recipe, CompoundTag nbt) {
        super(recipe);
        this.readFromNBT(nbt);
    }

    @Override
    public boolean matches(TileAttunementAltar altar) {
        if (!super.matches(altar)) {
            return false;
        }
        Player player;
        return (player = altar.getLevel().getPlayerByUuid(this.playerUUID)) != null && player.isAlive();
    }

    @Override
    public void startCrafting(TileAttunementAltar altar) {
        Player player = altar.getLevel().getPlayerByUuid(this.playerUUID);
        if (player != null && player.isAlive()) {
            Vector3 offset = new Vector3(altar).add(0.5F, 1.2F, 0.5F);
            player.setPositionAndRotation(offset.getX(), offset.getY(), offset.getZ(), 0F, 0F);
            player.setPositionAndRotation(offset.getX(), offset.getY(), offset.getZ(), 0F, 0F);
        }
    }

    @Override
    public void stopCrafting(TileAttunementAltar altar) {

    }

    @Override
    public void finishRecipe(TileAttunementAltar altar) {
        Player player = altar.getLevel().getPlayerByUuid(this.playerUUID);
        if (player != null) {
            ResearchManager.setAttunedConstellation(player, this.constellation);
        }
    }

    @Override
    public void doTick(LogicalSide direction, TileAttunementAltar altar) {
        if (direction.isServer()) {
            Player player = altar.getLevel().getPlayerByUuid(this.playerUUID);
            if (player != null) {
                EventHelperInvulnerability.makeInvulnerable(player);
            }
        } else {
            setupPlanes();
            doClientSetup(altar);
            doEffectTick(altar);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void setupPlanes() {
        if (this.playerNoisePlanes.isEmpty()) {
            this.playerNoisePlanes.add(new RenderOffsetNoisePlane(1F));
            this.playerNoisePlanes.add(new RenderOffsetNoisePlane(1.4F));
            this.playerNoisePlanes.add(new RenderOffsetNoisePlane(1.8F));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void doEffectTick(TileAttunementAltar altar) {
        IConstellation cst = altar.getActiveConstellation();
        if (cst == null) {
            return;
        }

        Vector3 playerTarget = new Vector3(altar).add(0.5, 2.5, 0.5);
        VFXColorFunction<?> beamColor = VFXColorFunction.constant(ColorsAS.DEFAULT_GENERIC_PARTICLE);
        int tick = getTick();

        if (tick % 40 == 0) {
            for (BlockPos pos : altar.getConstellationPositions(cst)) {
                Vector3 from = new Vector3(pos).add(0.5, 0, 0.5);
                MiscUtils.applyRandomOffset(from, random, 0.1F);
                EffectHelper.of(EffectTemplatesAS.LIGHTBEAM)
                        .spawn(from)
                        .setup(from.clone().addY(6), 1.2, 1.2)
                        .setAlphaMultiplier(0.8F)
                        .color(beamColor)
                        .setMaxAge(60);
            }
        }

        double scale = 7.0D;
        double edgeScale = (scale * 2 + 1);
        for (int i = 0; i < 7; i++) {
            Vector3 offset = new Vector3(altar).add(-scale, 0.1, -scale);
            if (random.nextBoolean()) {
                offset.add(edgeScale * (random.nextBoolean() ? 1 : 0), 0, random.nextFloat() * edgeScale);
            } else {
                offset.add(random.nextFloat() * edgeScale, 0, edgeScale * (random.nextBoolean() ? 1 : 0));
            }
            FXFacingParticle particle = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(offset)
                    .alpha1arg(VFXAlphaFunction.FADE_OUT)
                    .setGravityStrength(-0.0002F + random.nextFloat() * -0.0001F)
                    .setScaleMultiplier(0.3F + random.nextFloat() * 0.15F)
                    .color(VFXColorFunction.WHITE)
                    .setMaxAge(40 + random.nextInt(10));
            if (random.nextBoolean()) {
                particle.color(VFXColorFunction.constant(this.constellation.getConstellationColor()));
            }
        }

        for (int i = 0; i < 5; i++) {
            Set<BlockPos> offsets = altar.getConstellationPositions(cst);
            BlockPos pos = MiscUtils.getRandomEntry(offsets, random);

            if (tick <= 380) {
                Vector3 offset = new Vector3(pos)
                        .add(0.5, 0, 0.5)
                        .add(Vector3.random().setY(0).mul(0.6));

                EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(offset)
                        .color(VFXColorFunction.WHITE)
                        .setGravityStrength(-0.0006F + random.nextFloat() * -0.003F)
                        .setDeltaMovement(Vector3.random().addY(4).normalize().mul(0.015 + random.nextFloat() * 0.01))
                        .setAlphaMultiplier(0.6F)
                        .setScaleMultiplier(0.3F + random.nextFloat() * 0.15F)
                        .alpha1arg(VFXAlphaFunction.FADE_OUT)
                        .setMaxAge(60 + random.nextInt(20));
            } else {
                Vector3 offset = new Vector3(pos)
                        .add(0.5, 0, 0.5)
                        .add(Vector3.random().setY(0).mul(0.5));

                EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(offset)
                        .setAlphaMultiplier(0.6F)
                        .alpha1arg(VFXAlphaFunction.proximity(playerTarget::clone, 3F))
                        .motion(VFXMotionController.target(playerTarget::clone, 0.08F))
                        .setScaleMultiplier(0.2F + random.nextFloat() * 0.1F)
                        .color(VFXColorFunction.WHITE)
                        .setDeltaMovement(new Vector3(0, 0.2 + random.nextFloat() * 0.15F, 0))
                        .setMaxAge(60 + random.nextInt(20));

                offset = new Vector3(altar)
                        .add(0.5, 0, 0.5)
                        .add(Vector3.random().setY(0).mul(0.6));
                EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(offset)
                        .color(VFXColorFunction.WHITE)
                        .setGravityStrength(-0.0006F + random.nextFloat() * -0.004F)
                        .setDeltaMovement(Vector3.random().addY(4).normalize().mul(0.02 + random.nextFloat() * 0.01))
                        .setAlphaMultiplier(0.75F)
                        .setScaleMultiplier(0.3F + random.nextFloat() * 0.1F)
                        .alpha1arg(VFXAlphaFunction.FADE_OUT)
                        .setMaxAge(40 + random.nextInt(10));
            }
        }

        if (tick >= 220) {
            Vector3 offset = new Vector3(altar)
                    .add(0.5, 0, 0.5)
                    .add(Vector3.random().setY(0));

            FXFacingParticle p = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(offset)
                    .setAlphaMultiplier(1F)
                    .alpha1arg(VFXAlphaFunction.proximity(playerTarget::clone, 3F))
                    .motion(VFXMotionController.target(playerTarget::clone, 0.1F))
                    .setScaleMultiplier(0.2F + random.nextFloat() * 0.1F)
                    .color(VFXColorFunction.WHITE)
                    .setDeltaMovement(Vector3.positiveYRandom().setY(1).normalize().mul(0.5F + random.nextFloat() * 0.1F))
                    .setMaxAge(60 + random.nextInt(20));

            if (random.nextBoolean()) {
                p.color(VFXColorFunction.constant(this.constellation.getConstellationColor()));
            }

            for (int i = 0; i < 3; i++) {
                Vector3 at = new Vector3(altar).add(0.5, 0, 0.5);
                at.addX(random.nextFloat() * 7F * (random.nextBoolean() ? 1 : -1));
                at.addZ(random.nextFloat() * 7F * (random.nextBoolean() ? 1 : -1));

                p = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(at)
                        .setAlphaMultiplier(0.75F)
                        .alpha1arg(VFXAlphaFunction.FADE_OUT)
                        .setGravityStrength(-0.001F + random.nextFloat() * -0.0005F)
                        .color(VFXColorFunction.WHITE)
                        .setScaleMultiplier(0.3F + random.nextFloat() * 0.1F)
                        .setMaxAge(20 + random.nextInt(10));

                if (random.nextBoolean()) {
                    p.color(VFXColorFunction.constant(this.constellation.getConstellationColor()));
                }
                if (tick >= 500) {
                    p.setScaleMultiplier(0.3F + random.nextFloat() * 0.15F);
                }
            }
        }

        if (tick >= 400) {
            int amt = tick >= 500 ? 4 : 1;
            for (int i = 0; i < amt; i++) {
                RenderOffsetNoisePlane plane = (RenderOffsetNoisePlane) MiscUtils.getRandomEntry(this.playerNoisePlanes, random);
                FXFacingParticle p = plane.createParticle(playerTarget.clone())
                        .setDeltaMovement(Vector3.random().setY(0).mul(random.nextFloat() * 0.015F))
                        .setAlphaMultiplier(0.6F)
                        .setScaleMultiplier(0.2F + random.nextFloat() * 0.05F)
                        .alpha1arg(VFXAlphaFunction.FADE_OUT)
                        .setMaxAge(60 + random.nextInt(20));

                if (random.nextBoolean()) {
                    p.color(VFXColorFunction.constant(this.constellation.getConstellationColor()));
                }
            }
        }

        if (tick >= 600) {
            if (tick % 10 == 0) {
                Vector3 from = new Vector3(altar).add(0.5, 0, 0.5);
                MiscUtils.applyRandomOffset(from, random, 0.25F);
                EffectHelper.of(EffectTemplatesAS.LIGHTBEAM)
                        .spawn(from)
                        .setup(from.clone().addY(8), 2.4, 2)
                        .setAlphaMultiplier(0.8F)
                        .setMaxAge(40 + random.nextInt(20));
            }
        }

        if (tick >= (DURATION_PLAYER_ATTUNEMENT - 4)) {
            for (int i = 0; i < 60; i++) {
                Vector3 at = new Vector3(altar)
                        .add(0.5, 0, 0.5)
                        .addY(random.nextFloat() * 15);

                FXFacingParticle p = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(at)
                        .color(VFXColorFunction.WHITE)
                        .setDeltaMovement(Vector3.random().setY(0).normalize().mul(0.03 + random.nextFloat() * 0.01))
                        .setAlphaMultiplier(0.7F)
                        .setScaleMultiplier(0.3F + random.nextFloat() * 0.15F)
                        .alpha1arg(VFXAlphaFunction.FADE_OUT)
                        .setMaxAge(140 + random.nextInt(60));

                if (random.nextBoolean()) {
                    p.color(VFXColorFunction.constant(this.constellation.getConstellationColor()));
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void doClientSetup(TileAttunementAltar altar) {
        if (this.cameraHack == null &&
                Minecraft.getInstance().player != null &&
                Minecraft.getInstance().player.getUUID().equals(this.getPlayerUUID())) {
            Vector3 offset = new Vector3(altar).add(0.5, 6, 0.5);
            CameraPathBuilder builder = CameraPathBuilder.builder(offset.clone().add(4, 0, 4), new Vector3(altar).add(0.5, 0.5, 0.5));
            builder.addCircularPoints(offset, CameraPathBuilder.DynamicRadiusGetter.dyanmicIncrease( 5,  0.025), 200, 2);
            builder.addCircularPoints(offset, CameraPathBuilder.DynamicRadiusGetter.dyanmicIncrease(10, -0.01) , 200, 2);
            builder.setTickDelegate(createTickListener(new Vector3(altar).add(0.5F, 1.2F, 0.5F)));
            builder.setStopDelegate(createAttunementListener(altar));

            this.cameraHack = builder.finishAndStart();
        }

        if (!startedPlayerSound) {
            startedPlayerSound = true;
            SoundHelper.playSoundFadeInClient(SoundsAS.ATTUNEMENT_ATLAR_PLAYER_ATTUNE,
                    new Vector3(altar).add(0.5, 1, 0.5),
                    0.7F,
                    1F,
                    false,
                    (s) -> {
                        return !altar.canPlayConstellationActiveEffects() ||
                                altar.getActiveRecipe() != this;
                    })
                    .setFadeInTicks(10)
                    .setFadeOutTicks(80);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private ICameraTickListener createTickListener(Vector3 offset) {
        return (renderView, focusedEntity) -> {
            if (focusedEntity == null) {
                return;
            }

            float floatTick = (ClientScheduler.getClientTick() % 40) / 40F;
            float sin = Mth.sin((float) (floatTick * 2 * Math.PI)) / 2F + 0.5F;
            focusedEntity.setCustomNameVisible(false);
            focusedEntity.setPositionAndRotation(offset.getX(), offset.getY() + sin * 0.2D, offset.getZ(), 0F, 0F);
            focusedEntity.setPositionAndRotation(offset.getX(), offset.getY() + sin * 0.2D, offset.getZ(), 0F, 0F);
            focusedEntity.yHeadRot = 0;
            focusedEntity.yHeadRotO = 0;
            focusedEntity.yBodyRot = 0;
            focusedEntity.yBodyRotO = 0;
            focusedEntity.lerpMotion(0, 0, 0);
        };
    }

    @OnlyIn(Dist.CLIENT)
    private ICameraStopListener createAttunementListener(TileAttunementAltar altar) {
        BlockPos at = altar.getBlockPos();
        return () -> {
            if (this.cameraHack != null) {
                ICameraTransformer transformer = (ICameraTransformer) this.cameraHack;
                ICameraPersistencyFunction persistency = transformer.getPersistencyFunction();
                if (persistency.timedOut() && !persistency.wasForciblyStopped()) {
                    PktAttunePlayerConstellation attuneRequest = new PktAttunePlayerConstellation(this.constellation, altar.getLevel().dimension(), at);
                    PacketChannel.CHANNEL.sendToServer(attuneRequest);
                }
            }
        };
    }

    @Override
    public boolean isFinished(TileAttunementAltar altar) {
        return this.getTick() >= DURATION_PLAYER_ATTUNEMENT;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void stopEffects(TileAttunementAltar altar) {
        if (this.cameraHack != null) {
            ClientCameraManager.INSTANCE.removeTransformer((ICameraTransformer) this.cameraHack);
        }
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    @Override
    public void save(CompoundTag nbt) {
        super.save(nbt);

        nbt.putUniqueId("playerUUID", this.playerUUID);
        nbt.putString("constellation", this.constellation.getRegistryName().toString());
    }

    @Override
    protected void readFromNBT(CompoundTag nbt) {
        super.readFromNBT(nbt);

        this.playerUUID = nbt.getUniqueId("playerUUID");
        this.constellation = (IMajorConstellation) RegistriesAS.REGISTRY_CONSTELLATIONS.getValue(ResourceLocation.parse(nbt.getString("constellation")));
    }
}
