package se.cygni.texasholdem.player;

import se.cygni.texasholdem.game.Card;
import se.cygni.texasholdem.game.Deck;
import se.cygni.texasholdem.game.definitions.PokerHand;
import se.cygni.texasholdem.game.util.PokerHandUtil;

import java.util.*;

/**
 * Created by HalfLeif on 2015-04-20.
 */
public class Statistics {

    public static double factorial(final double n){
        double result = 1.0;
        for(double ix=2.0; ix<=n; ++ix){
            result *= ix;
        }
        return result;
    }

    public static double combinations(double n, double k){
        return (factorial(n)/factorial(k)) / factorial(n-k);
    }

    /**
     *
     * @param wants
     * @param draws
     * @param gems
     * @param rocks
     * @return Probability of getting exactly so many gems when drawing
     */
    public static double drawExactly(final double wants, final double draws, double gems, double rocks){
        if(wants >  draws){
            return 0.0;
        }
        final double total = combinations(gems+rocks, draws);

        double combs = 1;
//        double g = gems;
        for(int ix=0; ix<wants; ++ix){
            combs *= gems;
            --gems;
        }
        combs /= factorial(wants);

        final double others = draws - wants;
//        double r = rocks;
        for(int ix=0; ix<others; ++ix){
            combs *= rocks;
            --rocks;
        }
        combs /= factorial(others);

        System.out.println("Found "+combs);
        return combs / total;
    }

    public static double drawAtLeast(final double wants, final double draws, final double gems, final double rocks){
        double prob = 0.0;
        for(double ix=wants; ix <= draws; ++ix){
            prob += drawExactly(ix, draws, gems, rocks);
        }
        return prob;
    }

    /**
     *
     * @return Probability-map of getting such a hand. Is a PDF.
     */
    public static Map<PokerHand,Double> priors(){
        Map<PokerHand,Double> map = new EnumMap<PokerHand, Double>(PokerHand.class);

        //map.put(PokerHand.ROYAL_FLUSH, 1.0/(26.0*51*25*49*48*45));
        map.put(PokerHand.ROYAL_FLUSH, 2.85e-10);
        map.put(PokerHand.STRAIGHT_FLUSH, 1124.0/1e6);
        map.put(PokerHand.FOUR_OF_A_KIND, 4071.0/1e6);
        map.put(PokerHand.FULL_HOUSE, 56593.0/1e6);
        map.put(PokerHand.FLUSH, 51874.0/1e6);
        map.put(PokerHand.STRAIGHT, 62701.0/1e6);
        map.put(PokerHand.THREE_OF_A_KIND, 54959.0/1e6);
        map.put(PokerHand.TWO_PAIRS, 236960.0/1e6);
        map.put(PokerHand.ONE_PAIR, 391068.0/1e6);
        map.put(PokerHand.HIGH_HAND, 140650.0/1e6);
        map.put(PokerHand.NOTHING, 0.0);

        return map;
    }

    /**
     *
     * @return Probability-map of beating a uniform hand.
     */
    public static Map<PokerHand,Double> score(){
        Map<PokerHand,Double> map = new EnumMap<PokerHand, Double>(PokerHand.class);

        map.put(PokerHand.ROYAL_FLUSH, 1.0);
        map.put(PokerHand.STRAIGHT_FLUSH, 998876.0/1e6);
        map.put(PokerHand.FOUR_OF_A_KIND, 994805.0/1e6);
        map.put(PokerHand.FULL_HOUSE, 938212.0/1e6);
        map.put(PokerHand.FLUSH, 886338.0/1e6);
        map.put(PokerHand.STRAIGHT, 823637.0/1e6);
        map.put(PokerHand.THREE_OF_A_KIND, 768678.0/1e6);
        map.put(PokerHand.TWO_PAIRS, 531718.0/1e6);
        map.put(PokerHand.ONE_PAIR, 140650.0/1e6);
        map.put(PokerHand.HIGH_HAND, 0.0);
        map.put(PokerHand.NOTHING, 0.0);

        return map;
    }

    private final Map<PokerHand,Long> handMap = new HashMap<PokerHand, Long>(11);

    private Statistics(){
        for(PokerHand h : PokerHand.values()){
            handMap.put(h,0L);
        }
    }

    /**
     *
     * @param iters
     * @return
     *
     * Out of 1000000 times, got
    ROYAL_FLUSH: 0
    STRAIGHT_FLUSH: 1124
    FOUR_OF_A_KIND: 4071
    FULL_HOUSE: 56593
    FLUSH: 51874
    STRAIGHT: 62701
    THREE_OF_A_KIND: 54959
    TWO_PAIRS: 236960
    ONE_PAIR: 391068
    HIGH_HAND: 140650
    NOTHING: 0
     */
    public static String run(final long iters){
        Statistics s = new Statistics();

        long tenth = iters/10;
        for(long ix=0; ix<iters; ++ix){
            if(ix%tenth == 0){
                System.out.println("Passed "+ix);
            }
            Deck d = Deck.getShuffledDeck();
            List<Card> cardList = new ArrayList<Card>(7);
            for(int c=0; c<7; ++c){
                cardList.add(d.getNextCard());
            }
            PokerHandUtil util = new PokerHandUtil(cardList);
            PokerHand pokerHand = util.getBestHand().getPokerHand();

            Long timesBefore = s.handMap.get(pokerHand);
            s.handMap.put(pokerHand, timesBefore+1);
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Out of "+iters+" times, got ");
        for(PokerHand h : PokerHand.values()){
            stringBuilder.append("\t"+h + ": " + s.handMap.get(h) + "\n");
        }
        return stringBuilder.toString();
    }

}
