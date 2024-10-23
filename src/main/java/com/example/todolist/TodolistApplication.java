package com.example.todolist;

import com.example.todolist.db.DatabaseConnection;
import com.example.todolist.db.DatabaseSetup;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TodolistApplication {

	public static void main(String[] args) {
		SpringApplication.run(TodolistApplication.class, args);
		DatabaseSetup.createTables();
	}

}
