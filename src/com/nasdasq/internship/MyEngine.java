package com.nasdasq.internship;

import java.math.BigDecimal;
import java.util.*;

class MyEngine implements MatchingEngine {
    private List<Order> sellOrders = new ArrayList<>();
    private List<Order> buyOrders = new ArrayList<>();

    MyEngine() {
    }

    public List<Trade> enterOrder(Order orderNew){
        List<Trade> trades = new ArrayList<>();
        //Place your implementation here
        Order bestMatchingOrder, sellOrder, buyOrder;
        List<Order> orderBook; //order book of opposite side

        while(orderNew.getQuantity().compareTo(BigDecimal.ZERO) > 0) {

            if(orderNew.getSide() == Side.BUY){
                orderBook = sellOrders;
                bestMatchingOrder = findBestMatchingOrder(orderNew,orderBook);

                if(bestMatchingOrder == null){
                    buyOrders.add(orderNew);
                    break;
                }

                sellOrder = bestMatchingOrder;
                buyOrder = orderNew;
            }
            else{
                orderBook = buyOrders;
                bestMatchingOrder = findBestMatchingOrder(orderNew, orderBook);

                if(bestMatchingOrder == null){
                    sellOrders.add(orderNew);
                    break;
                }

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

    private Order findBestMatchingOrder(Order orderNew, List<Order> orderBook){
        final String stock = orderNew.getStock();
        final BigDecimal price = orderNew.getPrice();

        if(orderNew.getSide() == Side.BUY)
            return orderBook.stream()
                    .filter(order -> order.getStock().equals(stock)
                            && price.compareTo(order.getPrice()) >= 0)
                    .min(Comparator.comparing(Order::getPrice))
                    .orElse(null);
        else
            return orderBook.stream()
                    .filter(order -> order.getStock().equals(stock)
                            && price.compareTo(order.getPrice()) <= 0)
                    .max(Comparator.comparing(Order::getPrice))
                    .orElse(null);
    }

    //method to decrease quantity of opposite order in order book
    private void decreaseInOrderBook(Order order, BigDecimal quantity, List<Order> orderBook){
        //order is referenced to best matching order, which was filtered out of list, so this works
        order.decrease(quantity);

        //if order's quantity in order book is 0, it is removed from the book
        if(order.getQuantity().compareTo(BigDecimal.ZERO) == 0)
            orderBook.remove(order);
    }
}