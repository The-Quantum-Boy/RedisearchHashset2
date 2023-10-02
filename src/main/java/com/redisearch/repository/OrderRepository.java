package com.redisearch.repository;


import com.google.gson.Gson;
import com.redisearch.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.search.Document;
import redis.clients.jedis.search.Query;
import redis.clients.jedis.search.SearchResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class OrderRepository {

    @Autowired
    private JedisPooled jedis;

    public static final Integer PAGE_SIZE = 5;

    public Order save(Order order) {
        Gson gson = new Gson();
        String hashKey = "orders";  // Redis hash key to store all orders
        String fieldKey = "order:" + order.getInternalOrdNo();
        jedis.hset(fieldKey, (Map<String, String>) order);
        jedis.sadd("order", fieldKey);

        return order;
    }

    public void deleteAll() {
        Set<String> keys = jedis.smembers("order");
        if (!keys.isEmpty()) {
            keys.stream().forEach(jedis::del);
        }
    }

    public List<Order> search(String commodityCode) {

        StringBuilder queryBuilder = new StringBuilder();
        if(commodityCode!=null && !commodityCode.isEmpty()){
            queryBuilder.append("@commodityCode:" + commodityCode);
        }
        String queryCriteria = queryBuilder.toString();
        Query query = new Query(queryCriteria);

        SearchResult searchResult = jedis.ftSearch("order-idx",query);

        List<Order> orderList = searchResult.getDocuments()
                .stream()
                .map(this::converDocumentToOrder)
                .collect(Collectors.toList());



        return orderList;
    }

    private Order converDocumentToOrder(Document document){

        Gson gson = new Gson();

        String jsonDoc = document
                .getProperties()
                .iterator()
                .next()
                .getValue()
                .toString();

        return gson.fromJson(jsonDoc, Order.class);
    }
}
