package game;

import java.util.*;
import model.Card;
import model.Deck;

public class GameState {
    public Stack<Card>[] columns = new Stack[10]; // 10列
    public Stack<Card> stock = new Stack<>();     // 剩余牌堆
    public Stack<Move> undoStack = new Stack<>();  // 撤销栈
    public int score = 500;
    public int completedSets = 0;
    public int remainingDeals = 5;

    public GameState(int difficulty) {
        Deck deck = new Deck(difficulty);
        for (int i = 0; i < 10; i++) {
            columns[i] = new Stack<>();
            for (int j = 0; j < (i < 4 ? 6 : 5); j++) {
                Card card = deck.draw();
                if (j == (i < 4 ? 5 : 4)) card.flip(); // 最底部的牌翻面
                columns[i].push(card);
            }
        }
        
        // 将剩余的牌存入牌堆
        while (!deck.isEmpty()) {
            stock.push(deck.draw());
        }
    }
}
