package util;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * 成就管理器类
 * 负责管理游戏中的成就系统，包括成就的创建、加载、保存和检查
 * 使用单例模式确保全局只有一个成就管理器实例
 * 成就数据以JSON格式持久化存储在data/achievements.json文件中
 */
public class AchievementManager {
    /**
     * 数据目录路径
     * 用于存储成就数据的文件夹名称
     */
    private static final String DATA_DIR = "data";
    
    /**
     * 成就数据文件路径
     * JSON格式的成就数据文件
     */
    private static final String ACHIEVEMENTS_FILE = DATA_DIR + "/achievements.json";
    
    /**
     * 总通关次数
     * 记录玩家总共通关蜘蛛纸牌的次数
     */
    private int totalWins;
    
    /**
     * 成就列表
     * 存储游戏中的所有成就信息
     */
    private List<Achievement> achievements;
    
    /**
     * 成就类
     * 表示游戏中的一个成就，包含成就的ID、名称、描述、需求和状态
     */
    public static class Achievement {
        /**
         * 成就的唯一标识符
         * 用于在系统中识别特定的成就
         */
        public String id;
        
        /**
         * 成就的名称
         * 显示给玩家的成就标题
         */
        public String name;
        
        /**
         * 成就的描述
         * 详细说明如何获得该成就
         */
        public String description;
        
        /**
         * 需要的通关次数
         * 玩家需要通关多少次才能解锁该成就
         */
        public int requiredWins;
        
        /**
         * 成就是否已解锁
         * true表示玩家已经解锁该成就，false表示还未解锁
         */
        public boolean unlocked;
        
        /**
         * 空构造函数
         * 用于JSON反序列化
         */
        public Achievement() {}
        
        /**
         * 构造函数
         * 创建一个新的成就对象
         * @param id 成就ID
         * @param name 成就名称
         * @param description 成就描述
         * @param requiredWins 需要的通关次数
         * @param unlocked 是否已解锁
         */
        public Achievement(String id, String name, String description, int requiredWins, boolean unlocked) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.requiredWins = requiredWins;
            this.unlocked = unlocked;
        }
    }
    
    /**
     * 成就管理器的单例实例
     * 确保全局只有一个成就管理器对象
     */
    private static AchievementManager instance;
    
    /**
     * 私有构造函数
     * 防止外部创建实例，使用单例模式
     */
    private AchievementManager() {
        achievements = new ArrayList<>();
        loadAchievements();
    }
    
    /**
     * 获取成就管理器的单例实例
     * 使用同步方法确保线程安全
     * @return 成就管理器的实例
     */
    public static synchronized AchievementManager getInstance() {
        if (instance == null) {
            instance = new AchievementManager();
        }
        return instance;
    }
    
    /**
     * 增加一次通关记录
     * 更新总通关次数，检查并解锁可能达成的成就，然后保存数据
     */
    public void addWin() {
        totalWins++;
        checkAchievements();
        saveAchievements();
    }
    
    /**
     * 获取总通关次数
     * @return 玩家总共通关的次数
     */
    public int getTotalWins() {
        return totalWins;
    }
    
    /**
     * 获取所有成就
     * @return 所有成就的列表副本
     */
    public List<Achievement> getAchievements() {
        return new ArrayList<>(achievements);
    }
    
    /**
     * 获取已解锁的成就
     * @return 已解锁成就的列表
     */
    public List<Achievement> getUnlockedAchievements() {
        List<Achievement> unlocked = new ArrayList<>();
        for (Achievement a : achievements) {
            if (a.unlocked) {
                unlocked.add(a);
            }
        }
        return unlocked;
    }
    
    /**
     * 获取未解锁的成就
     * @return 未解锁成就的列表
     */
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
