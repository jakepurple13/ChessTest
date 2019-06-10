package crestron.com.deckofcards;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    private void log(String s) {
        System.out.println(s);
    }

    @Test
    public void deckTest() throws CardNotFoundException {
        Deck d = new Deck(true);

        System.out.println(d.removeColor(Color.BLACK));

        System.out.println(d.removeSuit(Suit.HEARTS));

        System.out.println(d.removeNumber(6));

        System.out.println(d);

        d = new Deck(true);

        log(d.getRandomCard().toString());
        log(d.getCard(4).toString());

    }
}