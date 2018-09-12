package crestron.com.deckofcards;

@SuppressWarnings("hiding")
public class HandNode<Card> {

    Card data;
    HandNode<Card> left;
    HandNode<Card> right;


    public HandNode(Card data) {
        this.data = data;

    }

    public HandNode(Card data, HandNode<Card> left, HandNode<Card> right) {

        this(data);
        this.left = left;
        this.right = right;

    }

    public Card getData() {
        return data;
    }

    public void setData(Card data) {
        this.data = data;
    }

    public HandNode<Card> getLeft() {
        return left;
    }

    public void setLeft(HandNode<Card> left) {
        this.left = left;
    }

    public HandNode<Card> getRight() {
        return right;
    }

    public void setRight(HandNode<Card> right) {
        this.right = right;
    }

    @Override
    public String toString() {
        return data.toString();
    }


}
