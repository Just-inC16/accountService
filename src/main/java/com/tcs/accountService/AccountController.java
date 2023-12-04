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
	private KafkaTemplate<String, TransactionEvent> kafkaTransactionTemplate;

	public AccountController(AccountRepository accountRepository, KafkaTemplate<String, AccountEvent> kafkaTemplate,
			KafkaTemplate<String, TransactionEvent> kafkaTransactionTemplate) {
		this.accountRepository = accountRepository;
		this.kafkaTemplate = kafkaTemplate;
		this.kafkaTransactionTemplate = kafkaTransactionTemplate;
	}

	@GetMapping
	public String getHelloWorld() {
		return "Hello world! :)";
	}

	@KafkaListener(topics = "new-orders")
	public void processTransaction(String event) throws JsonMappingException, JsonProcessingException {

		System.out.println("Recieved transaction event" + event);
		TransactionEvent orderEvent = new ObjectMapper().readValue(event, TransactionEvent.class);
		CustomerOrder transaction = orderEvent.getOrder();

		Long senderId = transaction.getSenderId();
		Long receiverId = transaction.getReceiverId();
		Double amount = transaction.getAmount();

		Account senderAccount = accountRepository.findByAccountId(senderId);
		Account receiverAccount = accountRepository.findByAccountId(receiverId);
		try {

			if (amount <= 0.0) {
				throw new Exception("You can't enter a negative or 0 amount.");
			}
			if (!(isAccountAvailable(senderId) && isAccountAvailable(receiverId))) {
				throw new Exception("One of the accounts is not correctly entered.");
			}
			if (senderAccount.getBalance() < amount) {
				throw new Exception("You have insufficient funds.");
			}

			updateAccountBalance(senderAccount, -amount);
			updateAccountBalance(receiverAccount, +amount);
			senderAccount.setStatus(Status.SUCCESS);
			receiverAccount.setStatus(Status.SUCCESS);

			// publish account created event for notification microservice to consume.

			AccountEvent accountEvent = new AccountEvent();
			accountEvent.setType("TRANSACTION_CREATED");
			accountEvent.setSenderAccount(senderAccount);
			accountEvent.setReceiverAccount(receiverAccount);
			System.out.println(accountEvent.toString());
			this.kafkaTemplate.send("new-transaction", accountEvent);
		} catch (Exception e) {
			System.out.println("Something went wrong!");
			// Set the status of the accounts to failure
			if (senderAccount != null) {
				senderAccount.setStatus(Status.FAILURE);
				this.accountRepository.save(senderAccount);
			}
			if (receiverAccount != null) {
				receiverAccount.setStatus(Status.FAILURE);
				this.accountRepository.save(receiverAccount);
			}

			// reverse previous task
			TransactionEvent transactionEvent = new TransactionEvent();
			transactionEvent.setOrder(transaction);
			transactionEvent.setType("TRANSACTION_REVERSED");
			this.kafkaTransactionTemplate.send("reversed-transaction", transactionEvent);
		}
	}

	@KafkaListener(topics = "reverse-account")
	public void reverseOrder(String event) throws JsonMappingException, JsonProcessingException {
		System.out.println("Reverse this account" + event);

	}

	public Boolean isAccountAvailable(Long Id) {
		return accountRepository.findByAccountId(Id) != null;
	}

	public Account updateAccountBalance(Account account, Double amount) {
		Double newBalance = account.getBalance() + amount;
		account.setBalance(newBalance);
		return accountRepository.save(account);

	}

}
