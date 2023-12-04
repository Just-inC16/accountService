package com.tcs.accountService;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/accounts")
public class AccountController {
	private AccountRepository accountRepository;
	private KafkaTemplate<String, AccountEvent> kafkaTemplate;
	private KafkaTemplate<String, TransactionEvent> kafkaOrderTemplate;

	public AccountController(AccountRepository accountRepository, KafkaTemplate<String, AccountEvent> kafkaTemplate,
			KafkaTemplate<String, TransactionEvent> kafkaOrderTemplate) {
		this.accountRepository = accountRepository;
		this.kafkaTemplate = kafkaTemplate;
		this.kafkaOrderTemplate = kafkaOrderTemplate;
	}

	@GetMapping
	public String getHelloWorld() {
		return "Hello world! :)";
	}

//	@KafkaListener(topics = "new-orders")
//	public String processTransaction2(String event) throws JsonMappingException, JsonProcessingException {
//		return "Kafka listener works";
//	}
	@KafkaListener(topics = "new-orders")
	public void processTransaction(String event) throws JsonMappingException, JsonProcessingException {

		System.out.println("Recieved transaction event" + event);
		TransactionEvent orderEvent = new ObjectMapper().readValue(event, TransactionEvent.class);
		CustomerOrder order = orderEvent.getOrder();

		try {
			Long senderId = order.getSenderId();
			Long receiverId = order.getReceiverId();
			Double amount = order.getAmount();

			if (amount <= 0.0) {
				throw new Exception("You can't enter a negative or 0 amount.");
			}
			if (!(isAccountAvailable(senderId) && isAccountAvailable(receiverId))) {
				throw new Exception("One of the accounts is not correctly entered.");
			}
			Account senderAccount = accountRepository.findByAccountId(senderId);
			Account receiverAccount = accountRepository.findByAccountId(receiverId);
			updateAccountBalance(senderAccount, -amount);
			updateAccountBalance(receiverAccount, +amount);

			// publish account created event for notification microservice to consume.

			AccountEvent accountEvent = new AccountEvent();
			accountEvent.setType("TRANSACTION_CREATED");
			accountEvent.setSenderAccount(senderAccount);
			accountEvent.setReceiverAccount(receiverAccount);
			System.out.println(accountEvent.toString());
			this.kafkaTemplate.send("new-transaction", accountEvent);
		} catch (Exception e) {
			System.out.println("Something went wrong!");
//			payment.setOrderId(order.getOrderId());
//			payment.setStatus("FAILED");
//			this.repository.save(payment);
//
//			// reverse previous task
//			OrderEvent oe = new OrderEvent();
//			oe.setOrder(order);
//			oe.setType("ORDER_REVERSED");
//			this.kafkaOrderTemplate.send("reversed-orders", orderEvent);

		}

	}

//	@GetMapping("/account/{id}")
	public Boolean isAccountAvailable(Long Id) {
		return accountRepository.findByAccountId(Id) != null;
	}

//	@GetMapping("/account/{id}")
	public Account updateAccountBalance(Account account, Double amount) {
		Double newBalance = account.getBalance() + amount;
		account.setBalance(newBalance);
		return accountRepository.save(account);

	}

}
