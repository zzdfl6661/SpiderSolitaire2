package model;

import java.awt.Color;

/**
 * 扑克牌类
 * 表示一张扑克牌，包含花色、点数和朝面状态等属性
 * 支持获取牌面信息、翻牌等基本操作
 */
public class Card {
    /**
     * 花色枚举
     * 表示扑克牌的四种花色：黑桃、红桃、梅花和方块
     */
    public enum Suit { SPADES, HEARTS, CLUBS, DIAMONDS }

    /**
     * 牌的花色
     */
    private Suit suit;
    
    /**
     * 牌的点数，1-13分别表示A-K
     */
    private int rank;
    
    /**
     * 牌是否正面朝上
     * true表示正面朝上可见，false表示背面朝上不可见
     */
    private boolean faceUp;

    /**
     * 构造函数
     * 创建一张新的扑克牌，默认为背面朝上
     * @param suit 牌的花色
     * @param rank 牌的点数（1-13，A=1，J=11，Q=12，K=13）
     */
    public Card(Suit suit, int rank) {
        this.suit = suit;
        this.rank = rank;
        this.faceUp = false;
    }

    /**
     * 获取牌的花色
     * @return 牌的花色枚举值
     */
    public Suit getSuit() { return suit; }
    
    /**
     * 获取牌的点数
     * @return 牌的点数（1-13）
     */
    public int getRank() { return rank; }
    
    /**
     * 判断牌是否正面朝上
     * @return true表示正面朝上，false表示背面朝上
     */
    public boolean isFaceUp() { return faceUp; }
    
    /**
     * 翻转牌的状态
     * 如果是正面朝上则翻转为背面朝上，反之亦然
     */
    public void flip() { faceUp = !faceUp; }
    
    /**
     * 获取牌的点数符号
     * 将数字1-13转换为标准的扑克牌点数符号：A, J, Q, K
     * @return 牌的点数符号字符串
     */
    public String getRankSymbol() {
        switch (rank) {
            case 1: return "A";
            case 11: return "J";
            case 12: return "Q";
            case 13: return "K";
            default: return String.valueOf(rank);
        }
    }

    /**
     * 获取牌的花色符号
     * 使用Unicode字符表示扑克牌花色：♠, ♥, ♣, ♦
     * @return 牌的花色符号字符串
     */
    public String getSuitSymbol() {
        switch (suit) {
            case SPADES: return "♠";
            case HEARTS: return "♥";
            case CLUBS: return "♣";
            case DIAMONDS: return "♦";
            default: return "";
        }
    }

    /**
     * 获取牌的花色颜色
     * 红桃和方块显示为红色，黑桃和梅花显示为黑色
     * @return 牌的花色颜色（红色或黑色）
     */
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
