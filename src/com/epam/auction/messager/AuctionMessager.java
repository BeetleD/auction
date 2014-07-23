package com.epam.auction.messager;

import com.epam.auction.entities.AuctionItem;
import com.epam.auction.entities.Participant;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

/**
 * Created by Dmitry on 23.07.2014.
 */
public class AuctionMessager {

    private static final Logger logger = Logger.getLogger( AuctionMessager.class);
    static{
            DOMConfigurator.configure("src\\log4j.xml");
    }
    public static void itemForSale( AuctionItem item ){
        logger.info( "Item for sale: " + item.getItemName() +". Approximate price: " + item.getApproxPrice()+"$." + " Start price: " + item.getStartPrice() +"$." );
    }
    public static void paymentDone( Participant participant, double price ){
        logger.info( participant.getParticipantName() + " paid " + price + "$" );
    }
    public static void paymentIgnored( Participant participant ){
        logger.info( participant.getParticipantName() + " ignored payment");
    }
    public static void readyToPay( Participant participant, double price ){
        logger.info( "Participant " + participant.getParticipantName() + " ready to pay " + price +"$" );
    }
    public static void itemSoldMessage( AuctionItem item, Participant owner, double price ){
        logger.info("Item " + item.getItemName() + " goes to " + owner.getParticipantName() + " by price " + price + "$" );
    }
    public static void itemIgnored( AuctionItem item ){
        logger.info( "No one wants to buy " + item.getItemName() );
    }
    public static void paymentWait( Participant participant ){
        logger.info( "Auction waits for payment from " + participant.getParticipantName() );
    }
    public static void banned( Participant participant, int cnt ){
        logger.info( participant.getParticipantName() + " banned for " + cnt + " sales" );
    }
    public static void auctionEnded( ){
        logger.info( "Auction ended");
    }
}
