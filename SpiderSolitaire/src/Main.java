import ui.GameFrame;

/**
用程序的启动点，负责启动蜘蛛纸牌游戏的主界面
 * 应用程序启动流程：
 * 1. JVM加载Main类并执行main方法
 * 2. main方法调用GameFrame.main(null)
 * 3. GameFrame类创建游戏窗口和用户界面
 * 4. 显示难度选择对话框
 * 5. 玩家选择难度后开始游戏
 */
public class Main {
    /**
     * @param args 启动时传入的命令行参数，数组中每个元素都是一个字符串
     *              在本程序中未使用这些参数，所以传入null
     */
    public static void main(String[] args) {
        // 调用GameFrame类的静态main方法
        // 传入null表示没有命令行参数
        // GameFrame.main方法会显示难度选择对话框并启动游戏
        GameFrame.main(null);
    }
}
