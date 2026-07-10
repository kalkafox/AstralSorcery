/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.type;

import hellfirepvp.astralsorcery.common.CommonProxy;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.event.AttributeEvent;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.PerkAttributeHelper;
import hellfirepvp.astralsorcery.common.perk.modifier.AttributeModifierThorns;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.util.DamageUtil;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nonnull;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AttributeTypeThorns
 * Created by HellFirePvP
 * Date: 25.08.2019 / 00:43
 */
public class AttributeTypeThorns extends PerkAttributeType {

    public AttributeTypeThorns() {
        super(PerkAttributeTypesAS.KEY_ATTR_TYPE_INC_THORNS);
    }

    @Override
    protected void attachListeners(IEventBus eventBus) {
        super.attachListeners(eventBus);
        eventBus.addListener(this::onThronsReflect);
    }

    @Nonnull
    @Override
    public PerkAttributeModifier createModifier(float modifier, ModifierType mode) {
        return new AttributeModifierThorns(this, mode, modifier);
    }

    private void onThronsReflect(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getEntity();
        LogicalSide direction = this.getSide(player);
        if (!hasTypeApplied(player, direction)) {
            return;
        }

        PlayerProgress prog = ResearchHelper.getProgress(player, direction);

        float reflectAmount = PerkAttributeHelper.getOrCreateMap(player, direction)
                .modifyValue(player, prog, this, 0F);
        reflectAmount = AttributeEvent.postProcessModded(player, this, reflectAmount);
        reflectAmount /= 100.0F;
        if (reflectAmount <= 0) {
            return;
        }
        reflectAmount = Mth.clamp(reflectAmount, 0F, 1F);

        DamageSource source = event.getSource();
        LivingEntity reflectTarget = null;
        if (source.getDirectEntity() != null &&
                source.getDirectEntity() instanceof LivingEntity &&
                source.getDirectEntity().isAlive()) {
            reflectTarget = (LivingEntity) source.getDirectEntity();
        }

        if (reflectTarget == null &&
                AttributeEvent.postProcessModded(player, this,
                        PerkAttributeHelper.getOrCreateMap(player, direction)
                                .getAttributeInstance(player, prog, PerkAttributeTypesAS.ATTR_TYPE_INC_THORNS_RANGED)) > 1) {
            if (source.getEntity() != null &&
                    source.getEntity() instanceof LivingEntity &&
                    source.getEntity().isAlive()) {
                reflectTarget = (LivingEntity) source.getEntity();
            }
        }

        if (reflectTarget != null) {
            float dmgReflected = event.getAmount() * reflectAmount;
            if (dmgReflected > 0 && !event.getEntity().equals(reflectTarget)) {
                if (MiscUtils.canPlayerAttackServer(event.getEntity(), reflectTarget)) {
                    DamageUtil.hurt(reflectTarget, CommonProxy.DAMAGE_SOURCE_REFLECT, dmgReflected, player);
                }
            }
        }
    }
}
