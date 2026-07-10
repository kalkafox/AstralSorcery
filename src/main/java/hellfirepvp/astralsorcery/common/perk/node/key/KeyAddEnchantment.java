/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.node.key;

import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.enchantment.dynamic.DynamicEnchantment;
import hellfirepvp.astralsorcery.common.enchantment.dynamic.DynamicEnchantmentType;
import hellfirepvp.astralsorcery.common.event.DynamicEnchantmentEvent;
import hellfirepvp.astralsorcery.common.perk.node.KeyPerk;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;

import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: KeyAddEnchantment
 * Created by HellFirePvP
 * Date: 25.08.2019 / 19:08
 */
public class KeyAddEnchantment extends KeyPerk {

    private final List<DynamicEnchantment> enchantments = Lists.newArrayList();

    public KeyAddEnchantment(ResourceLocation name, float x, float y) {
        super(name, x, y);
    }

    @Override
    public void attachListeners(LogicalSide direction, IEventBus bus) {
        super.attachListeners(direction, bus);
        if (direction.isServer()) {
            bus.addListener(this::onEnchantmentAddServer);
        } else {
            bus.addListener(this::onEnchantmentAddClient);
        }
    }

    public KeyAddEnchantment fillItemCategory(ResourceKey<Enchantment> ench, int level) {
        return fillItemCategory(DynamicEnchantmentType.ADD_TO_SPECIFIC, ench, level);
    }

    public KeyAddEnchantment fillItemCategory(DynamicEnchantmentType type, ResourceKey<Enchantment> ench, int level) {
        this.enchantments.add(new DynamicEnchantment(type, ench, level));
        return this;
    }

    public KeyAddEnchantment addAllEnchantmentIncrease(int level) {
        this.enchantments.add(new DynamicEnchantment(DynamicEnchantmentType.ADD_TO_EXISTING_ALL, level));
        return this;
    }
    private void onEnchantmentAddClient(DynamicEnchantmentEvent.Add event) {
        Player player = event.getResolvedPlayer();
        LogicalSide direction = this.getSide(player);
        if (direction.isClient()) {
            addEnchantments(player, direction, event);
        }
    }

    private void onEnchantmentAddServer(DynamicEnchantmentEvent.Add event) {
        Player player = event.getResolvedPlayer();
        LogicalSide direction = this.getSide(player);
        if (direction.isServer()) {
            addEnchantments(player, direction, event);
        }
    }

    private void addEnchantments(Player player, LogicalSide direction, DynamicEnchantmentEvent.Add event) {
        PlayerProgress prog = ResearchHelper.getProgress(player, direction);
        if (prog.getPerkData().hasPerkEffect(this)) {
            List<DynamicEnchantment> listedEnchantments = event.getEnchantmentsToApply();
            for (DynamicEnchantment ench : this.enchantments) {
                DynamicEnchantment added = MiscUtils.iterativeSearch(listedEnchantments, e ->
                        (e.getEnchantment() == null ? ench.getEnchantment() == null : e.getEnchantment().equals(ench.getEnchantment())) &&
                                e.getType().equals(ench.getType()));
                if (added != null) {
                    added.setLevelAddition(added.getLevelAddition() + ench.getLevelAddition());
                } else {
                    listedEnchantments.add(ench.copy());
                }
            }
        }
    }

    @Override
    public void deserializeData(JsonObject perkData) {
        super.deserializeData(perkData);

        this.enchantments.clear();

        if (perkData.has("enchantments")) {
            JsonArray array = GsonHelper.getAsJsonArray(perkData, "enchantments");
            for (int i = 0; i < array.size(); i++) {
                JsonObject serializedEnchantment = GsonHelper.convertToJsonObject(array.get(i), "enchantments[" + i + "]");

                String typeKey = GsonHelper.getAsString(serializedEnchantment, "type");
                DynamicEnchantmentType type;
                try {
                    type = DynamicEnchantmentType.valueOf(typeKey);
                } catch (Exception exc) {
                    throw new IllegalArgumentException("Unknown dynamic enchantment type: " + typeKey);
                }
                int level = GsonHelper.getAsInt(serializedEnchantment, "level");

                if (type.isEnchantmentSpecific()) {
                    String enchantmentKey = GsonHelper.getAsString(serializedEnchantment, "enchantment");
                    ResourceKey<Enchantment> ench = ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse(enchantmentKey));
                    this.fillItemCategory(type, ench, level);
                } else {
                    this.addAllEnchantmentIncrease(level);
                }
            }
        }
    }

    @Override
    public void serializeData(JsonObject perkData) {
        super.serializeData(perkData);

        if (!this.enchantments.isEmpty()) {
            JsonArray array = new JsonArray();

            for (DynamicEnchantment enchantment : this.enchantments) {
                JsonObject serializedEnchantment = new JsonObject();

                serializedEnchantment.addProperty("type", enchantment.getType().name());
                if (enchantment.getEnchantment() != null) {
                    serializedEnchantment.addProperty("enchantment", enchantment.getEnchantment().location().toString());
                }
                serializedEnchantment.addProperty("level", enchantment.getLevelAddition());

                array.add(serializedEnchantment);
            }

            perkData.add("enchantments", array);
        }
    }
}
