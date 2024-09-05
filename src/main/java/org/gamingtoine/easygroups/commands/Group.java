//@author: gamingtoine
//Version : 1.0

package org.gamingtoine.easygroups.commands;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.gamingtoine.easygroups.pluginConfigFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Group implements CommandExecutor {

    private final FileConfiguration groupsFile;
    private final File dataFolder;
    private boolean invited;


    public Group(FileConfiguration groupsFile, File dataFolder) {
        this.groupsFile = groupsFile;
        this.dataFolder = dataFolder;
        this.invited = false;//Seule façon de faire marcher la commande invite
    }




    @Override
    public boolean onCommand(CommandSender commandSender,Command command,String arg,String[] strings) {
        //On vérifie si le monde est bien autorisé
        if(commandSender instanceof Player)
        {
            pluginConfigFile configYaml = new pluginConfigFile();
            Player cmdsnder = (Player) commandSender;
            if(configYaml.bannedWorlds.contains(cmdsnder.getWorld().getName()) && !commandSender.isOp()){
                commandSender.sendMessage(ChatColor.RED + "| EasyGroups | The world you are in as been disabled for EasyGroups.");
                return false;
            }
        }

        //Vérification qu'il y a bien un argument
        if (strings.length == 0) {
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This command need an argument to work, use: '/group help' to see all the commands");
            return false;
        }


        if(!commandSender.hasPermission("easygroups.group.use"))
        {
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | You are not allow to perform this command.");
            return false;
        }



        //Si il y a un argument alors on lance la commande qui lui correspond
        switch (strings[0]) {
            case "help":
                groupHelp(commandSender);
                break;
            case "create":
                groupCreate(commandSender, strings);
                break;
            case "delete":
                groupDelete(commandSender, strings, false);
                break;
            case "player"://group player add nomJoueur nomGroupe
                groupPlayerOptions(commandSender, strings);
                break;
            case "list":
                groupList(commandSender, strings);
                break;
            case "invite":
                groupInvite(commandSender, strings);
                break;
            case "leave":
                groupLeave(commandSender, strings);
                break;
            case "sethome":
                groupSetHome(commandSender, strings);
                break;
            case "homes":
                String[] temp = new String[2];
                temp[1] = "homes";
                groupList(commandSender, temp);
                break;
            default:
                groupHelp(commandSender);
                break;
        }
        return false;
    }


    public void groupHelp(CommandSender commandSender) {//méthode à améliorer
        commandSender.sendMessage(ChatColor.DARK_PURPLE + "| EasyGroups | all Commands for your Groups : ");
        commandSender.sendMessage(ChatColor.AQUA + "| EasyGroups |"  +ChatColor.YELLOW + " '/group help' "  +ChatColor.AQUA + " send you this page.");
        commandSender.sendMessage(ChatColor.AQUA + "| EasyGroups |"  +ChatColor.YELLOW + " '/group delete [GROUPNAME]'"  +ChatColor.AQUA + " delete a group from the server.");
        commandSender.sendMessage(ChatColor.AQUA + "| EasyGroups |"  +ChatColor.YELLOW + " '/group create [GROUPNAME]'"  +ChatColor.AQUA + " create a group with a special name.");
        commandSender.sendMessage(ChatColor.AQUA + "| EasyGroups |"  +ChatColor.YELLOW + " '/group invite [PLAYERNAME]'"  +ChatColor.AQUA + " invite a player into your group.");
        commandSender.sendMessage(ChatColor.AQUA + "| EasyGroups |"  +ChatColor.YELLOW + " '/group list [players|Homes|'']' 'or /group homes'"  +ChatColor.YELLOW + " tel you every existing groups with their players/homes they have in.");
        commandSender.sendMessage(ChatColor.AQUA + "| EasyGroups |"  +ChatColor.YELLOW + " '/group player add [GROUPNAME] [PLAYERNAME]'"  +ChatColor.AQUA + " tel you in which group is a player.");
        commandSender.sendMessage(ChatColor.AQUA + "| EasyGroups |"  +ChatColor.YELLOW + " '/group player remove [GROUPNAME] [PLAYERNAME]'"  +ChatColor.AQUA + " remove a player from a specified group.");
        commandSender.sendMessage(ChatColor.AQUA + "| EasyGroups |"  +ChatColor.YELLOW + " '/group sethome [GROUPNAME] [HOMENAME]'"  +ChatColor.AQUA + " creates an home at your position and allow the other players from your group to see the coordinates.");

    }

    public void groupCreate(CommandSender commandSender, String[] args) {//Méthode complète

        if(args.length == 1){
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This commands needs more arguments to work.");
            return;
        }

        final ConfigurationSection configSection = this.groupsFile.getConfigurationSection("EasyGroups.groups");

        List<String> listeJoueurs = this.groupsFile.getStringList("EasyGroups.groups."+args[1]+".players");//Liste des joueurs dans les groupes
        List<String> listeGroupes = this.groupsFile.getStringList("EasyGroups.groupList");//Liste des groupes


        if(!commandSender.hasPermission("easygroups.group.use")){
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | Your are not allow to perform this command.");
            return;
        }


        if (configSection == null) { //Cas ou il n'y a pas encore de groupes de créées (évite le bug out of index car Config section de args[1] n'existe pas)
            listeGroupes.add(args[1]);
            listeJoueurs.add(commandSender.getName());
            this.groupsFile.set("EasyGroups.groups." + args[1] + ".players", listeJoueurs);
            this.groupsFile.set("EasyGroups.groupList", listeGroupes);
            commandSender.sendMessage(ChatColor.BLUE + "| EasyGroups | Successfully created group " + args[1] + ".");
        } else {
            if (configSection.getString(args[1]) == null) { //Cas quand il y a déjà un groupe
                listeGroupes.add(args[1]);
                listeJoueurs.add(commandSender.getName());
                this.groupsFile.set("EasyGroups.groups." + args[1] + ".players", listeJoueurs);
                this.groupsFile.set("EasyGroups.groupList", listeGroupes);
                commandSender.sendMessage(ChatColor.BLUE + "| EasyGroups | Successfully created group " + args[1] + ".");
            } else {
                commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This group name already exists.");
            }
        }

        try {//Save fichier YAML
            this.groupsFile.save(getFile("groups.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    private void groupDelete(CommandSender commandSender, String[] args, boolean leave) {//Méthode complète

        if(args.length == 1){
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This commands needs more arguments to work.");
            return;
        }

        final ConfigurationSection configSection = this.groupsFile.getConfigurationSection("EasyGroups.groups");
        final pluginConfigFile configYaml = new pluginConfigFile();

        List<String> listeJoueurs = this.groupsFile.getStringList("EasyGroups.groups."+args[1]+".players");//Liste des joueurs dans les groupes


        List<String> listeGroupes = this.groupsFile.getStringList("EasyGroups.groupList");

        if(!listeJoueurs.contains(commandSender.getName()) && !commandSender.hasPermission("easygroups.groups.addRemove") && !configYaml.everyoneAddRemove && !leave){
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | You are not allowed to remove this group because you are not in.");
            return;
        }

        if(!commandSender.hasPermission("easygroups.group.use") && !commandSender.hasPermission("easygroups.groups.addRemove") && !configYaml.everyoneAddRemove){
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | Your are not allow to perform this command.");
            return;
        }

        if (configSection == null) {
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | Their is not existing group !");
        } else {
            if (!configSection.contains(args[1])) {
                commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This group doesn't exist !");
            } else {
                this.groupsFile.set("EasyGroups.groups." + args[1], null);
                listeGroupes.remove(args[1]);
                this.groupsFile.set("EasyGroups.groupList", listeGroupes);
                this.groupsFile.set("EasyGroups.groupList", listeGroupes);
                commandSender.sendMessage(ChatColor.BLUE + "| EasyGroups | The group " + args[1] + " has been deleted.");
            }

        }

        try {//On save le fichier YAML
            this.groupsFile.save(getFile("groups.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void groupPlayerOptions(CommandSender commandSender, String[] args) { //Méthode à améliorer ?
        if(args.length < 2){
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This commands needs more arguments to work.");
            return;
        }

        switch (args[1]) {
            case "add":
                playerAdd(commandSender, args);
                break;
            case "remove":
                playerRemove(commandSender, args);
                break;
        }

    }


    private void playerAdd(CommandSender commandSender, String[] args) { //Méthode complète

        if(args.length < 4){
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This commands needs more arguments to work.");
            return;
        }

        final ConfigurationSection configSection = this.groupsFile.getConfigurationSection("EasyGroups.groups");

        List<String> listeJoueurs = this.groupsFile.getStringList("EasyGroups.groups."+args[2]+".players");
        final pluginConfigFile configYaml = new pluginConfigFile();


        if(commandSender.hasPermission("easygroups.group.addRemove") || configYaml.everyoneAddRemove || listeJoueurs.contains(commandSender.getName()) || this.invited) {
            this.invited = false;//Seule façon de faire marcher la commande invite
            if (configSection == null) {
                commandSender.sendMessage(ChatColor.RED + "| EasyGroups | Their is not existing group !");
            } else {
                if (configSection.getString(args[2]) == null) {
                    System.out.println(args[2]);
                    commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This group doesn't exist !");
                } else {
                    if (!this.groupsFile.contains("EasyGroups.groups." + args[2] + ".players" + args[3])) {
                        if (!listeJoueurs.contains(args[3])) {//Si le pseudo renseigner n'existe pas déjà

                            listeJoueurs.add(args[3]);
                            commandSender.sendMessage(ChatColor.BLUE + "| EasyGroups | The player '" + args[3] + "' as been added to this group !");
                        } else {
                            commandSender.sendMessage(ChatColor.YELLOW + "| EasyGroups | This player is already in this group !");
                        }
                        this.groupsFile.set("EasyGroups.groups." + args[2] + ".players", listeJoueurs); //On affiche les joueurs sous forme de liste
                    }
                }
            }
        }else{
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | You are not allow to modify this player's group !");
        }


        try {//On save le fichier YAML
            this.groupsFile.save(getFile("groups.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }




    private void playerRemove(CommandSender commandSender, String[] args) {

        if(args.length < 4){
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This commands needs more arguments to work.");
            return;
        }


        final ConfigurationSection configSection = this.groupsFile.getConfigurationSection("EasyGroups.groups");

        List<String> listeJoueurs = this.groupsFile.getStringList("EasyGroups.groups."+args[2]+".players");

        List<String> listeGroupes = this.groupsFile.getStringList("EasyGroups.groupList");

        final pluginConfigFile configYaml = new pluginConfigFile();

        if(!commandSender.hasPermission("easygroups.group.use")){
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | Your are not allow to perform this command.");
            return;
        }

        if(commandSender.hasPermission("easygroups.group.addRemove") || configYaml.everyoneAddRemove || listeJoueurs.contains(commandSender.getName())) {//Permission de modifier groupes ou est dans le group
            if (configSection == null) {
                commandSender.sendMessage(ChatColor.RED + "| EasyGroups | Their is not existing group !");
            } else {
                if (configSection.getString(args[2]) == null) {
                    System.out.println(args[2]);
                    commandSender.sendMessage(ChatColor.RED + "| EasyGroups| This group doesn't exist !");
                } else {
                    if (!this.groupsFile.contains("EasyGroups.groups." + args[2] + ".players" + args[3])) {
                        if (listeJoueurs.contains(args[3])) {//Si le pseudo renseigner n'existe pas déjà
                            listeJoueurs.remove(args[3]);
                            commandSender.sendMessage(ChatColor.BLUE + "| EasyGroups | The player '" + args[3] + "' as been removed from this group.");
                        } else {
                            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This player is not in this group !");
                        }
                        this.groupsFile.set("EasyGroups.groups." + args[2] + ".players", listeJoueurs); //On affiche les joueurs sous forme de liste
                    }
                }
            }
        }else{
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | You are not allowed to change this player's group !");
        }


        if(listeJoueurs.isEmpty()){//Si quand on enlève le dernier joueur le groupe est vide,on le supprime
            String[] tempTable = new String[2];
            tempTable[0] = "delete";
            tempTable[1] = args[2];
            listeGroupes.remove(args[2]);
            groupDelete(commandSender, tempTable, true);
        }


        try {//On save le fichier YAML
            this.groupsFile.save(getFile("groups.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void groupList(CommandSender commandSender, String[] args) { //Méthode complète

        List<String> listeGroupes = this.groupsFile.getStringList("EasyGroups.groupList");

        if(!commandSender.hasPermission("easygroups.group.use")){
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | You are not allow to perform this command.");
            return;
        }

        if(args.length < 1){
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This command needs arguments to work, do /group help.");
            return;
        }

        if(!listeGroupes.isEmpty()) {
            if(args.length > 1){
                commandSender.sendMessage(ChatColor.BLUE + "| EasyGroups | All the server's groups "+args[1]+" are : ");
            }else{
                commandSender.sendMessage(ChatColor.BLUE + "| EasyGroups | All the server's groups are : ");
            }
            for(int i = 0 ; i < listeGroupes.size() ; i++){
                if(args.length > 1){
                    commandSender.sendMessage("- "+listeGroupes.get(i) + ": ");
                    if(args[1].equals("players")){
                        List<String> listeJoueurs = this.groupsFile.getStringList("EasyGroups.groups."+listeGroupes.get(i)+".players");
                        for(int j = 0 ; j < listeJoueurs.size() ; j++){
                            commandSender.sendMessage(ChatColor.AQUA + "     " + listeJoueurs.get(j));
                        }
                    } else if(args[1].equals("homes")){
                        List<String> listeHomes = this.groupsFile.getStringList("EasyGroups.homesName."+listeGroupes.get(i));
                        for(int j = 0 ; j < listeHomes.size() ; j++){
                            commandSender.sendMessage(ChatColor.AQUA + "     " + listeHomes.get(j));
                        }
                    }
                }
                else{
                    commandSender.sendMessage(ChatColor.AQUA + "- "+listeGroupes.get(i));
                }
            }
        }else{
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | Their is no existing group for now.");
        }
    }


    private void groupInvite(CommandSender commandSender, String[] args) {

        if(args.length < 3){
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This commands needs more arguments to work.");
            return;
        }

        List<String> listeJoueurs = this.groupsFile.getStringList("EasyGroups.groups."+args[2]+".players");

        List<String> listeGroupes = this.groupsFile.getStringList("EasyGroups.groupList");

        List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());//On place la collection 'Online players' dans une list pour la manipulation

        if(!listeGroupes.contains(args[2])){
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This group doesn't exist.");
            return;
        }

        if(!commandSender.hasPermission("easygroups.group.use")){
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | Your are not allow to perform this command.");
            return;
        }


       for (int i = 0 ; i < onlinePlayers.size() ; i++){//On recherche dans une boucle si le joueur est bien connecter

           if (onlinePlayers.get(i).getName().equals(args[1])){//Si le joueur est connecter
               if(listeJoueurs.contains(commandSender.getName())){//Si le joueur qui envoie la commande est bien dans le groupe//Si le joueur qui reçoit l'invitation est bien dans le groupe demander
                   String sendedMessage = ChatColor.DARK_BLUE + "| EasyGroups | " + commandSender.getName() + " send you an invitation to the group "+ args[2] + ChatColor.GOLD + ". Click here to accept the invitation.";
                   TextComponent inviteMessage = new TextComponent(sendedMessage);
                   inviteMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/group player add " + args[2] + " " + onlinePlayers.get(i).getName()));
                   Player player = onlinePlayers.get(i);
                   player.spigot().sendMessage(inviteMessage);
                   commandSender.sendMessage(ChatColor.AQUA + "| EasyGroups | This player has been invited in your group.");
                   this.invited = true;
                   return;
               }else {
                   commandSender.sendMessage(ChatColor.RED + "| EasyGroups | You are not in this group.");
                   return;
               }
           }
       }
       commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This player is not connected.");
    }

    private void groupLeave(CommandSender commandSender, String[] args) {
        if(args.length == 1){
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This commands needs more arguments to work.");
            return;
        }

        List<String> listeJoueurs = this.groupsFile.getStringList("EasyGroups.groups."+args[1]+".players");


        String[] temp = new String[4];//On créer un tableau temporaire pour trier dans le bon ordre les données pour qu'elles soient correctes pour la méthode removePlayer

        if(listeJoueurs.contains(commandSender.getName())){//Verification si le joueur est bien dans le bon groupe
            temp[2] = args[1];
            temp[3] = commandSender.getName();
            playerRemove(commandSender,temp);
        }else{
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | You are not in this group.");
        }

    }

    private void groupSetHome(CommandSender commandSender, String[] args) {

        if(args.length < 3){
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This commands needs more arguments to work.");
            return;
        }

        List<String> listeJoueurs = this.groupsFile.getStringList("EasyGroups.groups."+args[1]+".players");

        List<String> listeGroupes = this.groupsFile.getStringList("EasyGroups.groupList");

        final ConfigurationSection configSection = this.groupsFile.getConfigurationSection("EasyGroups.GroupHomes");

        if(!commandSender.hasPermission("easygroups.group.use")){
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | Your are not allow to perform this command.");
            return;
        }

        if(!listeGroupes.contains(args[1])){
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This group doesn't exist.");
            return;

        }


        List<String> listeGroupesHomes = this.groupsFile.getStringList("EasyGroups.GroupHomes."+args[1]+".homes."+args[2]);
        List<String> listeGroupesHomesName = this.groupsFile.getStringList("EasyGroups.homesName."+args[1]);

        if(configSection == null){
            this.groupsFile.set("EasyGroups.GroupHomes."+args[1]+".homes", listeGroupesHomes);
        }

        if(!listeGroupesHomes.isEmpty()){
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This Home's name already exists.");
            return;

        }


        if(!listeJoueurs.contains(commandSender.getName())){
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | Your are not in this group.");
            return;
        }

        if(listeGroupesHomes.contains(args[2])){
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This home already exists in this group.");
            return;
        }

        if(commandSender instanceof Player){
            Player cmdSnder = (Player) commandSender;
            listeGroupesHomes.add("" + (int)cmdSnder.getLocation().getX());
            listeGroupesHomes.add("" + (int)cmdSnder.getLocation().getY());
            listeGroupesHomes.add("" + (int)cmdSnder.getLocation().getZ());
            listeGroupesHomes.add(cmdSnder.getWorld().getName());
            this.groupsFile.set("EasyGroups.GroupHomes."+args[1]+".homes."+args[2], listeGroupesHomes);
            listeGroupesHomesName.add(args[2]);
            this.groupsFile.set("EasyGroups.homesName."+args[1], listeGroupesHomesName);
            commandSender.sendMessage(ChatColor.AQUA + "| EasyGroups | The home "+args[2]+" has been created in the group "+args[1]);
        }



        try {//Save fichier YAML
            this.groupsFile.save(getFile("groups.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }





    public File getFile(String fileName){
        return new File(dataFolder, fileName);
    }



}

