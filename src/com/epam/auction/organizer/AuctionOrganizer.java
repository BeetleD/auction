package com.epam.auction.organizer;

import com.epam.auction.entities.Auction;
import com.epam.auction.entities.AuctionItem;
import com.epam.auction.entities.Participant;
import com.epam.auction.messager.AuctionMessager;

import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * Created by Dmitry on 16.07.2014.
 */
public class AuctionOrganizer extends Thread {

    public static final int TIME_TO_THINK = 50;
    public static final int MAX_PAY_TIME = 100;
    private CyclicBarrier startBarrier;
    private CyclicBarrier endBarrier;
    private Auction auction;
    private final double bidDeltaPrice;
    private final int banningCnt;
    private boolean modifiedFlag = false;
    private boolean currentItemSold = false;
    private Semaphore payWait = new Semaphore(0);
    private Lock locker = new ReentrantLock();
    private volatile double currentPrice;
    private volatile Participant currentOwner = null;
    private boolean auctionEnded = false;


    ArrayList<Integer> banned = new ArrayList<Integer>();
    ArrayList<Participant> participants = new ArrayList<Participant>();

    public AuctionOrganizer( Auction auction, double bidDeltaPrice, int banningCnt ){
        this.auction = auction;
        this.bidDeltaPrice = bidDeltaPrice;
        this.banningCnt = banningCnt;
    }
    public boolean isOwner( Participant participant ){
        if ( getCurrentOwner() == null ){
            return false;
        }
        return getCurrentOwner().getParticipantId() == participant.getParticipantId();
    }
    public void register( Participant participant ){
        participant.setParticipantId(banned.size());
        banned.add(0);
        participant.setOrganizer(this);
        participant.setDeltaPrice(bidDeltaPrice);
        participants.add( participant );
    }
    public boolean isAuctionEnded() {
        return auction.isEmpty();
    }
    public AuctionItem getCurrentItem(){
        return auction.getFirst();
    }
    public Participant getCurrentOwner() {
        return currentOwner;
    }
    public double getCurrentPrice() {
        return currentPrice;
    }

    private void initialize(){
        startBarrier = new CyclicBarrier( participants.size() + 1  );
        endBarrier = new CyclicBarrier( participants.size() + 1  );
        for ( Participant it : participants ){
            it.setBarriers(startBarrier,endBarrier);
        }
    }
    public void makeBid( Participant participant, double price ){
        int id = participant.getParticipantId();
        if ( banned.get( id ) <= 0 ){
            modifiedFlag = true;
            if ( price > getCurrentPrice() ) {
                AuctionMessager.readyToPay( participant, price );
                setCurrentOwner(participant);
                setCurrentPrice(price);
            }
        }
    }
    public boolean isCurrentItemSold(){
        return currentItemSold;
    }
    public boolean isBanned( Participant participant ){
        if (( participant.getParticipantId() >= banned.size())||(participant.getParticipantId() < 0 )) {
            return true;
        }
        return ( banned.get( participant.getParticipantId() ) > 0 );
    }
    private void initializeItemSale(){
        if ( isAuctionEnded()){
            return;
        }
        setCurrentItemSold(false);
        setCurrentOwner(null);
        setCurrentPrice( getCurrentItem().getStartPrice()-bidDeltaPrice );
    }
    public void goToNextItem(){
        auction.pop();
        for ( int i = 0; i < banned.size(); i++ ){
            banned.set( i, Math.max(0, banned.get(i)-1 ) );
        }
        initializeItemSale();
    }
    public boolean sellCurrentItem() throws InterruptedException {

        do {
            setModifiedFlag(false);
            this.sleep( TIME_TO_THINK );

        } while (isModifiedFlag());
        setCurrentItemSold( true );
        return ( getCurrentOwner() != null );
    }

    public void pay( Participant participant ){
        payWait.release();
    }
    @Override
    public void run(){

        initialize();
        ExecutorService executorService = Executors.newFixedThreadPool( participants.size() );

        for ( Participant it : participants ){
            it.setDaemon(true);
            executorService.execute( it );
        }

        try {
            initializeItemSale();

            while (!auction.isEmpty()) {

                AuctionMessager.itemForSale( getCurrentItem() );
                startBarrier.await();
                sellCurrentItem();
                if ( getCurrentOwner() != null ) {
                    AuctionMessager.itemSoldMessage(getCurrentItem(), getCurrentOwner(), getCurrentPrice());
                    AuctionMessager.paymentWait(getCurrentOwner());
                    payWait.drainPermits();
                    boolean paymentDone = payWait.tryAcquire( MAX_PAY_TIME, TimeUnit.MILLISECONDS );
                    if (paymentDone){
                        AuctionMessager.paymentDone( getCurrentOwner(), getCurrentPrice() );
                    } else {
                        AuctionMessager.paymentIgnored( getCurrentOwner() );
                        AuctionMessager.banned( getCurrentOwner(), banningCnt );
                        banned.set( getCurrentOwner().getParticipantId(), banningCnt+1 );
                    }
                } else {
                    AuctionMessager.itemIgnored(getCurrentItem());
                }
                goToNextItem();
                endBarrier.await();
            }
            AuctionMessager.auctionEnded();
            executorService.shutdown();
        } catch ( InterruptedException e ) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }

    private void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }
    private void setCurrentOwner(Participant currentOwner) {
        this.currentOwner = currentOwner;
    }
    private void setCurrentItemSold( boolean flag ){
        currentItemSold = flag;
    }
    private void setModifiedFlag( boolean flag ){
        modifiedFlag = flag;
    }
    private boolean isModifiedFlag() {
        return modifiedFlag;
    }
}
