package com.redisearch;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.redisearch.model.Order;
import com.redisearch.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.Resource;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.search.FieldName;
import redis.clients.jedis.search.IndexDefinition;
import redis.clients.jedis.search.IndexOptions;
import redis.clients.jedis.search.Schema;

import java.util.Arrays;

@SpringBootApplication
public class Redisearchex2Application implements ApplicationRunner {

	@Autowired
	private JedisPooled jedis;

	@Autowired
	private OrderRepository orderRepo;

	@Value("classpath:data.json")
	Resource resourceFile;
	public static void main(String[] args) {
		SpringApplication.run(Redisearchex2Application.class, args);
	}


	@Override
	public void run(ApplicationArguments args) throws Exception {
		// Delete all existing orders and the index


		orderRepo.deleteAll();
		try {
			jedis.ftDropIndex("order-idx");
		} catch (Exception e) {
			System.out.println("Index is not available ");
		}


		// Read the order data from the JSON file
		String data = new String(resourceFile.getInputStream().readAllBytes());

		// Deserialize the JSON data into an array of OrderData objects
		ObjectMapper objectMapper = new ObjectMapper()
				.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		Order[] orders = objectMapper.readValue(data, Order[].class);

		Arrays.stream(orders).forEach(orderRepo::save);

		Schema schema = new Schema()
				.addField(new Schema.Field(FieldName.of("$.commodityCode").as("commodityCode"), Schema.FieldType.TAG));

		IndexDefinition indexDefinition = new IndexDefinition(IndexDefinition.Type.HASH);

		jedis.ftCreate("order-idx",
				IndexOptions.defaultOptions().setDefinition(indexDefinition), schema);


	}
}
