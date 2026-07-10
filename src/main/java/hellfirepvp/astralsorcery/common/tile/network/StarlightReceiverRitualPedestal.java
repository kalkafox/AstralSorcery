/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.tile.network;

import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.constellation.SkyHandler;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffect;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectRegistry;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectStatus;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.constellation.world.WorldContext;
import hellfirepvp.astralsorcery.common.crystal.CrystalAttributes;
import hellfirepvp.astralsorcery.common.crystal.CrystalCalculations;
import hellfirepvp.astralsorcery.common.starlight.WorldNetworkHandler;
import hellfirepvp.astralsorcery.common.starlight.transmission.IPrismTransmissionNode;
import hellfirepvp.astralsorcery.common.starlight.transmission.NodeConnection;
import hellfirepvp.astralsorcery.common.starlight.transmission.base.SimpleTransmissionReceiver;
import hellfirepvp.astralsorcery.common.starlight.transmission.registry.TransmissionProvider;
import hellfirepvp.astralsorcery.common.tile.TileRitualPedestal;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.PartialEffectExecutor;
import hellfirepvp.astralsorcery.common.util.RaytraceAssist;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import hellfirepvp.astralsorcery.common.util.world.SkyCollectionHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.Level;
import hellfirepvp.astralsorcery.common.util.Constants;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nullable;
import java.util.*;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: StarlightReceiverRitualPedestal
 * Created by HellFirePvP
 * Date: 09.07.2019 / 19:26
 */
public class StarlightReceiverRitualPedestal extends SimpleTransmissionReceiver<TileRitualPedestal> {

    private static final Random random = new Random();

    //own receiver data
    private final Map<BlockPos, Boolean> offsetMirrors = new HashMap<>();

    //tile data
    private boolean doesSeeSky = false, hasMultiblock = false;
    private IWeakConstellation channelingType = null;
    private IMinorConstellation channelingTrait = null;
    private CrystalAttributes attributes = null;
    private BlockPos ritualLinkPos = null;

    //Random other data
    private int tickCount = 0;
    private ConstellationEffect effect = null;
    private double collectedStarlight = 0;

    private float noiseDistribution = -1;

    public StarlightReceiverRitualPedestal(BlockPos thisPos) {
        super(thisPos);
    }

    @Override
    public void update(Level level) {
        super.update(level);
        this.tickCount++;

        if (!this.hasMultiblock || this.channelingType == null || this.attributes == null) {
            return;
        }

        if ((this.tickCount % 20) == 0) {
            validateMirrorPositions(level);
        }

        if (this.doesSeeSky) {
            collectStarlight(level);
        }

        if (this.effect != null && this.collectedStarlight > 0) {
            doRitualEffect(level);
        }
    }

    private void doRitualEffect(Level level) {
        ConstellationEffectProperties properties = this.effect.createProperties(this.getMirrorCount());
        if (this.channelingTrait != null) {
            this.channelingTrait.affectConstellationEffect(properties);
        }
        properties.multiplySize(CrystalCalculations.getRitualEffectRangeFactor(this, this.attributes));

        float maxDrain = 12;
        maxDrain *= CrystalCalculations.getRitualCostReductionFactor(this, this.attributes);
        maxDrain /= Math.max(1F, ((float) (this.getMirrorCount() - 1)) * 0.33F);
        float ritualStrength = ((float) collectedStarlight) / maxDrain;

        BlockPos to = getLocationPos();
        if (this.ritualLinkPos != null) {
            to = this.ritualLinkPos;
        }

        if (this.effect instanceof ConstellationEffectStatus && this.collectedStarlight > 0) {
            this.collectedStarlight = 0;
            if (this.effect.getConfig().enabled.get() && ((ConstellationEffectStatus) this.effect).runStatusEffect(level, to, this.getMirrorCount(), properties, this.channelingTrait)) {
                setChanged(level);
            }
            return;
        }

        float max = 10F * properties.getEffectAmplifier();
        float stretch = 10F / properties.getPotency();

        float executeTimes = (float) Math.atan(ritualStrength / stretch) * max;
        if (properties.isCorrupted()) {
            executeTimes *= Math.max(random.nextDouble() * 1.4, 0.1);
        }
        PartialEffectExecutor exec = new PartialEffectExecutor(executeTimes, random);
        while (exec.canExecute()) {
            exec.markExecution();
            if (this.effect.getConfig().enabled.get()) {
                boolean didEffectExecute;
                if (this.effect.needsChunkToBeLoaded()) {
                    didEffectExecute = MiscUtils.executeWithChunk(level, to, to, (pos) -> {
                        return this.effect.playEffect(level, pos, properties, this.channelingTrait);
                    }, false);
                } else {
                    didEffectExecute = this.effect.playEffect(level, to, properties, this.channelingTrait);
                }

                if (didEffectExecute) {
                    setChanged(level);
                }
            }
        }
        this.collectedStarlight = 0F;
    }

    private void collectStarlight(Level level) {
        WorldContext ctx = SkyHandler.getContext(level, LogicalSide.SERVER);
        if (ctx == null) {
            return;
        }

        double collected = 1.3;
        collected *= 0.25 + (0.75 * DayTimeHelper.getCurrentDaytimeDistribution(level));

        if (this.noiseDistribution == -1) {
            if (level instanceof WorldGenLevel) {
                this.noiseDistribution = SkyCollectionHelper.getSkyNoiseDistribution((WorldGenLevel) level, this.getLocationPos());
            } else {
                this.noiseDistribution = 0.3F;
            }
        }

        collected *= CrystalCalculations.getCrystalCollectionRate(attributes);
        collected *= 0.4F + (0.6F * ctx.getDistributionHandler().getDistribution(this.channelingType));
        collected *= 1F + (0.5F * this.noiseDistribution);

        this.collectedStarlight += collected;
    }

    @Override
    public void onStarlightReceive(Level level, IWeakConstellation type, double amount) {
        if (this.channelingType != null && this.hasMultiblock && this.channelingType.equals(type)) {
            this.collectedStarlight += amount / 2;
            this.findNextMirror(level);
        }
    }

    //=========================================================================================
    // Tile/Node sync stuff
    //=========================================================================================


    @Override
    public boolean syncTileData(Level level, TileRitualPedestal tile) {
        tile.setReceiverData(this.effect != null, this.offsetMirrors, this.attributes);
        this.setChanged(level);
        return true;
    }

    @Override
    public <T extends BlockEntity> boolean updateFromTileEntity(T tile) {
        if (!(tile instanceof TileRitualPedestal)) {
            return super.updateFromTileEntity(tile); //Whatever.
        }

        TileRitualPedestal trp = (TileRitualPedestal) tile;
        if (this.channelingType != trp.getRitualConstellation() ||
                (this.attributes != null && trp.getAttributes() == null) ||
                this.hasMultiblock != trp.hasMultiblock()) {

            this.effect = null;
            this.offsetMirrors.clear();
            if (trp.isWorking() || !trp.getMirrors().isEmpty()) {
                this.markForTileSync();
            }
        }

        boolean ritualLinkChanged;
        if (this.ritualLinkPos == null) {
            ritualLinkChanged = trp.getRitualLinkTo() != null;
        } else {
            ritualLinkChanged = !this.ritualLinkPos.equals(trp.getRitualLinkTo());
        }

        this.doesSeeSky = trp.doesSeeSky();
        this.hasMultiblock = trp.hasMultiblock();
        this.channelingType = trp.getRitualConstellation();
        this.channelingTrait = trp.getRitualTrait();
        this.attributes = trp.getAttributes();
        this.ritualLinkPos = trp.getRitualLinkTo();

        if (this.channelingType != null && this.attributes != null && this.hasMultiblock && (this.effect == null || ritualLinkChanged)) {
            this.effect = ConstellationEffectRegistry.createInstance(this, this.channelingType);
            this.markForTileSync();
        }

        if (!this.hasMultiblock || this.effect == null) {
            this.collectedStarlight = 0;
        }

        this.setChanged(trp.getLevel());
        return super.updateFromTileEntity(tile);
    }

    @Override
    public Class<TileRitualPedestal> getTileClass() {
        return TileRitualPedestal.class;
    }

    //=========================================================================================
    // Stuff surrounding lenses
    //=========================================================================================

    private void findNextMirror(Level level) {
        if (this.offsetMirrors.size() >= TileRitualPedestal.MAX_MIRROR_COUNT || this.effect == null || this.channelingType == null) {
            return;
        }

        long seed = 3451968351053166105L;
        seed |= this.getLocationPos().asLong() * 31;
        seed |= this.channelingType.getName().hashCode() * 31;
        Random r = new Random(seed);
        for (int i = 0; i < this.getMirrorCount(); i++) {
            r.nextInt(TileRitualPedestal.RITUAL_CIRCLE_OFFSETS.size());
        }
        BlockPos offset = null;
        int c = 100;
        lblWhile: while (offset == null && c > 0) {
            c--;

            BlockPos test = MiscUtils.getRandomEntry(TileRitualPedestal.RITUAL_CIRCLE_OFFSETS, r);
            RaytraceAssist ray = new RaytraceAssist(getLocationPos(), getLocationPos().offset(test));
            Vector3 from = new Vector3(0.5, 0.7, 0.5);
            Vector3 newDir = new Vector3(test).add(0.5, 0.5, 0.5).subtract(from);

            for (BlockPos p : offsetMirrors.keySet()) {
                Vector3 toDir = new Vector3(p).add(0.5, 0.5, 0.5).subtract(from);
                if (Math.toDegrees(toDir.angle(newDir)) <= 30) {
                    continue lblWhile;
                }
                if (from.distanceSquared(Vec3.copyCentered(p)) <= 3) {
                    continue lblWhile;
                }
            }

            if (!ray.isClear(level)) {
                continue;
            }
            offset = test;
        }

        if (offset != null) {
            this.offsetMirrors.put(offset, false);
            this.markForTileSync();
            this.setChanged(level);
        }
    }

    private void validateMirrorPositions(Level level) {
        WorldNetworkHandler handle = WorldNetworkHandler.getNetworkHandler(level);
        List<BlockPos> srcLinkingToThis = this.getSources();

        boolean needsUpdate = false;
        for (BlockPos pos : new ArrayList<>(this.offsetMirrors.keySet())) {
            BlockPos actualPos = this.getLocationPos().offset(pos);
            boolean existingFlag = this.offsetMirrors.get(pos);

            //If the source is not linking to this
            if (!srcLinkingToThis.contains(actualPos)) {
                this.offsetMirrors.put(pos, false);
                if (existingFlag) {
                    needsUpdate = true;
                }
                continue;
            }

            IPrismTransmissionNode other = handle.getTransmissionNode(actualPos);
            if (other == null) {
                continue;
            }

            boolean foundLink = false;
            for (NodeConnection<IPrismTransmissionNode> n : other.queryNext(handle)) {
                if (n.getTo().equals(getLocationPos())) {
                    boolean connect = n.canConnect();
                    this.offsetMirrors.put(pos, connect);
                    if (connect != existingFlag) {
                        needsUpdate = true;
                    }
                    foundLink = true;
                    break;
                }
            }

            if (!foundLink) {
                this.offsetMirrors.put(pos, false);
                if (existingFlag) {
                    needsUpdate = true;
                }
            }
        }
        if (needsUpdate) {
            this.markForTileSync();
        }
    }

    private int getMirrorCount() {
        return (int) this.offsetMirrors.values().stream()
                .filter(b -> b)
                .count();
    }

    //=========================================================================================
    // Misc and I/O
    //=========================================================================================

    @Nullable
    public IWeakConstellation getChannelingType() {
        return channelingType;
    }

    @Nullable
    public IMinorConstellation getChannelingTrait() {
        return channelingTrait;
    }

    @Override
    public boolean needsUpdate() {
        return true;
    }

    @Override
    public TransmissionProvider getProvider() {
        return new Provider();
    }

    @Override
    public void readFromNBT(CompoundTag pattern) {
        super.readFromNBT(pattern);

        this.doesSeeSky = pattern.getBoolean("doesSeeSky");
        this.hasMultiblock = pattern.getBoolean("hasMultiblock");
        this.tickCount = pattern.getInt("ticksExisted");
        this.collectedStarlight = pattern.getDouble("collectedStarlight");

        IConstellation channeling = IConstellation.readFromNBT(pattern, "channelingType");
        if (channeling instanceof IWeakConstellation) {
            this.channelingType = (IWeakConstellation) channeling;
        } else {
            this.channelingType = null;
        }
        IConstellation trait = IConstellation.readFromNBT(pattern, "channelingTrait");
        if (trait instanceof IMinorConstellation) {
            this.channelingTrait = (IMinorConstellation) trait;
        } else {
            this.channelingTrait = null;
        }

        this.attributes = CrystalAttributes.getCrystalAttributes(pattern);
        if (pattern.contains("ritualLinkPos")) {
            this.ritualLinkPos = NBTHelper.readBlockPosFromNBT(pattern.getCompound("ritualLinkPos"));
        } else {
            this.ritualLinkPos = null;
        }

        this.offsetMirrors.clear();
        ListTag list = pattern.getList("mirrors", Constants.NBT.TAG_COMPOUND);
        for (Tag nbt : list) {
            CompoundTag tag = (CompoundTag) nbt;
            this.offsetMirrors.put(NBTHelper.readBlockPosFromNBT(tag), tag.getBoolean("connect"));
        }

        //Reset ritual effect.
        if (this.channelingType != null) {
            this.effect = ConstellationEffectRegistry.createInstance(this, this.channelingType);
            if (this.effect != null && pattern.contains("effect")) {
                this.effect.readFromNBT(pattern.getCompound("effect"));
            }
        }
    }

    @Override
    public void save(CompoundTag pattern) {
        super.save(pattern);

        pattern.putBoolean("doesSeeSky", this.doesSeeSky);
        pattern.putBoolean("hasMultiblock", this.hasMultiblock);
        pattern.putInt("ticksExisted", this.tickCount);
        pattern.putDouble("collectedStarlight", this.collectedStarlight);

        if (this.channelingType != null) {
            this.channelingType.save(pattern, "channelingType");
        }
        if (this.channelingTrait != null) {
            this.channelingTrait.save(pattern, "channelingTrait");
        }

        if (attributes != null) {
            attributes.store(pattern);
        }
        if (this.ritualLinkPos != null) {
            pattern.put("ritualLinkPos", NBTHelper.writeBlockPosToNBT(this.ritualLinkPos, new CompoundTag()));
        }

        ListTag listPositions = new ListTag();
        for (Map.Entry<BlockPos, Boolean> posEntry : this.offsetMirrors.entrySet()) {
            CompoundTag cmp = new CompoundTag();
            NBTHelper.writeBlockPosToNBT(posEntry.getKey(), cmp);
            cmp.putBoolean("connect", posEntry.getValue());
            listPositions.add(cmp);
        }
        pattern.put("mirrors", listPositions);

        if (this.channelingType != null && this.effect != null) {
            NBTHelper.setAsSubTag(pattern, "effect", this.effect::save);
        }
    }

    public static class Provider extends TransmissionProvider {

        @Override
        public IPrismTransmissionNode get() {
            return new StarlightReceiverRitualPedestal(null);
        }

    }
}
