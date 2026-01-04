package game;

import java.io.Serializable;
import java.util.List;
import model.Card;

/**
 * 移动记录类
 * 记录游戏中的操作，用于撤销功能
 * 存储移动的起始列、目标列、移动的牌列表以及翻牌状态等信息
 */
public class Move implements Serializable {

    public int from;
    
    /**
     * 移动的目标列索引
     * -1表示发牌操作，-2表示移除完整牌组操作
     */
    public int to;
    
    /**
     * 被移动的牌的列表
     * 包含所有从from列移动到to列的牌
     */
    public List<Card> movedCards;
    
    /**
     * 是否涉及翻牌操作
     * true表示该操作导致了一张牌翻面，false表示没有翻牌
     */
    public boolean flipped;
    
    /**
     * 移动前牌是否正面朝上
     * 用于撤销时精确恢复翻牌状态
     */
    public boolean wasFaceUpBeforeMove;
    
    /**
     * 被翻面的牌的引用
     * 保存具体被翻面的那张牌，用于撤销时精确恢复
     */
    public Card flippedCard;

    /**
     * 构造函数
     * 创建一个移动记录对象
     * @param from 起始列索引
     * @param to 目标列索引
     * @param movedCards 被移动的牌的列表
     * @param flipped 是否涉及翻牌操作
     * @param wasFaceUpBeforeMove 移动前牌是否正面朝上
     * @param flippedCard 被翻面的牌的引用
     */
    public Move(int from, int to, List<Card> movedCards, boolean flipped, boolean wasFaceUpBeforeMove, Card flippedCard) {
        this.from = from;
        this.to = to;
        this.movedCards = movedCards;
        this.flipped = flipped;
        this.wasFaceUpBeforeMove = wasFaceUpBeforeMove;
        this.flippedCard = flippedCard;
    }
}
