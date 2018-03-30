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

        if(orderNew.getSide() == Side.SELL){
            //trades = makeTrades(trades,orderNew,buyOrders);
            sellOrders.add(orderNew);
        }
        else {
            trades = makeTrades(trades,orderNew,sellOrders);

//            Order bestSellOrder;
//            while(orderNew.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
//                final String buyStock = orderNew.getStock();
//                final BigDecimal buyPrice = orderNew.getPrice();
//
//                //filter sell orders to only ones that match the buy order
//                List<Order> matchingSellOrders = sellOrders.stream()
//                        .filter(order -> order.getStock().equals(buyStock)
//                                && buyPrice.compareTo(order.getPrice()) >= 0).collect(Collectors.toList());
//
//                //if there is no matching order, then add to order book and end loop
//                if(matchingSellOrders.isEmpty()){
//                    buyOrders.add(orderNew);
//                    break;
//                }
//
//                bestSellOrder = matchingSellOrders.stream()
//                        .min(Comparator.comparing(Order::getPrice))
//                        .orElse(null);
//
//                //if(bestSellOrder != null) {
//                    if(orderNew.getQuantity().compareTo(bestSellOrder.getQuantity()) < 0) {
//                        trades.add(new Trade(bestSellOrder,orderNew,orderNew.getQuantity(),bestSellOrder.getPrice()));
//                        subtractSellOrder(bestSellOrder,orderNew.getQuantity());
//                        orderNew = subtractBuyOrder(orderNew,orderNew.getQuantity());
//                    }
//                    else if(orderNew.getQuantity().compareTo(bestSellOrder.getQuantity()) == 0){
//                        trades.add(new Trade(bestSellOrder,orderNew,orderNew.getQuantity(),bestSellOrder.getPrice()));
//                        sellOrders.remove(bestSellOrder);
//                        orderNew = subtractBuyOrder(orderNew,orderNew.getQuantity());
//                    }
//                    else if(orderNew.getQuantity().compareTo(bestSellOrder.getQuantity()) > 0){
//                        trades.add(new Trade(bestSellOrder,orderNew,bestSellOrder.getQuantity(),bestSellOrder.getPrice()));
//                        sellOrders.remove(bestSellOrder);
//                        orderNew = subtractBuyOrder(orderNew,bestSellOrder.getQuantity());
//                    }
//                //}
//
////                //if there is no
////                else{
////                    buyOrders.add(orderNew);
////                    break;
////                }
//            }

        }

        //An example:

        //Constructing a fake opposite matching order for demo purposes only
        //In the real implementation an opposite matching order should be identified among the orders entered already
        /*Order orderOppposite = new Order(orderNew.getClient() +
                "_Opposite " + orderNew.getSide().getOppositeSide() + " " +
                orderNew.getStock() + " " +
                Environment.decimalFormat.format(orderNew.getQuantity()) + "@" +
                Environment.decimalFormat.format(orderNew.getPrice()));

        //Constructing a new trade from (orderSell, orderBuy....)
        Trade tradeNew;
        if (orderNew.getSide() == Side.SELL) {
            tradeNew = new Trade(orderNew, orderOppposite, orderNew.getQuantity(), orderNew.getPrice());
        }else{
            tradeNew = new Trade(orderOppposite, orderNew, orderNew.getQuantity(), orderNew.getPrice());
        }

        //Adding a new trade
        trades.add(tradeNew);*/

        return trades;
    }

    private List<Trade> makeTrades(List<Trade> trades, Order orderNew, List<Order> orderBook){
        Order bestSellOrder;
        while(orderNew.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
            final String buyStock = orderNew.getStock();
            final BigDecimal buyPrice = orderNew.getPrice();

            //filter sell orders to only ones that match the buy order
            List<Order> matchingSellOrders = orderBook.stream()
                    .filter(order -> order.getStock().equals(buyStock)
                            && buyPrice.compareTo(order.getPrice()) >= 0).collect(Collectors.toList());

            //if there is no matching order, then add to order book and end loop
            if (matchingSellOrders.isEmpty()) {
                buyOrders.add(orderNew);
                break;
            }

            bestSellOrder = matchingSellOrders.stream()
                    .min(Comparator.comparing(Order::getPrice))
                    .orElse(null);

            //if(bestSellOrder != null) {
            if (orderNew.getQuantity().compareTo(bestSellOrder.getQuantity()) < 0) {
                trades.add(new Trade(bestSellOrder, orderNew, orderNew.getQuantity(), bestSellOrder.getPrice()));
                subtractSellOrder(bestSellOrder, orderNew.getQuantity());
                orderNew = subtractBuyOrder(orderNew, orderNew.getQuantity());
            } else if (orderNew.getQuantity().compareTo(bestSellOrder.getQuantity()) == 0) {
                trades.add(new Trade(bestSellOrder, orderNew, orderNew.getQuantity(), bestSellOrder.getPrice()));
                orderBook.remove(bestSellOrder);
                orderNew = subtractBuyOrder(orderNew, orderNew.getQuantity());
            } else if (orderNew.getQuantity().compareTo(bestSellOrder.getQuantity()) > 0) {
                trades.add(new Trade(bestSellOrder, orderNew, bestSellOrder.getQuantity(), bestSellOrder.getPrice()));
                orderBook.remove(bestSellOrder);
                orderNew = subtractBuyOrder(orderNew, bestSellOrder.getQuantity());
            }
        }
        return trades;
    }

    private void subtractSellOrder(Order order, BigDecimal quantity){
        int index = sellOrders.indexOf(order);
        Order orderNew =  new Order(order.getTimeStamp() + " " +
                order.getId() + " " +
                order.getClient() + " " +
                order.getSide() + " " +
                order.getStock() + " " +
                order.getQuantity().subtract(quantity) + "@" +
                order.getPrice()
        );
        sellOrders.set(index, orderNew);
    }

    private Order subtractBuyOrder(Order order, BigDecimal quantity){
        return new Order(order.getTimeStamp() + " " +
                order.getId() + " " +
                order.getClient() + " " +
                order.getSide() + " " +
                order.getStock() + " " +
                order.getQuantity().subtract(quantity) + "@" +
                order.getPrice());
    }
}