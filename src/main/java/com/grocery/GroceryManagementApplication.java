package com.grocery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GroceryManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(GroceryManagementApplication.class, args);
		System.out.println("Open: http://localhost:8080");
	}
}