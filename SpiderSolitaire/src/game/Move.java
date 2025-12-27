package game;

import java.util.List;
import model.Card;

public class Move {
    public int from;
    public int to;
    public List<Card> movedCards;
    public boolean flipped;
    public boolean wasFaceUpBeforeMove;
    public Card flippedCard;  // 保存被翻面的那张牌的引用

    public Move(int from, int to, List<Card> movedCards, boolean flipped, boolean wasFaceUpBeforeMove, Card flippedCard) {
        this.from = from;
        this.to = to;
        this.movedCards = movedCards;
        this.flipped = flipped;
        this.wasFaceUpBeforeMove = wasFaceUpBeforeMove;
        this.flippedCard = flippedCard;
    }
}
