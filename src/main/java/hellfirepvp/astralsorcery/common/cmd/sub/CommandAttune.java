/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2022
 *
 * All rights reserved.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.cmd.sub;

import net.minecraft.network.chat.Component;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import hellfirepvp.astralsorcery.common.cmd.argument.ArgumentTypeConstellation;
import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.constellation.IMajorConstellation;
import hellfirepvp.astralsorcery.common.data.research.ResearchManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.ChatFormatting;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CommandAttune
 * Created by HellFirePvP
 * Date: 21.07.2019 / 20:19
 */
public class CommandAttune implements Command<CommandSourceStack> {

    private static final CommandAttune CMD = new CommandAttune();

    private CommandAttune() {}

    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("attune")
                .requires(cs -> cs.hasPermission(2))
                .then(Commands.argument("player", EntityArgument.player())
                        .then(Commands.argument("constellation", ArgumentTypeConstellation.major())
                                .executes(CMD)));
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Player player = (Player) context.getArgument("player", EntitySelector.class).findSinglePlayer(context.getSource());
        IMajorConstellation cst = (IMajorConstellation) context.getArgument("constellation", IConstellation.class);

        if (ResearchManager.setAttunedConstellation(player, cst)) {
            context.getSource().sendSuccess(() -> 
                    Component.literal("Success! Player has been attuned to ").append(cst.getConstellationName().withStyle(ChatFormatting.BLUE))
                            .withStyle(ChatFormatting.GREEN), true);
        } else {
            context.getSource().sendSuccess(() -> 
                    Component.literal("Failed! Player specified doesn't seem to have the research progress necessary!").withStyle(ChatFormatting.RED), true);
        }
        return 0;
    }
}
