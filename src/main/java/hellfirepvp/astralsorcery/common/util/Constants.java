package hellfirepvp.astralsorcery.common.util;

import net.minecraft.nbt.Tag;

/**
 * Compatibility constants retained from Forge 1.16.
 */
public final class Constants {

    private Constants() {}

    public static final class NBT {
        public static final int TAG_END = Tag.TAG_END;
        public static final int TAG_BYTE = Tag.TAG_BYTE;
        public static final int TAG_SHORT = Tag.TAG_SHORT;
        public static final int TAG_INT = Tag.TAG_INT;
        public static final int TAG_LONG = Tag.TAG_LONG;
        public static final int TAG_FLOAT = Tag.TAG_FLOAT;
        public static final int TAG_DOUBLE = Tag.TAG_DOUBLE;
        public static final int TAG_BYTE_ARRAY = Tag.TAG_BYTE_ARRAY;
        public static final int TAG_STRING = Tag.TAG_STRING;
        public static final int TAG_LIST = Tag.TAG_LIST;
        public static final int TAG_COMPOUND = Tag.TAG_COMPOUND;
        public static final int TAG_INT_ARRAY = Tag.TAG_INT_ARRAY;
        public static final int TAG_LONG_ARRAY = Tag.TAG_LONG_ARRAY;

        private NBT() {}
    }

    public static final class BlockFlags {
        public static final int BLOCK_UPDATE = 1;
        public static final int SEND_TO_CLIENTS = 2;
        public static final int NO_RERENDER = 4;
        public static final int RERENDER_MAIN_THREAD = 8;
        public static final int BLOCK_MOVED = 16;
        public static final int UPDATE_NEIGHBORS = 32;
        public static final int IS_MOVING = 64;
        public static final int DEFAULT = BLOCK_UPDATE | SEND_TO_CLIENTS;
        public static final int DEFAULT_AND_RERENDER = DEFAULT | RERENDER_MAIN_THREAD;

        private BlockFlags() {}
    }
}
