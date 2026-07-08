/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.event;

import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeType;
import hellfirepvp.astralsorcery.common.perk.type.PerkAttributeTypeHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.Event;

import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: AttributeEvent
 * Created by HellFirePvP
 * Date: 08.08.2019 / 06:57
 */
public class AttributeEvent {

    public static class PostProcessVanilla extends Event {

        private final AttributeInstance instance;
        private final double originalValue;
        private double value;

        public PostProcessVanilla(AttributeInstance instance, double value) {
            this.instance = instance;
            this.originalValue = value;
            this.value = value;
        }

        public double getOriginalValue() {
            return originalValue;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public AttributeInstance getInstance() {
            return instance;
        }

        public Attribute getAttribute() {
            return instance.getAttribute();
        }

        @Nullable
        public PerkAttributeType resolveAttributeType() {
            return PerkAttributeTypeHelper.findVanillaType(getAttribute());
        }
    }

    public static class PostProcessModded extends Event {

        private final Player player;
        private final PerkAttributeType type;
        private final double originalValue;
        private double value;

        public PostProcessModded(double value, PerkAttributeType type, Player player) {
            this.player = player;
            this.type = type;
            this.originalValue = value;
            this.value = value;
        }

        public double getOriginalValue() {
            return originalValue;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }

        public PerkAttributeType getType() {
            return type;
        }

        public Player getPlayer() {
            return player;
        }
    }

    public static double postProcessModded(Player player, PerkAttributeType type, double value) {
        PostProcessModded ev = new PostProcessModded(value, type, player);
        NeoForge.EVENT_BUS.post(ev);
        return ev.getValue();
    }

    public static float postProcessModded(Player player, PerkAttributeType type, float value) {
        return (float) postProcessModded(player, type, (double) value);
    }

    public static double postProcessModded(Player player, ResourceLocation key, double value) {
        PerkAttributeType pType = RegistriesAS.REGISTRY_PERK_ATTRIBUTE_TYPES.getValue(key);
        if (pType == null) {
            return value;
        }
        return postProcessModded(player, pType, value);
    }

    public static float postProcessModded(Player player, ResourceLocation key, float value) {
        return (float) postProcessModded(player, key, (double) value);
    }

    public static double postProcessVanilla(double value, AttributeInstance attribute) {
        AttributeEvent.PostProcessVanilla event = new AttributeEvent.PostProcessVanilla(attribute, value);
        NeoForge.EVENT_BUS.post(event);
        return event.getAttribute().clampValue(event.getValue());
    }

    @Nullable
    private static LivingEntity getEntity(AttributeMap map) {
        if (map instanceof EntityModifierManager) {
            return ((EntityModifierManager) map).getLivingEntity();
        }
        return null;
    }

    public static void setEntity(AttributeMap map, LivingEntity entity) {
        if (map instanceof EntityModifierManager) {
            ((EntityModifierManager) map).setLivingEntity(entity);
        }
    }

    public static interface EntityModifierManager {

        @Nullable
        LivingEntity getLivingEntity();

        void setLivingEntity(LivingEntity entity);

    }
}
