package com.tcs.accountService;

public class AccountEvent {
	private String type;
	private Account senderAccount;
	private Account receiverAccount;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Account getSenderAccount() {
		return senderAccount;
	}

	public void setSenderAccount(Account senderAccount) {
		this.senderAccount = senderAccount;
	}

	public Account getReceiverAccount() {
		return receiverAccount;
	}

	public void setReceiverAccount(Account receiverAccount) {
		this.receiverAccount = receiverAccount;
	}

}