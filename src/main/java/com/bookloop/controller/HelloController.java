package com.bookloop.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
	
	@GetMapping
	public String hellocontroller() {
		return "This is the hello controller";
	}

}
