/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.item.lens;

import hellfirepvp.astralsorcery.AstralSorcery;
import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.data.config.entry.GeneralConfig;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.util.DamageUtil;
import hellfirepvp.astralsorcery.common.util.PartialEffectExecutor;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ItemColoredLensRegeneration
 * Created by HellFirePvP
 * Date: 21.09.2019 / 19:35
 */
public class ItemColoredLensRegeneration extends ItemColoredLens {

    private static final ColorTypeRegeneration COLOR_TYPE_REGENERATION = new ColorTypeRegeneration();

    public ItemColoredLensRegeneration() {
        super(COLOR_TYPE_REGENERATION);
    }

    private static class ColorTypeRegeneration extends LensColorType {

        private ColorTypeRegeneration() {
            super(AstralSorcery.key("regeneration"),
                    TargetType.ENTITY,
                    () -> new ItemStack(ItemsAS.COLORED_LENS_REGENERATION),
                    ColorsAS.COLORED_LENS_REGEN,
                    0.1F,
                    false);
        }

        @Override
        public void entityInBeam(Level world, Vector3 origin, Vector3 target, Entity entity, PartialEffectExecutor executor) {
            if (world.isRemote() || !(entity instanceof LivingEntity) || !entity.isAlive()) {
                return;
            }
            if (entity instanceof Player && !GeneralConfig.CONFIG.doColoredLensesAffectPlayers.get()) {
                return;
            }
            LivingEntity le = (LivingEntity) entity;
            executor.executeAll(() -> {
                if (random.nextInt(8) != 0) {
                    return;
                }
                if (le.isEntityUndead()) {
                    DamageUtil.shotgunAttack(le, e -> {
                        DamageUtil.attackEntityFrom(e, CommonProxy.DAMAGE_SOURCE_STELLAR, 0.5F);
                    });
                } else {
                    le.heal(0.5F);
                }
            });
        }

        @Override
        public void blockInBeam(Level world, BlockPos pos, BlockState state, PartialEffectExecutor executor) {}
    }
}
