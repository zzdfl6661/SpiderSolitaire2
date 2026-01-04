package model;

import java.awt.Color;
import java.io.Serializable;

/**
 * 扑克牌类
 * 表示一张扑克牌，包含花色、点数和朝面状态等属性
 * 支持获取牌面信息、翻牌等基本操作
 * 
 * 这个类封装了扑克牌的所有基本属性和行为：
 * - 花色（spades黑桃、hearts红桃、clubs梅花、diamonds方块）
 * - 点数（1-13分别表示A、2-10、J、Q、K）
 * - 朝面状态（正面朝上或背面朝上）
 * - 各种显示和查询方法
 */
public class Card implements Serializable {
    /**
     * 花色枚举
     * 表示扑克牌的四种花色：黑桃、红桃、梅花和方块
     * 
     * enum（枚举）是Java中的一种特殊数据类型，它是一组固定数量的常量
     * Suit枚举继承自java.lang.Enum，每个枚举常量都是Suit类的一个实例
     * - SPADES: 黑桃，符号♠，在游戏中通常显示为黑色
     * - HEARTS: 红桃，符号♥，在游戏中通常显示为红色
     * - CLUBS: 梅花，符号♣，在游戏中通常显示为黑色
     * - DIAMONDS: 方块，符号♦，在游戏中通常显示为红色
     */
    public enum Suit { SPADES, HEARTS, CLUBS, DIAMONDS }
    //enum枚举在Java中是一种特殊的类，它继承自 java.lang.Enum ，

    /**
     * 牌的花色
     * 
     * 这是一个私有字段，通过getSuit()方法访问
     * 使用private访问修饰符确保外部不能直接修改花色，保持封装性
     * 
     * @see #getSuit()
     */
    private Suit suit;
    
    /**
     * 牌的点数，1-13分别表示A-K
     * 
     * 1表示A（Ace），11表示J（Jack），12表示Q（Queen），13表示K（King）
     * 这是扑克牌的数值属性，用于游戏规则判断（如是否可以移动等）
     * 
     * @see #getRank()
     * @see #getRankSymbol()
     */
    private int rank;
    
    /**
     * 牌是否正面朝上
     * 
     * true表示正面朝上可见（显示点数和花色），false表示背面朝上不可见（显示背面图案）
     * 这个字段控制着牌的显示状态和游戏逻辑
     * 初始状态为false（新创建的牌默认是背面朝上的）
     * 
     * @see #isFaceUp()
     * @see #flip()
     */
    private boolean faceUp;

    /**
     * 构造函数
     * 创建一张新的扑克牌，默认为背面朝上
     * 
     * 这是Card类的唯一构造函数，需要指定花色和点数来创建牌
     * 新创建的牌默认为背面朝上状态（faceUp = false）
     * 
     * @param suit 牌的花色，必须是Suit枚举中的值之一（SPADES, HEARTS, CLUBS, DIAMONDS）
     * @param rank 牌的点数，范围1-13：
     *        1=A（Ace），2-10=数字牌，11=J（Jack），12=Q（Queen），13=K（King）
     * 
     * @throws IllegalArgumentException 如果rank不在1-13范围内时
     * @see Suit
     */
    public Card(Suit suit, int rank) {
        // 验证参数有效性
        if (rank < 1 || rank > 13) {
            throw new IllegalArgumentException("牌的点数必须在1-13范围内，传入的值是: " + rank);
        }
        
        // 初始化牌的属性
        this.suit = suit;    // 设置花色
        this.rank = rank;    // 设置点数
        this.faceUp = false; // 默认为背面朝上
    }

    /**
     * 获取牌的花色
     * 
     * 这是花色属性的访问器方法（getter），遵循JavaBean命名规范
     * 提供对私有字段suit的只读访问，保持封装性
     * 
     * @return 牌的花色枚举值，Suit类型：
     *         SPADES（黑桃）、HEARTS（红桃）、CLUBS（梅花）、DIAMONDS（方块）
     * 
     * @see Suit
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
     * 
     * 这是牌的朝面状态修改器方法（setter），用于切换牌的显示状态
     * 如果牌当前是正面朝上，翻转后会变成背面朝上，反之亦然
     * 这个操作是瞬时的，一次调用就能完成翻转
     * 
     * 在蜘蛛纸牌游戏中，这个方法通常在以下情况调用：
     * - 发牌时将牌翻转为正面朝上
     * - 移动牌后自动翻开下一张牌
     * - 撤销操作时恢复牌的原始状态
     * 
     * @see #isFaceUp()
     */
    public void flip() { faceUp = !faceUp; }
    
    /**
     * 获取牌的点数符号
     * 将数字1-13转换为标准的扑克牌点数符号：A, J, Q, K
     * @return 牌的点数符号字符串
     */
    public String getRankSymbol() {
        // 使用switch语句根据点数返回对应的符号
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
     * 
     * 使用Unicode字符表示扑克牌花色符号
     * 这些符号可以直接在支持Unicode的字体中显示，无需额外图片资源
     * 
     * 花色符号对应关系：
     * - SPADES(黑桃) → ♠ (Unicode: U+2660)
     * - HEARTS(红桃) → ♥ (Unicode: U+2665)
     * - CLUBS(梅花) → ♣ (Unicode: U+2663)
     * - DIAMONDS(方块) → ♦ (Unicode: U+2666)
     * 
     * @return 牌的花色符号字符串，用于UI显示
     * 
     * @see Suit
     */
    public String getSuitSymbol() {
        // 使用switch语句根据花色返回对应的Unicode符号
        switch (suit) {
            case SPADES: return "♠";   // 黑桃符号
            case HEARTS: return "♥";   // 红桃符号
            case CLUBS: return "♣";    // 梅花符号
            case DIAMONDS: return "♦"; // 方块符号
            default: return "";         // 理论上不会执行到这里
        }
    }

    /**
     * 获取牌的花色颜色
     * 红桃和方块显示为红色，黑桃和梅花显示为黑色
     * @return 牌的花色颜色（红色或黑色）
     */
    public Color getSuitColor() {
        // 根据花色枚举值返回对应的颜色
        switch (suit) {
            case HEARTS:     // 红桃
            case DIAMONDS:   // 方块
                return Color.RED;   // 返回红色
            default:         // 黑桃和梅花
                return Color.BLACK; // 返回黑色
        }
    }

    /**
     * 将牌转换为字符串表示
     * 
     * 这是Object类的toString()方法的重写，用于提供对象的字符串表示
     * 当需要将Card对象转换为字符串时（如打印、调试、显示等），会自动调用此方法
     * 
     * 返回格式：
     * - 如果牌正面朝上（faceUp为true）：返回"点数符号-花色"的格式，如"A-SPADES"、"10-HEARTS"
     * - 如果牌背面朝上（faceUp为false）：返回"XX"，表示未知牌面
     * 
     * @return 牌的字符串表示，用于显示和调试
     * 
     * @see Object#toString()
     * @see #isFaceUp()
     * @see #getRankSymbol()
     * @see #getSuit()
     */
    @Override
    public String toString() {
        return (faceUp ? getRankSymbol() + "-" + suit : "XX");
    }
    /** 如果牌正面朝上（ faceUp 为 true），返回格式如 "A-SPADES" 的字符串
     *如果牌背面朝上（ faceUp 为 false），返回 "XX" （表示未知牌面）
     *toString() 是一个特殊的方法，当需要将对象转换为字符串表示时会自动调用
     *在以下情况会自动调用 toString() ：
     *打印对象时： System.out.println(card);
     *字符串连接时： String info = "牌面: " + card;
     *对象被转换为字符串时
     */
}
