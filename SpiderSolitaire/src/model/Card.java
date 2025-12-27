package model;

public class Card {
    public enum Suit { SPADES, HEARTS, CLUBS, DIAMONDS }

    private Suit suit;
    private int rank; // 1~13 (A~K)
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
    
    // 将数字rank转换为对应的符号（A, 2, 3, 4, 5, 6, 7, 8, 9, 10, J, Q, K）
    public String getRankSymbol() {
        switch (rank) {
            case 1: return "A";
            case 11: return "J";
            case 12: return "Q";
            case 13: return "K";
            default: return String.valueOf(rank);
        }
    }

    @Override
    public String toString() {
        return (faceUp ? getRankSymbol() + "-" + suit : "XX");
    }
}
