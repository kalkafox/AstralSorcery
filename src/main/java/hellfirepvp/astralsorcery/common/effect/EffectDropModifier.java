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
import hellfirepvp.astralsorcery.common.util.entity.EntityUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.GameRules;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;

import net.neoforged.neoforge.common.EffectCure;

import java.util.List;
import java.util.Set;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EffectDropModifier
 * Created by HellFirePvP
 * Date: 26.08.2019 / 20:08
 */
public class EffectDropModifier extends EffectCustomTexture {

    public EffectDropModifier() {
        super(MobEffectCategory.BENEFICIAL, ColorsAS.EFFECT_DROP_MODIFIER);
    }

    @Override
    public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
        //Not curable
    }

    @Override
    public void attachEventListeners(IEventBus bus) {
        super.attachEventListeners(bus);
        bus.addListener(EventPriority.HIGH, this::onDrops);
    }

    private void onDrops(LivingDropsEvent event) {
        LivingEntity le = event.getEntity();
        if (le.getCommandSenderWorld().isClientSide() ||
                !(le instanceof Mob) ||
                !(le.getCommandSenderWorld() instanceof ServerLevel) ||
                !le.getCommandSenderWorld().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            return;
        }

        Holder<MobEffect> dropModifier = BuiltInRegistries.MOB_EFFECT.wrapAsHolder(EffectsAS.EFFECT_DROP_MODIFIER);
        if (le.hasEffect(dropModifier)) {
            DamageSource src = event.getSource();

            int amplifier = le.getEffect(dropModifier).getAmplifier();
            le.removeEffect(dropModifier);
            if (amplifier == 0) {
                event.getDrops().clear(); //Special case to void all items
            } else {
                for (int i = 0; i < amplifier; i++) {
                    List<ItemStack> loot = EntityUtils.generateLoot(le, random, src, event.isRecentlyHit() ? le.getKillCredit() : null);
                    for (ItemStack stack : loot) {
                        if (stack.isEmpty()) {
                            continue;
                        }
                        ItemEntity dropped = le.spawnAtLocation(stack);
                        if (dropped != null) {
                            event.getDrops().add(dropped);
                        }
                    }
                }
            }
        }
    }

    @Override
    public SpriteQuery getSpriteQuery() {
        return new SpriteQuery(AssetLoader.TextureLocation.GUI, 1, 1, "effect", "drop_modifier");
    }
}
