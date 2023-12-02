package com.tcs.accountService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {
	@Autowired
	private ApplicationEventPublisher applicationEventPublisher;

	public void publishStartTransactionEvent(StartTransactionEvent event) {
		applicationEventPublisher.publishEvent(event);
	}

	// Other publish methods for various events
}