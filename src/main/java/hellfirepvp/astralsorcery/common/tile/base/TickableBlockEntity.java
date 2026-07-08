package hellfirepvp.astralsorcery.common.tile.base;

/**
 * Local ticking contract retained for Astral Sorcery block entities.
 *
 * <p>Modern Minecraft supplies block-entity tickers from the block instead of
 * exposing a tickable marker on the block entity instance.</p>
 */
public interface TickableBlockEntity {

    void tick();
}
