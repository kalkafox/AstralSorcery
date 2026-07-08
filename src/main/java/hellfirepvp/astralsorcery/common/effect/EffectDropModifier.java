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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.GameRules;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EffectDropModifier
 * Created by HellFirePvP
 * Date: 26.08.2019 / 20:08
 */
public class EffectDropModifier extends EffectCustomTexture {

    public EffectDropModifier() {
        super(EffectType.BENEFICIAL, ColorsAS.EFFECT_DROP_MODIFIER);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return new ArrayList<>(0);
    }

    @Override
    public void attachEventListeners(IEventBus bus) {
        super.attachEventListeners(bus);
        bus.addListener(EventPriority.HIGH, this::onDrops);
    }

    private void onDrops(LivingDropsEvent event) {
        LivingEntity le = event.getEntityLiving();
        if (le.getEntityWorld().isRemote() ||
                !(le instanceof Mob) ||
                !(le.getEntityWorld() instanceof ServerLevel) ||
                !le.getEntityWorld().getGameRules().getBoolean(GameRules.DO_MOB_LOOT)) {
            return;
        }

        if (le.isPotionActive(EffectsAS.EFFECT_DROP_MODIFIER)) {
            DamageSource src = event.getSource();

            int amplifier = le.removeActivePotionEffect(EffectsAS.EFFECT_DROP_MODIFIER).getAmplifier();
            if (amplifier == 0) {
                event.getDrops().clear(); //Special case to void all items
            } else {
                for (int i = 0; i < amplifier; i++) {
                    List<ItemStack> loot = EntityUtils.generateLoot(le, rand, src, event.isRecentlyHit() ? le.getAttackingEntity() : null);
                    for (ItemStack stack : loot) {
                        if (stack.isEmpty()) {
                            continue;
                        }
                        event.getDrops().add(le.entityDropItem(stack));
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
