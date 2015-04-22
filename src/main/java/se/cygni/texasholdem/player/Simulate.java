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

    private int round = 0;

    private int falsePositive = 0;
    private int falseNegative = 0;
    private int truePositive = 0;
    private int trueNegative = 0;


    private final double[][] estimatesA;
    private final double[][] estimatesB;
    private final int[] playerAWon;

    private Simulate(int rounds){
        estimatesA = new double[3][rounds];
        estimatesB = new double[3][rounds];
        playerAWon = new int[rounds];
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
     A expected 0.47742864379156436
     B expected 0.47446378871298556

     A won 0.2905
     B won 0.2816
     Tied  0.4279

     False pos: 880
     True pos:  4864
     False neg: 857
     True neg: 4841
     */
    public void summary(){
        System.out.println("Played for "+round+" rounds.");

        double aWon = 0;
        double bWon = 0;
        double ties = 0;

        double totalA = 0.0;
        double totalB = 0.0;
        for(int ix=0; ix<round; ++ix){
//            StringBuilder a = new StringBuilder();
//            StringBuilder b = new StringBuilder();
            double avgA = 0.0;
            double avgB = 0.0;
            for(int jx = 0; jx < 3; ++jx){
                avgA += estimatesA[jx][ix];
                avgB += estimatesB[jx][ix];
//                a.append(estimatesA[jx][ix]+", ");
//                b.append(estimatesB[jx][ix] + ", ");
            }
            avgA /= 3;
            avgB /= 3;
            totalA += avgA;
            totalB += avgB;
//            System.out.println("A expected "+a);
//            System.out.println("B expected "+b);

            if(playerAWon[ix] > 0){
//                System.out.println("A won this time.");
                addStats(true, avgA);
                addStats(false, avgB);
                aWon++;
            } else if(playerAWon[ix] < 0) {
                addStats(false, avgA);
                addStats(true, avgB);
//                System.out.println("B won this time.");
                bWon++;
            } else {
//                System.out.println("Tied.");
                ++ties;
            }
        }
        System.out.println("A expected "+totalA/round);
        System.out.println("B expected "+totalB/round);
        System.out.println(" ");
        System.out.println("A won "+aWon/round);
        System.out.println("B won "+bWon/round);
        System.out.println("Tied  "+ties/round);
        System.out.println(" ");
        System.out.println("False pos: "+falsePositive);
        System.out.println("True pos: "+truePositive);
        System.out.println("False neg: "+falseNegative);
        System.out.println("True neg: "+trueNegative);
    }

    private void addStats(boolean won, double exp){
        if(exp >= 0.5){
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

}
