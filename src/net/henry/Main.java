package net.henry;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.wurmonline.mesh.Tiles;
import com.wurmonline.wurmapi.api.WurmAPI;

public class Main {

    public static void main(String[] args) {
        String directory = ".";
        String configPath = ".";
        for (String arg : args){
            if(arg.startsWith("-map=")){
                directory = arg.substring(5);
            }
            else if(arg.startsWith("-config=")){
                configPath=arg.substring(8);
            }
        }
        if(args.length != 2){
            System.out.println("Invalid argument count. Please specify the path to your map and config.");
            return;
        }
        Properties prop = new Properties();
        InputStream is = null;
        try {
            is = new FileInputStream(configPath);
        } catch (FileNotFoundException ex) {
        }
        try {
            prop.load(is);
        } catch (IOException ex) {
        }
        double percent;
        percent = Double.parseDouble((String) prop.get("percentage"));
        if(percent < 0 || percent > 1){
            System.out.println("Percent value needs to be between 0 and 1");
        }
        String replaceConfig = (String) prop.get("ids_to_replace");
        List<String> configStuff = Arrays.asList(replaceConfig.split(","));
        List<Integer> stuffToReplace = new ArrayList<>();
        for(String s : configStuff){
            stuffToReplace.add(Integer.valueOf(s));
        }
        if(stuffToReplace.isEmpty()){
            System.out.println("Nothing is configured to be replaced. Please specify in the config");
        }
        Integer replacementId = Integer.valueOf((String) prop.get("replacement_id"));
        System.out.println("Config loaded");
        System.out.println("Percentage: "+percent);
        System.out.println("Ids to replace"+stuffToReplace);
        System.out.println("Tiles will be replaced by: "+replacementId);
        try {
            Random rand = new Random(System.currentTimeMillis());
            WurmAPI api = WurmAPI.open(directory);
            if(api.getMapData() == null){
                System.out.println("Could not get map data");
                return;
            }
            System.out.println("Map Loaded..");

            int replaced = 0;
            for (int x = 0; x < api.getMapData().getWidth(); x++) {
                for (int y = 0; y < api.getMapData().getHeight(); y++) {
                    if (stuffToReplace.contains((api.getMapData().getSurfaceTile(x, y).getIntId())) && rand.nextDouble() < percent) {
                        api.getMapData().setSurfaceTile(x, y, Tiles.getTile(replacementId));
                        replaced++;
                    }
                }
            }
            api.getMapData().saveChanges(true);

            System.out.println(replaced + " tiles were replaced by "+replacementId);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}