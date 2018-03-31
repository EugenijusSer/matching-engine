package com.nasdasq.internship;

import java.math.BigDecimal;
import java.util.*;

class MyEngine implements MatchingEngine {
    //Book of open buy and sell orders
    private List<Order> orderBook = new ArrayList<>();

    MyEngine() {
    }

    public List<Trade> enterOrder(Order orderNew){
        List<Trade> trades = new ArrayList<>();
        Order bestMatchingOrder, sellOrder, buyOrder;

        //Loop till the entered order is fully matched or put into order book
        while(orderNew.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
            bestMatchingOrder = findBestMatchingOrder(orderNew);

            //If no matching order is found, then entered order is added to order book and loop is ended
            if(bestMatchingOrder == null){
                orderBook.add(orderNew);
                break;
            }

            //Assigning appropriate values for sellOrder and buyOrder
            if(orderNew.getSide() == Side.BUY){
                sellOrder = bestMatchingOrder;
                buyOrder = orderNew;
            }
            else{
                sellOrder = orderNew;
                buyOrder = bestMatchingOrder;
            }

            //Constructing a new trade
            Trade tradeNew;
            if (buyOrder.getQuantity().compareTo(sellOrder.getQuantity()) <= 0)
                tradeNew = new Trade(sellOrder, buyOrder, buyOrder.getQuantity(), bestMatchingOrder.getPrice());
            else
                tradeNew = new Trade(sellOrder, buyOrder, sellOrder.getQuantity(), bestMatchingOrder.getPrice());

            //Adding a new trade
            trades.add(tradeNew);
            //Decreasing quantity of order in the order book
            decreaseInOrderBook(bestMatchingOrder, tradeNew.getQuantity());
            //Decreasing quantity of the entered order
            orderNew.decrease(tradeNew.getQuantity());
        }
        return trades;
    }

    //Method to find a matching order with the best price in order book
    private Order findBestMatchingOrder(Order orderNew){
        //Assigning required values of entered order to local final variables
        final String stock = orderNew.getStock();
        final BigDecimal price = orderNew.getPrice();
        final Side oppositeSide = orderNew.getSide().getOppositeSide();

        if(oppositeSide == Side.SELL)
            return orderBook.stream()                                     //filter conditions:
                    .filter(order -> order.getSide().equals(oppositeSide) //sides are opposite
                            && order.getStock().equals(stock)             //same stock
                            && price.compareTo(order.getPrice()) >= 0)    //buy price >= sell price
                    .min(Comparator.comparing(Order::getPrice))           //find lowest price sell order
                    .orElse(null);                                  //if not found, return null
        else
            return orderBook.stream()                                     //filter conditions:
                    .filter(order -> order.getSide().equals(oppositeSide) //sides are opposite
                            && order.getStock().equals(stock)             //same stock
                            && price.compareTo(order.getPrice()) <= 0)    //sell price <= buy price
                    .max(Comparator.comparing(Order::getPrice))           //find highest price buy order
                    .orElse(null);                                  //if not found, return null
    }

    //Method to decrease quantity of order in order book
    private void decreaseInOrderBook(Order order, BigDecimal quantity){
        order.decrease(quantity);

        //If order's quantity in order book is 0, it is removed from the book
        if(order.getQuantity().compareTo(BigDecimal.ZERO) == 0)
            orderBook.remove(order);
    }
}