/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crystal;

import net.minecraft.network.chat.MutableComponent;

import net.minecraft.network.chat.Component;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.data.research.ResearchHelper;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.util.MiscUtils;
import hellfirepvp.astralsorcery.common.util.nbt.NBTHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import hellfirepvp.astralsorcery.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.ChatFormatting;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CrystalAttributes
 * Created by HellFirePvP
 * Date: 29.01.2019 / 21:30
 */
public final class CrystalAttributes {

    private static final Random random = new Random();

    private LinkedList<Attribute> crystalAttributes = Lists.newLinkedList();

    private CrystalAttributes() {}

    private CrystalAttributes(Map<CrystalProperty, Integer> crystalAttributes) {
        this.crystalAttributes = Lists.newLinkedList();
        for (Map.Entry<CrystalProperty, Integer> propertyEntry : crystalAttributes.entrySet()) {
            this.crystalAttributes.add(new Attribute(propertyEntry.getKey(), propertyEntry.getValue(), false));
        }
        this.crystalAttributes.sort(Comparator.comparing(Attribute::getProperty));
    }

    public List<Attribute> getCrystalAttributes() {
        return Collections.unmodifiableList(crystalAttributes);
    }

    public List<CrystalProperty> getPropertiesPerTier(boolean combineDuplicates) {
        List<CrystalProperty> properties = new ArrayList<>(!combineDuplicates ? getTotalTierLevel() : getCrystalAttributes().size());
        for (Attribute attribute : getCrystalAttributes()) {
            if (combineDuplicates) {
                properties.add(attribute.getProperty());
            } else {
                for (int i = 0; i < attribute.getTier(); i++) {
                    properties.add(attribute.getProperty());
                }
            }
        }
        return properties;
    }

    @Nullable
    public Attribute getAttribute(CrystalProperty property) {
        for (Attribute attr : this.crystalAttributes) {
            if (attr.getProperty().equals(property)) {
                return attr;
            }
        }
        return null;
    }

    public int getTotalTierLevel() {
        int tier = 0;
        for (Attribute attr : this.getCrystalAttributes()) {
            tier += attr.getTier();
        }
        return tier;
    }

    public CrystalAttributes discoverAll(PlayerProgress prog) {
        if (MiscUtils.contains(this.getCrystalAttributes(), attr -> attr.canNewDiscover(prog))) {
            CrystalAttributes thisCopy = this.copy();
            thisCopy.crystalAttributes.forEach(attribute -> attribute.discover(prog));
            return thisCopy;
        }
        return this;
    }

    public boolean hasUnknownAttributes() {
        return MiscUtils.contains(this.getCrystalAttributes(), attribute -> !attribute.isDiscovered());
    }

    public boolean isEmpty() {
        return this.getTotalTierLevel() <= 0;
    }

    public List<CrystalProperty> getProperties() {
        return this.getCrystalAttributes().stream()
                .map(Attribute::getProperty)
                .collect(Collectors.toList());
    }

    @Nonnull
    @OnlyIn(Dist.CLIENT)
    public TooltipResult addTooltip(List<Component> tooltip) {
        return addTooltip(tooltip, CalculationContext.Builder.properties().build());
    }

    @Nonnull
    @OnlyIn(Dist.CLIENT)
    public TooltipResult addTooltip(List<Component> tooltip, PlayerProgress progress) {
        return addTooltip(tooltip, progress, CalculationContext.Builder.properties().build());
    }

    @Nonnull
    @OnlyIn(Dist.CLIENT)
    public TooltipResult addTooltip(List<Component> tooltip, CalculationContext ctx) {
        return addTooltip(tooltip, ResearchHelper.getClientProgress(), ctx);
    }

    @Nonnull
    @OnlyIn(Dist.CLIENT)
    private TooltipResult addTooltip(List<Component> tooltip, PlayerProgress progress, CalculationContext ctx) {
        boolean missing = false;
        boolean addedAtLeastOne = false;

        for (Attribute attr : this.getCrystalAttributes()) {
            if (attr.getTier() > 0) {
                CrystalProperty prop = attr.getProperty();
                if (!prop.hasUsageFor(ctx) && !ctx.isEmpty()) {
                    //Don't add a line for it if it's there, but not used in context
                    continue;
                }

                if (!prop.canSee(progress) || !attr.isDiscovered()) {
                    missing = true;
                } else {
                    MutableComponent level = Component.translatable(String.format("enchantment.level.%s", attr.getTier()))
                            .withStyle(ChatFormatting.GOLD);
                    MutableComponent name = prop.getName(attr.getTier()).withStyle(ChatFormatting.GRAY);

                    tooltip.add(name
                            .append(Component.literal(" "))
                            .append(level));
                    addedAtLeastOne = true;
                }
            }
        }

        if (missing) {
            tooltip.add(Component.translatable("astralsorcery.progress.missing.knowledge").withStyle(ChatFormatting.GRAY));
        }
        return missing && !addedAtLeastOne ? TooltipResult.ALL_MISSING :
                missing ?  TooltipResult.ADDED_ALL_WITH_MISSING : TooltipResult.ADDED_ALL;
    }

    public CrystalAttributes combine(CrystalAttributes other, boolean ignoreTierMax) {
        return combine(other, ignoreTierMax, 1F);
    }

    public CrystalAttributes combine(CrystalAttributes other, boolean ignoreTierMax, float mergeChance) {
        List<Attribute> otherAttributes = other.getCrystalAttributes();
        if (otherAttributes.isEmpty()) {
            return this.copy();
        }
        Builder builder = Builder.properties(ignoreTierMax);
        builder.addAll(this.copy());
        for (Attribute otherAttr : otherAttributes) {
            for (int i = 0; i < otherAttr.getTier(); i++) {
                if (random.nextFloat() <= mergeChance) {
                    builder.addProperty(otherAttr.getProperty(), 1);
                }
            }
        }
        return builder.build();
    }

    public CrystalAttributes modifyLevel(CrystalProperty prop, int change) {
        return modifyLevel(prop, change, false);
    }

    public CrystalAttributes modifyLevel(CrystalProperty prop, int change, boolean ignoreTierMax) {
        Attribute existing = getAttribute(prop);
        if (existing != null && change != 0) {
            int newTier = Mth.clamp(existing.getTier() + change, 0,
                    ignoreTierMax ? Integer.MAX_VALUE : prop.getMaxTier());
            if (newTier <= 0) {
                return transform(Function.identity(), Lists.newArrayList(), Lists.newArrayList(prop));
            } else if (newTier != existing.getTier()) {
                return transform(attribute -> {
                    if (attribute.getProperty().equals(prop)) {
                        attribute.tier = newTier;
                    }
                    return attribute;
                });
            }
        } else if (change > 0) {
            return transform(Function.identity(),
                    Lists.newArrayList(new Attribute(prop, Mth.clamp(change, 0, prop.getMaxTier()))),
                    Lists.newArrayList());
        }
        return this;
    }

    private CrystalAttributes transform(Function<Attribute, Attribute> modify) {
        return transform(modify, Lists.newArrayList(), Lists.newArrayList());
    }

    private CrystalAttributes transform(Function<Attribute, Attribute> modify,
                                        Collection<Attribute> additions,
                                        Collection<CrystalProperty> removals) {
        List<Attribute> current = Lists.newArrayList();
        for (Attribute attr : this.getCrystalAttributes()) {
            current.add(new Attribute(attr.getProperty(), attr.getTier()));
        }
        List<Attribute> isDirty = current.stream().map(modify).collect(Collectors.toList());
        for (Attribute added : additions) {
            Attribute existing;
            if ((existing = MiscUtils.iterativeSearch(isDirty, tpl -> tpl.getProperty().equals(added.getProperty()))) != null) {
                existing.tier += added.getTier();
            } else {
                isDirty.add(new Attribute(added.getProperty(), added.getTier()));
            }
        }
        isDirty.removeIf(attr -> removals.contains(attr.getProperty()));
        return new CrystalAttributes(isDirty.stream().collect(Collectors.toMap(Attribute::getProperty, Attribute::getTier)));
    }

    public CrystalAttributes copy() {
        return deserialize(serialize());
    }

    public CrystalAttributes clampMaxTier() {
        CrystalAttributes attributes = this.copy();
        for (Attribute attr : attributes.crystalAttributes) {
            attr.tier = Mth.clamp(attr.getTier(), 0, attr.getProperty().getMaxTier());
        }
        return attributes;
    }

    public void store(ItemStack stack) {
        if (!stack.isEmpty()) {
            this.store(NBTHelper.getPersistentData(stack));
        }
    }

    public void store(CompoundTag baseTag) {
        baseTag.put("crystalProperties", this.serialize());
    }

    public static void storeNull(ItemStack stack) {
        if (!stack.isEmpty()) {
            storeNull(NBTHelper.getPersistentData(stack));
        }
    }

    public static void storeNull(CompoundTag baseTag) {
        baseTag.remove("crystalProperties");
    }


    @Nullable
    public static CrystalAttributes getCrystalAttributes(ItemStack stack) {
        return !stack.isEmpty() ? getCrystalAttributes(NBTHelper.getPersistentData(stack)) : null;
    }

    @Nullable
    public static CrystalAttributes getCrystalAttributes(CompoundTag baseTag) {
        if (!baseTag.contains("crystalProperties")) {
            return null;
        }
        CompoundTag tag = baseTag.getCompound("crystalProperties");
        if (tag.size() == 0) {
            return null; // At least has to have the list tag inside it.
        }
        return deserialize(tag);
    }

    public CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Attribute attr : crystalAttributes) {
            list.add(attr.serialize());
        }
        tag.put("attributes", list);
        return tag;
    }

    public static CrystalAttributes deserialize(CompoundTag tag) {
        CrystalAttributes attributes = new CrystalAttributes();
        ListTag list = tag.getList("attributes", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            Attribute attr = Attribute.deserialize(list.getCompound(i));
            if (attr != null) {
                attributes.crystalAttributes.add(attr);
            }
        }
        return attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CrystalAttributes that = (CrystalAttributes) o;
        return Objects.equals(crystalAttributes, that.crystalAttributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(crystalAttributes);
    }

    public static enum TooltipResult {

        ADDED_ALL,
        ADDED_ALL_WITH_MISSING,
        ALL_MISSING

    }

    public static class Builder {

        private Map<CrystalProperty, Integer> properties = Maps.newHashMap();
        private final boolean ignoreTierCap;

        private Builder(boolean ignoreTierCap) {
            this.ignoreTierCap = ignoreTierCap;
        }

        public static Builder properties(boolean ignoreTierCap) {
            return new Builder(ignoreTierCap);
        }

        public Builder addAll(CrystalAttributes other) {
            for (Attribute attr : other.getCrystalAttributes()) {
                CrystalProperty property = attr.getProperty();
                int cTier = this.properties.getOrDefault(property, 0);
                cTier = Mth.clamp(cTier + attr.getTier(),
                        0, this.ignoreTierCap ? Integer.MAX_VALUE : property.getMaxTier());
                this.properties.put(property, cTier);
            }
            return this;
        }

        public Builder addProperty(CrystalProperty property, int tier) {
            int cTier = this.properties.getOrDefault(property, 0);
            cTier = Mth.clamp(cTier + tier,
                    0, this.ignoreTierCap ? Integer.MAX_VALUE : property.getMaxTier());
            this.properties.remove(property);
            if (cTier > 0) {
                this.properties.put(property, cTier);
            }
            return this;
        }

        // Yes, i know this breaks the Builder design pattern...
        public int getPropertyLvl(CrystalProperty property, int defaultValue) {
            return this.properties.getOrDefault(property, defaultValue);
        }

        public List<CrystalProperty> getProperties() {
            return Lists.newArrayList(this.properties.keySet());
        }

        public CrystalAttributes buildAverage(int count) {
            Map<CrystalProperty, Integer> average = new HashMap<>();
            for (CrystalProperty prop : this.properties.keySet()) {
                int newLevel = Mth.ceil(this.properties.getOrDefault(prop, 0) / (float) count);
                if (newLevel > 0) {
                    average.put(prop, newLevel);
                }
            }
            return new CrystalAttributes(average);
        }

        public CrystalAttributes build() {
            return new CrystalAttributes(properties);
        }

    }

    public static class Attribute {

        private boolean discovered;
        private int tier;
        private CrystalProperty property;

        private Attribute(CrystalProperty property, int tier) {
            this(property, tier, false);
        }

        private Attribute(CrystalProperty property, int tier, boolean discovered) {
            this.tier = tier;
            this.property = property;
            this.discovered = true;
        }

        public CrystalProperty getProperty() {
            return property;
        }

        private boolean canNewDiscover(PlayerProgress prog) {
            return !this.isDiscovered() && this.property.canSee(prog);
        }

        private void discover(PlayerProgress prog) {
            if (!this.isDiscovered() && this.property.canSee(prog)) {
                this.discovered = true;
            }
        }

        public boolean isDiscovered() {
            return discovered;
        }

        public int getTier() {
            return this.tier;
        }

        private CompoundTag serialize() {
            CompoundTag tag = new CompoundTag();
            tag.putString("property", property.getRegistryName().toString());
            tag.putInt("pLevel", tier);
            tag.putBoolean("discovered", discovered);
            return tag;
        }

        @Nullable
        private static Attribute deserialize(CompoundTag tag) {
            ResourceLocation key = ResourceLocation.parse(tag.getString("property"));
            CrystalProperty prop = RegistriesAS.REGISTRY_CRYSTAL_PROPERTIES.getValue(key);
            if (prop == null) {
                return null;
            }
            int tier = tag.getInt("pLevel");
            boolean discovered = tag.getBoolean("discovered");
            return new Attribute(prop, tier, discovered);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Attribute attribute = (Attribute) o;
            return discovered == attribute.discovered &&
                    tier == attribute.tier &&
                    Objects.equals(property, attribute.property);
        }

        @Override
        public int hashCode() {
            return Objects.hash(discovered, tier, property);
        }
    }

}
