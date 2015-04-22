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

    private final double[][] estimatesA;
    private final double[][] estimatesB;
    private final int[] playerAWon;

    private Simulate(int rounds){
        estimatesA = new double[3][rounds];
        estimatesB = new double[3][rounds];
        playerAWon = new int[rounds];
    }

    public static void main(String[] args){
        runSimulation(1);
    }

    public static void runSimulation(int rounds){
        Simulate s = new Simulate(rounds);
        for(int ix=0; ix<rounds; ++ix){
            s.oneRound();
        }
        s.summary();
    }

    public void summary(){
        System.out.println("Played for "+round+" rounds.");

        int aWon = 0;
        int bWon = 0;
        int ties = 0;
        for(int ix=0; ix<round; ++ix){
            StringBuilder a = new StringBuilder();
            StringBuilder b = new StringBuilder();
            for(int jx = 0; jx < 3; ++jx){
                a.append(estimatesA[jx][ix]+", ");
                b.append(estimatesB[jx][ix] + ", ");
            }
            System.out.println("A expected "+a);
            System.out.println("B expected "+b);

            if(playerAWon[ix] > 0){
                System.out.println("A won this time.");
                aWon++;
            } else if(playerAWon[ix] < 0) {
                System.out.println("B won this time.");
                bWon++;
            } else {
                System.out.println("Tied.");
                ++ties;
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
