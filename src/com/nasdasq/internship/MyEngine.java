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

            if (buyOrder.getQuantity().compareTo(sellOrder.getQuantity()) < 0) {
                trades.add(new Trade(sellOrder, buyOrder, buyOrder.getQuantity(), bestMatchingOrder.getPrice()));
                subtractFromOrderBook(bestMatchingOrder, buyOrder.getQuantity(), orderBook);
                orderNew = subtractOrder(orderNew, buyOrder.getQuantity());
            }
            else if (buyOrder.getQuantity().compareTo(sellOrder.getQuantity()) == 0) {
                trades.add(new Trade(sellOrder, buyOrder, buyOrder.getQuantity(), bestMatchingOrder.getPrice()));
                orderBook.remove(bestMatchingOrder);
                orderNew = subtractOrder(orderNew, buyOrder.getQuantity());
            }
            else if (buyOrder.getQuantity().compareTo(sellOrder.getQuantity()) > 0) {
                trades.add(new Trade(sellOrder, buyOrder, sellOrder.getQuantity(), bestMatchingOrder.getPrice()));
                subtractFromOrderBook(bestMatchingOrder, sellOrder.getQuantity(), orderBook);
                orderNew = subtractOrder(orderNew, sellOrder.getQuantity());
            }
        }
        return trades;
    }

    private void subtractFromOrderBook(Order order, BigDecimal quantity, List<Order> orderBook){

        int index = orderBook.indexOf(order);
        Order orderNew = subtractOrder(order,quantity);

        if(orderNew.getQuantity().compareTo(BigDecimal.ZERO) == 0)
            orderBook.remove(index);
        else
            orderBook.set(index, orderNew);
    }

    private Order subtractOrder(Order order, BigDecimal quantity){
        return new Order(order.getTimeStamp() + " " +
                order.getId() + " " +
                order.getClient() + " " +
                order.getSide() + " " +
                order.getStock() + " " +
                order.getQuantity().subtract(quantity) + "@" +
                order.getPrice());
    }
}