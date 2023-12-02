package com.tcs.accountService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountController {
	@Autowired
	private AccountRepository accountRepository;

	@GetMapping
	public String getHelloWorld() {
		return "Hello world! :)";
	}

}
