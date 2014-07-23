package com.epam.auction.entities;

/**
 * Created by Dmitry on 16.07.2014.
 */
public class AuctionItem {
    private String itemName;
    private double startPrice;
    private double approxPrice;
    public AuctionItem(){

    }
    public  AuctionItem( String itemName, double startPrice, double approxPrice ){
        this.itemName = itemName;
        this.startPrice = startPrice;
        this.approxPrice = approxPrice;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public double getStartPrice() {
        return startPrice;
    }

    public void setStartPrice(int startPrice) {
        this.startPrice = startPrice;
    }

    public double getApproxPrice() {
        return approxPrice;
    }
}
