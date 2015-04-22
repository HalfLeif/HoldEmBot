package se.cygni.texasholdem.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.cygni.texasholdem.client.CurrentPlayState;
import se.cygni.texasholdem.client.PlayerClient;
import se.cygni.texasholdem.communication.message.event.*;
import se.cygni.texasholdem.communication.message.request.ActionRequest;
import se.cygni.texasholdem.game.Action;
import se.cygni.texasholdem.game.Card;
import se.cygni.texasholdem.game.PlayerShowDown;
import se.cygni.texasholdem.game.Room;
import se.cygni.texasholdem.game.definitions.PlayState;
import se.cygni.texasholdem.game.definitions.PokerHand;
import se.cygni.texasholdem.game.definitions.Rank;

import java.util.Formatter;
import java.util.List;

/*
 * First victory!
 * http://poker.cygni.se/showgame/table/118
 *
 * Second victory!
 * http://poker.cygni.se/showgame/table/125
 *
 * Third victory!
 * http://poker.cygni.se/showgame/table/127
 */

/**
 * This is an example Poker bot player, you can use it as
 * a starting point when creating your own.
 *      <p/>
 *      Javadocs for common utilities and classes used may be
 *      found here:
 *      http://poker.cygni.se/mavensite/texas-holdem-common/apidocs/index.html
 *      <p/>
 *      You can inspect the games you bot has played here:
 *      http://poker.cygni.se/showgame
 */
public class FullyImplementedBot implements Player {

    private static Logger log = LoggerFactory
            .getLogger(FullyImplementedBot.class);

    private final String serverHost;
    private final int serverPort;
    private final PlayerClient playerClient;

    private PlayState currentState = null;
    private double chance = 0.0;
    private boolean someoneWentAllIn = false;
    private boolean pleasePrintStrategy = true;
    private int gameRound = 1;

    /**
     * Default constructor for a Java Poker Bot.
     *
     * @param serverHost IP or hostname to the poker server
     * @param serverPort port at which the poker server listens
     */
    public FullyImplementedBot(String serverHost, int serverPort) {
        this.serverHost = serverHost;
        this.serverPort = serverPort;

        // Initialize the player client
        playerClient = new PlayerClient(this, serverHost, serverPort);
    }

    public void playATrainingGame() throws Exception {
        playerClient.connect();
        playerClient.registerForPlay(Room.TRAINING);
    }

    /**
     * The main method to start your bot.
     *
     * @param args
     */
    public static void main(String... args) {

        FullyImplementedBot bot = new FullyImplementedBot("poker.cygni.se", 4711);

        try {
            bot.playATrainingGame();

        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * The name you choose must be unique, if another connected bot has
     * the same name your bot will be denied connection.
     *
     * @return The name under which this bot will be known
     */
    @Override
    public String getName() {
        return "HalfLeif";
    }

    /**
     * This is where you supply your bot with your special mojo!
     * <p/>
     * The ActionRequest contains a list of all the possible actions
     * your bot can perform.
     *
     * @param request The list of Actions that the bot may perform.
     *
     * @return The action the bot wants to perform.
     *
     * @see ActionRequest
     *      <p/>
     *      Given the current situation you need to choose the best
     *      action. It is not allowed to change any values in the
     *      ActionRequest. The amount you may RAISE or CALL is already
     *      predermined by the poker server.
     *      <p/>
     *      If an invalid Action is returned the server will ask two
     *      more times. Failure to comply (i.e. returning an incorrect
     *      or non valid Action) will result in a forced FOLD for the
     *      current Game Round.
     * @see Action
     */
    @Override
    public Action actionRequired(ActionRequest request) {

        Action response = getBestAction(request);
        log.info("I'm going to {} {}",
                response.getActionType(),
                response.getAmount() > 0 ? "with " + response.getAmount() : "");

        return response;
    }

    private void updateChance(){
        final CurrentPlayState playState = playerClient.getCurrentPlayState();
        this.chance = Scoring.chanceOfWinning(playState.getMyCardsAndCommunityCards(), playState.getCommunityCards());
    }

    /**
     * The best action
     */
    private Action getBestAction(ActionRequest request) {

        final ActionsAvailable actionsAvailable = new ActionsAvailable(request);
        final CurrentPlayState playState = playerClient.getCurrentPlayState();

        final int numPlayers = playState.getNumberOfPlayers();
        final boolean onlyTwoPlayers = numPlayers < 3;

        if(this.pleasePrintStrategy){
            log.info("# "+currentState.getName());
        }

        if(someoneWentAllIn){
            if(chance > 0.72) {
                log.info("Someone went ALL IN, but I'm still confident!");
                return stayInGame(actionsAvailable);
            } else {
                log.info("Someone went ALL IN, but it's not worth the risk.");
                return justFold(actionsAvailable);
            }
        }

        if(currentState.equals(PlayState.PRE_FLOP)){
            List<Card> cards = playState.getMyCards();
            final boolean worth = worthKeeping(cards.get(0), cards.get(1));
//            if(worth && this.pleasePrintStrategy){
//                log.info("Apparently worth keeping? Chance: "+chance);
//            }

            if(onlyTwoPlayers){
                if(worth || chance > 0.49){
                    return stayInGame(actionsAvailable);
                } else {
                    return justFold(actionsAvailable);
                }
            }

            if(chance > 0.52 || (worth && chance > 0.480) ){
                return stayInGame(actionsAvailable);
            } else {
                return justFold(actionsAvailable);
            }
        }

        // After PRE_FLOP
        if(onlyTwoPlayers){
            if(chance < 0.44){
                return justFold(actionsAvailable);
            }
            if(chance < 0.59){
                return stayInGame(actionsAvailable);
            }
            return keepRaising(actionsAvailable);
        }

        if(chance < 0.48){
            return justFold(actionsAvailable);
        }
        if(chance < 0.60){
            return stayInGame(actionsAvailable);
        }
        return keepRaising(actionsAvailable);
    }

    private Action justFold(ActionsAvailable available){
        final CurrentPlayState playState = playerClient.getCurrentPlayState();
        if(this.pleasePrintStrategy){
            StringBuilder s = new StringBuilder();
            for(Card c : playState.getMyCards()){
                s.append(c.toShortString()+", ");
            }
            log.info("Quick exit, got: "+s);
        }

        if(available.checkAction != null){
            return available.checkAction;
        }

        return available.foldAction;
    }

    private Action keepRaising(ActionsAvailable available){
        if(pleasePrintStrategy){
            log.info("Bot is quite confident in this!");
            this.pleasePrintStrategy = false;
        }

        if(available.raiseAction != null){
            return available.raiseAction;
        }
        if(available.callAction != null){
            return available.callAction;
        }
        if(chance > 0.74){
            return available.allInAction;
        }
        if(available.checkAction != null){
            return available.checkAction;
        }
        log.info("keepRaising: THIS SHOULD NEVER HAPPEN?");
        return available.foldAction;

    }

    private Action stayInGame(ActionsAvailable available){
        if(pleasePrintStrategy){
            log.info("Just stay alive");
            this.pleasePrintStrategy = false;
        }

        if(available.checkAction != null){
            return available.checkAction;
        }
        if(available.callAction != null){
            return available.callAction;
        }
        // Can happen when out of resources
        return available.allInAction;
    }

    private boolean worthKeeping(Card a, Card b){
        if(a.getRank() == b.getRank()){
            return true;
        }
        if(a.getSuit() == b.getSuit()){
            return true;
        }
        return a.getRank().getOrderValue() > Rank.NINE.getOrderValue() || b.getRank().ordinal() > Rank.NINE.getOrderValue() ;
    }

    private class ActionsAvailable {
        private Action callAction = null;
        private Action checkAction = null;
        private Action raiseAction = null;
        private Action foldAction = null;
        private Action allInAction = null;

        private ActionsAvailable(ActionRequest request){
            for (final Action action : request.getPossibleActions()) {
                switch (action.getActionType()) {
                    case CALL:
                        callAction = action;
                        break;
                    case CHECK:
                        checkAction = action;
                        break;
                    case FOLD:
                        foldAction = action;
                        break;
                    case RAISE:
                        raiseAction = action;
                        break;
                    case ALL_IN:
                        allInAction = action;
                    default:
                        break;
                }
            }
        }
    }

    /**
     * Compares two pokerhands.
     *
     * @return TRUE if myPokerHand is valued higher than otherPokerHand
     */
    private boolean isHandBetterThan(PokerHand myPokerHand, PokerHand otherPokerHand) {
        return myPokerHand.getOrderValue() > otherPokerHand.getOrderValue();
    }

    /**
     * **********************************************************************
     * <p/>
     * Event methods
     * <p/>
     * These methods tells the bot what is happening around the Poker Table.
     * The methods must be implemented but it is not mandatory to act on the
     * information provided.
     * <p/>
     * The helper class CurrentPlayState provides most of the book keeping
     * needed to keep track of the total picture around the table.
     *
     * @see CurrentPlayState
     *      <p/>
     *      ***********************************************************************
     */

    @Override
    public void onPlayIsStarted(final PlayIsStartedEvent event) {
        log.debug("Play is started");
    }

    @Override
    public void onTableChangedStateEvent(TableChangedStateEvent event) {
        this.currentState = event.getState();
        this.someoneWentAllIn = false;
        this.pleasePrintStrategy = true;

        log.debug("Table changed state: {}", event.getState());
    }

    @Override
    public void onYouHaveBeenDealtACard(final YouHaveBeenDealtACardEvent event) {

        log.debug("I, {}, got a card: {}", getName(), event.getCard());
        updateChance();
    }

    @Override
    public void onCommunityHasBeenDealtACard(
            final CommunityHasBeenDealtACardEvent event) {

        log.debug("Community got a card: {}", event.getCard());
        updateChance();
    }

    @Override
    public void onPlayerBetBigBlind(PlayerBetBigBlindEvent event) {

        log.debug("{} placed big blind with amount {}", event.getPlayer().getName(), event.getBigBlind());
    }

    @Override
    public void onPlayerBetSmallBlind(PlayerBetSmallBlindEvent event) {

        log.debug("{} placed small blind with amount {}", event.getPlayer().getName(), event.getSmallBlind());
    }

    @Override
    public void onPlayerFolded(final PlayerFoldedEvent event) {

        log.debug("{} folded after putting {} in the pot", event.getPlayer().getName(), event.getInvestmentInPot());
    }

    @Override
    public void onPlayerForcedFolded(PlayerForcedFoldedEvent event) {

        log.info("NOT GOOD! {} was forced to fold after putting {} in the pot because exceeding the time limit", event.getPlayer().getName(), event.getInvestmentInPot());
    }

    @Override
    public void onPlayerCalled(final PlayerCalledEvent event) {

        log.debug("{} called with amount {}", event.getPlayer().getName(), event.getCallBet());
    }

    @Override
    public void onPlayerRaised(final PlayerRaisedEvent event) {

        log.debug("{} raised with bet {}", event.getPlayer().getName(), event.getRaiseBet());
    }

    @Override
    public void onTableIsDone(TableIsDoneEvent event) {

        log.debug("Table is done, I'm leaving the table with ${}", playerClient.getCurrentPlayState().getMyCurrentChipAmount());
        log.info("Ending poker session, the last game may be viewed at: http://{}/showgame/table/{}", serverHost, playerClient.getCurrentPlayState().getTableId());
    }

    @Override
    public void onPlayerWentAllIn(final PlayerWentAllInEvent event) {
        this.someoneWentAllIn = true;
        log.info("{} went all in with amount {}", event.getPlayer().getName(), event.getAllInAmount());
    }

    @Override
    public void onPlayerChecked(final PlayerCheckedEvent event) {

        log.debug("{} checked", event.getPlayer().getName());
    }

    @Override
    public void onYouWonAmount(final YouWonAmountEvent event) {

        log.debug("I, {}, won: {}", getName(), event.getWonAmount());
    }

    @Override
    public void onShowDown(final ShowDownEvent event) {

        if (!log.isInfoEnabled()) {
            return;
        }

        final StringBuilder sb = new StringBuilder();
        final Formatter formatter = new Formatter(sb);

        sb.append("ShowDown:\n");

        for (final PlayerShowDown psd : event.getPlayersShowDown()) {
            formatter.format("%-13s won: %6s  hand: %-15s ",
                    psd.getPlayer().getName(),
                    psd.getHand().isFolded() ? "Fold" : psd.getWonAmount(),
                    psd.getHand().getPokerHand().getName());

            sb.append(" cards: | ");
            for (final Card card : psd.getHand().getCards()) {
                formatter.format("%-13s | ", card);
            }
//            sb.append("\n");
        }

        log.info(sb.toString());

        log.info("That was round "+this.gameRound+"\n");
        this.gameRound++;
    }

    @Override
    public void onPlayerQuit(final PlayerQuitEvent event) {

        log.debug("Player {} has quit", event.getPlayer());
    }

    @Override
    public void connectionToGameServerLost() {

        log.debug("Lost connection to game server, exiting");
        System.exit(0);
    }

    @Override
    public void connectionToGameServerEstablished() {

        log.debug("Connection to game server established");
    }

    @Override
    public void serverIsShuttingDown(final ServerIsShuttingDownEvent event) {
        log.debug("Server is shutting down");
    }


}
