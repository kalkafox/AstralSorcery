/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.node.key;

import hellfirepvp.astralsorcery.common.data.config.base.ConfigEntry;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.lib.PerkAttributeTypesAS;
import hellfirepvp.astralsorcery.common.perk.PerkAttributeHelper;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;
import net.neoforged.neoforge.event.entity.living.LivingHurtEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: KeyLastBreath
 * Created by HellFirePvP
 * Date: 31.08.2019 / 18:54
 */
public class KeyLastBreath extends KeyPerk {

    private static final float defaultDigSpeedMultiplier = 1.5F;
    private static final float defaultDamageMultiplier = 3F;

    public static final Config CONFIG = new Config("key.last_breath");

    public KeyLastBreath(ResourceLocation name, float x, float y) {
        super(name, x, y);
    }

    @Override
    public void attachListeners(LogicalSide side, IEventBus bus) {
        super.attachListeners(side, bus);

        bus.addListener(this::onAttack);
        bus.addListener(this::onBreakSpeed);
    }

    private void onAttack(LivingHurtEvent event) {
        DamageSource source = event.getSource();
        if (source.getTrueSource() != null && source.getTrueSource() instanceof Player) {
            Player player = (Player) source.getTrueSource();
            LogicalSide side = this.getSide(player);
            PlayerProgress prog = ResearchHelper.getProgress(player, side);
            if (prog.getPerkData().hasPerkEffect(this)) {
                float actIncrease = PerkAttributeHelper.getOrCreateMap(player, side)
                        .modifyValue(player, prog, PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EFFECT, CONFIG.damageMultiplier.get().floatValue());
                float healthPerc = 1F - (player.getHealth() / player.getMaxHealth());
                event.setAmount(event.getAmount() * (1F + (healthPerc * actIncrease)));
            }
        }
    }

    private void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getPlayer();
        LogicalSide side = this.getSide(player);
        PlayerProgress prog = ResearchHelper.getProgress(player, side);
        if (prog.getPerkData().hasPerkEffect(this)) {
            float actIncrease = PerkAttributeHelper.getOrCreateMap(player, side)
                    .modifyValue(player, prog, PerkAttributeTypesAS.ATTR_TYPE_INC_PERK_EFFECT, CONFIG.digSpeedMultiplier.get().floatValue());
            float healthPerc = 1F - (player.getHealth() / player.getMaxHealth());
            event.setNewSpeed(event.getNewSpeed() * (1F + (healthPerc * actIncrease)));
        }
    }

    public static class Config extends ConfigEntry {

        private ModConfigSpec.DoubleValue digSpeedMultiplier;
        private ModConfigSpec.DoubleValue damageMultiplier;

        private Config(String section) {
            super(section);
        }

        @Override
        public void createEntries(ModConfigSpec.Builder cfgBuilder) {
            this.digSpeedMultiplier = cfgBuilder
                    .comment("Defines the dig speed multiplier you get additionally to your normal dig speed when being low on health (25% health = 75% of this additional multiplier)")
                    .translation(translationKey("digSpeedMultiplier"))
                    .defineInRange("digSpeedMultiplier", defaultDigSpeedMultiplier, 0.1, 10);

            this.damageMultiplier = cfgBuilder
                    .comment("Defines the damage multiplier you get additionally to your normal damage when being low on health (25% health = 75% of this additional multiplier)")
                    .translation(translationKey("damageMultiplier"))
                    .defineInRange("damageMultiplier", defaultDamageMultiplier, 0.1, 10);
        }
    }
}
