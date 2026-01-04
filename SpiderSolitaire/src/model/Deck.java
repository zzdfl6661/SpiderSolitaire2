package model;

import java.util.*;//直接导入整个包

/**
 * 牌堆类
 * 表示蜘蛛纸牌的牌堆，包含104张牌（8副牌的组合）
 * 根据游戏难度（单花色、双花色、四花色）生成不同的牌堆
 */
public class Deck {
    /**
     * 牌的栈
     * 使用栈结构存储牌堆中的牌，栈顶是下一张要发的牌
     */
    private Stack<Card> cards = new Stack<>();

    /**
     * 构造函数
     * 根据游戏难度创建牌堆
     * 蜘蛛纸牌使用8副牌（104张牌）
     * @param difficulty 游戏难度：1=单花色，2=双花色，4=四花色
     */
    public Deck(int difficulty) {
        // 创建花色列表，根据难度添加不同数量的花色
        List<Card.Suit> suits = new ArrayList<>();

        // 根据难度设置花色数量
        if (difficulty == 1) {
            // 简单模式：只使用黑桃一种花色
            suits.add(Card.Suit.SPADES);
        } else if (difficulty == 2) {
            // 中等模式：使用黑桃和红桃两种花色
            suits.add(Card.Suit.SPADES);
            suits.add(Card.Suit.HEARTS);
        } else {
            // 困难模式：使用全部四种花色
            suits.add(Card.Suit.SPADES);
            suits.add(Card.Suit.HEARTS);
            suits.add(Card.Suit.CLUBS);
            suits.add(Card.Suit.DIAMONDS);
        }

        // 生成8副牌（104张牌）
        for (int i = 0; i < 8; i++) {
            // 为每种花色生成13张牌（A-K）
            for (Card.Suit suit : suits) {
                for (int rank = 1; rank <= 13; rank++) {
                    cards.push(new Card(suit, rank));
                }
            }
        }
        
        // 洗牌，随机打乱牌堆顺序
        Collections.shuffle(cards);
    }

    /**
     * 从牌堆中抽一张牌
     * @return 抽取的牌，如果牌堆为空则返回null
     * 如果牌堆为空（ cards.isEmpty() 为 true），返回 null （表示没有牌可抽）
     * 如果牌堆不为空，调用 cards.pop() 从牌堆中取出一张牌（通常是牌堆顶部的牌）
     */
    public Card draw() {
        return cards.isEmpty() ? null : cards.pop();
    }

    /**
     * 检查牌堆是否为空
     * @return true表示牌堆中没有牌，false表示还有牌
     */
    public boolean isEmpty() {
        return cards.isEmpty();
    }
}
