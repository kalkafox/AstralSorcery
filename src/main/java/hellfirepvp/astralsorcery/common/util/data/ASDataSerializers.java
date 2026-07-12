/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util.data;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
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
        private final StreamCodec<RegistryFriendlyByteBuf, Long> codec = StreamCodec.of(
                (buf, value) -> buf.writeLongLE(value),
                buf -> buf.readLongLE());

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, Long> codec() {
            return codec;
        }

        @Override
        public Long copy(Long value) {
            return value;
        }
    };

    public static EntityDataSerializer<Vector3> VECTOR = new EntityDataSerializer<Vector3>() {
        private final StreamCodec<RegistryFriendlyByteBuf, Vector3> codec = StreamCodec.of(
                (buf, value) -> {
                    buf.writeDouble(value.getX());
                    buf.writeDouble(value.getY());
                    buf.writeDouble(value.getZ());
                },
                buf -> new Vector3(buf.readDouble(), buf.readDouble(), buf.readDouble()));

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, Vector3> codec() {
            return codec;
        }

        @Override
        public Vector3 copy(Vector3 value) {
            return value.clone();
        }
    };

    public static EntityDataSerializer<FluidStack> FLUID = new EntityDataSerializer<FluidStack>() {
        private final StreamCodec<RegistryFriendlyByteBuf, FluidStack> codec = StreamCodec.of(
                (buf, value) -> ByteBufUtils.writeFluidStack(buf, value),
                buf -> ByteBufUtils.readFluidStack(buf));

        @Override
        public StreamCodec<? super RegistryFriendlyByteBuf, FluidStack> codec() {
            return codec;
        }

        @Override
        public FluidStack copy(FluidStack value) {
            return value.copy();
        }
    };

}
