package com.tcs.accountService;

import javax.security.auth.login.AccountException;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TransactionEventListener {
	@EventListener
	public void handleStartTransactionEvent(StartTransactionEvent event) {
		// Logic to handle the event
		System.out.println("Custom event received: " + event);
	}

	@EventListener
	public void handleStartTransactionEvent2(StartTransactionEvent event) {
		try {
			Transaction transaction = fetchTransactionDetails(event.getTransactionId());
			processTransaction(transaction);
			eventPublisher.publishTransactionSuccessEvent(new TransactionSuccessEvent(transaction.getId()));
		} catch (AccountException e) {
			eventPublisher.publishTransactionFailureEvent(
					new TransactionFailureEvent(event.getTransactionId(), e.getMessage()));
		}
	}
}