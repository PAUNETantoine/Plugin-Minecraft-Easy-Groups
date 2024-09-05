//@author: gamingtoine
//Version : 1.0

package org.gamingtoine.easygroups.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class commandsEasyGroupsTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender commandSender,Command command,String s,String[] args) {

        List<String> completions = new ArrayList<>();

        if (args.length == 1) {//Liste des sous-commandes disponibles
            completions.add("help");
            completions.add("coordinates");
            completions.add("coordinates-stop");
            completions.add("trackPlayer");
            completions.add("trackPlayer-stop");
            completions.add("home");
            completions.add("home-stop");
            completions.add("trackHome");
            completions.add("trackHome-stop");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("trackPlayer") || args[0].equalsIgnoreCase("trackPlayer-stop")){
            completions.add("GROUPNAME PLAYERNAME");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("home") || args[0].equalsIgnoreCase("trackHome") || args[0].equalsIgnoreCase("trackHome-stop") || args[0].equalsIgnoreCase("home-stop")){
            completions.add("GROUPNAME HOMENAME");
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("coordinates") || args[0].equalsIgnoreCase("coordinates-stop")){
            completions.add("GROUPNAME");
        }

        return completions;
    }
}
