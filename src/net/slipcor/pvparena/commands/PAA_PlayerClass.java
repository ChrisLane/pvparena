package net.slipcor.pvparena.commands;

import net.slipcor.pvparena.PVPArena;
import net.slipcor.pvparena.arena.Arena;
import net.slipcor.pvparena.arena.ArenaPlayer;
import net.slipcor.pvparena.core.Help;
import net.slipcor.pvparena.core.Help.HELP;
import net.slipcor.pvparena.core.Language;
import net.slipcor.pvparena.core.Language.MSG;
import net.slipcor.pvparena.core.StringParser;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <pre>
 * PVP Arena PLAYERCLASS Command class
 * </pre>
 * <p/>
 * A command to manage arena player classes
 *
 * @author slipcor
 */

public class PAA_PlayerClass extends AbstractArenaCommand {

    public PAA_PlayerClass() {
        super(new String[]{"pvparena.create.class","pvparena.cmds.playerclass"});
    }

    @Override
    public void commit(final Arena arena, final CommandSender sender, final String[] args) {
        if (!hasPerms(sender, arena)) {
            return;
        }

        if (!argCountValid(sender, arena, args, new Integer[]{1, 2})) {
            return;
        }

        if (!(sender instanceof Player)) {
            Arena.pmsg(sender, Language.parse(arena, MSG.ERROR_ONLY_PLAYERS));
            return;
        }

        final String className;

        if (args.length > 1) {
            if (PVPArena.hasCreatePerms(sender, arena)) {
                className = args[1];
            } else {
                className = sender.getName();
            }
        } else {
            className = sender.getName();
        }


        // /pa {arenaname} playerclass save {name}
        // /pa {arenaname} playerclass remove {name}

        final Player player = (Player) sender;

        if ("save".equalsIgnoreCase(args[0])) {
            final List<ItemStack> items = new ArrayList<>();

            for (final ItemStack is : player.getInventory().getContents()) {
                if (is != null) {
                    items.add(is);
                }
            }

            final ItemStack[] isItems = new ItemStack[items.size()];
            int position = 0;
            for (final ItemStack is : items) {
                isItems[position++] = is;
            }

            final String sItems = isItems.length < 1 ? "AIR"
                    : StringParser.getStringFromItemStacks(isItems);
            final StringBuilder armor = new StringBuilder("");
            int pos = 0;
            for (final ItemStack item : player.getInventory().getArmorContents()) {
                armor.append(',');
                armor.append(pos++);
                armor.append(">>!<<");
                armor.append(StringParser.getStringFromItemStack(item));
            }
            /*if (player.getInventory().getItemInOffHand() != null) {
                armor.append(',');
                armor.append(0);
                armor.append(">>O<<");
                armor.append(StringParser.getStringFromItemStack(player.getInventory().getItemInOffHand()));
            }*/

            arena.getArenaConfig().setManually("classitems." + className, sItems + armor);
            arena.getArenaConfig().save();
            arena.addClass(className, isItems, player.getInventory().getArmorContents());
            Arena.pmsg(player, Language.parse(arena, MSG.CLASS_SAVED, className));

        } else if ("remove".equalsIgnoreCase(args[0])) {
            arena.getArenaConfig().setManually("classitems." + className, null);
            arena.getArenaConfig().save();
            arena.removeClass(className);
            Arena.pmsg(player, Language.parse(arena, MSG.CLASS_REMOVED, className));
        }
        reset(player);
    }

    private void reset(final Player player) {
        PVPArena.instance.getLogger().info("Exiting edit mode: " + player.getName());

        final ArenaPlayer aPlayer = ArenaPlayer.parsePlayer(player.getName());

        aPlayer.setArena(null);
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public void displayHelp(final CommandSender sender) {
        Arena.pmsg(sender, Help.parse(HELP.PLAYERCLASS));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("playerclass");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!pcl");
    }

    @Override
    public CommandTree<String> getSubs(final Arena arena) {
        final CommandTree<String> result = new CommandTree<>(null);
        result.define(new String[]{"save"});
        result.define(new String[]{"remove"});
        return result;
    }
}
