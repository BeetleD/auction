package com.epam.auction.entities;

import com.epam.auction.messager.AuctionMessager;
import com.epam.auction.organizer.AuctionOrganizer;
import org.apache.log4j.Logger;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Dmitry on 16.07.2014.
 */
public class Participant extends Thread {

    private final static Logger logger = Logger.getLogger(Participant.class);
    private int participantId;
    private String participantName;
    private double lavishnessRatio;
    private double purchaseRatio;
    private double punctuality;
    private double deltaPrice;
    private AuctionOrganizer organizer;
    private static Lock locker = new ReentrantLock();
    private static CyclicBarrier startBarrier;
    private static CyclicBarrier endBarrier;
    private Random rand = new Random();
    public Participant( String name, double lavishnessRatio, double purchaseRatio, double punctuality ){
        this.participantName = name;
        this.lavishnessRatio = lavishnessRatio;
        this.purchaseRatio = purchaseRatio;
        this.punctuality = punctuality;
    }
    public void setBarriers(CyclicBarrier startBarrier,CyclicBarrier endBarrier) {
        Participant.startBarrier = startBarrier;
        Participant.endBarrier = endBarrier;
    }

    public void tryToGetItem( double readyToPay) {

        while ( !organizer.isCurrentItemSold() ){
            locker.lock();
                if (( organizer.getCurrentPrice() < readyToPay )&&( !organizer.isOwner( this ) )){
                    organizer.makeBid( this, organizer.getCurrentPrice() + deltaPrice );
                }
            locker.unlock();
        }

    }
    @Override
    public void run(){

        try {

            while (!organizer.isAuctionEnded()) {

                startBarrier.await();
                if (( wantsToBuy()) && (!organizer.isBanned(this))) {
                    AuctionItem currentItem = organizer.getCurrentItem();
                    tryToGetItem(currentItem.getStartPrice() + currentItem.getApproxPrice() * lavishnessRatio);
                }
                if ( organizer.isOwner(this)) {
                    if ( wantsToPay() ) {
                        sleep((long) ((1 - punctuality) * organizer.MAX_PAY_TIME));
                        organizer.pay( this );
                    }
                }

                endBarrier.await();
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (BrokenBarrierException e) {
            e.printStackTrace();
        }
    }
    private boolean wantsToBuy(){
        return rand.nextDouble() < purchaseRatio;
    }
    private boolean wantsToPay(){
        return rand.nextDouble() < punctuality;
    }

    public String getParticipantName() {
        return participantName;
    }

    public double getLavishnessRatio() {
        return this.lavishnessRatio;
    }

    public void setLavishnessRatio(int lavishnessRatio) {
        this.lavishnessRatio = lavishnessRatio;
    }

    public double getPurchaseRatio() {
        return purchaseRatio;
    }

    public void setPurchaseRatio(double purchaseRatio) {
        this.purchaseRatio = purchaseRatio;
    }

    public void setOrganizer(AuctionOrganizer organizer) {
        this.organizer = organizer;
    }

    public int getParticipantId() {
        return participantId;
    }

    public void setParticipantId(int participantId) {
        this.participantId = participantId;
    }

    public double getDeltaPrice() {
        return deltaPrice;
    }

    public void setDeltaPrice(double deltaPrice) {
        this.deltaPrice = deltaPrice;
    }

    public double getPunctuality() {
        return punctuality;
    }

    public void setPunctuality(double punctualityRatio) {
        this.punctuality = punctualityRatio;
    }
}
