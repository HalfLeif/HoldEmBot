package se.cygni.texasholdem.player;

import se.cygni.texasholdem.game.Card;
import se.cygni.texasholdem.game.definitions.PokerHand;
import se.cygni.texasholdem.game.definitions.Rank;
import se.cygni.texasholdem.game.definitions.Suit;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Created by HalfLeif on 2015-04-20.
 */
public class Scoring {

    /**
     * @return Positive if a is better than b, 0 if ties, negative otherwise.
     */
    public static int compareHands(PokerHand a, PokerHand b) {
        return a.getOrderValue() - b.getOrderValue();
    }

    /**
     * Calculates and compares the chance of winning for user's hand and
     * the chance for all other players' hands, given the known cards.
     *
     * @param myCardsAndCommunity
     * @param community
     * @return Probability of winning in this specific round.
     */
    public static double chanceOfWinning(List<Card> myCardsAndCommunity, List<Card> community){
        final double myChance = Scoring.chanceToWinAgainstUniform(myCardsAndCommunity);
        final double theirChance = Scoring.chanceToWinAgainstUniform(community);

        final double combinedChance = myChance / (myChance + theirChance);

        return combinedChance;
    }

    /**
     * @param cards Total hand.
     * @return Probability of winning against a uniform hand.
     */
    public static double chanceToWinAgainstUniform(List<Card> cards){
        Map<PokerHand, Double> probMap = probabilities(cards);
        Map<PokerHand, Double> scoreMap = Statistics.score();

        double prob = 0.0;
        for(PokerHand h : PokerHand.values()){
            prob += probMap.get(h)*scoreMap.get(h);
        }
        return prob;
    }

    /**
     * Calculates a probability distribution over all PokerHands:
     * The chance of getting this PokerHand in SHOWDOWN.
     * Also uses information about the unknown cards (that have not yet been turned).
     *
     * @param cards All known cards
     * @return Probability distribution
     */
    public static Map<PokerHand, Double> probabilities(List<Card> cards){
        final CardCounter counter = countCards(cards);
        final Map<PokerHand, Double> map = new EnumMap<PokerHand, Double>(PokerHand.class);

        for(PokerHand h : PokerHand.values()){
            final double p = probabilityPokerHand(h, cards, counter);
            map.put(h,p);
        }

        // Make probabilities (more) disjoint!
        decrement(map, PokerHand.HIGH_HAND, map.get(PokerHand.STRAIGHT));

        final double straightFlushUnion = map.get(PokerHand.STRAIGHT_FLUSH);
        decrement(map, PokerHand.STRAIGHT, straightFlushUnion);
        decrement(map, PokerHand.FLUSH, straightFlushUnion);
        decrement(map, PokerHand.STRAIGHT_FLUSH, map.get(PokerHand.ROYAL_FLUSH));

        final double house = map.get(PokerHand.FULL_HOUSE);
        decrement(map, PokerHand.THREE_OF_A_KIND, house);
        decrement(map, PokerHand.TWO_PAIRS, house);
//        decrement(map, PokerHand.ONE_PAIR, 2*map.get(PokerHand.TWO_PAIRS));
        decrement(map, PokerHand.ONE_PAIR, map.get(PokerHand.TWO_PAIRS));

        double probSum = 0.0;
        boolean surpassed = false;
        for(PokerHand h : PokerHand.values()){
            double p = map.get(h);

            if(surpassed){
//                System.out.println(h.getName()+" is surpassed!");
                map.put(h, 0.0);
                p = 0.0;
            }

//            System.out.println("Before, "+h.getName()+":\t"+p);
            probSum += p;

            if(Statistics.closeEnough(p-1.0)){
                surpassed = true;
            }
        }
//        System.out.println("Total probability: "+probSum+"\n");

//        double TEST = 0.0;
        // Normalize the distribution (since is approximation)
        for(PokerHand h : PokerHand.values()){
//            System.out.println(map.get(h));
            final double p = map.get(h) / probSum;
//            System.out.println(p);
//            TEST += p;
            map.put(h, p);

//            System.out.println("After, "+h.getName()+":\t"+map.get(h));
        }
//        System.out.println("Sum after normalization: "+TEST);


        return map;
    }

    private static void decrement(Map<PokerHand, Double> map, PokerHand h, double d){
        double p = map.get(h) - d;
        if(p < 0.0){
            p = 0.0;
        }
        map.put(h, p);
    }

//        CurrentPlayState state
//        List<Card> cards = state.getMyCardsAndCommunityCards();
    public static double probabilityPokerHand(PokerHand h, List<Card> cards, CardCounter counter){
        switch (h){
            case ROYAL_FLUSH:
                return probabilityRoyalFlush(cards, counter);
            case STRAIGHT_FLUSH:
                return probabilityStraightFlush(cards, counter);
            case FOUR_OF_A_KIND:
                return probability_nOfAKind(4, cards, counter);
            case FULL_HOUSE:
                return probability_fullHouse(cards, counter);
            case FLUSH:
                return probabilityFlush(cards, counter);
            case STRAIGHT:
                return probabilityStraight(cards, counter);
            case THREE_OF_A_KIND:
                return probability_nOfAKind(3, cards, counter);
            case TWO_PAIRS:
                return probability_twoPair(cards, counter);
            case ONE_PAIR:
                return probability_nOfAKind(2, cards, counter);
            case HIGH_HAND:
                return probabilityHighHand(cards, counter);
            case NOTHING:
                return 0.0;
            default:
                System.out.println("Unimplemented hand: "+h.getName());
                return 0.0;
        }
    }

    public static double probabilityRoyalFlush(List<Card> cards, CardCounter counter) {
        final int unknownCards = 7 - cards.size();

        // TODO optimize with matrix instead? ie if slow.
        double prob = 0.0;
        for(Suit s : Suit.values()){
            boolean[] arr = new boolean[13];
            for(Card c : cards){
                if(c.getSuit().equals(s)){
                    arr[c.getRank().ordinal()] = true;
                }
            }

            int missing = 0;
            for(int ix = 8; ix<13; ++ix){
                if(! arr[ix]){
                    ++missing;
                }
            }
            if(missing == 0){
                return 1.0;
            }
            prob += Statistics.atLeastOneOfSeveral(1, missing, unknownCards, counter.cardsLeft);
        }
        return prob;
    }

    public static double probabilityStraightFlush(List<Card> cards, CardCounter counter) {
        final int unknownCards = 7 - cards.size();

        double prob = 0.0;
        for(Suit s : Suit.values()){
            boolean[] arr = new boolean[13];
            for(Card c : cards){
                if(c.getSuit().equals(s)){
                    arr[c.getRank().ordinal()] = true;
                }
            }

            int missing = 0;
            for(int ix = 0; ix<13; ++ix){
                if(! arr[ix]){
                    ++missing;
                }
                if(ix >= 4){
                    if(missing == 0){
                        return 1.0;
                    }
                    prob += Statistics.atLeastOneOfSeveral(1, missing, unknownCards, counter.cardsLeft);
                    if(! arr[ix-4]){
                        --missing;
                    }
                }
            }
        }

        return prob;
    }

    public static double probabilityStraight(List<Card> cards, CardCounter counter) {
        final int unknownCards = 7 - cards.size();

        boolean[] arr = new boolean[13];
        for(Card c : cards){
            arr[c.getRank().ordinal()] = true;
        }

        double prob = 0.0;
        int missing = 0;
        for(int ix = 0; ix<13; ++ix){
            if(! arr[ix]){
                ++missing;
            }
            if(ix >= 4){
                if(missing == 0){
                    return 1.0;
                }
                prob += Statistics.atLeastOneOfSeveral(4, missing, unknownCards, counter.cardsLeft);
                if(! arr[ix-4]){
                    --missing;
                }
            }
        }
        return prob;
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

    /**
     * @return Probability of not getting a single pair.
     */
    public static double probabilityHighHand(List<Card> cards, CardCounter counter) {
        final int unknownCards = 7 - cards.size(); // k
        final int ranksLeft = 13 - cards.size();   // n

        for(Rank r : Rank.values()){
            if( counter.rankMap.get(r) < CardCounter.INIT_RANK-1 ){
                // There is a pair already
                return 0.0;
            }
        }

//        double prob = 1.0;
        return Math.pow(4.0,unknownCards)
                * Statistics.combinations(ranksLeft, unknownCards)
                / Statistics.combinations(counter.cardsLeft, unknownCards);

//        return prob;
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
            prob += Statistics.drawExactly(n - hasDrawn, unknownCards, numInDeck, counter.cardsLeft - numInDeck);
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
