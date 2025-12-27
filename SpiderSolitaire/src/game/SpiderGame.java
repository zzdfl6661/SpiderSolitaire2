package game;

import java.util.*;
import model.Card;

public class SpiderGame {
    private GameState state;

    public SpiderGame(int difficulty) {
        state = new GameState(difficulty);
    }

    public GameState getState() {
        return state;
    }

    public boolean canMove(int from, int to, int count) {
        Stack<Card> src = state.columns[from];
        Stack<Card> dst = state.columns[to];

        if (src.size() < count) return false;

        Card base = src.get(src.size() - count); // 最底部牌
        Card top = src.peek(); // 最顶部牌
        
        // 检查K只能移动到空位上
        if (base.getRank() == 13 && !dst.isEmpty()) {
            return false; // K只能移动到空位
        }
        
        // 检查要移动的牌组是否都是正面朝上、同花色且连续递减
        Card.Suit suit = base.getSuit(); // 牌组花色
        for (int i = src.size() - count; i < src.size(); i++) {
            Card current = src.get(i);
            if (!current.isFaceUp()) {
                return false; // 所有牌必须正面朝上
            }
            if (current.getSuit() != suit) {
                return false; // 必须同花色
            }
        }
        
        // 检查是否连续递减
        for (int i = src.size() - count; i < src.size() - 1; i++) {
            Card current = src.get(i);
            Card next = src.get(i + 1);
            if (current.getRank() != next.getRank() + 1) {
                return false; // 必须连续递减
            }
        }

        if (!dst.isEmpty()) {
            Card dstTop = dst.peek();
            
            // 检查A只能接在2下面
            if (base.getRank() == 1 && dstTop.getRank() != 2) {
                return false; // A只能接在2下面
            }
            
            // 检查目标牌是否比基础牌大1
            return dstTop.getRank() == base.getRank() + 1;
        }
        
        return true;
    }

    public void move(int from, int to, int count) {
        // 先验证移动是否合法
        if (!canMove(from, to, count)) {
            return; // 如果不合法，直接返回
        }
        
        Stack<Card> src = state.columns[from];
        Stack<Card> dst = state.columns[to];

        List<Card> moved = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            moved.add(0, src.pop());
        }
        dst.addAll(moved);

        boolean flipped = false;
        boolean wasFaceUpBeforeMove = true;
        Card flippedCard = null;
        if (!src.isEmpty() && !src.peek().isFaceUp()) {
            src.peek().flip();
            flipped = true;
            wasFaceUpBeforeMove = false;
            flippedCard = src.peek();
        }

        state.undoStack.push(new Move(from, to, moved, flipped, wasFaceUpBeforeMove, flippedCard));
        state.score--;
    }

    public void undo() {
        if (state.undoStack.isEmpty()) return;
        Move m = state.undoStack.pop();

        // 处理发牌操作的撤销
        if (m.from == -1 && m.to == -1) {
            // 从每一列取最后一张牌放回牌堆
            for (int i = 0; i < 10; i++) {
                if (!state.columns[i].isEmpty()) {
                    Card card = state.columns[i].pop();
                    card.flip(); // 翻回背面
                    state.stock.push(card);
                }
            }
        } 
        // 处理移除完整牌组操作的撤销
        else if (m.to == -2) {
            // 将移除的牌组放回原来的列
            Stack<Card> column = state.columns[m.from];
            for (Card card : m.movedCards) {
                column.push(card);
            }
            state.completedSets--;
            state.score -= 100;
        }
        else {
            // 普通移动操作的撤销
            Stack<Card> src = state.columns[m.from];
            Stack<Card> dst = state.columns[m.to];

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
        state.score += 1; // 撤销操作恢复1分
    }

    public String hint() {
        // 遍历所有列，寻找有效的移动
        for (int from = 0; from < 10; from++) {
            if (state.columns[from].isEmpty()) continue;
            
            // 检查从该列可以移动的最大连续牌组
            int maxCount = 1;
            Card baseCard = state.columns[from].peek();
            
            // 从后往前检查连续递减的牌组
            for (int i = state.columns[from].size() - 2; i >= 0; i--) {
                Card current = state.columns[from].get(i);
                Card next = state.columns[from].get(i + 1);
                if (current.isFaceUp() && next.getRank() == current.getRank() + 1) {
                    maxCount++;
                } else {
                    break;
                }
            }
            
            // 检查是否可以移动到其他列
            for (int to = 0; to < 10; to++) {
                if (from == to) continue;
                
                if (canMove(from, to, maxCount)) {
                    return "提示: 可以将第" + (from + 1) + "列的" + maxCount + "张牌移动到第" + (to + 1) + "列";
                }
            }
        }
        
        return "没有可用的移动提示";
    }
    
    // 发牌方法
    public boolean deal() {
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
            card.flip(); // 翻成正面
            state.columns[i].push(card);
            dealtCards.add(card);
        }
        
        // 将发牌操作添加到撤销栈
        state.undoStack.push(new Move(-1, -1, dealtCards, false, true, null));
        // 发牌不扣分，只有合法移动才扣分
        
        return true;
    }
    
    // 检查并移除所有完整的牌组（K到A）
    public boolean checkAndRemoveCompleteSets() {
        boolean removed = false;
        
        for (int i = 0; i < 10; i++) {
            Stack<Card> column = state.columns[i];
            if (column.isEmpty()) continue;
            
            // 检查是否有完整的K到A序列（13张牌）
            if (column.size() >= 13 && column.peek().getRank() == 1) { // 顶部是A
                boolean isComplete = true;
                Card.Suit suit = column.get(column.size() - 13).getSuit(); // 序列的花色
                
                // 从A开始往上检查是否是KQJ...2A序列
                for (int j = column.size() - 13; j < column.size(); j++) {
                    Card card = column.get(j);
                    int expectedRank = 13 - (j - (column.size() - 13)); // K(13)到A(1)
                    
                    if (card.getRank() != expectedRank || card.getSuit() != suit || !card.isFaceUp()) {
                        isComplete = false;
                        break;
                    }
                }
                
                if (isComplete) {
                    // 移除完整的牌组
                    List<Card> removedCards = new ArrayList<>();
                    for (int j = 0; j < 13; j++) {
                        removedCards.add(column.pop());
                    }
                    
                    // 记录移除操作以便撤销
                    state.undoStack.push(new Move(i, -2, removedCards, false, true, null)); // to=-2表示移到完成区域
                    state.completedSets++;
                    state.score += 100; // 移除完整牌组加分
                    removed = true;
                    
                    // 如果该列还有牌且是背面朝上，翻成正面
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
