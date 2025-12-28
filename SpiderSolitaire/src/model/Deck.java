package model;

import java.util.*;

public class Deck {
    private Stack<Card> cards = new Stack<>();

    public Deck(int difficulty) {
        List<Card.Suit> suits = new ArrayList<>();

        if (difficulty == 1) {
            suits.add(Card.Suit.SPADES);
        } else if (difficulty == 2) {
            suits.add(Card.Suit.SPADES);
            suits.add(Card.Suit.HEARTS);
        } else {
            suits.add(Card.Suit.SPADES);
            suits.add(Card.Suit.HEARTS);
            suits.add(Card.Suit.CLUBS);
            suits.add(Card.Suit.DIAMONDS);
        }

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
