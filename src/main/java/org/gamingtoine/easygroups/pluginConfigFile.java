//@author: gamingtoine

package org.gamingtoine.easygroups;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import sun.security.provider.ConfigFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class pluginConfigFile {

    //Variables
    final File dataFolder = EasyGroups.getPlugin(EasyGroups.class).getDataFolder();
    public boolean everyoneAddRemove;
    public boolean homeTeleport;
    public List<String> bannedWorlds;
    File configFile = new File(dataFolder, "config.yml");

    FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

    List<String> listeBannedWorlds = new ArrayList<>(10);


    public pluginConfigFile(){//Création du fichier de config
        if(!EasyGroups.getPlugin(EasyGroups.class).getDataFolder().exists()){//On creer le dossier pour le plugin si il existe pas
            EasyGroups.getPlugin(EasyGroups.class).getDataFolder().mkdirs();
        }
        createFiles("config.yml");
        this.homeTeleport = config.getBoolean("config.homeTeleport", false);
        this.bannedWorlds = config.getStringList("config.bannedWorlds");
        this.everyoneAddRemove = config.getBoolean("config.everyoneAddRemove", false);
    }

    public File getFile(String filename){
        return new File(dataFolder,filename);
    }

    public void createFiles(String name) {

        File groups = getFile(name);

        if (!groups.exists()) {
            try {
                groups.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Oups");

            //Les valeurs ainsi que les commentaires

            config.set("config.homeTeleport", this.homeTeleport);

            config.set("config.bannedWorlds", listeBannedWorlds);

            config.set("config.everyoneAddRemove", this.everyoneAddRemove);

            saveFile("config.yml");
        }
    }


    public List<String> setCom(String strings){
        List<String> temp = new ArrayList<>();
        temp.add(0, strings);
        return temp;
    }

    public void saveFile(String name){
        try {//Save fichier YAML
            config.save(getFile(name));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
