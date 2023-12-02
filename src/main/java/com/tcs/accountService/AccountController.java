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
	@Autowired
	private EventPublisher eventPublisher;

	@GetMapping
	public String getHelloWorld() {
		return "Hello world! :)";
	}

	@GetMapping("/publish-event")
	public String publishEvent() {
		Account acc = new Account((long) 1, 29.99);
		StartTransactionEvent event = new StartTransactionEvent(acc);
		eventPublisher.publishStartTransactionEvent(event);
		return "Event Publisher";
	}

}
