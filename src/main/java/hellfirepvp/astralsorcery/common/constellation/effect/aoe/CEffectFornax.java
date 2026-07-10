/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation.effect.aoe;

import hellfirepvp.astralsorcery.client.effect.function.VFXAlphaFunction;
import hellfirepvp.astralsorcery.client.effect.function.VFXColorFunction;
import hellfirepvp.astralsorcery.client.effect.handler.EffectHelper;
import hellfirepvp.astralsorcery.client.lib.EffectTemplatesAS;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.effect.ConstellationEffectProperties;
import hellfirepvp.astralsorcery.common.constellation.effect.base.CEffectAbstractList;
import hellfirepvp.astralsorcery.common.constellation.effect.base.ListEntries;
import hellfirepvp.astralsorcery.common.crafting.nojson.WorldFreezingRegistry;
import hellfirepvp.astralsorcery.common.crafting.nojson.WorldMeltableRegistry;
import hellfirepvp.astralsorcery.common.crafting.nojson.freezing.WorldFreezingRecipe;
import hellfirepvp.astralsorcery.common.crafting.nojson.meltable.WorldMeltableRecipe;
import hellfirepvp.astralsorcery.common.event.PlayerAffectionFlags;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ConstellationsAS;
import hellfirepvp.astralsorcery.common.tile.TileRitualPedestal;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.block.ILocatable;
import hellfirepvp.astralsorcery.common.util.block.iterator.BlockPositionGenerator;
import hellfirepvp.astralsorcery.common.util.block.iterator.BlockRandomPositionGenerator;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.item.ItemUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.ModConfigSpec;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.util.function.Consumer;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CEffectFornax
 * Created by HellFirePvP
 * Date: 29.11.2019 / 22:23
 */
public class CEffectFornax extends CEffectAbstractList<ListEntries.PosEntry> {

    public static PlayerAffectionFlags.AffectionFlag FLAG = makeAffectionFlag("fornax");
    public static FornaxConfig CONFIG = new FornaxConfig();

    public CEffectFornax(@Nonnull ILocatable origin) {
        super(origin, ConstellationsAS.fornax, 1, (level, pos, state) -> true);
        this.excludeRitualPositions();
        this.selectSphericalPositions();
    }

    @Nonnull
    @Override
    protected BlockPositionGenerator createPositionStrategy() {
        BlockPositionGenerator gen = new BlockRandomPositionGenerator();
        gen.andFilter(pos -> pos.getY() < 0);
        return gen;
    }

    @Nullable
    @Override
    public ListEntries.PosEntry recreateElement(CompoundTag tag, BlockPos pos) {
        return new ListEntries.PosEntry(pos);
    }

    @Nullable
    @Override
    public ListEntries.PosEntry createElement(Level level, BlockPos pos) {
        return new ListEntries.PosEntry(pos);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void playClientEffect(Level level, BlockPos pos, TileRitualPedestal pedestal, float alphaMultiplier, boolean extended) {
        Vector3 motion = Vector3.random().mul(0.04);
        if (pos.equals(pedestal.getBlockPos())) {
            motion.setY(0);
        } else {
            motion.setY(Math.abs(motion.getY()) * -1);
        }
        Color c = MiscUtils.eitherOf(random,
                () -> ColorsAS.CONSTELLATION_FORNAX.brighter(),
                () -> ColorsAS.CONSTELLATION_FORNAX.darker(),
                () -> ColorsAS.CONSTELLATION_FORNAX.darker(),
                () -> ColorsAS.CONSTELLATION_FORNAX.darker().darker());
        EffectHelper.of(EffectTemplatesAS.GENERIC_PARTICLE)
                .spawn(new Vector3(pos).add(0.5, 0.2, 0.5))
                .alpha1arg(VFXAlphaFunction.FADE_OUT)
                .color(VFXColorFunction.constant(c))
                .setScaleMultiplier(0.3F + random.nextFloat() * 0.4F)
                .setDeltaMovement(motion)
                .setGravityStrength(-0.0015F)
                .setMaxAge(60 + random.nextInt(30));
    }

    @Override
    public boolean playEffect(Level level, BlockPos pos, ConstellationEffectProperties properties, @Nullable IMinorConstellation trait) {
        if (!(level instanceof ServerLevel)) {
            return false;
        }

        Consumer<ItemStack> dropResult = stack -> ItemUtils.dropItemNaturally(level, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, stack);

        return this.peekNewPosition(level, pos, properties).mapLeft(newEntry -> {
            BlockPos at = newEntry.getBlockPos();

            if (properties.isCorrupted()) {
                WorldFreezingRecipe freezingRecipe = WorldFreezingRegistry.INSTANCE.getRecipeFor(level, at);
                if (freezingRecipe != null) {
                    freezingRecipe.doOutput(level, at, level.getBlockState(at), dropResult);
                    return true;
                }
                sendConstellationPing(level, new Vector3(at).add(0.5, 0.5, 0.5));
                return false;
            }

            WorldMeltableRecipe meltRecipe = WorldMeltableRegistry.INSTANCE.getRecipeFor(level, at);
            if (meltRecipe != null) {
                meltRecipe.doOutput(level, at, level.getBlockState(at), dropResult);
                return true;
            }
            sendConstellationPing(level, new Vector3(at).add(0.5, 0.5, 0.5));
            return false;
        }).left().orElse(false);
    }

    @Override
    public Config getConfig() {
        return CONFIG;
    }

    @Override
    public PlayerAffectionFlags.AffectionFlag getPlayerAffectionFlag() {
        return FLAG;
    }

    private static class FornaxConfig extends Config {

        private final float defaultMeltFailChance = 0F;

        public ModConfigSpec.DoubleValue meltFailChance;

        public FornaxConfig() {
            super("fornax", 8D, 2D);
        }

        @Override
        public void createEntries(ModConfigSpec.Builder cfgBuilder) {
            super.createEntries(cfgBuilder);

            this.meltFailChance = cfgBuilder
                    .comment("Defines the chance (0% to 100% -> 0.0 to 1.0) if the block will be replaced with air instead of being properly melted into something.")
                    .translation(translationKey("meltFailChance"))
                    .defineInRange("meltFailChance", this.defaultMeltFailChance, 0.0D, 1.0D);
        }
    }
}
