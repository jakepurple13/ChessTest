package crestron.com.deckofcards;

import java.util.ArrayList;
import java.util.Arrays;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeMap;


public class Hand {

    ArrayList<Card> hands;
    TreeMap<Integer, Card> tm;
    HandNode<Card> listing;
    String name;

    public Hand() {
        hands = new ArrayList<Card>();
        tm = new TreeMap<Integer, Card>();
    }

    public Hand(String name) {
        hands = new ArrayList<Card>();
        tm = new TreeMap<Integer, Card>();
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String s) {
        name = s;
    }

    public ArrayList<Card> getHand() {
        return hands;
    }

    public void add(Card c) {
        hands.add(c);
        tm.put(c.getValue(), c);
    }

    public void add(List<Card> c) {
        hands.addAll(c);
    }


    public Card getCard(int num) {
        return hands.get(num);
    }

    public Card removeCard(int num) {
        return hands.get(num);
    }

    public void replaceCard(int place, Card c) {
        hands.set(place, c);
    }

    @Override
    public String toString() {
        String q = name + " | ";

        for (int i = 0; i < hands.size(); i++) {
            q += hands.get(i) + "\t";
        }

        return q;
    }


    public String toStrings() {
        String q = name + " | ";
        Iterator<Card> ir = tm.values().iterator();
        for (int i = 0; i < tm.size(); i++) {
            q += ir.next() + "\t";
            if (!ir.hasNext()) {
                break;
            }
        }

        return q;
    }

    public void sortHandByValue() {
        sortHandByValue(hands);
    }

    private void sortHandByValue(ArrayList<Card> q) {
        Object[] a = hands.toArray();
        Arrays.sort(a);
        ListIterator<Card> i = hands.listIterator();
        for (int j = 0; j < a.length; j++) {
            i.next();
            i.set((Card) a[j]);
        }
    }

    public void sortHandBySuit() {
        sortHandBySuit(hands);
    }

    private void sortHandBySuit(ArrayList<Card> q) {
        ArrayList<Card> spades = new ArrayList<Card>();
        ArrayList<Card> hearts = new ArrayList<Card>();
        ArrayList<Card> clubs = new ArrayList<Card>();
        ArrayList<Card> diamonds = new ArrayList<Card>();

        for (Card c : q) {
            if (c.getSuit().equals(Suit.SPADES)) {
                spades.add(c);
            } else if (c.getSuit().equals(Suit.CLUBS)) {
                clubs.add(c);
            } else if (c.getSuit().equals(Suit.DIAMONDS)) {
                diamonds.add(c);
            } else if (c.getSuit().equals(Suit.HEARTS)) {
                hearts.add(c);
            }
        }

        q.clear();

        q.addAll(spades);
        q.addAll(clubs);
        q.addAll(diamonds);
        q.addAll(hearts);

    }

    public void clearHand() {
        hands.clear();
    }

}
