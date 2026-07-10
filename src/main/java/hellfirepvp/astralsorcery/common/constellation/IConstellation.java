/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.constellation;

import net.minecraft.network.chat.Component;

import hellfirepvp.astralsorcery.common.constellation.engraving.EngravingEffect;
import hellfirepvp.astralsorcery.common.constellation.star.StarConnection;
import hellfirepvp.astralsorcery.common.constellation.star.StarLocation;
import hellfirepvp.astralsorcery.common.data.research.PlayerProgress;
import hellfirepvp.astralsorcery.common.lib.ColorsAS;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.registry.internal.AstralRegistryEntry;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ItemLike;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.function.Supplier;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: IConstellation
 * Created by HellFirePvP
 * Date: 16.11.2016 / 23:04
 */
public interface IConstellation extends AstralRegistryEntry<IConstellation>, Comparable<IConstellation> {

    // 0-indexed
    public static final int STAR_GRID_INDEX = 31;
    // 1-indexed
    public static final int STAR_GRID_WIDTH_HEIGHT = (STAR_GRID_INDEX + 1);

    /**
     * Should only be called before registering the Constellation.
     */
    public StarLocation addStar(int x, int y);

    /**
     * Should only be called before registering the Constellation.
     */
    public StarConnection addConnection(StarLocation star1, StarLocation star2);

    public int getSortingId();

    public List<StarLocation> getStars();

    public List<StarConnection> getStarConnections();

    public String getName();

    public String getDescriptionId();

    default public MutableComponent getConstellationName() {
        return Component.translatable(this.getDescriptionId());
    }

    default public MutableComponent getConstellationTypeDescription() {
        String type = "unknown";
        if (this instanceof IMajorConstellation) {
            type = "major";
        } else if (this instanceof IWeakConstellation) {
            type = "weak";
        } else if (this instanceof IMinorConstellation) {
            type = "minor";
        }
        return Component.translatable(String.format("astralsorcery.journal.constellation.type.%s", type));
    }

    default public MutableComponent getConstellationTag() {
        return Component.translatable(this.getDescriptionId() + ".tag");
    }

    default public MutableComponent getConstellationDescription() {
        return Component.translatable(this.getDescriptionId() + ".description");
    }

    default public MutableComponent getConstellationEnchantmentDescription() {
        return Component.translatable(this.getDescriptionId() + ".enchantments");
    }

    public static String getDefaultSaveKey() {
        return "constellationName";
    }

    public List<Ingredient> getConstellationSignatureItems();

    @Nullable
    default public EngravingEffect getEngravingEffect() {
        return RegistriesAS.REGISTRY_ENGRAVING_EFFECT.getValue(this.getRegistryName());
    }

    default public IConstellation addSignatureItem(ItemStack item) {
        return this.addSignatureItem(() -> Ingredient.of(item));
    }

    default public IConstellation addSignatureItem(ItemLike item) {
        return this.addSignatureItem(() -> Ingredient.of(item));
    }

    default public IConstellation addSignatureItem(TagKey<Item> tag) {
        return this.addSignatureItem(() -> Ingredient.of(tag));
    }

    public IConstellation addSignatureItem(Supplier<Ingredient> ingredient);

    public Color getConstellationColor();

    default public Color getTierRenderColor() {
        if (this instanceof IMinorConstellation) {
            return ColorsAS.CONSTELLATION_TYPE_MINOR;
        }
        if (this instanceof IMajorConstellation) {
            return ColorsAS.CONSTELLATION_TYPE_MAJOR;
        }
        return ColorsAS.CONSTELLATION_TYPE_WEAK;
    }

    boolean canDiscover(Player player, PlayerProgress progress);

    default public void save(CompoundTag pattern) {
        save(pattern, getDefaultSaveKey());
    }

    default public void save(CompoundTag pattern, String key) {
        pattern.putString(key, getRegistryName().toString());
    }

    @Nullable
    public static IConstellation readFromNBT(CompoundTag pattern) {
        return readFromNBT(pattern, getDefaultSaveKey());
    }

    @Nullable
    public static IConstellation readFromNBT(CompoundTag pattern, String key) {
        return ConstellationRegistry.getConstellation(ResourceLocation.parse(pattern.getString(key)));
    }

}
