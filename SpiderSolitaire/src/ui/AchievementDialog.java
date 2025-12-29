package ui;

import util.AchievementManager;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AchievementDialog extends JDialog {
    public AchievementDialog(JFrame parent) {
        super(parent, "成就", true);
        
        AchievementManager manager = AchievementManager.getInstance();
        int totalWins = manager.getTotalWins();
        
        setSize(400, 350);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());
        
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(new Color(240, 240, 240));
        
        JLabel titleLabel = new JLabel("游戏成就");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createVerticalStrut(15));
        
        JLabel statsLabel = new JLabel("总通关次数: " + totalWins);
        statsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(statsLabel);
        mainPanel.add(Box.createVerticalStrut(20));
        
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(new Color(240, 240, 240));
        add(scrollPane, BorderLayout.CENTER);
        
        List<AchievementManager.Achievement> unlocked = manager.getUnlockedAchievements();
        List<AchievementManager.Achievement> locked = manager.getLockedAchievements();
        
        if (!unlocked.isEmpty()) {
            JLabel unlockedLabel = new JLabel("已解锁");
            unlockedLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
            unlockedLabel.setForeground(new Color(0, 150, 0));
            unlockedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainPanel.add(unlockedLabel);
            mainPanel.add(Box.createVerticalStrut(10));
            
            for (AchievementManager.Achievement a : unlocked) {
                mainPanel.add(createAchievementPanel(a, true));
                mainPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        if (!locked.isEmpty()) {
            JLabel lockedLabel = new JLabel("未解锁");
            lockedLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
            lockedLabel.setForeground(Color.GRAY);
            lockedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            mainPanel.add(Box.createVerticalStrut(15));
            mainPanel.add(lockedLabel);
            mainPanel.add(Box.createVerticalStrut(10));
            
            for (AchievementManager.Achievement a : locked) {
                mainPanel.add(createAchievementPanel(a, false));
                mainPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(new Color(240, 240, 240));
        
        JButton resetButton = new JButton("重置进度");
        resetButton.addActionListener(e -> {
            int result = JOptionPane.showConfirmDialog(this, 
                "确定要重置所有成就进度吗？此操作不可撤销！", 
                "确认重置", JOptionPane.YES_NO_OPTION);
            if (result == JOptionPane.YES_OPTION) {
                manager.resetAchievements();
                dispose();
                new AchievementDialog(parent).setVisible(true);
            }
        });
        buttonPanel.add(resetButton);
        
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createAchievementPanel(AchievementManager.Achievement achievement, boolean unlocked) {
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        panel.setBorder(BorderFactory.createLineBorder(unlocked ? new Color(0, 150, 0) : Color.GRAY, 2));
        panel.setBackground(unlocked ? new Color(220, 255, 220) : new Color(250, 250, 250));
        panel.setMaximumSize(new Dimension(350, 60));
        
        JLabel iconLabel = new JLabel(unlocked ? "✓" : "✗");
        iconLabel.setFont(new Font("Arial", Font.BOLD, 24));
        iconLabel.setForeground(unlocked ? new Color(0, 150, 0) : Color.GRAY);
        iconLabel.setPreferredSize(new Dimension(40, 40));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(iconLabel, BorderLayout.WEST);
        
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(unlocked ? new Color(220, 255, 220) : new Color(250, 250, 250));
        
        JLabel nameLabel = new JLabel(achievement.name);
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        nameLabel.setForeground(unlocked ? Color.BLACK : Color.GRAY);
        textPanel.add(nameLabel);
        
        JLabel descLabel = new JLabel(achievement.description + " (已通关: " + 
            Math.min(AchievementManager.getInstance().getTotalWins(), achievement.requiredWins) + 
            "/" + achievement.requiredWins + ")");
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        descLabel.setForeground(unlocked ? Color.DARK_GRAY : Color.GRAY);
        textPanel.add(descLabel);
        
        panel.add(textPanel, BorderLayout.CENTER);
        
        return panel;
    }
}
