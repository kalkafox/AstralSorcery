/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.wand;

import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.block.ore.BlockRockCrystalOre;
import hellfirepvp.astralsorcery.common.constellation.world.DayTimeHelper;
import hellfirepvp.astralsorcery.common.data.world.RockCrystalBuffer;
import hellfirepvp.astralsorcery.common.item.base.OverrideInteractItem;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.DataAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.structure.types.StructureType;
import hellfirepvp.astralsorcery.common.tile.base.TileRequiresMultiblock;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.observerlib.api.structure.MatchableStructure;
import hellfirepvp.observerlib.api.util.BlockArray;
import hellfirepvp.observerlib.client.preview.StructurePreview;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemWand
 * Created by HellFirePvP
 * Date: 17.08.2019 / 23:03
 */
public class ItemWand extends Item implements OverrideInteractItem {

    private static final java.util.Random random = new java.util.Random();

    public ItemWand() {
        super(new Properties()
                .stacksTo(1)
);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        boolean active = isSelected || (entity instanceof Player && ((Player) entity).getOffhandItem() == stack);

        if (!level.isClientSide()) {
            if (active) {
                if (entity instanceof ServerPlayer) {
                    RockCrystalBuffer buf = DataAS.DOMAIN_AS.getData(level, DataAS.KEY_ROCK_CRYSTAL_BUFFER);

                    ChunkPos pos = new ChunkPos(entity.position());
                    for (BlockPos rPos : buf.collectPositions(pos, 6)) {
                        MiscUtils.executeWithChunk(level, rPos, () -> {
                            BlockState state = level.getBlockState(rPos);
                            if (!(state.getBlock() instanceof BlockRockCrystalOre)) {
                                buf.removeOre(rPos);
                                return;
                            }
                            if (!DayTimeHelper.isDay(level) && random.nextInt(600) == 0) {
                                PktPlayEffect pkt = new PktPlayEffect(PktPlayEffect.Type.ROCK_CRYSTAL_COLUMN)
                                        .addData(b -> ByteBufUtils.writeVector(b, new Vector3(rPos.above())));
                                PacketChannel.CHANNEL.sendToPlayer((Player) entity, pkt);
                            }
                            if (random.nextInt(800) == 0) {
                                PktPlayEffect pkt = new PktPlayEffect(PktPlayEffect.Type.ROCK_CRYSTAL_SPARKS)
                                        .addData(b -> ByteBufUtils.writeVector(b, new Vector3(rPos.above())));
                                PacketChannel.CHANNEL.sendToPlayer((Player) entity, pkt);
                            }
                        });
                    }
                }
            }
        }
    }

    @Override
    public boolean shouldInterceptBlockInteract(LogicalSide direction, Player player, InteractionHand hand, BlockPos pos, Direction face) {
        return true;
    }

    @Override
    public boolean doBlockInteract(LogicalSide direction, Player player, InteractionHand hand, BlockPos pos, Direction face) {
        Level level = player.getCommandSenderWorld();
        BlockState state = level.getBlockState(pos);
        Block b = state.getBlock();
        if (b instanceof WandInteractable) {
            if (((WandInteractable) b).onInteract(level, pos, player, face, player.isShiftKeyDown())) {
                return true;
            }
        }
        WandInteractable wandTe = MiscUtils.getTileAt(level, pos, WandInteractable.class, true);
        if (wandTe != null) {
            if (wandTe.onInteract(level, pos, player, face, player.isShiftKeyDown())) {
                return true;
            }
        }
        TileRequiresMultiblock mbTe = MiscUtils.getTileAt(level, pos, TileRequiresMultiblock.class, true);
        if (mbTe != null) {
            if (mbTe.getRequiredStructureType() != null &&
                    mbTe.getRequiredStructureType().getFeature() instanceof MatchableStructure &&
                    !((MatchableStructure) mbTe.getRequiredStructureType().getFeature()).matches(level, pos)) {
                if (level.isClientSide()) {
                    this.displayClientStructurePreview(level, pos, mbTe.getRequiredStructureType());
                } else if (player.isCrouching() && player.isCreative()) {
                    BlockArray structure = mbTe.getRequiredStructureType().getFeature();
                    structure.getContents().forEach((offset, rState) -> {
                        level.setBlock(pos.offset(offset), rState.getDescriptiveState(0));
                    });
                }
                return true;
            }
        }
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    private void displayClientStructurePreview(Level level, BlockPos pos, StructureType type) {
        StructurePreview.properties(level.dimension(), pos, (MatchableStructure) type.getFeature())
                .removeIfOutInDifferentWorld()
                .andPersistOnlyIf((inWorld, at) -> {
                    return MiscUtils.executeWithChunk(level, pos, () -> {
                        TileRequiresMultiblock tileFound = MiscUtils.getTileAt(level, pos, TileRequiresMultiblock.class, true);
                        if (tileFound == null) {
                            return false;
                        }
                        return tileFound.getRequiredStructureType() != null &&
                                tileFound.getRequiredStructureType().equals(type);
                    }, true);
                })
                .andPersistOnlyIf((inWorld, at) -> !((MatchableStructure) type.getFeature()).matches(level, pos))
                .showBar(type.getDisplayName())
                .buildAndSet();
    }

    @OnlyIn(Dist.CLIENT)
    public static void playUndergroundEffect(PktPlayEffect effect) {
        Vector3 at = ByteBufUtils.readVector(effect.getExtraData());

        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        float dstr = 0.4F + 0.6F * DayTimeHelper.getCurrentDaytimeDistribution(level);
        Vector3 plVec = Vector3.atEntityCorner(Minecraft.getInstance().player);
        float dst = (float) at.distance(plVec);
        float dstMul = dst <= 25 ? 1F : (dst >= 50 ? 0F : (1F - (dst - 25F) / 25F));
        for (int i = 0; i < 3; i++) {
            if (random.nextBoolean()) {
                EffectHelper.of(EffectTemplatesAS.GENERIC_DEPTH_PARTICLE)
                        .spawn(at.clone().add(-1 + random.nextFloat() * 3, -1 + random.nextFloat() * 3, -1 + random.nextFloat() * 3))
                        .color(VFXColorFunction.constant(ColorsAS.ROCK_CRYSTAL))
                        .setScaleMultiplier(0.4F)
                        .setAlphaMultiplier(((150F * dstr) / 255F) * dstMul)
                        .alpha1arg(VFXAlphaFunction.FADE_OUT)
                        .setMaxAge(30 + random.nextInt(10));
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void playEffect(PktPlayEffect effect) {
        Vector3 pos = ByteBufUtils.readVector(effect.getExtraData());

        Level level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        BlockPos at = pos.toBlockPos();
        BlockPos top = level.getHeight(Heightmap.Type.WORLD_SURFACE, at);

        Vector3 columnDisplay = new Vector3(top);
        MiscUtils.applyRandomOffset(columnDisplay, random, 2F);

        double mX = random.nextFloat() * 0.01F * (random.nextBoolean() ? 1 : -1);
        double mY = random.nextFloat() * 0.5F;
        double mZ = random.nextFloat() * 0.01F * (random.nextBoolean() ? 1 : -1);
        float dstr = DayTimeHelper.getCurrentDaytimeDistribution(level);
        for (int i = 0; i < 8 + random.nextInt(10); i++) {
            EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                    .spawn(columnDisplay)
                    .setDeltaMovement(new Vector3(
                            mX * (0.2 + 0.8 * random.nextFloat()),
                            mY * (random.nextFloat()),
                            mZ * (0.2 + 0.8 * random.nextFloat())
                    ))
                    .color(VFXColorFunction.constant(ColorsAS.ROCK_CRYSTAL))
                    .setAlphaMultiplier((150 * dstr) / 255F)
                    .alpha1arg(VFXAlphaFunction.FADE_OUT)
                    .setScaleMultiplier(0.3F + 0.3F * random.nextFloat())
                    .setMaxAge(25 + random.nextInt(30));
        }
    }
}
