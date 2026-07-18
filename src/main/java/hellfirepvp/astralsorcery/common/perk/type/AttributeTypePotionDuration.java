/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.type;

import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.event.AttributeEvent;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.PerkAttributeHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.util.Mth;
import hellfirepvp.astralsorcery.common.util.reflection.ReflectionHelper;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AttributeTypePotionDuration
 * Created by HellFirePvP
 * Date: 25.08.2019 / 00:36
 */
public class AttributeTypePotionDuration extends PerkAttributeType {

    public AttributeTypePotionDuration() {
        super(PerkAttributeTypesAS.KEY_ATTR_TYPE_POTION_DURATION, true);
    }

    @Override
    protected void attachListeners(IEventBus eventBus) {
        super.attachListeners(eventBus);
        eventBus.addListener(this::onEffect);
    }

    private void onEffect(MobEffectEvent.Added event) {
        if (event.getEntity() instanceof Player) {
            if (event.getOldEffectInstance() == null) {
                //New effect
                modifyPotionDuration((Player) event.getEntity(), event.getEffectInstance(), event.getEffectInstance());
            } else {
                //Existing effect
                if (new MobEffectInstance(event.getOldEffectInstance()).update(event.getEffectInstance())) {
                    modifyPotionDuration((Player) event.getEntity(), event.getEffectInstance(), event.getOldEffectInstance());
                }
            }
        }
    }

    private void modifyPotionDuration(Player player, MobEffectInstance newSetEffect, MobEffectInstance existingEffect) {
        if (player.getCommandSenderWorld().isClientSide() ||
                newSetEffect.getEffect().value().getCategory().equals(MobEffectCategory.HARMFUL) ||
                existingEffect.getAmplifier() < newSetEffect.getAmplifier()) {
            return;
        }

        float newDuration = existingEffect.getDuration();
        newDuration = PerkAttributeHelper.getOrCreateMap(player, LogicalSide.SERVER)
                .modifyValue(player, ResearchHelper.getProgress(player, LogicalSide.SERVER), this, newDuration);
        newDuration = AttributeEvent.postProcessModded(player, this, newDuration);

        if (newSetEffect.getDuration() < newDuration) {
            ReflectionHelper.setMobEffectDuration(newSetEffect, Mth.floor(newDuration));
        }
    }

}
