/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.common.auxiliary.CropHelper;
import hellfirepvp.astralsorcery.common.auxiliary.charge.AlignmentChargeHandler;
import hellfirepvp.astralsorcery.common.data.config.base.ConfigEntry;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.network.PacketChannel;
import hellfirepvp.astralsorcery.common.network.play.server.PktPlayEffect;
import hellfirepvp.astralsorcery.common.perk.PerkAttributeHelper;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;
import hellfirepvp.astralsorcery.common.perk.tick.PlayerTickPerk;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: KeyGrowables
 * Created by HellFirePvP
 * Date: 31.08.2019 / 17:48
 */
public class KeyGrowables extends KeyPerk implements PlayerTickPerk {

    private static final float defaultChanceToBonemeal = 0.3F;
    private static final int defaultRadius = 3;
    private static final int defaultChargeCost = 120;

    public static final Config CONFIG = new Config("key.growable");

    public KeyGrowables(ResourceLocation name, float x, float y) {
        super(name, x, y);
    }

    @Override
    public void onPlayerTick(Player player, LogicalSide direction) {
        if (!direction.isServer()) {
            return;
        }

        PlayerProgress prog = ResearchHelper.getProgress(player, direction);
        float cChance = PerkAttributeHelper.getOrCreateMap(player, direction)
                .modifyValue(player, prog, PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EFFECT, CONFIG.chanceToBonemeal.get().floatValue());
        if (random.nextFloat() < cChance && AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, CONFIG.chargeCost.get(), true)) {
            float fRadius = PerkAttributeHelper.getOrCreateMap(player, direction)
                    .modifyValue(player, prog, PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EFFECT, CONFIG.radius.get());
            int rRadius = Math.max(Mth.floor(fRadius), 1);

            BlockPos pos = player.position().add(
                    random.nextInt(rRadius * 2) + 1 - rRadius,
                    random.nextInt(rRadius * 2) + 1 - rRadius,
                    random.nextInt(rRadius * 2) + 1 - rRadius);
            Level w = player.getCommandSenderWorld();
            CropHelper.GrowablePlant plant = CropHelper.wrapPlant(w, pos);
            PktPlayEffect pkt = null;
            if (plant != null) {
                if (plant.tryGrow(w, random)) {
                    AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, CONFIG.chargeCost.get(), false);
                    pkt = new PktPlayEffect(PktPlayEffect.Type.CROP_GROWTH)
                        .addData(buf -> ByteBufUtils.writeVector(buf, new Vector3(pos)));
                }
            } else {
                BlockState at = w.getBlockState(pos);
                if (at.getBlock().equals(Blocks.DIRT) && w.isEmptyBlock(pos.above())) {
                    if (w.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState())) {
                        AlignmentChargeHandler.INSTANCE.drainCharge(player, LogicalSide.SERVER, CONFIG.chargeCost.get(), false);
                        pkt = new PktPlayEffect(PktPlayEffect.Type.CROP_GROWTH)
                                .addData(buf -> ByteBufUtils.writeVector(buf, new Vector3(pos)));
                    }
                }
            }
            if (pkt != null) {
                PacketChannel.CHANNEL.sendToAllAround(pkt, PacketChannel.pointFromPos(w, pos, 16));
            }
        }
    }

    public static class Config extends ConfigEntry {

        private ModConfigSpec.DoubleValue chanceToBonemeal;
        private ModConfigSpec.IntValue radius;
        private ModConfigSpec.IntValue chargeCost;

        private Config(String section) {
            super(section);
        }

        @Override
        public void createEntries(ModConfigSpec.Builder cfgBuilder) {
            this.chanceToBonemeal = cfgBuilder
                    .comment("Sets the chance to try to see if a random plant near the player gets bonemeal'd.")
                    .translation(translationKey("chanceToBonemeal"))
                    .defineInRange("chanceToBonemeal", defaultChanceToBonemeal, 0F, 1F);
            this.radius = cfgBuilder
                    .comment("Defines the radius around which the perk effect should apply around the player.")
                    .translation(translationKey("radius"))
                    .defineInRange("radius", defaultRadius, 1, 16);
            this.chargeCost = cfgBuilder
                    .comment("Defines the amount of starlight charge consumed per growth-attempt.")
                    .translation(translationKey("chargeCost"))
                    .defineInRange("chargeCost", defaultChargeCost, 1, 500);
        }
    }
}
