package model;

import java.util.*;

public class Deck {
    private Stack<Card> cards = new Stack<>();

    public Deck(int difficulty) {
        List<Card.Suit> suits = new ArrayList<>();

        // 无论难度如何，都只使用一个花色（黑桃）
        suits.add(Card.Suit.SPADES);

        // 每种花色的牌有13张，总共8组
        for (int i = 0; i < 8; i++) {
            for (Card.Suit suit : suits) {
                for (int rank = 1; rank <= 13; rank++) {
                    cards.push(new Card(suit, rank));
                }
            }
        }
        Collections.shuffle(cards);
    }

    public Card draw() {
        return cards.isEmpty() ? null : cards.pop();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }
}
