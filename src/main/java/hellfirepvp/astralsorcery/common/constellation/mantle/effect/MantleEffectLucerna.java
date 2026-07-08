/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.mantle.effect;

import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.constellation.mantle.MantleEffect;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.util.block.BlockDiscoverer;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.items.CapabilityItemHandler;

import java.awt.*;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: MantleEffectLucerna
 * Created by HellFirePvP
 * Date: 21.02.2020 / 20:58
 */
public class MantleEffectLucerna extends MantleEffect {

    public static LucernaConfig CONFIG = new LucernaConfig();

    public MantleEffectLucerna() {
        super(ConstellationsAS.lucerna);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    protected void tickClient(Player player) {
        super.tickClient(player);

        this.playCapeSparkles(player, 0.15F);

        if (rand.nextBoolean()) {
            this.playEntityHighlight(player);
        }
        if (CONFIG.findSpawners.get() && rand.nextInt(10) == 0) {
            this.playBlockHighlight(player, ColorsAS.MANTLE_LUCERNA_SPAWNER, (tileEntity) -> tileEntity instanceof SpawnerBlockEntity);
        }
        if (CONFIG.findChests.get() && rand.nextInt(10) == 0) {
            this.playBlockHighlight(player, ColorsAS.MANTLE_LUCERNA_INVENTORY, (tileEntity) -> tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent());
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playBlockHighlight(Player player, Color highlightColor, Predicate<BlockEntity> test) {
        float chance = 0.9F;
        Set<BlockPos> positions = BlockDiscoverer.searchForTileEntitiesAround(player.getEntityWorld(), player.getPosition(), CONFIG.range.get(), test);
        for (BlockPos pos : positions) {
            if (rand.nextFloat() > chance) {
                continue;
            }
            Vector3 at = new Vector3(pos).add(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
            if (at.distance(player) < 4) {
                continue;
            }

            EffectHelper.of(EffectTemplatesAS.GENERIC_DEPTH_PARTICLE)
                    .setOwner(player.getUniqueID())
                    .spawn(at)
                    .color(VFXColorFunction.constant(highlightColor))
                    .alpha(VFXAlphaFunction.FADE_OUT)
                    .setScaleMultiplier(0.4F + rand.nextFloat() * 0.4F)
                    .setMaxAge(30 + rand.nextInt(15));

            if (rand.nextFloat() > 0.35F) {
                EffectHelper.of(EffectTemplatesAS.GENERIC_DEPTH_PARTICLE)
                        .setOwner(player.getUniqueID())
                        .spawn(at)
                        .color(VFXColorFunction.WHITE)
                        .alpha(VFXAlphaFunction.FADE_OUT)
                        .setScaleMultiplier(0.2F + rand.nextFloat() * 0.2F)
                        .setMaxAge(20 + rand.nextInt(10));
            }

            chance *= 0.9F;
        }
    }

    @OnlyIn(Dist.CLIENT)
    private void playEntityHighlight(Player player) {
        AABB box = new AABB(0, 0, 0, 0, 0, 0)
                .grow(CONFIG.range.get())
                .offset(player.getPosition());
        List<LivingEntity> entities = player.getEntityWorld().getEntitiesWithinAABB(LivingEntity.class, box);
        for (LivingEntity entity : entities) {
            if (!entity.isAlive() || entity.equals(player) || rand.nextInt(8) != 0) {
                continue;
            }

            Vector3 atEntity = Vector3.atEntityCorner(entity);
            if (atEntity.distance(player) < 2) {
                continue;
            }
            atEntity.add(rand.nextFloat() * entity.getWidth(), rand.nextFloat() * entity.getHeight(), rand.nextFloat() * entity.getWidth());

            EffectHelper.of(EffectTemplatesAS.GENERIC_DEPTH_PARTICLE)
                    .setOwner(player.getUniqueID())
                    .spawn(atEntity)
                    .color(VFXColorFunction.constant(this.getAssociatedConstellation().getConstellationColor()))
                    .alpha(VFXAlphaFunction.FADE_OUT)
                    .setScaleMultiplier(0.4F + rand.nextFloat() * 0.4F)
                    .setMaxAge(30 + rand.nextInt(15));

            if (rand.nextFloat() > 0.35F) {
                EffectHelper.of(EffectTemplatesAS.GENERIC_DEPTH_PARTICLE)
                        .setOwner(player.getUniqueID())
                        .spawn(atEntity)
                        .color(VFXColorFunction.WHITE)
                        .alpha(VFXAlphaFunction.FADE_OUT)
                        .setScaleMultiplier(0.2F + rand.nextFloat() * 0.2F)
                        .setMaxAge(20 + rand.nextInt(10));
            }
        }
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    @Override
    protected boolean usesTickMethods() {
        return true;
    }

    public static class LucernaConfig extends Config {

        private final int defaultRange = 48;
        private final boolean defaultFindSpawners = true;
        private final boolean defaultFindChests = true;

        public ModConfigSpec.IntValue range;
        public ModConfigSpec.BooleanValue findSpawners;
        public ModConfigSpec.BooleanValue findChests;

        public LucernaConfig() {
            super("lucerna");
        }

        @Override
        public void createEntries(ModConfigSpec.Builder cfgBuilder) {
            super.createEntries(cfgBuilder);

            this.range = cfgBuilder
                    .comment("Sets the maximum range of where the lucerna cape effect will get entities (and potentially other stuff given the config option for that is enabled) to highlight.")
                    .translation(translationKey("range"))
                    .defineInRange("range", this.defaultRange, 0, 512);
            this.findSpawners = cfgBuilder
                    .comment("If this is set to true, particles spawned by the lucerna cape effect will also highlight spawners nearby.")
                    .translation(translationKey("findSpawners"))
                    .define("findSpawners", this.defaultFindSpawners);
            this.findChests = cfgBuilder
                    .comment("If this is set to true, particles spawned by the lucerna cape effect will also highlight chests nearby.")
                    .translation(translationKey("findChests"))
                    .define("findChests", this.defaultFindChests);
        }
    }

}
