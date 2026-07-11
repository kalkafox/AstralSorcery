/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.recipe.altar.builtin;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import hellfirepvp.astralsorcery.common.block.tile.altar.AltarType;
import hellfirepvp.astralsorcery.common.constellation.ConstellationItem;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMinorConstellation;
import hellfirepvp.astralsorcery.common.constellation.IWeakConstellation;
import hellfirepvp.astralsorcery.common.crafting.recipe.SimpleAltarRecipe;
import hellfirepvp.astralsorcery.common.crafting.recipe.altar.AltarRecipeGrid;
import hellfirepvp.astralsorcery.common.lib.RegistriesAS;
import hellfirepvp.astralsorcery.common.tile.altar.TileAltar;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ConstellationItemRecipe
 * Created by HellFirePvP
 * Date: 28.09.2019 / 06:48
 */
public class ConstellationItemRecipe extends SimpleAltarRecipe {

    private static final String KEY_CONSTELLATION_ATTUNE = "attuned_constellation";
    private static final String KEY_CONSTELLATION_TRAIT = "trait_constellation";

    private IWeakConstellation attunedConstellation = null;
    private IMinorConstellation traitConstellation = null;

    public ConstellationItemRecipe(ResourceLocation recipeId, AltarType altarType, int duration, int starlightRequirement, AltarRecipeGrid recipeGrid) {
        super(recipeId, altarType, duration, starlightRequirement, recipeGrid);
    }

    public static ConstellationItemRecipe convertToThis(SimpleAltarRecipe other) {
        return new ConstellationItemRecipe(other.getId(), other.getAltarType(), other.getDuration(), other.getStarlightRequirement(), other.getInputs());
    }

    @Override
    public void deserializeAdditionalJson(JsonObject recipeObject) throws JsonSyntaxException {
        super.deserializeAdditionalJson(recipeObject);

        if (recipeObject.has(KEY_CONSTELLATION_ATTUNE)) {
            ResourceLocation cstName = ResourceLocation.parse(GsonHelper.getAsString(recipeObject, KEY_CONSTELLATION_ATTUNE));
            IConstellation cst = RegistriesAS.REGISTRY_CONSTELLATIONS.getValue(cstName);
            if (cst instanceof IWeakConstellation) {
                this.attunedConstellation = (IWeakConstellation) cst;
            }
        }
        if (recipeObject.has(KEY_CONSTELLATION_TRAIT)) {
            ResourceLocation cstName = ResourceLocation.parse(GsonHelper.getAsString(recipeObject, KEY_CONSTELLATION_TRAIT));
            IConstellation cst = RegistriesAS.REGISTRY_CONSTELLATIONS.getValue(cstName);
            if (cst instanceof IMinorConstellation) {
                this.setTraitConstellation((IMinorConstellation) cst);
                this.traitConstellation = (IMinorConstellation) cst;
            }
        }
    }

    @Override
    public void serializeAdditionalJson(JsonObject recipeObject) {
        super.serializeAdditionalJson(recipeObject);

        if (this.getAttunedConstellation() != null) {
            recipeObject.addProperty(KEY_CONSTELLATION_ATTUNE, this.getAttunedConstellation().getRegistryName().toString());
        }
        if (this.getTraitConstellation() != null) {
            recipeObject.addProperty(KEY_CONSTELLATION_TRAIT, this.getTraitConstellation().getRegistryName().toString());
        }
    }

    public void setAttunedConstellation(IWeakConstellation attunedConstellation) {
        this.attunedConstellation = attunedConstellation;
    }

    public void setTraitConstellation(IMinorConstellation traitConstellation) {
        this.traitConstellation = traitConstellation;
    }

    public IWeakConstellation getAttunedConstellation() {
        return attunedConstellation;
    }

    public IMinorConstellation getTraitConstellation() {
        return traitConstellation;
    }

    @Override
    @Nonnull
    @OnlyIn(Dist.CLIENT)
    public ItemStack getOutputForRender(Iterable<ItemStack> items) {
        ItemStack out = super.getOutputForRender(items);
        setConstellations(out);
        return out;
    }

    @Override
    @Nonnull
    public List<ItemStack> getOutputs(TileAltar altar) {
        List<ItemStack> out = super.getOutputs(altar);
        out.forEach(this::setConstellations);
        return out;
    }

    private void setConstellations(ItemStack out) {
        if (out.getItem() instanceof ConstellationItem) {
            if (this.getAttunedConstellation() != null) {
                ((ConstellationItem) out.getItem()).setAttunedConstellation(out, this.getAttunedConstellation());
            }
            if (this.getTraitConstellation() != null) {
                ((ConstellationItem) out.getItem()).setTraitConstellation(out, this.getTraitConstellation());
            }
        }
    }

    @Override
    public void writeRecipeSync(RegistryFriendlyByteBuf buf) {
        super.writeRecipeSync(buf);

        ByteBufUtils.writeOptional(buf, this.getAttunedConstellation(), ByteBufUtils::writeRegistryEntry);
        ByteBufUtils.writeOptional(buf, this.getTraitConstellation(), ByteBufUtils::writeRegistryEntry);
    }

    @Override
    public void readRecipeSync(RegistryFriendlyByteBuf buf) {
        super.readRecipeSync(buf);

        this.attunedConstellation = ByteBufUtils.readOptional(buf, ByteBufUtils::readRegistryEntry);
        this.traitConstellation = ByteBufUtils.readOptional(buf, ByteBufUtils::readRegistryEntry);
    }
}
