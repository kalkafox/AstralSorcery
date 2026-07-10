/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.perk.type;

import hellfirepvp.astralsorcery.common.registry.internal.AbstractAstralRegistryEntry;
import com.google.common.collect.Maps;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.perk.modifier.PerkAttributeModifier;
import hellfirepvp.astralsorcery.common.perk.reader.PerkAttributeReader;
import hellfirepvp.astralsorcery.common.perk.source.ModifierSource;
import hellfirepvp.astralsorcery.common.util.ReadWriteLockable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PerkAttributeType
 * Created by HellFirePvP
 * Date: 08.08.2019 / 16:56
 */
public class PerkAttributeType extends AbstractAstralRegistryEntry<PerkAttributeType> implements ReadWriteLockable {

    protected static final Random random = new Random();

    //May be used by subclasses to more efficiently track who's got a perk applied
    private final Map<LogicalSide, Set<UUID>> applicationCache = Maps.newHashMap();
    private final ReadWriteLock accessLock = new ReentrantReadWriteLock(true);

    private final boolean isOnlyMultiplicative;

    protected PerkAttributeType(ResourceLocation key) {
        this(key, false);
    }

    protected PerkAttributeType(ResourceLocation key, boolean isMultiplicative) {
        this.setRegistryName(key);
        this.isOnlyMultiplicative = isMultiplicative;

        this.init();
        this.attachListeners(NeoForge.EVENT_BUS);
    }

    public static PerkAttributeType makeDefault(ResourceLocation name, boolean isMultiplicative) {
        return new PerkAttributeType(name, isMultiplicative);
    }

    public boolean isMultiplicative() {
        return isOnlyMultiplicative;
    }

    public Component getName() {
        return Component.translatable(this.getUnlocalizedName());
    }

    public String getUnlocalizedName() {
        return String.format("perk.attribute.%s.%s.name",
                this.getRegistryName().getNamespace(), this.getRegistryName().getPath());
    }

    protected void init() {}

    protected void attachListeners(IEventBus eventBus) {}

    protected LogicalSide getSide(Entity entity) {
        return entity.getCommandSenderWorld().isClientSide() ? LogicalSide.CLIENT : LogicalSide.SERVER;
    }

    @Nullable
    public PerkAttributeReader getReader() {
        return RegistriesAS.REGISTRY_PERK_ATTRIBUTE_READERS.getValue(this.getRegistryName());
    }

    @Nonnull
    public PerkAttributeModifier createModifier(float modifier, ModifierType mode) {
        if (isMultiplicative() && mode == ModifierType.ADDITION) {
            throw new IllegalArgumentException("Tried creating addition-modifier for a multiplicative-only modifier!");
        }
        return new PerkAttributeModifier(this, mode, modifier);
    }

    public void onApply(Player player, LogicalSide direction, ModifierSource source) {
        this.write(() -> {
            applicationCache.computeIfAbsent(direction, s -> new HashSet<>()).add(player.getUUID());
        });
    }

    public void onRemove(Player player, LogicalSide direction, boolean removedCompletely, ModifierSource source) {
        if (removedCompletely) {
            this.write(() -> {
                applicationCache.getOrDefault(direction, Collections.emptySet()).remove(player.getUUID());
            });
        }
    }

    //Called if no modifiers of this type were applied on the player, but now there is at least 1 added.
    //Called before any modifiers are actually applied!
    public void onModeApply(Player player, ModifierType mode, LogicalSide direction) {}

    //Called if no more modifiers of this type are applied on the player.
    //Called after that last modifier is removed!
    public void onModeRemove(Player player, ModifierType mode, LogicalSide direction, boolean removedCompletely) {}

    public boolean hasTypeApplied(Player player, LogicalSide direction) {
        return this.read(() -> applicationCache.getOrDefault(direction, Collections.emptySet()).contains(player.getUUID()));
    }

    private void clear(LogicalSide direction) {
        this.write(() -> {
            this.applicationCache.remove(direction);
        });
    }

    public static void clearCache(LogicalSide direction) {
        for (PerkAttributeType type : RegistriesAS.REGISTRY_PERK_ATTRIBUTE_TYPES) {
            type.clear(direction);
        }
    }

    @Override
    public ReadWriteLock getLock() {
        return this.accessLock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PerkAttributeType that = (PerkAttributeType) o;
        return Objects.equals(this.getRegistryName(), that.getRegistryName());
    }

    @Override
    public int hashCode() {
        return this.getRegistryName().hashCode();
    }
}
