package se.cygni.texasholdem.player;

import org.junit.Test;
import se.cygni.texasholdem.game.Card;
import se.cygni.texasholdem.game.definitions.Rank;
import se.cygni.texasholdem.game.definitions.Suit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HalfLeif on 2015-04-21.
 */
public class StatTest {

    @Test
    public void testCombinations(){
        assert Statistics.combinations(11,3) == 165.0;
        assert Statistics.combinations(8,3) == 56.0;
        assert Statistics.combinations(8,8) == 1.0;
        assert Statistics.combinations(8,0) == 1.0;
    }

    @Test
    public void testDrawExact(){
        assert Statistics.closeEnough(Statistics.drawExactly(2, 3, 3, 8) - 24.0 / 165.0);
    }

    @Test
    public void justRun(){
        List<Card> cards = new ArrayList<Card>();
        cards.add(new Card(Rank.ACE, Suit.CLUBS));
        cards.add(new Card(Rank.ACE, Suit.HEARTS));
        cards.add(new Card(Rank.ACE, Suit.DIAMONDS));

//        cards.add(new Card(Rank.KING, Suit.CLUBS));
//        cards.add(new Card(Rank.SEVEN, Suit.CLUBS));
//        cards.add(new Card(Rank.FIVE, Suit.DIAMONDS));
//        cards.add(new Card(Rank.EIGHT, Suit.DIAMONDS));

        System.out.println(Scoring.probabilities(cards));
    }
}
