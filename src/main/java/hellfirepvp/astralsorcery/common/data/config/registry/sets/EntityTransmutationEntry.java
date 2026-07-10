/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.data.config.registry.sets;

import hellfirepvp.astralsorcery.common.data.config.base.ConfigDataSet;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: EntityTransmutationEntry
 * Created by HellFirePvP
 * Date: 01.02.2020 / 13:55
 */
public class EntityTransmutationEntry implements ConfigDataSet {

    private final EntityType<?> fromEntity;
    private final EntityType<?> toEntity;

    public EntityTransmutationEntry(EntityType<?> fromEntity, EntityType<?> toEntity) {
        this.fromEntity = fromEntity;
        this.toEntity = toEntity;
    }

    public EntityType<?> getFromEntity() {
        return fromEntity;
    }

    public EntityType<?> getToEntity() {
        return toEntity;
    }

    @Nonnull
    @Override
    public String serialize() {
        return String.format("%s;%s", RegistryHelper.getKey(fromEntity).toString(), RegistryHelper.getKey(toEntity).toString());
    }

    @Nullable
    public static EntityTransmutationEntry deserialize(String str) throws IllegalArgumentException {
        String[] split = str.split(";");
        if (split.length != 2) {
            return null;
        }
        ResourceLocation fromKey = ResourceLocation.parse(split[0]);
        EntityType<?> fromType = BuiltInRegistries.ENTITY_TYPE.get(fromKey);
        if (fromType == null) {
            throw new IllegalArgumentException(split[0] + " is not a known EntityType.");
        }
        ResourceLocation toKey = ResourceLocation.parse(split[1]);
        EntityType<?> toType = BuiltInRegistries.ENTITY_TYPE.get(toKey);
        if (toType == null) {
            throw new IllegalArgumentException(split[0] + " is not a known EntityType.");
        }
        if (!toType.canSummon() || toType.getCategory() == MobCategory.MISC) {
            throw new IllegalArgumentException("EntityType " + split[1] + " seems to be not summonable or isn't classified as creature.");
        }
        return new EntityTransmutationEntry(fromType, toType);
    }
}
