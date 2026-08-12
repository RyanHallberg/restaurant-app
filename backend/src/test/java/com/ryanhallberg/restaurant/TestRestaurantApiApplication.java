package com.ryanhallberg.restaurant;

import org.springframework.boot.SpringApplication;

public class TestRestaurantApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(RestaurantApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
