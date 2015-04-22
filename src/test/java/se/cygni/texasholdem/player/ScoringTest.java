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
public class ScoringTest {

    @Test
    public void twoPairPositive(){
        List<Card> cards = new ArrayList<Card>();
        cards.add(new Card(Rank.ACE, Suit.CLUBS));
        cards.add(new Card(Rank.ACE, Suit.HEARTS));

        cards.add(new Card(Rank.KING, Suit.DIAMONDS));
        cards.add(new Card(Rank.KING, Suit.HEARTS));

        assert Statistics.closeEnough(1.0 - Scoring.probability_nOfAKind(2, cards, Scoring.countCards(cards)));
    }

    @Test
    public void fullHousePositive(){
        List<Card> cards = new ArrayList<Card>();
        cards.add(new Card(Rank.ACE, Suit.CLUBS));
        cards.add(new Card(Rank.ACE, Suit.HEARTS));

        cards.add(new Card(Rank.KING, Suit.DIAMONDS));
        cards.add(new Card(Rank.KING, Suit.HEARTS));
        cards.add(new Card(Rank.KING, Suit.CLUBS));

        assert Statistics.closeEnough(1.0 - Scoring.probability_nOfAKind(3, cards, Scoring.countCards(cards)));
    }

    @Test
    public void justRun(){
        List<Card> cards = new ArrayList<Card>();
//        cards.add(new Card(Rank.ACE, Suit.CLUBS));
//        cards.add(new Card(Rank.ACE, Suit.DIAMONDS));

        cards.add(new Card(Rank.KING, Suit.DIAMONDS));
        cards.add(new Card(Rank.QUEEN, Suit.DIAMONDS));
        cards.add(new Card(Rank.JACK, Suit.DIAMONDS));
        cards.add(new Card(Rank.TEN, Suit.DIAMONDS));

//        cards.add(new Card(Rank.KING, Suit.HEARTS));
//        cards.add(new Card(Rank.KING, Suit.CLUBS));

//        cards.add(new Card(Rank.EIGHT, Suit.CLUBS));
//        cards.add(new Card(Rank.FIVE, Suit.CLUBS));
//        cards.add(new Card(Rank.TEN, Suit.CLUBS));

//        double house = Scoring.probability_fullHouse(cards, Scoring.countCards(cards));
//        double twoPair = Scoring.probability_twoPair(cards, Scoring.countCards(cards));

//        System.out.println("Probability of Full house: "+house);
//        System.out.println("Probability of Two pair: "+twoPair);

        System.out.println(Scoring.probabilityStraightFlush(cards, Scoring.countCards(cards)));
//        System.out.println(Scoring.probabilityRoyalFlush(cards, Scoring.countCards(cards)));

//        Scoring.probabilities(cards);

//        System.out.println(Statistics.atLeastOneOfSeveral(1,4,49));
//        for(int ix=1; ix<=5; ++ix){
//            System.out.println(Statistics.atLeastOneOfSeveral(ix,4,49));
//        }

    }

    @Test
    public void experiment(){
        for(Rank r : Rank.values()){
            System.out.println(r.getName()+": "+r.ordinal());
        }
    }
}
