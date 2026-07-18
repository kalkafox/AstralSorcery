/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.effect;

import hellfirepvp.astralsorcery.client.resource.AssetLoader;
import hellfirepvp.astralsorcery.client.resource.query.SpriteQuery;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.EffectsAS;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;

import net.neoforged.neoforge.common.EffectCure;

import java.util.List;
import java.util.Set;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EffectCheatDeath
 * Created by HellFirePvP
 * Date: 26.08.2019 / 20:06
 */
public class EffectCheatDeath extends EffectCustomTexture {

    public EffectCheatDeath() {
        super(MobEffectCategory.BENEFICIAL, ColorsAS.EFFECT_CHEAT_DEATH);
    }

    @Override
    public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
        //Not curable
    }

    @Override
    public void attachEventListeners(IEventBus bus) {
        super.attachEventListeners(bus);
        bus.addListener(EventPriority.LOW, this::onDeath);
    }

    private void onDeath(LivingDeathEvent event) {
        LivingEntity le = event.getEntity();
        Holder<MobEffect> cheatDeath = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(EffectsAS.EFFECT_CHEAT_DEATH);
        if (!le.getCommandSenderWorld().isClientSide() && le.hasEffect(cheatDeath)) {
            event.setCanceled(true);

            int level = le.getEffect(cheatDeath).getAmplifier();
            le.removeEffect(cheatDeath);
            le.setHealth(Math.min(le.getMaxHealth(), 4 + level * 2));
            le.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 2, false, false, true));
            le.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 500, 1, false, false, true));
            List<LivingEntity> others = le.getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class,
                    le.getBoundingBox().inflate(3), (e) -> e.isAlive() && e != le);
            for (LivingEntity lb : others) {
                lb.igniteForSeconds(10);
                lb.knockback(2F, lb.getX() - le.getX(), lb.getZ() - le.getZ());
            }
            //TODO particles
            //PktParticleEvent ev = new PktParticleEvent(PktParticleEvent.ParticleEventType.PHOENIX_PROC, new Vector3(le.getPosX(), le.getPosY(), le.getPosZ()));
            //PacketChannel.CHANNEL.sendToAllAround(ev, PacketChannel.pointFromPos(le.world, le.getPosition(), 32));
        }
    }

    @Override
    public SpriteQuery getSpriteQuery() {
        return new SpriteQuery(AssetLoader.TextureLocation.GUI, 1, 1, "effect", "cheat_death");
    }
}
