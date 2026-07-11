/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.tile;

import com.google.common.collect.Lists;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.effect.vfx.FXFacingParticle;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.auxiliary.link.LinkableTileEntity;
import hellfirepvp.astralsorcery.common.lib.TileEntityTypesAS;
import hellfirepvp.astralsorcery.common.tile.base.TileEntityTick;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: TileRitualLink
 * Created by HellFirePvP
 * Date: 10.07.2019 / 21:07
 */
public class TileRitualLink extends TileEntityTick implements LinkableTileEntity {

    private BlockPos linkedTo = null;

    public TileRitualLink(BlockPos pos, BlockState state) {
        super(TileEntityTypesAS.RITUAL_LINK, pos, state);
    }

    @Override
    public void tick() {
        super.tick();

        if (getLevel().isClientSide()) {
            playClientEffects();
        } else {
            if (linkedTo != null) {
                MiscUtils.executeWithChunk(getLevel(), linkedTo, () -> {
                    TileRitualLink link = MiscUtils.getTileAt(getLevel(), linkedTo, TileRitualLink.class, true);
                    if (link == null) {
                        linkedTo = null;
                        markForUpdate();
                    }
                });
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playClientEffects() {
        if (this.linkedTo != null) {
            if (tickCount % 4 == 0) {
                Collection<Vector3> positions = MiscUtils.getCirclePositions(
                        new Vector3(this).add(0.5, 0.5, 0.5),
                        Vector3.RotAxis.Y_AXIS, 0.4F - random.nextFloat() * 0.1F, 10 + random.nextInt(10));
                for (Vector3 v : positions) {
                    FXFacingParticle particle = EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                            .spawn(v)
                            .setScaleMultiplier(0.15F)
                            .setDeltaMovement(new Vector3(0, (random.nextBoolean() ? 1 : -1) * random.nextFloat() * 0.01, 0));
                    if (random.nextBoolean()) {
                        particle.color(VFXColorFunction.WHITE);
                    }
                }
            }

            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(new Vector3(this).add(0.5, 0.5, 0.5))
                    .setScaleMultiplier(0.3F)
                    .setDeltaMovement(new Vector3(0, (random.nextBoolean() ? 1 : -1) * random.nextFloat() * 0.015, 0))
                    .color(VFXColorFunction.random());

        }
    }

    @Nullable
    public BlockPos getLinkedTo() {
        return linkedTo;
    }

    @Override
    public void readCustomNBT(CompoundTag pattern, HolderLookup.Provider registries) {
        super.readCustomNBT(pattern, registries);

        this.linkedTo = NBTHelper.readFromSubTag(pattern, "posLink", NBTHelper::readBlockPosFromNBT);
    }

    @Override
    public void writeCustomNBT(CompoundTag pattern, HolderLookup.Provider registries) {
        super.writeCustomNBT(pattern, registries);

        if (this.linkedTo != null) {
            NBTHelper.setAsSubTag(pattern, "posLink", nbt -> NBTHelper.writeBlockPosToNBT(this.linkedTo, nbt));
        }
    }

    @Override
    public void onBlockLinkCreate(Player player, BlockPos other) {
        if (this.linkedTo != null) {
            TileRitualLink otherLink = MiscUtils.getTileAt(player.getCommandSenderWorld(), this.linkedTo, TileRitualLink.class, true);
            if (otherLink != null) {
                otherLink.linkedTo = null;
                otherLink.markForUpdate();
            }
        }
        this.linkedTo = other;
        TileRitualLink otherLink = MiscUtils.getTileAt(player.getCommandSenderWorld(), other, TileRitualLink.class, true);
        if (otherLink != null) {
            otherLink.linkedTo = getBlockPos();
            otherLink.markForUpdate();
        }

        markForUpdate();
    }

    @Override
    public void onEntityLinkCreate(Player player, LivingEntity linked) {
    }

    @Override
    public boolean tryLinkBlock(Player player, BlockPos other) {
        TileRitualLink otherLink = MiscUtils.getTileAt(player.getCommandSenderWorld(), other, TileRitualLink.class, true);
        return otherLink != null && otherLink.linkedTo == null && !other.equals(getBlockPos());
    }

    @Override
    public boolean tryLinkEntity(Player player, LivingEntity other) {
        return false;
    }

    @Override
    public boolean tryUnlink(Player player, BlockPos other) {
        TileRitualLink otherLink = MiscUtils.getTileAt(player.getCommandSenderWorld(), other, TileRitualLink.class, true);
        if (otherLink == null || otherLink.linkedTo == null) return false;
        if (otherLink.linkedTo.equals(getBlockPos())) {
            this.linkedTo = null;
            otherLink.linkedTo = null;
            otherLink.markForUpdate();
            markForUpdate();
            return true;
        }
        return false;
    }

    @Override
    public List<BlockPos> getLinkedPositions() {
        return linkedTo != null ? Lists.newArrayList(linkedTo) : Lists.newArrayList();
    }
}
