//@author: gamingtoine
//Version : 1.0



package org.gamingtoine.easygroups;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.gamingtoine.easygroups.commands.Group;
import org.gamingtoine.easygroups.commands.commandsEasyGroups;
import org.gamingtoine.easygroups.commands.commandsEasyGroupsTabCompleter;
import org.gamingtoine.easygroups.commands.groupsTabCompleter;

import java.io.File;
import java.io.IOException;

public class EasyGroups extends JavaPlugin {

    public File dataFolder = getDataFolder();


    @Override
    public void onEnable() {
        System.out.println("Plugin EasyGroup is enable on version 1.0"); //Démarrage Plugin

        new pluginConfigFile();


        //Creation du dossier des fichiers de configuration
        if(!getDataFolder().exists()){
            getDataFolder().mkdirs();
        }


        createFiles("groups.yml");

        FileConfiguration groups = YamlConfiguration.loadConfiguration(getFile("groups.yml"));


        //Lecture des commandes
        getCommand("group").setExecutor(new Group(groups,getDataFolder()));//Quand on fait la commande on execute la classe Gourp

        getCommand("Group").setTabCompleter(new groupsTabCompleter());

        getCommand("easygroups").setExecutor(new commandsEasyGroups(groups,getDataFolder()));

        getCommand("easygroups").setTabCompleter(new commandsEasyGroupsTabCompleter());


    }

    FileConfiguration groupsFile;
    public EasyGroups(){
        this.groupsFile = YamlConfiguration.loadConfiguration(getFile("groups.yml"));
        this.dataFolder = getDataFolder();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        System.out.println(ChatColor.GOLD + "|EasyGroups | Plugin EasyGroup is successfully disable. Please leave a report if you have seen a bug.");
    }

    public File getFile(String filename){
        return new File(getDataFolder(),filename);
    }


    public void createFiles(String name) {

        File groups = getFile(name);

        if(!groups.exists()){
            try {
                groups.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

    }
}

