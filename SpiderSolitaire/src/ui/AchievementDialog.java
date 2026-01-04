package ui;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import util.AchievementManager;

/**
 * 成就对话框类
 * 
 * 这是一个模态对话框，用于显示游戏的成就系统
 * 继承自JDialog，提供一个专门显示游戏成就的窗口
 * 
 * 主要功能：
 * - 显示玩家已解锁的成就
 * - 显示玩家未解锁的成就及进度
 * - 提供重置成就进度的功能
 * - 提供关闭对话框的功能
 * 
 * 界面设计特点：
 * - 使用卡片式布局分别展示已解锁和未解锁的成就
 * - 通过颜色区分已解锁（绿色）和未解锁（灰色）的成就
 * - 提供进度条或进度信息，显示距离解锁成就还需要多少次通关
 * - 支持滚动浏览大量成就
 */
public class AchievementDialog extends JDialog {
    /**
     * 构造函数
     * 创建并初始化成就对话框
     * 
     * @param parent 父窗口，对话框将相对于此窗口居中显示
     */
    public AchievementDialog(JFrame parent) {
        // 调用父类构造函数，设置对话框标题为"成就"并设置为模态对话框
        super(parent, "成就", true);
        
        // 获取成就管理器单例实例
        AchievementManager manager = AchievementManager.getInstance();
        // 获取玩家总通关次数
        int totalWins = manager.getTotalWins();
        
        // 设置对话框大小
        setSize(400, 350);
        // 设置对话框相对于父窗口居中显示
        setLocationRelativeTo(parent);
        // 使用BorderLayout布局管理器
        setLayout(new BorderLayout());
        
        // 创建主面板，使用垂直盒布局管理器
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        // 设置面板边距
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        // 设置面板背景色
        mainPanel.setBackground(new Color(240, 240, 240));
        
        // 创建并设置标题标签
        JLabel titleLabel = new JLabel("游戏成就");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        // 设置标题居中对齐
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(titleLabel);
        // 添加标题下方空白
        mainPanel.add(Box.createVerticalStrut(15));
        
        // 创建并设置统计信息标签，显示总通关次数
        JLabel statsLabel = new JLabel("总通关次数: " + totalWins);
        statsLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        // 设置统计信息居中对齐
        statsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(statsLabel);
        // 添加统计信息下方空白
        mainPanel.add(Box.createVerticalStrut(20));
        
        // 创建滚动窗格，包裹主面板，支持滚动查看大量成就
        JScrollPane scrollPane = new JScrollPane(mainPanel);
        // 移除滚动窗格边框
        scrollPane.setBorder(null);
        // 设置视口背景色
        scrollPane.getViewport().setBackground(new Color(240, 240, 240));
        // 将滚动窗格添加到对话框中心位置
        add(scrollPane, BorderLayout.CENTER);
        
        // 获取已解锁和未解锁的成就列表
        List<AchievementManager.Achievement> unlocked = manager.getUnlockedAchievements();
        List<AchievementManager.Achievement> locked = manager.getLockedAchievements();
        
        // 如果有已解锁的成就，则显示"已解锁"部分
        if (!unlocked.isEmpty()) {
            // 创建并设置"已解锁"标签
            JLabel unlockedLabel = new JLabel("已解锁");
            unlockedLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
            // 设置文本颜色为绿色，表示已解锁状态
            unlockedLabel.setForeground(new Color(0, 150, 0));
            // 设置居中对齐
            unlockedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            // 添加到主面板
            mainPanel.add(unlockedLabel);
            // 添加标签下方空白
            mainPanel.add(Box.createVerticalStrut(10));
            
            // 遍历所有已解锁的成就，创建并添加到面板
            for (AchievementManager.Achievement a : unlocked) {
                mainPanel.add(createAchievementPanel(a, true));
                mainPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        // 如果有未解锁的成就，则显示"未解锁"部分
        if (!locked.isEmpty()) {
            // 创建并设置"未解锁"标签
            JLabel lockedLabel = new JLabel("未解锁");
            lockedLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
            // 设置文本颜色为灰色，表示未解锁状态
            lockedLabel.setForeground(Color.GRAY);
            // 设置居中对齐
            lockedLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            // 添加空白，与上一部分分隔
            mainPanel.add(Box.createVerticalStrut(15));
            // 添加标签到面板
            mainPanel.add(lockedLabel);
            // 添加标签下方空白
            mainPanel.add(Box.createVerticalStrut(10));
            
            // 遍历所有未解锁的成就，创建并添加到面板
            for (AchievementManager.Achievement a : locked) {
                mainPanel.add(createAchievementPanel(a, false));
                mainPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        // 创建按钮面板，使用右对齐的流式布局
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        // 设置按钮面板背景色
        buttonPanel.setBackground(new Color(240, 240, 240));
        
        // 创建"重置进度"按钮
        JButton resetButton = new JButton("重置进度");
        // 添加按钮事件监听器，处理重置进度操作
        resetButton.addActionListener(e -> {
            // 显示确认对话框，询问用户是否确定要重置所有成就进度
            int result = JOptionPane.showConfirmDialog(this, 
                "确定要重置所有成就进度吗？此操作不可撤销！", 
                "确认重置", JOptionPane.YES_NO_OPTION);
            // 如果用户确认重置
            if (result == JOptionPane.YES_OPTION) {
                // 调用成就管理器的重置方法
                manager.resetAchievements();
                // 关闭当前对话框
                dispose();
                // 重新打开成就对话框，显示重置后的状态
                new AchievementDialog(parent).setVisible(true);
            }
        });
        // 将重置按钮添加到按钮面板
        buttonPanel.add(resetButton);
        
        // 创建"关闭"按钮
        JButton closeButton = new JButton("关闭");
        // 添加按钮事件监听器，处理关闭对话框操作
        closeButton.addActionListener(e -> dispose());
        // 将关闭按钮添加到按钮面板
        buttonPanel.add(closeButton);
        
        // 将按钮面板添加到对话框的南部（底部）位置
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 创建成就面板
     * 为单个成就创建并返回一个显示面板
     * 
     * @param achievement 要显示的成就对象
     * @param unlocked 成就是否已解锁，已解锁的成就会以绿色高亮显示
     * @return 成就显示面板
     */
    private JPanel createAchievementPanel(AchievementManager.Achievement achievement, boolean unlocked) {
        // 创建主面板，使用边界布局管理器，设置组件间距为10像素
        JPanel panel = new JPanel(new BorderLayout(10, 0));
        // 根据解锁状态设置边框颜色：已解锁使用绿色，未解锁使用灰色
        panel.setBorder(BorderFactory.createLineBorder(unlocked ? new Color(0, 150, 0) : Color.GRAY, 2));
        // 根据解锁状态设置面板背景色：已解锁使用浅绿色，未解锁使用浅灰色
        panel.setBackground(unlocked ? new Color(220, 255, 220) : new Color(250, 250, 250));
        // 设置面板最大尺寸
        panel.setMaximumSize(new Dimension(350, 60));
        
        // 创建图标标签，已解锁显示✓，未解锁显示✗
        JLabel iconLabel = new JLabel(unlocked ? "✓" : "✗");
        // 设置图标字体和大小
        iconLabel.setFont(new Font("Arial", Font.BOLD, 24));
        // 根据解锁状态设置图标颜色
        iconLabel.setForeground(unlocked ? new Color(0, 150, 0) : Color.GRAY);
        // 设置图标标签的首选尺寸
        iconLabel.setPreferredSize(new Dimension(40, 40));
        // 设置图标居中对齐
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        // 将图标标签添加到面板西部（左侧）位置
        panel.add(iconLabel, BorderLayout.WEST);
        
        // 创建文本面板，用于放置成就名称和描述
        JPanel textPanel = new JPanel();
        // 设置文本面板使用垂直盒布局
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        // 设置文本面板背景色，与主面板保持一致
        textPanel.setBackground(unlocked ? new Color(220, 255, 220) : new Color(250, 250, 250));
        
        // 创建并设置成就名称标签
        JLabel nameLabel = new JLabel(achievement.name);
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 14));
        // 根据解锁状态设置文字颜色
        nameLabel.setForeground(unlocked ? Color.BLACK : Color.GRAY);
        textPanel.add(nameLabel);
        
        // 创建并设置成就描述标签，显示成就要求和当前进度
        JLabel descLabel = new JLabel(achievement.description + " (已通关: " + 
            Math.min(AchievementManager.getInstance().getTotalWins(), achievement.requiredWins) + 
            "/" + achievement.requiredWins + ")");
        descLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        // 根据解锁状态设置描述文字颜色
        descLabel.setForeground(unlocked ? Color.DARK_GRAY : Color.GRAY);
        textPanel.add(descLabel);
        
        // 将文本面板添加到主面板中心位置
        panel.add(textPanel, BorderLayout.CENTER);
        
        return panel;
    }
}
