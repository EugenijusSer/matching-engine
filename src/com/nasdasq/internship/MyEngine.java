package com.nasdasq.internship;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

class MyEngine implements MatchingEngine {
    private List<Order> sellOrders = new ArrayList<>();
    private List<Order> buyOrders = new ArrayList<>();

    MyEngine() {
    }

    public List<Trade> enterOrder(Order orderNew){
        List<Trade> trades = new ArrayList<>();
        //Place your implementation here

        if(orderNew.getSide() == Side.SELL)
            trades = makeTrades(trades,orderNew,buyOrders);
        else
            trades = makeTrades(trades,orderNew,sellOrders);

        return trades;
    }

    private List<Trade> makeTrades(List<Trade> trades, Order orderNew, List<Order> orderBook){
        Order bestMatchingOrder, sellOrder, buyOrder;
        final String stock = orderNew.getStock();
        final BigDecimal price = orderNew.getPrice();

        while(orderNew.getQuantity().compareTo(BigDecimal.ZERO) > 0) {

            List<Order> matchingOrders;

            if(orderNew.getSide() == Side.BUY){
                //filter sell orders to only ones that match the buy order
                matchingOrders = orderBook.stream()
                        .filter(order -> order.getStock().equals(stock)
                                && price.compareTo(order.getPrice()) >= 0).collect(Collectors.toList());

                if (matchingOrders.isEmpty()) {
                    buyOrders.add(orderNew);
                    break;
                }

                bestMatchingOrder = matchingOrders.stream()
                        .min(Comparator.comparing(Order::getPrice))
                        .orElse(null);

                sellOrder = bestMatchingOrder;
                buyOrder = orderNew;
            }
            else{
                //filter buy orders to only ones that match the sell order
                matchingOrders = orderBook.stream()
                        .filter(order -> order.getStock().equals(stock)
                                && price.compareTo(order.getPrice()) <= 0).collect(Collectors.toList());

                if (matchingOrders.isEmpty()) {
                    sellOrders.add(orderNew);
                    break;
                }

                bestMatchingOrder = matchingOrders.stream()
                        .max(Comparator.comparing(Order::getPrice))
                        .orElse(null);

                sellOrder = orderNew;
                buyOrder = bestMatchingOrder;
            }

            BigDecimal sellQuantity = sellOrder.getQuantity();
            BigDecimal buyQuantity = buyOrder.getQuantity();

            if (buyQuantity.compareTo(sellQuantity) < 0) {
                trades.add(new Trade(sellOrder, buyOrder, buyQuantity, bestMatchingOrder.getPrice()));
                decreaseInOrderBook(bestMatchingOrder, buyQuantity, orderBook);
                orderNew.decrease(buyQuantity);
            }
            else if (buyQuantity.compareTo(sellQuantity) == 0) {
                trades.add(new Trade(sellOrder, buyOrder, buyQuantity, bestMatchingOrder.getPrice()));
                orderBook.remove(bestMatchingOrder);
                orderNew.decrease(buyQuantity);
            }
            else if (buyQuantity.compareTo(sellQuantity) > 0) {
                trades.add(new Trade(sellOrder, buyOrder, sellQuantity, bestMatchingOrder.getPrice()));
                decreaseInOrderBook(bestMatchingOrder, sellQuantity, orderBook);
                orderNew.decrease(sellQuantity);
            }
        }
        return trades;
    }

    //method to decrease quantity of opposite order in order book
    private void decreaseInOrderBook(Order order, BigDecimal quantity, List<Order> orderBook){
        //order is referenced to best matching order, which was filtered out of list, so this works
        order.decrease(quantity);

        if(order.getQuantity().compareTo(BigDecimal.ZERO) == 0)
            orderBook.remove(order);
    }
}