/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2019
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.tile;

import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingSprite;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.client.lib.SpritesAS;
import hellfirepvp.astralsorcery.client.util.sound.PositionedLoopSound;
import hellfirepvp.astralsorcery.common.base.MoonPhase;
import hellfirepvp.astralsorcery.common.constellation.ConstellationRegistry;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.star.StarConnection;
import hellfirepvp.astralsorcery.common.constellation.star.StarLocation;
import hellfirepvp.astralsorcery.common.constellation.world.ConstellationHandler;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.common.crafting.nojson.AttunementCraftingRegistry;
import hellfirepvp.astralsorcery.common.crafting.nojson.attunement.AttunementRecipe;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.entity.EntityFlare;
import hellfirepvp.astralsorcery.common.item.ItemConstellationPaper;
import hellfirepvp.astralsorcery.common.lib.*;
import hellfirepvp.astralsorcery.common.structure.types.StructureType;
import hellfirepvp.astralsorcery.common.tile.base.TileEntityTick;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import hellfirepvp.astralsorcery.common.util.sound.SoundHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Tuple;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileAttunementAltar
 * Created by HellFirePvP
 * Date: 17.11.2019 / 07:49
 */
public class TileAttunementAltar extends TileEntityTick {

    private IConstellation activeConstellation = null;
    private AttunementRecipe.Active<?> currentRecipe = null;

    //Client Misc visuals
    private Map<BlockPos, Object> activeStarSprites = new HashMap<>();

    //Client Misc sounds
    private Object attunementAltarIdleSound = null;

    //TESR
    public static final int MAX_START_ANIMATION_TICK = 60;
    public static final int MAX_START_ANIMATION_SPIN = 100;
    public int activationTick = 0;
    public int prevActivationTick = 0;
    public boolean animate = false, tesrLocked = true;

    public TileAttunementAltar(BlockPos pos, BlockState state) {
        super(TileEntityTypesAS.ATTUNEMENT_ALTAR, pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        if (!getLevel().isClientSide()) {
            if (!doesSeeSky() || !hasMultiblock()) {

                if (this.activeConstellation != null) {
                    this.activeConstellation = null;
                    markForUpdate();
                }
                if (this.currentRecipe != null) {
                    this.currentRecipe = null;
                    markForUpdate();
                }
                return;
            }
            this.updateActiveConstellation();
            if (this.currentRecipe == null) {
                this.searchAndStartRecipe();
            } else {
                this.currentRecipe.tick(LogicalSide.SERVER, this);
                if (!this.currentRecipe.matches(this)) {
                    this.currentRecipe = null;
                    this.markForUpdate();
                } else if (this.currentRecipe.isFinished(this)) {
                    this.finishActiveRecipe();
                }
            }
        } else {
            tickEffects();
            if (this.currentRecipe != null) {
                this.currentRecipe.tick(LogicalSide.CLIENT, this);
            }
        }
    }

    private void updateActiveConstellation() {
        if (getTicksExisted() % 20 == 0) {
            IConstellation found = this.searchActiveConstellation();
            if (this.activeConstellation == null) {
                if (found != null) {
                    this.activeConstellation = found;
                    markForUpdate();
                }
            } else if (found != null) {
                if (!this.activeConstellation.equals(found)) {
                    this.activeConstellation = found;
                    markForUpdate();
                }
            } else {
                this.activeConstellation = null;
                markForUpdate();
            }

            if (this.activeConstellation != null) {
                for (BlockPos pos : this.getConstellationPositions(this.activeConstellation)) {
                    TileSpectralRelay relay = MiscUtils.getTileAt(getLevel(), pos, TileSpectralRelay.class, false);
                    if (relay != null && !relay.getItems().getStackInSlot(0).isEmpty()) {
                        ItemUtils.dropEquipment(relay.getItems(), getLevel(), pos.above());
                        relay.getItems().clearInventory();
                    }
                }
            }
        }
    }

    public void finishActiveRecipe() {
        if (this.currentRecipe != null) {
            this.currentRecipe.finishRecipe(this);
            this.currentRecipe.stopCrafting(this);
            this.currentRecipe = null;
            this.markForUpdate();

            EntityFlare.spawnAmbientFlare(getLevel(), getBlockPos().offset(-5 + random.nextInt(11), 1 + random.nextInt(3), -5 + random.nextInt(11)));
            EntityFlare.spawnAmbientFlare(getLevel(), getBlockPos().offset(-5 + random.nextInt(11), 1 + random.nextInt(3), -5 + random.nextInt(11)));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void tickEffects() {
        if (!hasMultiblock() || !doesSeeSky()) {
            this.tickEffectNonActive();
            return;
        }

        this.spawnAmbientEffects();
        this.spawnHighlightedEffects();
        this.tickEffectsConstellation();

        if (!this.canPlayConstellationActiveEffects()) {
            this.tickEffectNonActive();
            return;
        }

        this.tickEffectActive();
        this.tickConstellationBeams();

        if (this.getActiveRecipe() == null) {
            this.tickSoundIdle();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void tickEffectsConstellation() {
        if (this.activeConstellation != null) {
            Set<BlockPos> positions = this.getConstellationPositions(this.activeConstellation);
            for (BlockPos key : this.activeStarSprites.keySet()) {
                if (!positions.contains(key)) {
                    FXFacingSprite sprite = (FXFacingSprite) this.activeStarSprites.get(key);
                    if (!sprite.isRemoved()) {
                        sprite.requestRemoval();
                    }
                }
            }
            float night = DayTimeHelper.getCurrentDaytimeDistribution(getLevel());
            for (BlockPos key : positions) {
                if (!this.activeStarSprites.containsKey(key)) {
                    FXFacingSprite sprite = EffectHelper.of(EffectTemplatesAS.FACING_SPRITE)
                            .spawn(new Vector3(key).add(0.5, 0.5, 0.5))
                            .pickSprite(SpritesAS.SPR_RELAY_FLARE)
                            .setScaleMultiplier(1.4F)
                            .refresh(fx -> this.canPlayConstellationActiveEffects());

                    this.activeStarSprites.put(key, sprite);
                } else {
                    FXFacingSprite spr = (FXFacingSprite) this.activeStarSprites.get(key);
                    if (spr.isRemoved()) {
                        EffectHelper.refresh(spr, EffectTemplatesAS.FACING_SPRITE);
                    }
                }

                if (night >= 0.1F && getActiveRecipe() == null) {
                    this.playConstellationHighlightParticles(this.activeConstellation, key, night);
                }
            }
            this.playAltarConstellationHighlightParticles(this.activeConstellation, night);
        } else if (!this.activeStarSprites.isEmpty()) {
            for (BlockPos key : this.activeStarSprites.keySet()) {
                FXFacingSprite sprite = (FXFacingSprite) this.activeStarSprites.get(key);
                if (!sprite.isRemoved()) {
                    sprite.requestRemoval();
                }
            }
            this.activeStarSprites.clear();
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void tickSoundIdle() {
        if (SoundHelper.getSoundVolume(SoundSource.BLOCKS) <= 0) {
            this.attunementAltarIdleSound = null;
            return;
        }

        if (this.attunementAltarIdleSound == null || ((PositionedLoopSound) this.attunementAltarIdleSound).hasStoppedPlaying()) {
            this.attunementAltarIdleSound = SoundHelper.playSoundLoopFadeInClient(SoundsAS.ATTUNEMENT_ATLAR_IDLE,
                    new Vector3(this).add(0.5, 1, 0.5),
                    0.4F,
                    1F,
                    false,
                    (s) -> !this.canPlayConstellationActiveEffects() ||
                            SoundHelper.getSoundVolume(SoundSource.BLOCKS) <= 0 ||
                            this.getActiveRecipe() != null)
                    .setFadeInTicks(20)
                    .setFadeOutTicks(20);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void tickConstellationBeams() {
        VFXColorFunction<?> beamColor = VFXColorFunction.constant(ColorsAS.DEFAULT_GENERIC_PARTICLE);
        float beamSize = 0.8F;
        for (Tuple<BlockPos, BlockPos> conn : this.getConstellationConnectionPositions(this.activeConstellation)) {
            Vector3 from = new Vector3(conn.getA()).add(0.5, 0.5, 0.5);
            Vector3 to   = new Vector3(conn.getB()).add(0.5, 0.5, 0.5);

            if (this.getTicksExisted() % 50 == 0) {
                EffectHelper.of(EffectTemplatesAS.LIGHTBEAM)
                        .spawn(from)
                        .setup(to, beamSize, beamSize)
                        .color(beamColor);
                EffectHelper.of(EffectTemplatesAS.LIGHTBEAM)
                        .spawn(to)
                        .setup(from, beamSize, beamSize)
                        .color(beamColor);
            }

            if (random.nextBoolean()) {
                Vector3 at = from.clone()
                        .subtract(to)
                        .mul(random.nextFloat())
                        .add(to)
                        .add(Vector3.random().mul(random.nextFloat() * 0.25F));

                EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                        .spawn(at)
                        .alpha1arg(VFXAlphaFunction.FADE_OUT)
                        .color(VFXColorFunction.constant(this.activeConstellation.getConstellationColor()))
                        .setScaleMultiplier(0.2F + random.nextFloat() * 0.1F)
                        .setMaxAge(20 + random.nextInt(10));
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void tickEffectNonActive() {
        animate = false;

        prevActivationTick = activationTick;
        if (activationTick > 0) {
            activationTick--;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void tickEffectActive() {
        animate = true;

        prevActivationTick = activationTick;
        if (activationTick < MAX_START_ANIMATION_TICK) {
            activationTick++;
        }
    }

    @OnlyIn(Dist.CLIENT)
    public boolean canPlayConstellationActiveEffects() {
        WorldContext ctx = SkyHandler.getContext(getLevel(), LogicalSide.CLIENT);

        return ctx != null &&
                !this.isRemoved() &&
                this.hasMultiblock() &&
                this.doesSeeSky() &&
                this.getActiveConstellation() != null &&
                DayTimeHelper.isNight(getLevel()) &&
                ctx.getConstellationHandler().isActiveCurrently(getActiveConstellation(), MoonPhase.fromWorld(getLevel()));
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnAmbientEffects() {
        if (random.nextBoolean()) {
            Vector3 pos = new Vector3(this).add(
                    random.nextFloat() * 15 - 7,
                    0.01,
                    random.nextFloat() * 15 - 7);

            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(pos)
                    .color(VFXColorFunction.WHITE)
                    .setAlphaMultiplier(0.7F)
                    .setScaleMultiplier(0.3F + random.nextFloat() * 0.1F);
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void spawnHighlightedEffects() {
        if (this.canPlayConstellationActiveEffects()) {
            return;
        }
        WorldContext ctx = SkyHandler.getContext(getLevel(), LogicalSide.CLIENT);
        if (ctx == null) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        if (player == null || player.getDistanceSq(Vec3.copyCentered(getBlockPos())) >= 256) {
            return;
        }
        Tuple<InteractionHand, ItemStack> heldTpl = MiscUtils.getMainOrOffHand(player, stack -> stack.getItem() instanceof ItemConstellationPaper);
        if (heldTpl != null) {
            ItemStack cstPaper = heldTpl.getB();
            IConstellation cst = ((ItemConstellationPaper) cstPaper.getItem()).getConstellation(cstPaper);
            if (cst != null && ResearchHelper.getClientProgress().hasConstellationDiscovered(cst)) {
                float night = DayTimeHelper.getCurrentDaytimeDistribution(getLevel());
                if (night >= 0.1F) {
                    for (BlockPos pos : this.getConstellationPositions(cst)) {
                        this.playConstellationHighlightParticles(cst, pos, night);
                    }
                }
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playConstellationHighlightParticles(IConstellation cst, BlockPos pos, float nightPercent) {
        Vector3 at = new Vector3(pos).add(0.5, 0, 0.5);
        Vector3 offset = Vector3.random().mul(0.5F).setY(0);
        if (random.nextInt(3) == 0) {
            offset.mul(0.5);
            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(at.add(offset))
                    .color(VFXColorFunction.constant(cst.getConstellationColor()))
                    .setGravityStrength(-0.002F)
                    .setDeltaMovement(Vector3.random().addY(3).normalize().mul(0.03 + random.nextFloat() * 0.01))
                    .setAlphaMultiplier(0.6F * nightPercent)
                    .setScaleMultiplier(0.15F + random.nextFloat() * 0.1F)
                    .alpha1arg(VFXAlphaFunction.FADE_OUT);
        } else if (random.nextInt(3) == 0) {
            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(at.add(offset))
                    .setGravityStrength(-0.0005F)
                    .setDeltaMovement(Vector3.random().addY(3).normalize().mul(0.005))
                    .setAlphaMultiplier(0.6F * nightPercent)
                    .setScaleMultiplier(0.4F + random.nextFloat() * 0.2F)
                    .alpha1arg(VFXAlphaFunction.FADE_OUT)
                    .setMaxAge(60 + random.nextInt(40));
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playAltarConstellationHighlightParticles(IConstellation cst, float nightPercent) {
        Vector3 at = new Vector3(getBlockPos())
                .add(0.5, 0, 0.5)
                .add(Vector3.random().setY(0).mul(0.65F));

        EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                .spawn(at)
                .color(VFXColorFunction.constant(cst.getConstellationColor().brighter()))
                .setGravityStrength(-0.0015F)
                .setDeltaMovement(Vector3.random().addY(3).normalize().mul(0.03 + random.nextFloat() * 0.015))
                .setAlphaMultiplier(0.85F * nightPercent)
                .setScaleMultiplier(0.2F + random.nextFloat() * 0.1F)
                .alpha1arg(VFXAlphaFunction.FADE_OUT);
    }

    @Nullable
    public IConstellation getActiveConstellation() {
        return activeConstellation;
    }

    @Nullable
    public AttunementRecipe.Active<?> getActiveRecipe() {
        return currentRecipe;
    }

    @Nullable
    @Override
    public StructureType getRequiredStructureType() {
        return StructureTypesAS.PTYPE_ATTUNEMENT_ALTAR;
    }

    private void searchAndStartRecipe() {
        if (this.currentRecipe != null) {
            return;
        }
        AttunementRecipe<?> match = this.searchMatchingRecipe();
        if (match != null) {
            this.currentRecipe = match.createRecipe(this);
            this.currentRecipe.startCrafting(this);
            this.markForUpdate();
        }
    }

    @Nullable
    private AttunementRecipe<?> searchMatchingRecipe() {
        for (AttunementRecipe<?> recipe : AttunementCraftingRegistry.INSTANCE.getRecipes()) {
            if (recipe.canStartCrafting(this)) {
                return recipe;
            }
        }
        return null;
    }

    @Nullable
    private IConstellation searchActiveConstellation() {
        WorldContext ctx = SkyHandler.getContext(getLevel());
        if (ctx == null) {
            return null;
        }
        ConstellationHandler cstHandler = ctx.getConstellationHandler();
        IConstellation match = null;
        for (IConstellation cst : RegistriesAS.REGISTRY_CONSTELLATIONS.getValues()) {
            boolean resolved = true;
            for (BlockPos expectedRelayPos : getConstellationPositions(cst)) {
                if (expectedRelayPos.equals(this.getBlockPos())) {
                    continue;
                }

                BlockEntity tile = MiscUtils.getTileAt(getLevel(), expectedRelayPos, BlockEntity.class, true);
                if (!(tile instanceof TileSpectralRelay) && !(tile instanceof TileAttunementAltar)) {
                    resolved = false;
                    break;
                }
            }
            if (resolved) {
                match = cst;
                break;
            }
        }
        if (match != null && cstHandler.isActiveCurrently(match, MoonPhase.fromWorld(getLevel()))) {
            return match;
        }
        return null;
    }

    public Set<BlockPos> getConstellationPositions(IConstellation cst) {
        Set<BlockPos> offsetPositions = new HashSet<>();
        for (StarLocation sl : cst.getStars()) {
            int x = sl.x / 2;
            int z = sl.y / 2;
            offsetPositions.offset(new BlockPos(x - 7, 0, z - 7).add(getBlockPos()));
        }
        return offsetPositions;
    }

    private Set<Tuple<BlockPos, BlockPos>> getConstellationConnectionPositions(IConstellation cst) {
        Set<Tuple<BlockPos, BlockPos>> offsetPositions = new HashSet<>();
        for (StarConnection conn : cst.getStarConnections()) {
            StarLocation from = conn.getLeft();
            StarLocation to = conn.getRight();
            int fX = from.x / 2;
            int fZ = from.y / 2;
            int tX = to.x / 2;
            int tZ = to.y / 2;
            offsetPositions.add(
                    new Tuple<>(new BlockPos(fX - 7, 0, fZ - 7).offset(getBlockPos()),
                            new BlockPos(tX - 7, 0, tZ - 7).offset(getBlockPos()))
            );
        }
        return offsetPositions;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public AABB getBoundingBoxForCulling() {
        return super.getBoundingBoxForCulling().expand(3.5, 2, 3.5);
    }

    @Override
    public void writeNetNBT(CompoundTag pattern, HolderLookup.Provider registries) {
        super.writeNetNBT(pattern, registries);

        if (this.activeConstellation != null) {
            pattern.putString("activeConstellation", this.activeConstellation.getRegistryName().toString());
        }

        if (this.currentRecipe != null) {
            CompoundTag nbt = new CompoundTag();
            nbt.putString("recipe", this.currentRecipe.getRecipe().getKey().toString());
            this.currentRecipe.save(nbt);
            pattern.put("currentRecipe", nbt);
        }
    }

    @Override
    public void readNetNBT(CompoundTag pattern, HolderLookup.Provider registries) {
        super.readNetNBT(pattern, registries);

        if (pattern.contains("activeConstellation")) {
            this.activeConstellation = ConstellationRegistry.getConstellation(ResourceLocation.parse(pattern.getString("activeConstellation")));
        } else {
            this.activeConstellation = null;
        }

        if (pattern.contains("currentRecipe")) {
            CompoundTag nbt = pattern.getCompound("currentRecipe");
            AttunementRecipe recipe = AttunementCraftingRegistry.INSTANCE.getRecipe(ResourceLocation.parse(nbt.getString("recipe")));
            if (recipe != null) {
                this.currentRecipe = recipe.deserialize(this, nbt, this.currentRecipe);
            } else if (this.currentRecipe != null) {
                this.currentRecipe.stopEffects(this);
                this.currentRecipe = null;
            }
        } else if (this.currentRecipe != null) {
            this.currentRecipe.stopEffects(this);
            this.currentRecipe = null;
        }
    }
}
