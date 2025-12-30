package game;

import java.util.*;
import model.Card;
import model.Deck;

/**
 * 游戏状态管理类
 * 负责维护蜘蛛纸牌游戏的所有状态信息
 */
public class GameState {
    /**
     * 游戏牌列数组，共10列，用于存放玩家手中的牌
     * 每列是一个栈结构，栈顶是可见的牌
     */
    public Stack<Card>[] columns = new Stack[10]; // 10列
    
    /**
     * 牌堆，用于存放还未发到牌列中的剩余牌
     * 当牌堆为空时就不能再发牌了
     */
    public Stack<Card> stock = new Stack<>();     // 剩余牌堆
    
    /**
     * 撤销操作栈，用于存储游戏中的所有操作记录
     * 支持玩家撤销上一步操作，恢复到之前的状态
     */
    public Stack<Move> undoStack = new Stack<>();  // 撤销栈
    
    /**
     * 当前游戏分数，初始值为500分
     * 每次有效移动会扣1分，完成牌组会得100分
     */
    public int score = 500;
    
    /**
     * 已完成的牌组数量，蜘蛛纸牌需要完成8组牌才能获胜
     * 每完成一组K到A的同花色牌组，计数组数加1
     */
    public int completedSets = 0;
    
    /**
     * 剩余发牌次数，初始值为5次
     * 每次发牌操作会减少1次，撤销发牌时会恢复1次
     */
    public int remainingDeals = 5;//发牌次数

    /**
     * 游戏状态构造函数
     * @param difficulty 游戏难度级别：1=单花色(简单)，2=双花色(中等)，4=四花色(困难)
     */
    public GameState(int difficulty) {
        Deck deck = new Deck(difficulty);
        
        // 初始化10个牌列，前4列各6张牌，后6列各5张牌
        for (int i = 0; i < 10; i++) {
            columns[i] = new Stack<>();
            // 前4列发6张牌，后6列发5张牌
            for (int j = 0; j < (i < 4 ? 6 : 5); j++) {//内层循环控制每个牌列的发牌数量
                //Java 中唯一的三目运算符，本质是简化的 if-else,前4列发6张，后四列发5张
                Card card = deck.draw();
                // 每列的最底部牌(最后发的牌)需要翻面显示为正面
                if (j == (i < 4 ? 5 : 4)) card.flip(); // 最底部的牌翻面
                //判断当前发的是否是该列最后一张牌，如果是则执行 card.flip() 翻面,
                columns[i].push(card);//将抽到的牌压入当前牌列的栈中push()是栈的 “压栈” 操作，新牌会成为栈顶，即视觉上最上方的牌
            }
        }
        
        // 将牌堆中剩余的牌存入stock中，用于后续发牌
        while (!deck.isEmpty()) {
            stock.push(deck.draw());
        }
    }
}
