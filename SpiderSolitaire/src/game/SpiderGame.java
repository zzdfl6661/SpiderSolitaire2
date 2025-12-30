package game;

import java.util.*;
import model.Card;

/**
 * 蜘蛛纸牌游戏核心逻辑类
 * 负责处理游戏的所有核心逻辑，包括移动验证、游戏操作、撤销等
 */
public class SpiderGame {
    private GameState state;

    /**
     * 蜘蛛纸牌游戏构造函数
     * @param difficulty 游戏难度：1=单花色(简单)，2=双花色(中等)，4=四花色(困难)
     */
    public SpiderGame(int difficulty) {
        state = new GameState(difficulty);
    }

    /**
     * 获取当前游戏状态
     * @return GameState 当前游戏状态对象
     */
    public GameState getState() {
        return state;
    }

    /**
     * 检查是否可以将指定数量的牌从源列移动到目标列
     * @param from 源列索引
     * @param to 目标列索引
     * @param count 要移动的牌的数量
     * @return boolean 如果可以移动返回true，否则返回false
     */
    public boolean canMove(int from, int to, int count) {
        Stack<Card> src = state.columns[from];
        Stack<Card> dst = state.columns[to];

        // 检查源列是否有足够的牌
        if (src.size() < count) return false;

        Card base = src.get(src.size() - count); // 要移动的牌组中最底部的牌
        Card top = src.peek(); // 源列最顶部的牌
        
        // 检查K只能移动到空位上(蜘蛛纸牌规则)
        if (base.getRank() == 13 && !dst.isEmpty()) {
            return false; // K只能移动到空位
        }
        
        // 检查要移动的牌组是否都是正面朝上、同花色且连续递减
        Card.Suit suit = base.getSuit(); // 记录牌组的花色
        for (int i = src.size() - count; i < src.size(); i++) {
            Card current = src.get(i);
            // 所有牌必须正面朝上才能移动
            if (!current.isFaceUp()) {
                return false; // 所有牌必须正面朝上
            }
            // 所有牌必须同花色
            if (current.getSuit() != suit) {
                return false; // 必须同花色
            }
        }
        
        // 检查移动的牌组是否按顺序递减(例如：KQJ...3-2-A)
        for (int i = src.size() - count; i < src.size() - 1; i++) {
            Card current = src.get(i);
            Card next = src.get(i + 1);
            // 当前牌的点数必须比下一张牌大1(连续递减)
            if (current.getRank() != next.getRank() + 1) {
                return false; // 必须连续递减
            }
        }

        // 如果目标列不为空，需要检查连接规则
        if (!dst.isEmpty()) {
            Card dstTop = dst.peek(); // 目标列最顶部的牌
            
            // 检查A只能接在2下面(连续递增规则)
            if (base.getRank() == 1 && dstTop.getRank() != 2) {
                return false; // A只能接在2下面
            }
            
            // 检查目标牌的点数是否比基础牌大1(连续递增)
            return dstTop.getRank() == base.getRank() + 1;
        }
        
        // 目标列为空时，任何牌都可以移动到这里
        return true;
    }

    /**
     * 执行牌移动操作
     * @param from 源列索引
     * @param to 目标列索引
     * @param count 要移动的牌的数量
     */
    public void move(int from, int to, int count) {
        // 先验证移动是否合法
        if (!canMove(from, to, count)) {
            return; // 如果不合法，直接返回
        }
        
        Stack<Card> src = state.columns[from]; // 源列
        Stack<Card> dst = state.columns[to];   // 目标列

        // 创建移动牌组的记录，用于撤销操作
        List<Card> moved = new ArrayList<>();
        // 从源列取出指定数量的牌，添加到移动列表中
        for (int i = 0; i < count; i++) {
            moved.add(0, src.pop()); // 逆序添加，保持牌的顺序
        }
        // 将移动的牌添加到目标列
        dst.addAll(moved);

        // 检查源列是否需要翻牌
        boolean flipped = false;
        boolean wasFaceUpBeforeMove = true;
        Card flippedCard = null;
        // 如果源列还有牌且最顶部的牌是背面朝上，需要翻面
        if (!src.isEmpty() && !src.peek().isFaceUp()) {
            src.peek().flip(); // 翻牌
            flipped = true;
            wasFaceUpBeforeMove = false;
            flippedCard = src.peek();
        }

        // 将移动操作记录到撤销栈中
        state.undoStack.push(new Move(from, to, moved, flipped, wasFaceUpBeforeMove, flippedCard));
        // 每次有效移动扣1分
        state.score--;
    }

    /**
     * 执行撤销操作，恢复到上一步的游戏状态
     */
    public void undo() {
        // 检查撤销栈是否为空，如果为空则无法撤销
        if (state.undoStack.isEmpty()) return;
        
        // 从撤销栈中弹出最近的操作记录
        Move m = state.undoStack.pop();

        // 处理发牌操作的撤销 (from == -1 && to == -1)
        if (m.from == -1 && m.to == -1) {
            // 从每一列取最后一张牌放回牌堆
            for (int i = 0; i < 10; i++) {
                if (!state.columns[i].isEmpty()) {
                    Card card = state.columns[i].pop();
                    card.flip(); // 翻回背面
                    state.stock.push(card);
                }
            }
            // 恢复剩余发牌次数
            state.remainingDeals++;
        } 
        // 处理移除完整牌组操作的撤销 (to == -2)
        else if (m.to == -2) {
            // 将移除的牌组放回原来的列
            Stack<Card> column = state.columns[m.from];
            for (Card card : m.movedCards) {
                column.push(card);
            }
            // 恢复完成牌组计数和分数
            state.completedSets--;
            state.score -= 100;
        }
        else {
            // 普通移动操作的撤销
            Stack<Card> src = state.columns[m.from]; // 原来的源列
            Stack<Card> dst = state.columns[m.to];   // 原来的目标列

            // 将移动的牌从目标列移回源列
            for (int i = 0; i < m.movedCards.size(); i++) {
                src.push(dst.pop());
            }

            // 精确恢复翻牌状态：只有当移动前是背面朝上时才翻回背面
            if (m.flipped && m.flippedCard != null) {
                if (!m.wasFaceUpBeforeMove) {
                    m.flippedCard.flip();
                }
            }
        }
        // 撤销操作恢复1分
        state.score += 1;
    }

    /**
     * 获取游戏提示信息
     * @return String 提示信息，如果没有可用移动则返回"没有可用的移动提示"
     */
    public String hint() {
        // 遍历所有列，寻找有效的移动
        for (int from = 0; from < 10; from++) {
            // 跳过空列
            if (state.columns[from].isEmpty()) continue;
            
            // 检查从该列可以移动的最大连续牌组
            int maxCount = 1;
            Card baseCard = state.columns[from].peek();
            
            // 从后往前检查连续递减的牌组
            for (int i = state.columns[from].size() - 2; i >= 0; i--) {
                Card current = state.columns[from].get(i);
                Card next = state.columns[from].get(i + 1);
                // 检查当前牌和下一张牌是否连续递减且都是正面朝上
                if (current.isFaceUp() && next.getRank() == current.getRank() + 1) {
                    maxCount++;
                } else {
                    break; // 不连续时停止检查
                }
            }
            
            // 检查是否可以移动到其他列
            for (int to = 0; to < 10; to++) {
                if (from == to) continue; // 跳过源列和目标列相同的情况
                
                // 如果可以移动，返回提示信息
                if (canMove(from, to, maxCount)) {
                    return "提示: 可以将第" + (from + 1) + "列的" + maxCount + "张牌移动到第" + (to + 1) + "列";
                }
            }
        }
        
        // 没有找到可用移动
        return "没有可用的移动提示";
    }
    
    /**
     * 执行发牌操作，给每一列发一张牌
     * @return boolean 发牌成功返回true，失败返回false
     */
    public boolean deal() {
        // 检查剩余发牌次数
        if (state.remainingDeals <= 0) {
            return false; // 没有剩余发牌次数了
        }
        
        // 检查每一列是否至少有一张牌
        for (int i = 0; i < 10; i++) {
            if (state.columns[i].isEmpty()) {
                return false; // 有列是空的，不能发牌
            }
        }
        
        // 检查牌堆是否有足够的牌（至少10张）
        if (state.stock.size() < 10) {
            return false; // 牌堆的牌不够了
        }
        
        // 记录发牌操作，用于撤销
        List<Card> dealtCards = new ArrayList<>();
        
        // 给每一列发一张明牌
        for (int i = 0; i < 10; i++) {
            Card card = state.stock.pop();
            card.flip(); // 翻成正面显示
            state.columns[i].push(card);
            dealtCards.add(card);
        }
        
        // 将发牌操作添加到撤销栈(from=-1, to=-1表示发牌操作)
        state.undoStack.push(new Move(-1, -1, dealtCards, false, true, null));
        // 减少剩余发牌次数
        state.remainingDeals--;
        
        return true;
    }
    
    /**
     * 检查并移除所有完整的牌组（K到A）
     * @return boolean 如果移除了任何牌组返回true，否则返回false
     */
    public boolean checkAndRemoveCompleteSets() {
        boolean removed = false; // 标记是否移除了牌组
        
        // 遍历所有列，寻找完整的牌组
        for (int i = 0; i < 10; i++) {
            Stack<Card> column = state.columns[i];
            if (column.isEmpty()) continue; // 跳过空列
            
            // 检查是否有完整的K到A序列（13张牌）
            if (column.size() >= 13 && column.peek().getRank() == 1) { // 顶部是A
                boolean isComplete = true;
                // 获取序列的花色（序列中的牌都必须是同花色）
                Card.Suit suit = column.get(column.size() - 13).getSuit(); // 序列的花色
                
                // 从A开始往上检查是否是KQJ...2A序列
                for (int j = column.size() - 13; j < column.size(); j++) {
                    Card card = column.get(j);
                    // 计算期望的牌点数：K(13)到A(1)
                    int expectedRank = 13 - (j - (column.size() - 13)); // K(13)到A(1)
                    
                    // 检查牌的点数、花色和朝向是否正确
                    if (card.getRank() != expectedRank || card.getSuit() != suit || !card.isFaceUp()) {
                        isComplete = false;
                        break;
                    }
                }
                
                // 如果找到完整的牌组，移除它
                if (isComplete) {
                    // 移除完整的牌组
                    List<Card> removedCards = new ArrayList<>();
                    for (int j = 0; j < 13; j++) {
                        removedCards.add(column.pop());
                    }
                    
                    // 记录移除操作以便撤销(to=-2表示移到完成区域)
                    state.undoStack.push(new Move(i, -2, removedCards, false, true, null));
                    // 增加完成牌组计数和分数
                    state.completedSets++;
                    state.score += 100; // 移除完整牌组加分
                    removed = true;
                    
                    // 如果该列还有牌且是背面朝上，自动翻成正面
                    if (!column.isEmpty() && !column.peek().isFaceUp()) {
                        column.peek().flip();
                    }
                }
            }
        }
        
        return removed;
    }
    
    // 检查游戏是否胜利
    public boolean isGameWon() {
        return state.completedSets == 8;
    }
}
