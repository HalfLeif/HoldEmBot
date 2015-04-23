package se.cygni.texasholdem.player;

import se.cygni.texasholdem.game.Card;
import se.cygni.texasholdem.game.Deck;
import se.cygni.texasholdem.game.util.PokerHandUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HalfLeif on 2015-04-22.
 */
public class Simulate {

    private List<Card> playerA;
    private List<Card> playerB;
    private List<Card> community;

    private final static double LIMIT = 0.49;
    private static final int FIELDS = 50;

    private int round = 0;

    private final double[][] estimatesA;
    private final double[][] estimatesB;
    private final int[] playerAWon;
    private final PositiveCounter[] positiveCounters;

    private Simulate(int rounds){
        estimatesA = new double[3][rounds];
        estimatesB = new double[3][rounds];
        playerAWon = new int[rounds];
        positiveCounters = new PositiveCounter[3];
        for(int ix=0; ix<3; ++ix){
            positiveCounters[ix] = new PositiveCounter();
        }
    }

    public static void main(String[] args){
        runSimulation(10000);
    }

    public static void runSimulation(int rounds){
        Simulate s = new Simulate(rounds);
        for(int ix=0; ix<rounds; ++ix){
            s.oneRound();
        }
        s.summary();
    }

    /**
     Played for 10000 rounds.
     A expected 0.47505816093448566
     B expected 0.476388490642104

     A won 0.2857
     B won 0.2881
     Tied  0.4262

     False pos: 860
     True pos: 4880
     False neg: 858
     True neg: 4878


     Histogram:
     From 0.0: 0
     From 0.1: 0
     From 0.2: 0
     From 0.3: 0
     From 0.4: 7294
     From 0.5: 2706
     From 0.6: 0
     From 0.7: 0
     From 0.8: 0
     From 0.9: 0

     Histogram:
     From 0.0: 0
     From 0.1: 0
     From 0.2: 0
     From 0.3: 2559
     From 0.4: 3028
     From 0.5: 3880
     From 0.6: 449
     From 0.7: 84
     From 0.8: 0
     From 0.9: 0

     Histogram:
     From 0.0: 0
     From 0.1: 37
     From 0.2: 1671
     From 0.3: 2172
     From 0.4: 1939
     From 0.5: 3194
     From 0.6: 777
     From 0.7: 210
     From 0.8: 0
     From 0.9: 0

     Process finished with exit code 0
     */
    public void summary(){
        System.out.println("Played for "+round+" rounds.");

        double aWon = 0;
        double bWon = 0;
        double ties = 0;

        double totalA = 0.0;
        double totalB = 0.0;
        for(int ix=0; ix<round; ++ix){
            double avgA = 0.0;
            double avgB = 0.0;
            for(int jx = 0; jx < 3; ++jx){
                final double estA = estimatesA[jx][ix];
                final double estB = estimatesB[jx][ix];
                final PositiveCounter pc = this.positiveCounters[jx];
                avgA += estA;
                avgB += estB;

                if(playerAWon[ix] > 0){
                    pc.addStats(true, estA);
                    pc.addStats(false, estB);
                    aWon++;
                } else if(playerAWon[ix] < 0) {
                    pc.addStats(false, estA);
                    pc.addStats(true, estB);
                    bWon++;
                } else {
                    ++ties;
                }
            }
            avgA /= 3;
            avgB /= 3;
            totalA += avgA;
            totalB += avgB;


        }
        System.out.println("A expected "+totalA/round);
        System.out.println("B expected "+totalB/round);
        System.out.println(" ");
        System.out.println("A won "+aWon/round);
        System.out.println("B won "+bWon/round);
        System.out.println("Tied  "+ties/round);
        System.out.println(" ");

        for(int ix=0; ix<3; ++ix){
            this.positiveCounters[ix].summarize();
            System.out.println(" ");
        }

        for(int ix=0; ix<3; ++ix){
            new Histogram(estimatesA[ix]).summarize();
//            System.out.println(" ");
        }
    }

//    private void addStats(boolean won, double exp){
//        if(exp >= LIMIT){
//            if(won){
//                truePositive++;
//            } else {
//                falsePositive++;
//            }
//        } else {
//            if(won){
//                falseNegative++;
//            } else {
//                trueNegative++;
//            }
//        }
//    }

    public void oneRound(){
        Deck deck = Deck.getShuffledDeck();

        playerA = new ArrayList<Card>();
        playerB = new ArrayList<Card>();
        community = new ArrayList<Card>();

        for(int ix = 0; ix<2; ++ix){
            playerA.add(deck.getNextCard());
            playerB.add(deck.getNextCard());
        }
        estimateWinChance(0);

        for(int ix = 0; ix<3; ++ix){
            Card c = deck.getNextCard();
            playerA.add(c);
            playerB.add(c);
            community.add(c);
        }
        estimateWinChance(1);

        {
            Card c = deck.getNextCard();
            playerA.add(c);
            playerB.add(c);
            community.add(c);
        }
        estimateWinChance(2);

        PokerHandUtil utilA = new PokerHandUtil(playerA);
        PokerHandUtil utilB = new PokerHandUtil(playerB);

        playerAWon[round] = Scoring.compareHands(utilA.getBestHand().getPokerHand(), utilB.getBestHand().getPokerHand());

        ++round;
    }

    private void estimateWinChance(int part){
        estimatesA[part][round] = Scoring.chanceOfWinning(playerA, community);
        estimatesB[part][round] = Scoring.chanceOfWinning(playerB, community);
    }

    public static class Histogram{

        private final int[] intervals = new int[FIELDS];
        private final int total;

        public Histogram(double[] values){
            total = values.length;

            for(int ix=0; ix<total; ++ix){
                int y = intervalOf(values[ix]);
                intervals[y]++;
            }
        }

        private int intervalOf(double d){
            int big = (int) Math.floor(d*FIELDS);
            if(big == FIELDS){
                return FIELDS-1;
            }
            return big;
        }

        public void summarize(){
            System.out.println("\nHistogram:");
            for(int ix = 0; ix<FIELDS; ++ix){
                double lowBound = ix/ (double) FIELDS;
                System.out.println("From "+lowBound+": \t"+intervals[ix]);
            }
        }
    }

    private static class PositiveCounter{
        private int falsePositive = 0;
        private int falseNegative = 0;
        private int truePositive = 0;
        private int trueNegative = 0;

        private void summarize(){
            System.out.println("False pos: "+falsePositive);
            System.out.println("True pos: "+truePositive);
            System.out.println("False neg: "+falseNegative);
            System.out.println("True neg: "+trueNegative);
        }

        private void addStats(boolean won, double exp){
            if(exp >= LIMIT){
                if(won){
                    truePositive++;
                } else {
                    falsePositive++;
                }
            } else {
                if(won){
                    falseNegative++;
                } else {
                    trueNegative++;
                }
            }
        }
    }
}
