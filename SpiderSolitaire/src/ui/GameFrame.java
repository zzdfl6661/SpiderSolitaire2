package ui;

import game.GameState;
import game.SpiderGame;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Stack;
import javax.swing.*;
import model.Card;
import util.AchievementManager;
import util.SaveManager;

/**
 * 游戏主窗口类
 * 负责创建游戏界面、处理用户交互、显示游戏状态和成就系统
 */
public class GameFrame extends JFrame {
    /**
     * 程序入口点
     * 这是Java应用程序的标准入口方法，JVM首先调用此方法来启动程序
     * @param args 命令行参数数组，程序启动时可以通过命令行传递参数
     */
    public static void main(String[] args) {
        // 在事件分发线程中创建GUI，确保线程安全
        /**
         *  Lambda表达式语法
         * () -> 是Java Lambda表达式的语法，由三部分组成：
         * - 左侧参数列表 ： () 表示没有参数
         * - 箭头运算符 ： -> 分隔参数和表达式主体
         * - 右侧表达式主体 ：执行的操作
         * 
         * SwingUtilities.invokeLater() 确保GUI组件在事件分发线程中创建，
         * 这是Swing线程安全的要求，避免多线程访问UI组件导致的并发问题
         */
        SwingUtilities.invokeLater(() -> {
            // 检查是否存在保存的游戏文件
            GameState loadedState = null;
            boolean hasSavedGame = new java.io.File("save.dat").exists();
            
            if (hasSavedGame) {
                // 询问用户是否要加载保存的游戏
                int choice = JOptionPane.showOptionDialog(null, 
                    "检测到保存的游戏记录！\n\n" +
                    "是否要加载保存的游戏？\n\n" +
                    "是 - 加载保存的游戏\n" +
                    "否 - 开始新游戏",
                    "加载游戏", 
                    JOptionPane.YES_NO_OPTION, 
                    JOptionPane.QUESTION_MESSAGE, 
                    null, 
                    null, 
                    null);
                
                if (choice == JOptionPane.YES_OPTION) {
                    try {
                        // 尝试加载保存的游戏状态
                        loadedState = SaveManager.load();
                    } catch (Exception ex) {
                        // 加载失败，显示错误消息并开始新游戏
                        JOptionPane.showMessageDialog(null, 
                            "加载游戏失败: " + ex.getMessage() + "\n将开始新游戏", 
                            "错误", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            
            if (loadedState != null) {
                // 加载保存的游戏
                // 调用GameFrame(GameState)构造函数
                GameFrame frame = new GameFrame(loadedState);
                frame.setVisible(true);
            } else {
                // 创建难度选择对话框，开始新游戏
                String[] options = {"单花色（简单）", "双花色（中等）", "四花色（困难）"};
                int choice = JOptionPane.showOptionDialog(null, 
                    "请选择游戏难度：\n\n" +
                    "单花色：全部使用一种花色，最简单\n" +
                    "双花色：使用两种花色，中等难度\n" +
                    "四花色：使用四种花色，最困难",
                    "选择难度", 
                    JOptionPane.DEFAULT_OPTION, 
                    JOptionPane.QUESTION_MESSAGE, 
                    null, 
                    options, 
                    options[0]);

                // 根据用户选择设置游戏难度
                int difficulty;
                switch (choice) {
                    case 0: difficulty = 1; break; // 单花色
                    case 1: difficulty = 2; break; // 双花色
                    case 2: difficulty = 4; break; // 四花色
                    default: difficulty = 1; break; // 默认单花色
                }
                
                // 创建并显示游戏窗口
                // 调用GameFrame(int)构造函数
                GameFrame frame = new GameFrame(difficulty);
                frame.setVisible(true);
            }
        });
    }
    
    /**
     * 游戏核心逻辑对象，负责处理游戏规则和状态管理
     */
    private final SpiderGame game;
    
    /**
     * 鼠标位置跟踪，用于拖拽操作
     */
    private final Point mousePoint = new Point(0, 0);
    
    /**
     * 当前正在拖拽的牌
     */
    private Card draggedCard = null;
    
    /**
     * 拖拽操作中牌来源的列索引
     */
    private int draggedColumn = -1;
    
    /**
     * 拖拽操作中要移动的牌的数量
     */
    private int draggedCount = 1;

    /**
     * 游戏窗口构造函数
     * 初始化游戏界面，包括游戏对象、窗口属性、按钮面板和游戏棋盘
     * @param difficulty 游戏难度级别：1=单花色，2=双花色，4=四花色
     */
    public GameFrame(int difficulty) {
        // 创建游戏对象，传递难度参数给SpiderGame类
        game = new SpiderGame(difficulty);

        // 根据难度设置窗口标题
        String difficultyName;
        if (difficulty == 1) difficultyName = "单花色";
        else if (difficulty == 2) difficultyName = "双花色";
        else difficultyName = "四花色";
        
        // 设置窗口基本属性
        // setTitle() 设置窗口标题栏显示的文本
        setTitle("蜘蛛纸牌 - " + difficultyName);
        // setSize() 设置窗口大小，单位为像素
        setSize(1000, 600);
        // setDefaultCloseOperation() 设置点击关闭按钮时的行为，EXIT_ON_CLOSE表示退出程序
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // 修改为DO_NOTHING_ON_CLOSE以便手动处理关闭事件
        
        // 添加窗口关闭事件监听器
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 窗口关闭前自动保存游戏
                try {
                    SaveManager.save(game.getState());
                } catch (IOException ex) {
                    System.err.println("自动保存失败: " + ex.getMessage());
                }
                
                // 然后退出程序
                System.exit(0);
            }
        });
        // setLayout() 设置窗口的布局管理器，BorderLayout将窗口分为五个区域
        setLayout(new BorderLayout());

        // 创建底部按钮面板
        // JPanel是Swing中的容器组件，用于放置其他组件并管理它们的布局
        JPanel panel = new JPanel();
        
        // 创建撤销按钮
        // JButton是Swing中的按钮组件，可以显示文本并响应点击事件
        JButton undoBtn = new JButton("撤销");
        // 创建发牌按钮，显示剩余发牌次数
        // 按钮文本显示当前剩余的发牌次数，格式为"发牌(X)"，其中X是剩余次数
        JButton dealBtn = new JButton("发牌(" + game.getState().remainingDeals + ")");
        
        // 撤销按钮事件监听器
        // addActionListener()方法为按钮添加事件监听器，当按钮被点击时触发
        undoBtn.addActionListener(e -> {
            // 执行撤销操作，恢复到上一个游戏状态
            game.undo(); 
            // 更新发牌按钮的剩余次数显示
            // 撤销操作可能会恢复发牌次数，所以需要更新按钮文本
            dealBtn.setText("发牌(" + game.getState().remainingDeals + ")");
            // 重绘界面，更新显示内容
            repaint(); 
        });
        
        // 发牌按钮事件监听器
        dealBtn.addActionListener(e -> {
            // 调用游戏的发牌方法，传入false表示不是撤销操作
            if (!game.deal()) {
                // 发牌失败时显示错误信息
                // JOptionPane是Swing中的对话框组件，用于显示消息和用户交互
                JOptionPane.showMessageDialog(this, "发牌失败！请确保每列至少有一张牌，并且牌堆中有足够的牌。");
            } else {
                // 成功发牌后更新按钮文本显示剩余次数
                // 每发一次牌，remainingDeals减1
                dealBtn.setText("发牌(" + game.getState().remainingDeals + ")");
            }
            // 重绘界面显示更新后的状态
            repaint(); 
        });
        
        // 创建提示按钮
        // hintBtn按钮用于显示当前游戏的提示信息
        JButton hintBtn = new JButton("提示");
        hintBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, game.hint()));
        
        // 创建成就按钮
        // achievementBtn按钮用于打开成就对话框，显示玩家的成就信息
        JButton achievementBtn = new JButton("成就");
        achievementBtn.addActionListener(e -> new AchievementDialog(this).setVisible(true));
        
        // 创建保存游戏按钮
        // saveBtn按钮用于将当前游戏状态保存到文件
        JButton saveBtn = new JButton("保存游戏");
        saveBtn.addActionListener(e -> {
            try {
                // 调用SaveManager的静态方法保存游戏状态到文件
                // 传入当前游戏状态对象，序列化为二进制格式保存到save.dat文件
                SaveManager.save(game.getState());
                // 保存成功后显示确认消息
                JOptionPane.showMessageDialog(this, "游戏已保存到 save.dat 文件！");
            } catch (IOException ex) {
                // 如果保存过程中发生I/O错误，显示错误消息
                // JOptionPane.ERROR_MESSAGE表示显示错误图标
                JOptionPane.showMessageDialog(this, "保存失败: " + ex.getMessage(), 
                    "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // 创建新游戏按钮
        // newGameBtn按钮用于开始一个新的游戏，可以重新选择难度
        JButton newGameBtn = new JButton("新游戏");
        newGameBtn.addActionListener(e -> {
            // 在开始新游戏前，先保存当前游戏
            try {
                SaveManager.save(game.getState());
            } catch (IOException ex) {
                System.err.println("自动保存失败: " + ex.getMessage());
            }
            
            // 创建新游戏对话框
            // showOptionDialog()显示选项对话框，返回用户选择的选项索引
            String[] options = {"单花色（简单）", "双花色（中等）", "四花色（困难）"};
            int choice = JOptionPane.showOptionDialog(this, 
                "请选择新游戏难度：\n\n" +
                "单花色：全部使用一种花色，最简单\n" +
                "双花色：使用两种花色，中等难度\n" +
                "四花色：使用四种花色，最困难",
                "新游戏", 
                JOptionPane.DEFAULT_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                null, 
                options, 
                options[0]);

            // 根据用户选择设置游戏难度
            int newDifficulty;
            switch (choice) {
                case 0: newDifficulty = 1; break; // 单花色
                case 1: newDifficulty = 2; break; // 双花色
                case 2: newDifficulty = 4; break; // 四花色
                default: return; // 用户点击取消按钮或关闭对话框，不创建新游戏
            }
            
            // 创建新游戏窗口
            // dispose()方法释放当前窗口占用的系统资源
            this.dispose(); 
            // 创建新的游戏窗口实例，传递用户选择的难度
            GameFrame newFrame = new GameFrame(newDifficulty);
            newFrame.setVisible(true);
        });
        
        // 将按钮添加到面板中
        // JPanel使用FlowLayout布局管理器，默认从左到右排列组件
        panel.add(undoBtn);      // 添加撤销按钮
        panel.add(dealBtn);      // 添加发牌按钮
        panel.add(hintBtn);      // 添加提示按钮
        panel.add(achievementBtn); // 添加成就按钮
        panel.add(saveBtn);      // 添加保存游戏按钮
        panel.add(newGameBtn);   // 添加新游戏按钮
        
        // 将按钮面板添加到窗口底部
        // BorderLayout.SOUTH表示将面板放在窗口的南部（底部）区域
        add(panel, BorderLayout.SOUTH);

        // 创建并添加游戏棋盘面板到窗口中央
        // GameBoard是GameFrame的内部类，负责绘制游戏界面和处理鼠标事件
        GameBoard board = new GameBoard(game);
        // BorderLayout.CENTER表示将游戏棋盘放在窗口的中央区域，占据大部分空间
        add(board, BorderLayout.CENTER);
    }

    /**
     * 构造函数 - 从保存的游戏状态创建窗口
     * 用于加载之前保存的游戏状态
     * @param savedState 之前保存的游戏状态对象
     */
    public GameFrame(GameState savedState) {
        // 创建游戏对象，传递保存的状态
        game = new SpiderGame(savedState);

        // 设置窗口基本属性
        setTitle("蜘蛛纸牌 - 已保存的游戏");
        setSize(1000, 600);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE); // 修改为DO_NOTHING_ON_CLOSE以便手动处理关闭事件
        
        // 添加窗口关闭事件监听器
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // 窗口关闭前自动保存游戏
                try {
                    SaveManager.save(game.getState());
                } catch (IOException ex) {
                    System.err.println("自动保存失败: " + ex.getMessage());
                }
                
                // 然后退出程序
                System.exit(0);
            }
        });
        setLayout(new BorderLayout());

        // 创建底部按钮面板
        JPanel panel = new JPanel();
        
        // 创建撤销按钮
        JButton undoBtn = new JButton("撤销");
        // 创建发牌按钮，显示剩余发牌次数
        JButton dealBtn = new JButton("发牌(" + game.getState().remainingDeals + ")");
        
        // 撤销按钮事件监听器
        undoBtn.addActionListener(e -> {
            game.undo(); // 执行撤销操作
            // 更新发牌按钮的剩余次数显示
            dealBtn.setText("发牌(" + game.getState().remainingDeals + ")");
            repaint(); // 重绘界面
        });
        
        // 发牌按钮事件监听器
        dealBtn.addActionListener(e -> {
            if (!game.deal()) {
                // 发牌失败时显示错误信息
                JOptionPane.showMessageDialog(this, "发牌失败！请确保每列至少有一张牌，并且牌堆中有足够的牌。");
            } else {
                // 成功发牌后更新按钮文本显示剩余次数
                dealBtn.setText("发牌(" + game.getState().remainingDeals + ")");
            }
            repaint(); // 重绘界面显示更新后的状态
        });
        
        // 创建提示按钮
        JButton hintBtn = new JButton("提示");
        hintBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, game.hint()));
        
        // 创建成就按钮
        JButton achievementBtn = new JButton("成就");
        achievementBtn.addActionListener(e -> new AchievementDialog(this).setVisible(true));
        
        // 创建保存游戏按钮
        JButton saveBtn = new JButton("保存游戏");
        saveBtn.addActionListener(e -> {
            try {
                SaveManager.save(game.getState());
                JOptionPane.showMessageDialog(this, "游戏已保存到 save.dat 文件！");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "保存失败: " + ex.getMessage(), 
                    "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // 创建新游戏按钮
        JButton newGameBtn = new JButton("新游戏");
        newGameBtn.addActionListener(e -> {
            String[] options = {"单花色（简单）", "双花色（中等）", "四花色（困难）"};
            int choice = JOptionPane.showOptionDialog(this, 
                "请选择新游戏难度：\n\n" +
                "单花色：全部使用一种花色，最简单\n" +
                "双花色：使用两种花色，中等难度\n" +
                "四花色：使用四种花色，最困难",
                "新游戏", 
                JOptionPane.DEFAULT_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                null, 
                options, 
                options[0]);

            int newDifficulty;
            switch (choice) {
                case 0: newDifficulty = 1; break;
                case 1: newDifficulty = 2; break;
                case 2: newDifficulty = 4; break;
                default: return;
            }
            
            this.dispose(); 
            GameFrame newFrame = new GameFrame(newDifficulty);
            newFrame.setVisible(true);
        });
        
        // 将按钮添加到面板中
        panel.add(undoBtn);      
        panel.add(dealBtn);      
        panel.add(hintBtn);      
        panel.add(achievementBtn); 
        panel.add(saveBtn);      
        panel.add(newGameBtn);   
        
        add(panel, BorderLayout.SOUTH);

        // 创建并添加游戏棋盘面板到窗口中央
        GameBoard board = new GameBoard(game);
        add(board, BorderLayout.CENTER);
    }

    /**
     * 游戏棋盘内部类
     * 负责绘制游戏界面、处理鼠标事件、显示牌面等
     * GameBoard继承自JPanel，作为自定义的游戏面板组件
     */
    private class GameBoard extends JPanel {
        /**
         * 游戏对象引用，用于访问游戏状态和逻辑
         * final关键字确保在GameBoard生命周期内不会改变对游戏对象的引用
         */
        private final SpiderGame game;

        /**
         * 游戏棋盘构造函数
         * 初始化游戏棋盘，设置面板大小和事件监听器
         * @param game 游戏对象，用于访问游戏状态和执行游戏操作
         */
        public GameBoard(SpiderGame game) {
            // 保存游戏对象引用
            this.game = game;
            // 设置棋盘首选大小
            // Dimension类表示组件的宽度和高度，单位为像素
            setPreferredSize(new Dimension(800, 500));
            // 添加鼠标事件监听器
            // MouseAdapter是鼠标事件的适配器类，提供了空的鼠标事件处理方法
            addMouseListener(new MouseAdapter() {
                /**
                 * 鼠标按下事件处理
                 * 开始拖拽操作，识别用户点击的是哪张牌
                 */
                @Override
                public void mousePressed(MouseEvent e) {
                    // 获取鼠标点击位置对应的列索引
                    draggedColumn = getColumnAt(e.getPoint());
                    
                    // 如果点击位置在有效范围内
                    if (draggedColumn != -1) {
                        Stack<Card> column = game.getState().columns[draggedColumn];
                        
                        // 如果该列为空，返回
                        if (column.isEmpty()) return;
                        
                        int clickY = e.getY(); // 鼠标Y坐标
                        int cardIndex = -1;    // 点击的牌的索引
                        
                        // 从后往前遍历列中的牌，找到用户点击的牌
                        for (int j = column.size() - 1; j >= 0; j--) {
                            int cardY; // 当前牌的Y坐标
                            
                            if (j == 0) {
                                // 第一张牌的Y坐标固定为50
                                cardY = 50;
                            } else {
                                Card prevCard = column.get(j - 1);
                                if (prevCard.isFaceUp()) {
                                    // 如果前面的牌是正面朝上，间距较小
                                    cardY = 50 + (j - 1) * 25 + 25;
                                } else {
                                    // 如果前面的牌是背面朝上，间距较大
                                    cardY = 50 + j * 25;
                                }
                            }
                            
                            // 检查鼠标点击是否在该牌的范围内
                            if (clickY >= cardY && clickY <= cardY + 90) {
                                cardIndex = j;
                                break;
                            }
                        }
                        
                        // 如果点击的是正面朝上的牌，开始拖拽
                        if (cardIndex != -1 && column.get(cardIndex).isFaceUp()) {
                            draggedCard = column.get(cardIndex);
                            draggedCount = 1;
                            
                            // 计算可以拖拽的连续牌组数量（必须是连续递减的）
                            for (int i = cardIndex; i < column.size() - 1; i++) {
                                Card current = column.get(i);
                                Card next = column.get(i + 1);
                                if (current.getRank() == next.getRank() + 1) {
                                    draggedCount++;
                                } else {
                                    break; // 不是连续递减时停止
                                }
                            }
                        } else {
                            // 点击的不是有效的牌，清空拖拽状态
                            draggedCard = null;
                            draggedCount = 1;
                        }
                    }
                }

                /**
                 * 鼠标释放事件处理
                 * 完成拖拽操作，执行牌的移动或取消
                 */
                @Override
                public void mouseReleased(MouseEvent e) {
                    // 如果正在进行拖拽操作
                    if (draggedCard != null) {
                        // 获取鼠标释放位置对应的列索引
                        int targetColumn = getColumnAt(e.getPoint());
                        
                        // 如果目标列有效且不是源列，执行移动
                        if (targetColumn != -1 && targetColumn != draggedColumn) {
                            game.move(draggedColumn, targetColumn, draggedCount);
                            game.checkAndRemoveCompleteSets();
                            
                            // 检查是否获胜
                            if (game.isGameWon()) {
                                // 增加胜利次数记录
                                AchievementManager.getInstance().addWin();
                                StringBuilder message = new StringBuilder("恭喜！你赢了！\n\n");
                                message.append("这是你第 ").append(AchievementManager.getInstance().getTotalWins()).append(" 次通关！\n\n");
                                
                                // 检查是否获得新成就
                                for (AchievementManager.Achievement a : AchievementManager.getInstance().getUnlockedAchievements()) {
                                    if (!message.toString().contains(a.name)) {
                                        message.append("★ 获得成就: ").append(a.name).append("\n");
                                    }
                                }
                                
                                // 显示胜利信息和成就
                                JOptionPane.showMessageDialog(GameFrame.this, message.toString());
                            }
                        }
                        
                        // 清除拖拽状态
                        draggedCard = null;
                        draggedColumn = -1;
                        repaint(); // 重绘界面
                    }
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    mousePoint.setLocation(e.getPoint());
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(new Color(0, 100, 0));
            g.fillRect(0, 0, getWidth(), getHeight());
            
            int completedSets = game.getState().completedSets;
            int score = game.getState().score;
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("已完成: " + completedSets + "/8", 30, 25);
            g.drawString("分数: " + score, 300, 25);
            
            int completedAreaX = 120;
            int completedAreaY = 15;
            for (int i = 0; i < 8; i++) {
                g.setColor(i < completedSets ? new Color(255, 255, 0) : new Color(200, 200, 200));
                g.fillRect(completedAreaX + i * 20, completedAreaY, 15, 15);
                g.setColor(Color.BLACK);
                g.drawRect(completedAreaX + i * 20, completedAreaY, 14, 14);
            }
            
            int firstCardY = 50;
            int faceUpSpacing = 25;
            int faceDownSpacing = 25;
            
            for (int i = 0; i < game.getState().columns.length; i++) {
                Stack<Card> column = game.getState().columns[i];
                for (int j = 0; j < column.size(); j++) {
                    Card card = column.get(j);
                    int x = 30 + i * 80;
                    int y;
                    if (j == 0) {
                        y = firstCardY;
                    } else {
                        Card prevCard = column.get(j - 1);
                        if (prevCard.isFaceUp()) {
                            y = firstCardY + (j - 1) * faceUpSpacing + faceUpSpacing;
                        } else {
                            y = firstCardY + j * faceDownSpacing;
                        }
                    }
                    int width = 60;
                    int height = 90;
                    
                    if (card.isFaceUp()) {
                        g.setColor(Color.WHITE);
                        g.fillRect(x, y, width, height);
                        g.setColor(Color.BLACK);
                        g.drawRect(x, y, width - 1, height - 1);
                        
                        Color suitColor = card.getSuitColor();
                        g.setColor(suitColor);
                        g.setFont(new Font("Arial", Font.BOLD, 16));
                        String rankSymbol = card.getRankSymbol();
                        g.drawString(rankSymbol, x + 5, y + 18);
                        g.drawString(card.getSuitSymbol(), x + 5, y + 35);
                        g.setFont(new Font("Arial", Font.BOLD, 24));
                        g.drawString(card.getSuitSymbol(), x + width / 2 - 8, y + height / 2 + 8);
                    } else {
                        g.setColor(new Color(0, 0, 128));
                        g.fillRect(x, y, width, height);
                        g.setColor(new Color(0, 0, 64));
                        g.drawRect(x, y, width - 1, height - 1);
                        g.setColor(new Color(0, 0, 192));
                        g.drawLine(x + 10, y + 10, x + 50, y + 80);
                        g.drawLine(x + 50, y + 10, x + 10, y + 80);
                    }
                }
            }

            if (draggedCard != null && draggedColumn != -1) {
                Stack<Card> sourceColumn = game.getState().columns[draggedColumn];
                int x = mousePoint.x - 30;
                int y = mousePoint.y - 45;
                int width = 60;
                int height = 90;
                
                int startIndex = sourceColumn.size() - draggedCount;
                for (int i = 0; i < draggedCount; i++) {
                    Card card = sourceColumn.get(startIndex + i);
                    int cardY = y + i * 15;
                    
                    if (card.isFaceUp()) {
                        g.setColor(Color.WHITE);
                        g.fillRect(x, cardY, width, height);
                        g.setColor(Color.BLACK);
                        g.drawRect(x, cardY, width - 1, height - 1);
                        Color suitColor = card.getSuit() == Card.Suit.HEARTS || card.getSuit() == Card.Suit.DIAMONDS 
                            ? Color.RED : Color.BLACK;
                        g.setColor(suitColor);
                        String rankSymbol = card.getRankSymbol();
                        g.drawString(rankSymbol, x + 5, cardY + 15);
                        g.drawString(rankSymbol, x + width - 15, cardY + height - 5);
                    } else {
                        g.setColor(new Color(0, 0, 128));
                        g.fillRect(x, cardY, width, height);
                        g.setColor(new Color(0, 0, 64));
                        g.drawRect(x, cardY, width - 1, height - 1);
                        g.setColor(new Color(0, 0, 192));
                        g.drawLine(x + 10, cardY + 10, x + 50, cardY + 80);
                        g.drawLine(x + 50, cardY + 10, x + 10, cardY + 80);
                    }
                }
            }
        }

        private int getColumnAt(Point p) {
            for (int i = 0; i < game.getState().columns.length; i++) {
                if (p.x >= 30 + i * 80 && p.x <= 30 + (i + 1) * 80) {
                    return i;
                }
            }
            return -1;
        }
    }
}
