//@author: gamingtoine
//Version : 1.0

package org.gamingtoine.easygroups.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class groupsTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender commandSender,Command command,String s,String[] args) {

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {//Liste des sous-commandes disponibles
            completions.add("help");
            completions.add("create");
            completions.add("delete");
            completions.add("list");
            completions.add("player");
            completions.add("invite");
            completions.add("leave");
            completions.add("sethome");
            completions.add("homes");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("create")){
            completions.add("GROUPNAME");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("delete")){
            completions.add("GROUPNAME");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("list")){
            completions.add("players");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("list")){
            completions.add("homes");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("player")){
            completions.add("add");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("player")){
            completions.add("remove");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("leave")){
            completions.add("GROUPNAME");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("invite")){
            completions.add("PLAYER GROUPNAME");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("sethome")){
            completions.add("GROUPNAME HOMENAME");
        }

        if (args.length == 3 && args[1].equalsIgnoreCase("add")){
            completions.add("GROUPNAME PLAYER");
        }

        if (args.length == 3 && args[1].equalsIgnoreCase("remove")){
            completions.add("GROUPNAME PLAYER");
        }

        return completions;
    }
}
