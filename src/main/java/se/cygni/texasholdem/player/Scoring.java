package se.cygni.texasholdem.player;

import se.cygni.texasholdem.game.Card;
import se.cygni.texasholdem.game.definitions.Rank;
import se.cygni.texasholdem.game.definitions.Suit;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Created by HalfLeif on 2015-04-20.
 */
public class Scoring {

    public static double probabilities(List<Card> cards){
//        CurrentPlayState state
//        List<Card> cards = state.getMyCardsAndCommunityCards();
        CardCounter counter = countCards(cards);

//        probabilityFlush(cards, counter);
//        return probability_nOfAKind(4,cards,counter);

        return 0.0;
    }

    public static double probabilityFlush(List<Card> cards, CardCounter counter){
        final int unknownCards = 7 - cards.size();

        double flushProb = 0.0;
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

    public static double probability_nOfAKind(final int n, final List<Card> cards, final CardCounter counter){
        final int unknownCards = 7 - cards.size();

        double prob = 0.0;
        for(Rank r : Rank.values()){
            final double numInDeck = counter.rankMap.get(r);
            final double hasDrawn = CardCounter.INIT_RANK - numInDeck;

            if( Statistics.closeEnough(hasDrawn - n)){
                return 1.0;
            }
            if( hasDrawn > n ){
                return 0.0;
            }
            prob += Statistics.drawExactly(n - hasDrawn, unknownCards, numInDeck, counter.cardsLeft - numInDeck );
        }

        return prob;
    }

    public static double probability_twoPair(final List<Card> cards, final CardCounter counter){
        final int unknownCards = 7 - cards.size();
        double mainProb = 0.0;

        for(Rank i : Rank.values()){
            final double iLeft = counter.rankMap.get(i);
            final double iHave = CardCounter.INIT_RANK - iLeft;

            double iProb = 0.0;
            if(iHave >= 2.0){
                iProb = 1.0;
            } else {
                iProb = Statistics.drawAtLeast(2.0-iHave, unknownCards, iLeft, counter.cardsLeft - iLeft);
            }

            for(Rank j : Rank.values()){
                if(i.ordinal() <= j.ordinal()){
                    continue;
                }
                final double jLeft = counter.rankMap.get(j);
                final double jHave = CardCounter.INIT_RANK - jLeft;

                double jProb = 0.0;
                if(jHave >= 2.0){
                    jProb = 1.0;
                } else {
                    jProb = Statistics.drawAtLeast(2.0 - jHave, unknownCards, jLeft, counter.cardsLeft - jLeft);
                }

                if( Statistics.closeEnough(iProb*jProb - 1.0 ) ){
                    return 1.0;
                }

                mainProb += iProb * jProb;
            }
        }

        return mainProb;
    }

    public static double probability_fullHouse(final List<Card> cards, final CardCounter counter){
        final int unknownCards = 7 - cards.size();
        double mainProb = 0.0;

        for(Rank i : Rank.values()){
            final double iLeft = counter.rankMap.get(i);
            final double iHave = CardCounter.INIT_RANK - iLeft;

            double iProb = 0.0;
            if(iHave >= 2.0){
                iProb = 1.0;
            } else {
                iProb = Statistics.drawAtLeast(2.0-iHave, unknownCards, iLeft, counter.cardsLeft - iLeft);
            }

            for(Rank j : Rank.values()){
                if(i.equals(j)){
                    continue;
                }
                final double jLeft = counter.rankMap.get(j);
                final double jHave = CardCounter.INIT_RANK - jLeft;

                double jProb = 0.0;
                if(jHave >= 3.0){
                    jProb = 1.0;
                } else {
                    jProb = Statistics.drawAtLeast(3.0 - jHave, unknownCards, jLeft, counter.cardsLeft - jLeft);
                }

                if( Statistics.closeEnough(iProb*jProb - 1.0 ) ){
                    return 1.0;
                }

                mainProb += iProb * jProb;
            }
        }

        return mainProb;
    }



    protected static class CardCounter{
        public final static double INIT_SUIT = 13.0;
        public final static double INIT_RANK = 4.0;

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

    protected static CardCounter countCards(List<Card> cards){
        CardCounter counter = new CardCounter();
        for(Card c : cards){
            counter.decrRank(c.getRank());
            counter.decrSuit(c.getSuit());
        }
        return counter;
    }
}
