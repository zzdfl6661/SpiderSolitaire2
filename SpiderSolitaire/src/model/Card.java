package model;

import java.awt.Color;

public class Card {
    public enum Suit { SPADES, HEARTS, CLUBS, DIAMONDS }

    private Suit suit;
    private int rank;
    private boolean faceUp;

    public Card(Suit suit, int rank) {
        this.suit = suit;
        this.rank = rank;
        this.faceUp = false;
    }

    public Suit getSuit() { return suit; }
    public int getRank() { return rank; }
    public boolean isFaceUp() { return faceUp; }
    public void flip() { faceUp = !faceUp; }
    
    public String getRankSymbol() {
        switch (rank) {
            case 1: return "A";
            case 11: return "J";
            case 12: return "Q";
            case 13: return "K";
            default: return String.valueOf(rank);
        }
    }

    public String getSuitSymbol() {
        switch (suit) {
            case SPADES: return "♠";
            case HEARTS: return "♥";
            case CLUBS: return "♣";
            case DIAMONDS: return "♦";
            default: return "";
        }
    }

    public Color getSuitColor() {
        switch (suit) {
            case HEARTS:
            case DIAMONDS:
                return Color.RED;
            default:
                return Color.BLACK;
        }
    }

    @Override
    public String toString() {
        return (faceUp ? getRankSymbol() + "-" + suit : "XX");
    }
}
