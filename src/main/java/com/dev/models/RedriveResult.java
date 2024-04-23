package com.dev.models;

import lombok.Data;

@Data
public class RedriveResult {

	private String message;
	private String sendMessageBatchResult;
	private String deleteMessageBatchResult;


}
