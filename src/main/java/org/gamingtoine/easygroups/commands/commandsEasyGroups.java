//@author: gamingtoine
//Version : 1.0


package org.gamingtoine.easygroups.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.gamingtoine.easygroups.EasyGroups;
import org.gamingtoine.easygroups.pluginConfigFile;

import java.io.File;
import java.util.List;

public class commandsEasyGroups implements CommandExecutor {

    //Data
    private final FileConfiguration groupsFile;
    private final File dataFolder;

    public boolean stop;

    private int taskId;

    private int task2;

    final BossBar bossBar = Bukkit.createBossBar("'s position",BarColor.RED, BarStyle.SEGMENTED_10);

    public commandsEasyGroups(FileConfiguration groupsFile, File dataFolder){
        this.groupsFile = groupsFile;
        this.dataFolder = dataFolder;
        this.stop = false;
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

        if (strings.length == 0) {
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This command need an argument to work, use: '/easygroups help' to see all the commands");
            return false;
        }

        if(!commandSender.hasPermission("easygroups.easygroup.use"))
        {
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | You are not allow to perform this command.");
            return false;
        }


        switch (strings[0]) {
            case "help":
                commandHelp(commandSender);
                break;
            case "coordinates":
                try {
                    commandCoordinates(commandSender, strings, false, false);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                break;
            case "coordinates-stop":
                try{
                    commandCoordinates(commandSender, strings, true, false);
                    break;
                }catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            case "trackPlayer":
                commandTrackCompass(commandSender, strings, false, false);
                break;
            case "trackPlayer-stop":
                commandTrackCompass(commandSender, strings, true, false);
                break;
            case "home":
                try{
                    commandHome(commandSender, strings, false);
                }catch (InterruptedException e){
                    throw new RuntimeException(e);
                }
                break;
            case "home-stop":
                try{
                    commandHome(commandSender, strings, true);
                }catch (InterruptedException e){
                    throw new RuntimeException(e);
                }
                break;
            case "trackHome":
                commandTrackCompass(commandSender, strings, false, true);
                break;
            case "trackHome-stop":
                commandTrackCompass(commandSender, strings, true, true);
                break;
            default:
                commandHelp(commandSender);
                break;
        }



        return false;
    }



    private void commandHelp(CommandSender commandSender) {
        commandSender.sendMessage(ChatColor.BLUE + "| EasyGroups | This is the list of the available commands:");
        commandSender.sendMessage(ChatColor.BLUE + "| EasyGroups | " + ChatColor.GOLD + "'/easygroups  help'\n" + ChatColor.AQUA + "This command print you this page.");
        commandSender.sendMessage(ChatColor.BLUE + "| EasyGroups | " + ChatColor.GOLD + "'/easygroups coordinates 'GROUPNAME''\n" + ChatColor.AQUA + "This command print all the player's coordinates from the group you choose (if you are in) in the scoreboard.");
        commandSender.sendMessage(ChatColor.BLUE + "| EasyGroups | " + ChatColor.GOLD + "'/easygroups coordinates 'GROUPNAME' 'PLAYERNAME''\n" + ChatColor.AQUA + "This command print the player's coordinates you selected from the group you choose (if you are in) in the scoreboard.");
        commandSender.sendMessage(ChatColor.BLUE + "| EasyGroups | " + ChatColor.GOLD + " '/easygroups coordinates-stop 'GROUPNAME'\n" + ChatColor.AQUA + "This command remove the scoreBoard.");
        commandSender.sendMessage(ChatColor.BLUE + "| EasyGroups | " + ChatColor.GOLD + "'/easygroups trackPlayer 'GROUPNAME' 'PLAYERNAME''\n" + ChatColor.AQUA + "This command creates a compass that track the player you are looking for.");
        commandSender.sendMessage(ChatColor.BLUE + "| EasyGroups | " + ChatColor.GOLD + "'/easygroups trackPlayer-stop 'GROUPNAME' 'PLAYERNAME''\n" + ChatColor.AQUA + "This command hide the compass.");
        commandSender.sendMessage(ChatColor.BLUE + "| EasyGroups | " + ChatColor.GOLD + "'/easygroups home 'GROUPNAME' 'HOMENAME''\n" + ChatColor.AQUA + "This command creates a scoreboard with the coordinates of the group you have choosen.");
        commandSender.sendMessage(ChatColor.BLUE + "| EasyGroups | " + ChatColor.GOLD + "'/easygroups home-stop 'GROUPNAME' 'HOMENAME''\n" + ChatColor.AQUA + "This command remove the scoreboard with the coordinates of the group you have choosen.");
        commandSender.sendMessage(ChatColor.BLUE + "| EasyGroups | " + ChatColor.GOLD + "'/easygroups trackHome 'GROUPNAME' 'HOMENAME''\n" + ChatColor.AQUA + "This command creates à compass that track the home you are looking for.");
        commandSender.sendMessage(ChatColor.BLUE + "| EasyGroups | " + ChatColor.GOLD + "'/easygroups trackHome-stop 'GROUPNAME' 'HOMENAME''\n" + ChatColor.AQUA + "This command remove the compass that track the home you were looking for.");
    }


    private void commandCoordinates(CommandSender commandSender, String[] args, boolean stop, boolean home) throws InterruptedException {

        if(args.length < 2){
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This command needs more arguments to work.");
            return;
        }

        List<String> listeJoueurs = this.groupsFile.getStringList("EasyGroups.groups."+args[1]+".players");//Liste des joueurs dans les groupes
        List<String> listeGroupes = this.groupsFile.getStringList("EasyGroups.groupList");//Liste des groupes



        if(!commandSender.hasPermission("easygroups.easygroup.use")){
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | You are not allowed to perform this command.");
            return;
        }


        if(commandSender instanceof Player) {
            Player player = (Player) commandSender;


            Scoreboard groupBoard = Bukkit.getScoreboardManager().getNewScoreboard();

            if (!listeGroupes.contains(args[1])) {
                commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This group doesn't exist.");
                return;
            }



            if(!listeJoueurs.contains(player.getName())){
                commandSender.sendMessage(ChatColor.RED + "| EasyGroups | You are not in this group.");
                return;
            }

            World actualWorld = Bukkit.getWorld("world");//Le monde dans lequel le joueur est sera le seul affecter


            if(!home){//Si nous recherchons un joueur et non pas un group home
                Objective objective = groupBoard.registerNewObjective(args[1], "Coordinates");
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(EasyGroups.getPlugin(EasyGroups.class), new Runnable() {
                    @Override
                    public void run() {
                        List<Player> connectedPLayers = actualWorld.getPlayers();//Liste des joueurs connectés


                        for (String entry : groupBoard.getEntries()) {

                            objective.getScoreboard().resetScores(entry);//On vide le scoreBoard afin de ne pas dupliquer les lignes
                        }

                        for (int i = 0; i < connectedPLayers.size(); i++) {


                            Player temp = connectedPLayers.get(i);

                            if(listeJoueurs.contains(temp.getName())){//On affiche le scoreboard si et seulement si le joueurs bien dans le bon groupe

                                Location tempLocation = temp.getLocation(); //Récupère la position du joueur "i"

                                Score coordinates = objective.getScore(temp.getName() + ": " + ChatColor.RED + (int)tempLocation.getX() + "x " + ChatColor.GREEN + (int)tempLocation.getY() +  "y " +  ChatColor.BLUE + (int)tempLocation.getZ() + "z");


                                coordinates.setScore(temp.getLevel());//On met une valeur au scoreBoard
                            }
                            if(stop) {
                                Bukkit.getScheduler().cancelTask(taskId);
                                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
                            }
                        }
                    }
                }, 0L, 50L); // 20 ticks = 1 seconde, donc 50 ticks = 2.5 secondes
            }else{
                List<String> listeGroupesHomes = this.groupsFile.getStringList("EasyGroups.GroupHomes."+args[1]+".homes."+args[2]);
                Objective objective = groupBoard.registerNewObjective(args[2], "Group " + args[1] + " Home's Coordinates");
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                Score coordinates = objective.getScore(args[2] + ": " + ChatColor.RED + listeGroupesHomes.get(0) + "x " + ChatColor.GREEN + listeGroupesHomes.get(1) +  "y " +  ChatColor.BLUE + listeGroupesHomes.get(2) + "z");
                coordinates.setScore(player.getLevel());
                if(stop){
                    groupBoard.clearSlot(DisplaySlot.SIDEBAR);
                }
            }


            player.setScoreboard(groupBoard);//Affichage du scoreBoard

        }

    }



    public void commandTrackCompass(CommandSender commandSender, String[] args, boolean stop, boolean home) {

        if(args.length < 3){
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This command needs more arguments to work use /help for informations.");
            return;
        }

        if(!commandSender.hasPermission("easygroups.easygroup.use")){
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | You are not allowed to perform this command.");
            return;
        }

        List<String> listeJoueurs = this.groupsFile.getStringList("EasyGroups.groups."+args[1]+".players");//Liste des joueurs dans les groupes
        List<String> listeGroupes = this.groupsFile.getStringList("EasyGroups.groupList");//Liste des groupes


        if(!listeGroupes.contains(args[1])){
            commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This group doesn't exist.");
            return;
        }

        if(!home){
            Player playerTracked = Bukkit.getPlayerExact(args[2]);

            if(playerTracked == null){
                commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This Player is not connected");
                return;
            }

            if(playerTracked.getName().equals(commandSender.getName())) {
                commandSender.sendMessage(ChatColor.RED + "| EasyGroups | You can't track yourself...");
                return;
            }

            if((!listeJoueurs.contains(commandSender.getName()) || !listeJoueurs.contains(args[2]))){
                commandSender.sendMessage(ChatColor.RED + "| EasyGroups | You are not in the same group as this player.");
                return;
            }

        }


        if(commandSender instanceof Player){
            Player player = (Player) commandSender;


            task2 = Bukkit.getScheduler().scheduleSyncRepeatingTask(EasyGroups.getPlugin(EasyGroups.class), new Runnable() {

                private double posXPLayerTracked;
                private double posZPlayerTracked;





                @Override
                public void run() {

                    if (!stop) {

                        File dataFolder = EasyGroups.getPlugin(EasyGroups.class).getDataFolder();
                        FileConfiguration groupsFile = YamlConfiguration.loadConfiguration(new File(dataFolder, "groups.yml"));

                        //On créer les variables pour la suite
                        String playerTrackedDirection = "";
                        double posXPlayer = player.getLocation().getX();
                        double posZPlayer = player.getLocation().getZ();
                        if (!home) {
                            Player playerTracked = Bukkit.getPlayerExact(args[2]);
                            double posXPLayerTracked = playerTracked.getLocation().getX();
                            double posZPlayerTracked = playerTracked.getLocation().getZ();
                            this.posXPLayerTracked = posXPLayerTracked;
                            this.posZPlayerTracked = posZPlayerTracked;
                        } else {
                            List<String> listeGroupesHomes = groupsFile.getStringList("EasyGroups.GroupHomes." + args[1] + ".homes." + args[2]);
                            List<String> listeGroupesHomesName = groupsFile.getStringList("EasyGroups.homesName." + args[1]);
                            List<String> listeJoueurs = groupsFile.getStringList("EasyGroups.groups." + args[1] + ".players");

                            if (listeGroupesHomesName.contains(args[2]) && listeJoueurs.contains(commandSender.getName())) {
                                this.posXPLayerTracked = Double.parseDouble(listeGroupesHomes.get(0));
                                this.posZPlayerTracked = Double.parseDouble(listeGroupesHomes.get(2));
                            }else{
                                commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This home doesn't exists.");
                                Bukkit.getScheduler().cancelTask(task2);
                                return;
                            }

                        }

                        //On met le scorboard au joueur
                        bossBar.addPlayer(player);//On affiche la bossBar au joueur de la commande

                        //On regarde ou se situe le joueur tracker par rapport au joueur qui a entrer la commande
                        if (posZPlayer > posZPlayerTracked && (int) posXPlayer == (int) posXPLayerTracked) {
                            playerTrackedDirection = "N";

                        } else if ((int) posZPlayer == (int) posZPlayerTracked && posXPlayer > posXPLayerTracked) {
                            playerTrackedDirection = "O";

                        } else if ((int) posZPlayer == (int) posZPlayerTracked && posXPlayer < posXPLayerTracked) {
                            playerTrackedDirection = "E";

                        } else if (posZPlayer < posZPlayerTracked && (int) posXPlayer == (int) posXPLayerTracked) {
                            playerTrackedDirection = "S";

                        } else if (posZPlayer > posZPlayerTracked && posXPlayer > posXPLayerTracked) {
                            playerTrackedDirection = "NO";

                        } else if (posZPlayer > posZPlayerTracked && posXPlayer < posXPLayerTracked) {
                            playerTrackedDirection = "NE";

                        } else if (posZPlayer < posZPlayerTracked && posXPlayer < posXPLayerTracked) {
                            playerTrackedDirection = "SE";

                        } else {
                            playerTrackedDirection = "SO";

                        }


                        //On récupère la direction où regarde le joueur
                        double visionAnglePlayer = player.getLocation().getYaw();

                        //Titre mis à jour
                        bossBar.setVisible(true);
                        if(home){
                            bossBar.setTitle("Home: " +args[2]+ "'s position");
                        }else{
                            bossBar.setTitle("Player: " +args[2]+ "'s position");
                        }


                        //On change la couleur en fonction de si le joueur regarde dans la bonne direction
                        switch (playerTrackedDirection) {
                            case "N":
                                if (visionAnglePlayer < -160 || visionAnglePlayer > 160) {
                                    bossBar.setColor(BarColor.GREEN);
                                    bossBar.setProgress(1);
                                } else if (visionAnglePlayer < -135 || visionAnglePlayer > 135) {
                                    bossBar.setColor(BarColor.BLUE);
                                    bossBar.setProgress(0.5);
                                } else if (visionAnglePlayer < -90 || visionAnglePlayer > 90) {
                                    bossBar.setColor(BarColor.YELLOW);
                                    bossBar.setProgress(0.25);
                                } else {
                                    bossBar.setColor(BarColor.RED);
                                    bossBar.setProgress(0.1);
                                }
                                ;
                                break;
                            case "NO":
                                if ((visionAnglePlayer > 115 && visionAnglePlayer < 160)) {
                                    bossBar.setColor(BarColor.GREEN);
                                    bossBar.setProgress(1);
                                } else if (visionAnglePlayer < -90 || visionAnglePlayer > 90) {
                                    bossBar.setColor(BarColor.BLUE);
                                    bossBar.setProgress(0.5);
                                } else if (visionAnglePlayer < -45 || visionAnglePlayer > 45) {
                                    bossBar.setColor(BarColor.YELLOW);
                                    bossBar.setProgress(0.25);
                                } else {
                                    bossBar.setColor(BarColor.RED);
                                    bossBar.setProgress(0.1);
                                }
                                ;
                                break;
                            case "NE":
                                if ((visionAnglePlayer < -125 && visionAnglePlayer > -160)) {
                                    bossBar.setColor(BarColor.GREEN);
                                    bossBar.setProgress(1);
                                } else if (visionAnglePlayer < -90 || visionAnglePlayer > 90) {
                                    bossBar.setColor(BarColor.BLUE);
                                    bossBar.setProgress(0.5);
                                } else if (visionAnglePlayer < -45 || visionAnglePlayer > 45) {
                                    bossBar.setColor(BarColor.YELLOW);
                                    bossBar.setProgress(0.25);
                                } else {
                                    bossBar.setColor(BarColor.RED);
                                    bossBar.setProgress(0.1);
                                }
                                ;
                                break;
                            case "O":
                                if ((visionAnglePlayer < 110 && visionAnglePlayer > 70)) {
                                    bossBar.setColor(BarColor.GREEN);
                                    bossBar.setProgress(1);
                                } else if ((visionAnglePlayer > 110 && visionAnglePlayer < 155) || visionAnglePlayer < 70 && visionAnglePlayer > 30) {
                                    bossBar.setColor(BarColor.BLUE);
                                    bossBar.setProgress(0.5);
                                } else if (visionAnglePlayer > 155 || (visionAnglePlayer < 30 && visionAnglePlayer > -10)) {
                                    bossBar.setColor(BarColor.YELLOW);
                                    bossBar.setProgress(0.25);
                                } else {
                                    bossBar.setColor(BarColor.RED);
                                    bossBar.setProgress(0.1);
                                }
                                ;
                                break;
                            case "E":
                                if ((visionAnglePlayer > -110 && visionAnglePlayer < -70)) {
                                    bossBar.setColor(BarColor.GREEN);
                                    bossBar.setProgress(1);
                                } else if ((visionAnglePlayer < -110 && visionAnglePlayer > -155) || visionAnglePlayer > -70 && visionAnglePlayer < -30) {
                                    bossBar.setColor(BarColor.BLUE);
                                    bossBar.setProgress(0.5);
                                } else if (visionAnglePlayer < -155 || (visionAnglePlayer > -30 && visionAnglePlayer < 10)) {
                                    bossBar.setColor(BarColor.YELLOW);
                                    bossBar.setProgress(0.25);
                                } else {
                                    bossBar.setColor(BarColor.RED);
                                    bossBar.setProgress(0.1);
                                }
                                break;
                            case "S":
                                if (visionAnglePlayer > -20 && visionAnglePlayer < 20) {
                                    bossBar.setColor(BarColor.GREEN);
                                    bossBar.setProgress(1);
                                } else if (visionAnglePlayer > -45 && visionAnglePlayer < 45) {
                                    bossBar.setColor(BarColor.BLUE);
                                    bossBar.setProgress(0.5);
                                } else if (visionAnglePlayer < -45 && visionAnglePlayer > -90 || visionAnglePlayer > 45 && visionAnglePlayer < 90) {
                                    bossBar.setColor(BarColor.YELLOW);
                                    bossBar.setProgress(0.25);
                                } else {
                                    bossBar.setColor(BarColor.RED);
                                    bossBar.setProgress(0.1);
                                }
                                break;
                            case "SO":
                                if ((visionAnglePlayer > 25 && visionAnglePlayer < 60)) {
                                    bossBar.setColor(BarColor.GREEN);
                                    bossBar.setProgress(1);
                                } else if (visionAnglePlayer < 25 && visionAnglePlayer > -15 || visionAnglePlayer > 60 && visionAnglePlayer < 100) {
                                    bossBar.setColor(BarColor.BLUE);
                                    bossBar.setProgress(0.5);
                                } else if (visionAnglePlayer < -15 && visionAnglePlayer > -55 || visionAnglePlayer > 100 && visionAnglePlayer < 140) {
                                    bossBar.setColor(BarColor.YELLOW);
                                    bossBar.setProgress(0.25);
                                } else {
                                    bossBar.setColor(BarColor.RED);
                                    bossBar.setProgress(0.1);
                                }
                                break;
                            case "SE":
                                if ((visionAnglePlayer < -20 && visionAnglePlayer > -70)) {
                                    bossBar.setColor(BarColor.GREEN);
                                    bossBar.setProgress(1);
                                } else if (visionAnglePlayer > -20 && visionAnglePlayer < 15 || visionAnglePlayer < -70 && visionAnglePlayer > -100) {
                                    bossBar.setColor(BarColor.BLUE);
                                    bossBar.setProgress(0.5);
                                } else if (visionAnglePlayer > 15 && visionAnglePlayer < 55 || visionAnglePlayer < -100 && visionAnglePlayer > -140) {
                                    bossBar.setColor(BarColor.YELLOW);
                                    bossBar.setProgress(0.25);
                                } else {
                                    bossBar.setColor(BarColor.RED);
                                    bossBar.setProgress(0.1);
                                }
                                break;
                        }
                    } else {
                        bossBar.setVisible(false);
                        bossBar.removeAll();
                        Bukkit.getScheduler().cancelTasks(EasyGroups.getPlugin(EasyGroups.class));
                    }
                }

            }, 0L, 10L); // 20 ticks = 1 seconde, donc 50 ticks = 2.5 secondes
        }
    }


    private void commandHome(CommandSender commandSender, String[] args, boolean stop) throws InterruptedException {
        if(args.length < 3){
            commandSender.sendMessage(ChatColor.RED +"| EasyGroups | This command needs more arguments to work.");
            return;
        }

        if(!commandSender.hasPermission("easygroups.easygroup.use") && !commandSender.isOp()){
            commandSender.sendMessage(ChatColor.RED +"| EasyGroups | You are not allow to perform this command.");
            return;
        }


        pluginConfigFile configYaml = new pluginConfigFile();

        if (commandSender instanceof Player){
            Player cmdSender = (Player) commandSender;
            List<String> listeJoueurs = this.groupsFile.getStringList("EasyGroups.groups."+args[1]+".players");//Liste des joueurs dans les groupes
            List<String> listeGroupes = this.groupsFile.getStringList("EasyGroups.groupList");//Liste des groupes
            List<String> listeGroupesHomes = this.groupsFile.getStringList("EasyGroups.GroupHomes."+args[1]+".homes."+args[2]);

            if(!listeGroupes.contains(args[1]) || !listeJoueurs.contains(commandSender.getName()) && !commandSender.isOp()){
                commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This group doesn't exists or you are not in this group !");
                return;
            }

            if(listeGroupesHomes.isEmpty()){//Si la liste est vide alors le home n'existe pas
                commandSender.sendMessage(ChatColor.RED + "| EasyGroups | This home doesn't exists in this group.");
                return;
            }

            if(cmdSender.getWorld().getName().equals(listeGroupesHomes.get(3))) {
                if (configYaml.homeTeleport) {//Si le /homeTeleport est activé dans le serveur alors on TP le joueur

                    Location tpLocation = new Location(cmdSender.getWorld(), Double.parseDouble(listeGroupesHomes.get(0)), Double.parseDouble(listeGroupesHomes.get(1)), Double.parseDouble(listeGroupesHomes.get(2)));
                    cmdSender.teleport(tpLocation);
                    commandSender.sendMessage(ChatColor.AQUA + "| EasyGroups | Successfully teleport you to this home.");
                    return;
                }

                commandCoordinates(commandSender, args, stop, true);
            }else{
                commandSender.sendMessage(ChatColor.RED + "| EasyGroups | You are not in the same world as this home.");
            }
        }
    }
}



