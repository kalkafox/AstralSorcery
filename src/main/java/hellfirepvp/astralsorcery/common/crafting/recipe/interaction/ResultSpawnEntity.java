/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.recipe.interaction;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import hellfirepvp.astralsorcery.common.util.data.ByteBufUtils;
import hellfirepvp.astralsorcery.common.util.data.Vector3;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.registries.ForgeRegistries;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ResultSpawnEntity
 * Created by HellFirePvP
 * Date: 31.10.2020 / 14:05
 */
public class ResultSpawnEntity extends InteractionResult {

    private EntityType<?> entityType;

    ResultSpawnEntity() {
        super(InteractionResultRegistry.ID_SPAWN_ENTITY);
    }

    public static ResultSpawnEntity spawnEntity(EntityType<?> type) {
        if (!type.isSummonable()) {
            throw new IllegalArgumentException("EntityType " + type.getRegistryName() + " is not summonable!");
        }
        ResultSpawnEntity drop = new ResultSpawnEntity();
        drop.entityType = type;
        return drop;
    }

    public EntityType<?> getEntityType() {
        return entityType;
    }

    @Override
    public void doResult(Level world, Vector3 at) {
        Entity e = this.entityType.create(world);
        if (!(e instanceof LivingEntity)) {
            return;
        }
        e.setLocationAndAngles(at.getX(), at.getY(), at.getZ(), world.rand.nextFloat() * 360.0F, 0.0F);
        world.addEntity(e);
    }

    @Override
    public void read(JsonObject json) throws JsonParseException {
        ResourceLocation key = new ResourceLocation(JSONUtils.getString(json, "entityType"));
        EntityType<?> type = ForgeRegistries.ENTITIES.getValue(key);
        if (type == null) {
            throw new JsonParseException("Unknown entity type: " + key);
        }
        this.entityType = type;
    }

    @Override
    public void write(JsonObject json) {
        json.addProperty("entityType", this.entityType.getRegistryName().toString());
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.entityType = ByteBufUtils.readRegistryEntry(buf);
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        ByteBufUtils.writeRegistryEntry(buf, this.entityType);
    }
}
