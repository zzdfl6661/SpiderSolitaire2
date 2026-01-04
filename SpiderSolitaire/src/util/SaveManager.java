package util;

import game.GameState;
import java.io.*;

/**
 * 保存管理器类
 * 负责将游戏状态保存到文件，以及从文件加载游戏状态
 * 使用Java对象序列化机制实现游戏进度的持久化存储
 * 支持玩家中断游戏后再次恢复游戏
 */
public class SaveManager {

    /**
     * 保存游戏状态到文件
     * 将当前游戏状态序列化为二进制格式并保存到"save.dat"文件
     * @param state 要保存的游戏状态对象
     * @throws IOException 如果写入文件时发生I/O错误
     */
    public static void save(GameState state) throws IOException {
        // 创建文件输出流，用于将数据写入文件
        ObjectOutputStream out =
                new ObjectOutputStream(new FileOutputStream("save.dat"));
        
        // 将游戏状态对象写入文件（序列化）
        out.writeObject(state);
        
        // 关闭输出流，确保数据完全写入
        out.close();
    }

    /**
     * 从文件加载游戏状态
     * 读取"save.dat"文件中的数据并反序列化为GameState对象
     * @return 从文件中加载的游戏状态对象
     * @throws IOException 如果读取文件时发生I/O错误
     * @throws ClassNotFoundException 如果找不到对应的类定义
     */
    public static GameState load() throws IOException, ClassNotFoundException {
        // 创建文件输入流，用于从文件读取数据
        ObjectInputStream in =
                new ObjectInputStream(new FileInputStream("save.dat"));
        
        // 从文件中读取对象并强制类型转换为GameState（反序列化）
        return (GameState) in.readObject();
    }
}
