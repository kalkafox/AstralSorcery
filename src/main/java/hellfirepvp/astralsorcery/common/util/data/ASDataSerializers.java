/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.data;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: ASIDataSerializers
 * Created by HellFirePvP
 * Date: 18.07.2017 / 23:46
 */
public class ASDataSerializers {

    public static EntityDataSerializer<Long> LONG = new EntityDataSerializer<Long>() {
        @Override
        public void write(FriendlyByteBuf buf, Long value) {
            buf.writeLongLE(value);
        }

        @Override
        public Long read(FriendlyByteBuf buf) {
            return buf.readLongLE();
        }

        @Override
        public EntityDataAccessor<Long> createKey(int id) {
            return new EntityDataAccessor<>(id, this);
        }

        @Override
        public Long read(Long value) {
            return new Long(value);
        }
    };

    public static EntityDataSerializer<Vector3> VECTOR = new EntityDataSerializer<Vector3>() {
        @Override
        public void write(FriendlyByteBuf buf, Vector3 value) {
            buf.writeDouble(value.getX());
            buf.writeDouble(value.getY());
            buf.writeDouble(value.getZ());
        }

        @Override
        public Vector3 read(FriendlyByteBuf buf) {
            return new Vector3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }

        @Override
        public EntityDataAccessor<Vector3> createKey(int id) {
            return new EntityDataAccessor<>(id, this);
        }

        @Override
        public Vector3 read(Vector3 value) {
            return value.clone();
        }
    };

    public static EntityDataSerializer<FluidStack> FLUID = new EntityDataSerializer<FluidStack>() {
        @Override
        public void write(FriendlyByteBuf buf, FluidStack value) {
            ByteBufUtils.writeFluidStack(buf, value);
        }

        @Override
        public FluidStack read(FriendlyByteBuf buf) {
            return ByteBufUtils.readFluidStack(buf);
        }

        @Override
        public EntityDataAccessor<FluidStack> createKey(int id) {
            return new EntityDataAccessor<>(id, this);
        }

        @Override
        public FluidStack read(FluidStack value) {
            return value.copy();
        }
    };

}
