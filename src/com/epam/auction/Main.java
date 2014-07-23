package com.epam.auction;

import com.epam.auction.entities.Auction;
import com.epam.auction.entities.AuctionItem;
import com.epam.auction.entities.Participant;
import com.epam.auction.organizer.AuctionOrganizer;

/**
 * Created by Dmitry on 16.07.2014.
 */
public class Main extends Thread {
    public static void main(String[] args) {
        Auction auction = new Auction();
        auction.add( new AuctionItem( "Clocks", 70.0, 100.0 ) );
        auction.add( new AuctionItem( "Notebook", 500.0, 700.0 ) );
        auction.add( new AuctionItem( "big black stone", 10, 0 ) );
        auction.add( new AuctionItem( "drum", 30, 50 ) );
        auction.add( new AuctionItem( "water tanker", 500, 550 ) );
        auction.add( new AuctionItem( "Glass Top Table", 2, 200 ) );
        auction.add( new AuctionItem( "Mobile Whiteboard", 2, 400 ) );
        auction.add( new AuctionItem( "Office equipment", 100, 150 ) );
        auction.add( new AuctionItem( "Key cabinet", 50, 300 ) );

        AuctionOrganizer organizer = new AuctionOrganizer( auction, 50, 2 );
        organizer.register( new Participant( "Mr. Smith", 0.70, 0.50, 0.70 ) );
        organizer.register( new Participant( "Mr. Gauss", 0.80, 0.70, 0.50) );
        organizer.register( new Participant( "Mr. Strauss", 0.50 , 0.50, 0.30 ) );
        organizer.register( new Participant( "Mr. Clause", 0.90 , 0.50, 0.30 ) );
        organizer.start();


    }
}
