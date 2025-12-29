package util;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class AchievementManager {
    private static final String DATA_DIR = "data";
    private static final String ACHIEVEMENTS_FILE = DATA_DIR + "/achievements.json";
    
    private int totalWins;
    private List<Achievement> achievements;
    
    public static class Achievement {
        public String id;
        public String name;
        public String description;
        public int requiredWins;
        public boolean unlocked;
        
        public Achievement() {}
        
        public Achievement(String id, String name, String description, int requiredWins, boolean unlocked) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.requiredWins = requiredWins;
            this.unlocked = unlocked;
        }
    }
    
    private static AchievementManager instance;
    
    private AchievementManager() {
        achievements = new ArrayList<>();
        loadAchievements();
    }
    
    public static synchronized AchievementManager getInstance() {
        if (instance == null) {
            instance = new AchievementManager();
        }
        return instance;
    }
    
    public void addWin() {
        totalWins++;
        checkAchievements();
        saveAchievements();
    }
    
    public int getTotalWins() {
        return totalWins;
    }
    
    public List<Achievement> getAchievements() {
        return new ArrayList<>(achievements);
    }
    
    public List<Achievement> getUnlockedAchievements() {
        List<Achievement> unlocked = new ArrayList<>();
        for (Achievement a : achievements) {
            if (a.unlocked) {
                unlocked.add(a);
            }
        }
        return unlocked;
    }
    
    public List<Achievement> getLockedAchievements() {
        List<Achievement> locked = new ArrayList<>();
        for (Achievement a : achievements) {
            if (!a.unlocked) {
                locked.add(a);
            }
        }
        return locked;
    }
    
    private void checkAchievements() {
        for (Achievement a : achievements) {
            if (!a.unlocked && totalWins >= a.requiredWins) {
                a.unlocked = true;
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadAchievements() {
        try {
            File dataDir = new File(DATA_DIR);
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            
            File file = new File(ACHIEVEMENTS_FILE);
            if (!file.exists()) {
                createDefaultAchievements();
                saveAchievements();
                return;
            }
            
            String content = new String(Files.readAllBytes(file.toPath()));
            StringBuilder json = new StringBuilder();
            
            for (char c : content.toCharArray()) {
                if (c == '\n' || c == '\r' || c == ' ' || c == '\t') {
                    continue;
                }
                json.append(c);
            }
            
            int totalWinsIndex = json.indexOf("\"totalWins\"");
            if (totalWinsIndex != -1) {
                int colonIndex = json.indexOf(":", totalWinsIndex);
                int commaIndex = json.indexOf(",", colonIndex);
                if (commaIndex == -1) commaIndex = json.indexOf("}", colonIndex);
                String winsStr = json.substring(colonIndex + 1, commaIndex);
                totalWins = Integer.parseInt(winsStr.trim());
            }
            
            achievements.clear();
            int achievementsIndex = json.indexOf("\"achievements\"");
            if (achievementsIndex != -1) {
                int arrayStart = json.indexOf("[", achievementsIndex);
                int arrayEnd = json.lastIndexOf("]");
                
                String achievementsJson = json.substring(arrayStart, arrayEnd + 1);
                int braceIndex = 0;
                int startIndex = 0;
                
                while ((startIndex = achievementsJson.indexOf("{", startIndex)) != -1) {
                    int endIndex = achievementsJson.indexOf("}", startIndex);
                    String objJson = achievementsJson.substring(startIndex, endIndex + 1);
                    
                    Achievement a = parseAchievement(objJson);
                    if (a != null) {
                        achievements.add(a);
                    }
                    startIndex = endIndex + 1;
                }
            }
            
            if (achievements.isEmpty()) {
                createDefaultAchievements();
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            createDefaultAchievements();
        }
    }
    
    private Achievement parseAchievement(String json) {
        try {
            Achievement a = new Achievement();
            
            int idIndex = json.indexOf("\"id\"");
            int nameIndex = json.indexOf("\"name\"");
            int descIndex = json.indexOf("\"description\"");
            int reqIndex = json.indexOf("\"requiredWins\"");
            int unlIndex = json.indexOf("\"unlocked\"");
            
            if (idIndex != -1) {
                int colon = json.indexOf(":", idIndex);
                int quote1 = json.indexOf("\"", colon);
                int quote2 = json.indexOf("\"", quote1 + 1);
                a.id = json.substring(quote1 + 1, quote2);
            }
            
            if (nameIndex != -1) {
                int colon = json.indexOf(":", nameIndex);
                int quote1 = json.indexOf("\"", colon);
                int quote2 = json.indexOf("\"", quote1 + 1);
                a.name = json.substring(quote1 + 1, quote2);
            }
            
            if (descIndex != -1) {
                int colon = json.indexOf(":", descIndex);
                int quote1 = json.indexOf("\"", colon);
                int quote2 = json.indexOf("\"", quote1 + 1);
                a.description = json.substring(quote1 + 1, quote2);
            }
            
            if (reqIndex != -1) {
                int colon = json.indexOf(":", reqIndex);
                int comma = json.indexOf(",", colon);
                if (comma == -1) comma = json.indexOf("}", colon);
                String reqStr = json.substring(colon + 1, comma);
                a.requiredWins = Integer.parseInt(reqStr.trim());
            }
            
            if (unlIndex != -1) {
                int colon = json.indexOf(":", unlIndex);
                String val = json.substring(colon + 1, colon + 5).trim();
                a.unlocked = val.startsWith("true");
            }
            
            return a;
        } catch (Exception e) {
            return null;
        }
    }
    
    private void createDefaultAchievements() {
        achievements.clear();
        achievements.add(new Achievement("newbie", "纸牌新手", "通关3次", 3, false));
        achievements.add(new Achievement("master", "纸牌大师", "通关10次", 10, false));
        achievements.add(new Achievement("king", "纸牌王者", "通关50次", 50, false));
        totalWins = 0;
    }
    
    public void saveAchievements() {
        try {
            File dataDir = new File(DATA_DIR);
            if (!dataDir.exists()) {
                dataDir.mkdirs();
            }
            
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"totalWins\":").append(totalWins).append(",");
            json.append("\"achievements\":[");
            
            for (int i = 0; i < achievements.size(); i++) {
                Achievement a = achievements.get(i);
                json.append("{");
                json.append("\"id\":\"").append(a.id).append("\",");
                json.append("\"name\":\"").append(a.name).append("\",");
                json.append("\"description\":\"").append(a.description).append("\",");
                json.append("\"requiredWins\":").append(a.requiredWins).append(",");
                json.append("\"unlocked\":").append(a.unlocked);
                json.append("}");
                if (i < achievements.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]");
            json.append("}");
            
            Files.write(new File(ACHIEVEMENTS_FILE).toPath(), json.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void resetAchievements() {
        totalWins = 0;
        for (Achievement a : achievements) {
            a.unlocked = false;
        }
        saveAchievements();
    }
}
