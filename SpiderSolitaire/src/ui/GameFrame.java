package ui;

import game.SpiderGame;
import java.awt.*;
import java.awt.event.*;
import java.util.Stack;
import javax.swing.*;
import model.Card;
import util.AchievementManager;

public class GameFrame extends JFrame {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
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

            int difficulty;
            switch (choice) {
                case 0: difficulty = 1; break;
                case 1: difficulty = 2; break;
                case 2: difficulty = 4; break;
                default: difficulty = 1; break;
            }
            
            GameFrame frame = new GameFrame(difficulty);
            frame.setVisible(true);
        });
    }
    
    private final SpiderGame game;
    private final Point mousePoint = new Point(0, 0);
    private Card draggedCard = null;
    private int draggedColumn = -1;
    private int draggedCount = 1;

    public GameFrame(int difficulty) {
        game = new SpiderGame(difficulty);

        String difficultyName;
        if (difficulty == 1) difficultyName = "单花色";
        else if (difficulty == 2) difficultyName = "双花色";
        else difficultyName = "四花色";
        
        setTitle("蜘蛛纸牌 - " + difficultyName);
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

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
        JButton achievementBtn = new JButton("成就");
        achievementBtn.addActionListener(e -> new AchievementDialog(this).setVisible(true));
        panel.add(undoBtn);
        panel.add(dealBtn);
        panel.add(hintBtn);
        panel.add(achievementBtn);
        add(panel, BorderLayout.SOUTH);

        GameBoard board = new GameBoard(game);
        add(board, BorderLayout.CENTER);
    }

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
                        
                        int clickY = e.getY();
                        int cardIndex = -1;
                        
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
                            
                            if (clickY >= cardY && clickY <= cardY + 90) {
                                cardIndex = j;
                                break;
                            }
                        }
                        
                        if (cardIndex != -1 && column.get(cardIndex).isFaceUp()) {
                            draggedCard = column.get(cardIndex);
                            draggedCount = 1;
                            
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
                            game.move(draggedColumn, targetColumn, draggedCount);
                            game.checkAndRemoveCompleteSets();
                            if (game.isGameWon()) {
                                AchievementManager.getInstance().addWin();
                                StringBuilder message = new StringBuilder("恭喜！你赢了！\n\n");
                                message.append("这是你第 ").append(AchievementManager.getInstance().getTotalWins()).append(" 次通关！\n\n");
                                
                                for (AchievementManager.Achievement a : AchievementManager.getInstance().getUnlockedAchievements()) {
                                    if (!message.toString().contains(a.name)) {
                                        message.append("★ 获得成就: ").append(a.name).append("\n");
                                    }
                                }
                                JOptionPane.showMessageDialog(GameFrame.this, message.toString());
                            }
                        }
                        draggedCard = null;
                        draggedColumn = -1;
                        repaint();
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
            g.drawString("已完成: " + completedSets + "/8", 30, 20);
            g.drawString("分数: " + score, 300, 20);
            
            int completedAreaX = 120;
            int completedAreaY = 10;
            for (int i = 0; i < 8; i++) {
                g.setColor(i < completedSets ? new Color(255, 255, 0) : new Color(200, 200, 200));
                g.fillRect(completedAreaX + i * 20, completedAreaY, 15, 15);
                g.setColor(Color.BLACK);
                g.drawRect(completedAreaX + i * 20, completedAreaY, 14, 14);
            }
            
            for (int i = 0; i < game.getState().columns.length; i++) {
                Stack<Card> column = game.getState().columns[i];
                for (int j = 0; j < column.size(); j++) {
                    Card card = column.get(j);
                    int x = 30 + i * 80;
                    int y;
                    if (j == 0) {
                        y = 30;
                    } else {
                        Card prevCard = column.get(j - 1);
                        if (prevCard.isFaceUp()) {
                            y = 30 + (j - 1) * 30 + 15;
                        } else {
                            y = 30 + j * 30;
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
