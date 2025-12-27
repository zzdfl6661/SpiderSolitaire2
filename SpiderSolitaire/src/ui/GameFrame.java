package ui;

import game.SpiderGame;
import java.awt.*;
import java.awt.event.*;
import java.util.Stack;
import javax.swing.*;
import model.Card;

public class GameFrame extends JFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GameFrame frame = new GameFrame();
            frame.setVisible(true);
        });
    }
    private final SpiderGame game;  // game 应该是 final
    private final Point mousePoint = new Point(0, 0);  // mousePoint 应该是 final
    private Card draggedCard = null;  // 当前拖动的纸牌
    private int draggedColumn = -1;   // 当前拖动的列
    private int draggedCount = 1;     // 当前拖动的牌数

    public GameFrame() {
        game = new SpiderGame(2);  // 难度设为 2（两种花色）

        setTitle("蜘蛛纸牌");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 按钮
        JPanel panel = new JPanel();
        JButton undoBtn = new JButton("撤销");
        undoBtn.addActionListener(e -> {
            game.undo();
            repaint();
        });
        JButton dealBtn = new JButton("发牌");
        dealBtn.addActionListener(e -> {
            if (!game.deal()) {
                JOptionPane.showMessageDialog(this, "发牌失败！请确保每列至少有一张牌，并且牌堆中有足够的牌。");
            }
            repaint();
        });
        JButton hintBtn = new JButton("提示");
        hintBtn.addActionListener(e -> JOptionPane.showMessageDialog(this, game.hint()));
        panel.add(undoBtn);
        panel.add(dealBtn);
        panel.add(hintBtn);
        add(panel, BorderLayout.SOUTH);

        // 绘制纸牌的面板
        GameBoard board = new GameBoard(game);
        add(board, BorderLayout.CENTER);
    }

    // 用于绘制纸牌的自定义面板
    private class GameBoard extends JPanel {
        private final SpiderGame game;

        public GameBoard(SpiderGame game) {
            this.game = game;
            setPreferredSize(new Dimension(800, 500));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    draggedColumn = getColumnAt(e.getPoint());
                    if (draggedColumn != -1) {
                        Stack<Card> column = game.getState().columns[draggedColumn];
                        if (column.isEmpty()) return;
                        
                        // 找到用户点击的具体牌
                        int clickY = e.getY();
                        int cardIndex = -1;
                        
                        // 从下往上检查哪张牌被点击
                        for (int j = column.size() - 1; j >= 0; j--) {
                            int cardY;
                            if (j == 0) {
                                cardY = 30;
                            } else {
                                Card prevCard = column.get(j - 1);
                                if (prevCard.isFaceUp()) {
                                    cardY = 30 + (j - 1) * 30 + 15;
                                } else {
                                    cardY = 30 + j * 30;
                                }
                            }
                            
                            // 检查点击是否在该牌的范围内
                            if (clickY >= cardY && clickY <= cardY + 90) {
                                cardIndex = j;
                                break;
                            }
                        }
                        
                        if (cardIndex != -1 && column.get(cardIndex).isFaceUp()) {
                            // 计算可以拖动的最大连续牌数
                            draggedCard = column.get(cardIndex); // 点击的牌
                            draggedCount = 1; // 至少可以拖动当前点击的牌
                            
                            // 检查从点击的牌开始是否有连续递减的牌
                            for (int i = cardIndex; i < column.size() - 1; i++) {
                                Card current = column.get(i);
                                Card next = column.get(i + 1);
                                if (current.getRank() == next.getRank() + 1) {
                                    draggedCount++;
                                } else {
                                    break;
                                }
                            }
                        } else {
                            draggedCard = null;
                            draggedCount = 1;
                        }
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (draggedCard != null) {
                        int targetColumn = getColumnAt(e.getPoint());
                        if (targetColumn != -1 && targetColumn != draggedColumn) {
                            game.move(draggedColumn, targetColumn, draggedCount);  // 移动多张纸牌
                            // 检查是否有完整的牌组可以移除
                            game.checkAndRemoveCompleteSets();
                            // 检查游戏是否胜利
                            if (game.isGameWon()) {
                                JOptionPane.showMessageDialog(GameFrame.this, "恭喜！你赢了！");
                            }
                        }
                        draggedCard = null;
                        draggedColumn = -1; // 重置拖动列
                        repaint();
                    }
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    mousePoint.setLocation(e.getPoint());  // 更新拖动位置
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // 设置绿色背景，模拟真实纸牌游戏桌面
            g.setColor(new Color(0, 100, 0));
            g.fillRect(0, 0, getWidth(), getHeight());
            
            // 绘制完成牌组的显示区域和分数
            int completedSets = game.getState().completedSets;
            int score = game.getState().score;
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.drawString("已完成: " + completedSets + "/8", 30, 20);
            g.drawString("分数: " + score, 300, 20);
            
            // 绘制完成区域的图形表示
            int completedAreaX = 120;
            int completedAreaY = 10;
            for (int i = 0; i < 8; i++) {
                g.setColor(i < completedSets ? new Color(255, 255, 0) : new Color(200, 200, 200));
                g.fillRect(completedAreaX + i * 20, completedAreaY, 15, 15);
                g.setColor(Color.BLACK);
                g.drawRect(completedAreaX + i * 20, completedAreaY, 14, 14);
            }
            
            // 绘制每张纸牌
            for (int i = 0; i < game.getState().columns.length; i++) {
                Stack<Card> column = game.getState().columns[i];
                for (int j = 0; j < column.size(); j++) {
                    Card card = column.get(j);
                    int x = 30 + i * 80;
                    // 调整垂直间距：背面朝上的牌完全堆叠，正面朝上的牌部分重叠
                    int y;
                    if (j == 0) {
                        y = 30; // 第一张牌的位置
                    } else {
                        Card prevCard = column.get(j - 1);
                        if (prevCard.isFaceUp()) {
                            // 前面的牌是正面朝上，部分重叠（15像素间距）
                            y = 30 + (j - 1) * 30 + 15;
                        } else {
                            // 前面的牌是背面朝上，完全堆叠
                            y = 30 + j * 30;
                        }
                    }
                    int width = 60;
                    int height = 90;
                    
                    if (card.isFaceUp()) {
                        // 正面朝上的纸牌：白色背景，黑色边框和文字
                        g.setColor(Color.WHITE);
                        g.fillRect(x, y, width, height);
                        g.setColor(Color.BLACK);
                        g.drawRect(x, y, width - 1, height - 1);
                        // 在左上角标注数字
                        String rankSymbol = card.getRankSymbol();
                        g.drawString(rankSymbol, x + 5, y + 15);
                        // 在右下角标注数字（不旋转坐标系，直接计算位置）
                        g.drawString(rankSymbol, x + width - 15, y + height - 5);
                    } else {
                        // 背面朝上的纸牌：蓝色背景，深蓝色边框
                        g.setColor(new Color(0, 0, 128));
                        g.fillRect(x, y, width, height);
                        g.setColor(new Color(0, 0, 64));
                        g.drawRect(x, y, width - 1, height - 1);
                        // 添加简单的背面图案
                        g.setColor(new Color(0, 0, 192));
                        g.drawLine(x + 10, y + 10, x + 50, y + 80);
                        g.drawLine(x + 50, y + 10, x + 10, y + 80);
                    }
                }
            }

            // 绘制正在拖动的纸牌
            if (draggedCard != null && draggedColumn != -1) {
                Stack<Card> sourceColumn = game.getState().columns[draggedColumn];
                int x = mousePoint.x - 30;
                int y = mousePoint.y - 45;
                int width = 60;
                int height = 90;
                
                // 绘制所有被拖动的牌
                int startIndex = sourceColumn.size() - draggedCount;
                for (int i = 0; i < draggedCount; i++) {
                    Card card = sourceColumn.get(startIndex + i);
                    int cardY = y + i * 15;  // 15像素间距
                    
                    if (card.isFaceUp()) {
                        g.setColor(Color.WHITE);
                        g.fillRect(x, cardY, width, height);
                        g.setColor(Color.BLACK);
                        g.drawRect(x, cardY, width - 1, height - 1);
                        // 在左上角标注数字
                        String rankSymbol = card.getRankSymbol();
                        g.drawString(rankSymbol, x + 5, cardY + 15);
                        // 在右下角标注数字（不旋转坐标系，直接计算位置）
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

        // 根据鼠标位置确定列
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
