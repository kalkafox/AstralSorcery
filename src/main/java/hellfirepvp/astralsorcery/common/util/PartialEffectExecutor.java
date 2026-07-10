/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.util;

import java.util.Random;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: PartialEffectExecutor
 * Created by HellFirePvP
 * Date: 13.12.2020 / 18:16
 */
public class PartialEffectExecutor {

    private static final Random RAND = new Random();

    private final Random random;
    private final float amount;
    private float index;

    public PartialEffectExecutor(float amount) {
        this(amount, RAND);
    }

    public PartialEffectExecutor(float amount, Random random) {
        this.random = random;
        this.amount = amount;
        this.index = amount;
    }

    public boolean canExecute() {
        return index > 1 || random.nextFloat() < index;
    }

    public void markExecution() {
        index -= 1F;
    }

    public void reset() {
        this.index = this.amount;
    }

    public boolean executeAll(Runnable run) {
        boolean ranAtLeastOnce = false;
        while (canExecute()) {
            markExecution();
            run.run();
            ranAtLeastOnce = true;
        }
        return ranAtLeastOnce;
    }
}
