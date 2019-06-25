package net.henry;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import com.wurmonline.mesh.*;
import com.wurmonline.wurmapi.api.WurmAPI;

public class Main {

    public static void main(String[] args) {
        System.out.println("Starting Tile Regulator v1.2");
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
        String sourceConfig = (String) prop.get("source_ids");
        List<String> sourceIds = Arrays.asList(sourceConfig.split(","));
        List<Integer> stuffToReplace = new ArrayList<>();
        for(String s : sourceIds){
            stuffToReplace.add(Integer.valueOf(s));
        }
        if(stuffToReplace.isEmpty()){
            System.out.println("Nothing is configured to be replaced. Please specify in the config");
        }
        String targetConfig = (String) prop.get("target_ids");
        List<String> targetIds = Arrays.asList(targetConfig.split(","));
        List<Integer> stuffToPut = new ArrayList<>();
        for(String s : targetIds){
            stuffToPut.add(Integer.valueOf(s));
        }
        if(stuffToPut.isEmpty()){
            System.out.println("Nothing is configured to be used as replacement. Please specify in the config");
        }
        System.out.println("Config loaded");
        System.out.println("Percentage: "+percent*100+" %");
        System.out.println("Tiles to replace: "+stuffToReplace);
        System.out.println("Tiles will be replaced by: "+stuffToPut);
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
                        int id;
                        if(stuffToPut.size() == 1){
                            id = stuffToPut.get(0);
                        } else{
                            id = stuffToPut.get(rand.nextInt(stuffToPut.size()-1));
                        }
                        Tiles.Tile t = Tiles.getTile(id);
                        if(t.isBush()){
                            api.getMapData().setBush(x,y, BushData.BushType.fromTileData(id), FoliageAge.fromByte((byte) rand.nextInt(7)), GrassData.GrowthTreeStage.decodeTileData(id));
                        } else if (t.isTree()){
                            api.getMapData().setTree(x,y, TreeData.TreeType.fromTileData(id), FoliageAge.fromByte((byte) rand.nextInt(7)), GrassData.GrowthTreeStage.decodeTileData(id));
                        }
                        else{
                            api.getMapData().setSurfaceTile(x, y, Tiles.getTile(id));
                        }
                        replaced++;
                    }
                }
            }
            api.getMapData().saveChanges(true);

            System.out.println(replaced + " tiles were replaced by "+stuffToPut);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}