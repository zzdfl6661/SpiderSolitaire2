package ui;

import game.SpiderGame;
import java.awt.*;
import java.awt.event.*;
import java.util.Stack;
import javax.swing.*;
import model.Card;
import util.AchievementManager;

/**
 * 游戏主窗口类
 * 负责创建游戏界面、处理用户交互、显示游戏状态和成就系统
 */
public class GameFrame extends JFrame {
    /**
     * 程序入口点
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        // 在事件分发线程中创建GUI，确保线程安全
        SwingUtilities.invokeLater(() -> {
            // 创建难度选择对话框
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
            GameFrame frame = new GameFrame(difficulty);
            frame.setVisible(true);
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
     * @param difficulty 游戏难度级别：1=单花色，2=双花色，4=四花色
     */
    public GameFrame(int difficulty) {
        // 创建游戏对象
        game = new SpiderGame(difficulty);

        // 根据难度设置窗口标题
        String difficultyName;
        if (difficulty == 1) difficultyName = "单花色";
        else if (difficulty == 2) difficultyName = "双花色";
        else difficultyName = "四花色";
        
        // 设置窗口基本属性
        setTitle("蜘蛛纸牌 - " + difficultyName);
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
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
        
        // 将按钮添加到面板中
        panel.add(undoBtn);
        panel.add(dealBtn);
        panel.add(hintBtn);
        panel.add(achievementBtn);
        // 将按钮面板添加到窗口底部
        add(panel, BorderLayout.SOUTH);

        // 创建并添加游戏棋盘面板到窗口中央
        GameBoard board = new GameBoard(game);
        add(board, BorderLayout.CENTER);
    }

    /**
     * 游戏棋盘内部类
     * 负责绘制游戏界面、处理鼠标事件、显示牌面等
     */
    private class GameBoard extends JPanel {
        private final SpiderGame game;

        /**
         * 游戏棋盘构造函数
         * @param game 游戏对象
         */
        public GameBoard(SpiderGame game) {
            this.game = game;
            // 设置棋盘首选大小
            setPreferredSize(new Dimension(800, 500));
            // 添加鼠标事件监听器
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
