package se.cygni.texasholdem.player;

import se.cygni.texasholdem.client.CurrentPlayState;
import se.cygni.texasholdem.game.Card;
import se.cygni.texasholdem.game.definitions.Rank;
import se.cygni.texasholdem.game.definitions.Suit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by HalfLeif on 2015-04-20.
 */
public class Scoring {

    public static class CardCounter{
        public final static double INIT_SUIT = 13.0;
        public final static double INIT_RANK = 4.0;

        private final Map<Rank,Double> rankMap = new HashMap<Rank, Double>(13);
        private final Map<Suit,Double> suitMap = new HashMap<Suit, Double>(4);
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

    public static double probabilityOfFlush(CurrentPlayState state){
        List<Card> cards = state.getMyCardsAndCommunityCards();
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
            // TODO prob. of finding X or more cards of Suit s out of Y unknown cards
        }

        return flushProb;
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
