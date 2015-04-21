package se.cygni.texasholdem.player;

import se.cygni.texasholdem.game.Card;
import se.cygni.texasholdem.game.definitions.Rank;
import se.cygni.texasholdem.game.definitions.Suit;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Created by HalfLeif on 2015-04-20.
 */
public class Scoring {

    public static void test(){
        List<Card> cards = new ArrayList<Card>();
        cards.add(new Card(Rank.ACE, Suit.CLUBS));
        cards.add(new Card(Rank.KING, Suit.CLUBS));
//        cards.add(new Card(Rank.SEVEN, Suit.CLUBS));
//        cards.add(new Card(Rank.FIVE, Suit.DIAMONDS));
//        cards.add(new Card(Rank.EIGHT, Suit.DIAMONDS));

        System.out.println("Prob of flush: " + probabilityOfFlush(cards));
    }

    public static double probabilityOfFlush(List<Card> cards){
//        List<Card> cards = state.getMyCardsAndCommunityCards();
        int unknownCards = 7 - cards.size();

        CardCounter counter = countCards(cards);

        double flushProb = 0;
        for(Suit s : Suit.values()){
            double remaining = counter.suitMap.get(s);
            double has = CardCounter.INIT_SUIT - remaining;
            if( has >= 5){
                return 1.0;
            }
            if( has + unknownCards < 5){
                continue;
            }
            flushProb += Statistics.drawAtLeast(5 - has, unknownCards, remaining, counter.cardsLeft - remaining);
        }

        return flushProb;
    }

    private static class CardCounter{
        public final static double INIT_SUIT = 13.0;
        public final static double INIT_RANK = 4.0;

        private final double[] rankArr = {13,13,13,13};

        private final Map<Rank,Double> rankMap = new EnumMap<Rank, Double>(Rank.class);
        private final Map<Suit,Double> suitMap = new EnumMap<Suit, Double>(Suit.class);
        private double cardsLeft = 13*4;

        public CardCounter(){
            for(Rank r : Rank.values()){
                rankMap.put(r, INIT_RANK);
            }
            for(Suit s : Suit.values()){
                suitMap.put(s, INIT_SUIT);
            }
        }

        public void decrRank(Rank r){
            double a = rankMap.get(r);
            rankMap.put(r, a-1);
            cardsLeft--;
        }

        public void decrSuit(Suit s){
            double a = suitMap.get(s);
            suitMap.put(s, a-1);
            cardsLeft--;
        }
    }

    private static CardCounter countCards(List<Card> cards){
        CardCounter counter = new CardCounter();
        for(Card c : cards){
            counter.decrRank(c.getRank());
            counter.decrSuit(c.getSuit());
        }
        return counter;
    }
}
