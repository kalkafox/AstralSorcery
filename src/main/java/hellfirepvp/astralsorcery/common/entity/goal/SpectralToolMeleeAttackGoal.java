/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.entity.goal;

import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.constellation.mantle.effect.MantleEffectPelotrio;
import hellfirepvp.astralsorcery.common.entity.EntitySpectralTool;
import hellfirepvp.astralsorcery.common.util.DamageUtil;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import hellfirepvp.astralsorcery.common.util.entity.EntityUtils;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: SpectralToolMeleeAttackGoal
 * Created by HellFirePvP
 * Date: 22.02.2020 / 16:58
 */
public class SpectralToolMeleeAttackGoal extends SpectralToolGoal {

    private LivingEntity selectedTarget = null;

    public SpectralToolMeleeAttackGoal(EntitySpectralTool entity, double speedModifier) {
        super(entity, speedModifier);
    }

    private LivingEntity findClosestAttackableEntity() {
        List<LivingEntity> entities = this.getEntity().getCommandSenderWorld().getEntitiesWithinAABB(
                LivingEntity.class,
                new AABB(0, 0, 0, 0, 0, 0).grow(8).offset(this.getEntity().position()),
                e -> e != null && e.isAlive() && e.getType().getCategory() == MobCategory.MONSTER
        );
        return EntityUtils.selectClosest(entities, entity -> (double) entity.getDistance(this.getEntity()));
    }


    @Override
    public boolean canUse() {
        MoveControl ctrl = this.getEntity().getMoveControl();

        if (!ctrl.hasWanted()) {
            return true;
        } else {
            return this.findClosestAttackableEntity() != null;
        }
    }

    public boolean canContinueToUse() {
        return selectedTarget != null;
    }

    @Override
    public void start() {
        super.start();

        LivingEntity target = this.findClosestAttackableEntity();
        if (target != null) {
            this.selectedTarget = target;
            this.getEntity().getMoveControl().setWantedPosition(selectedTarget.getX(), selectedTarget.getY() + selectedTarget.getBbHeight() / 2, selectedTarget.getZ(), this.getSpeedModifier());
        }
    }

    @Override
    public void stop() {
        super.stop();

        this.selectedTarget = null;
        this.actionCooldown = 0;
    }

    @Override
    public void tick() {
        super.tick();

        if (!canContinueToUse()) {
            return;
        }

        if (this.actionCooldown < 0) {
            this.actionCooldown = 0; //lol. wtf.
        }

        boolean resetTimer = false;

        if (!this.selectedTarget.isAlive()) {
            this.selectedTarget = null;
            resetTimer = true;
        } else {
            this.getEntity().getMoveControl().setWantedPosition(selectedTarget.getX(), selectedTarget.getY() + selectedTarget.getBbHeight() / 2, selectedTarget.getZ(), this.getSpeedModifier());

            if (Vector3.atEntityCorner(this.getEntity()).distanceSquared(this.selectedTarget) <= 16) {
                this.actionCooldown++;
                if (this.actionCooldown >= MantleEffectPelotrio.CONFIG.ticksPerSwordAttack.get()) {
                    DamageUtil.hurt(this.selectedTarget, CommonProxy.DAMAGE_SOURCE_STELLAR, MantleEffectPelotrio.CONFIG.swordDamage.get().floatValue());
                    resetTimer = true;
                }
            }
        }

        if (resetTimer) {
            this.actionCooldown = 0;
        }
    }
}
