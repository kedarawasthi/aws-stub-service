package com.dev.services;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.*;
import com.dev.models.RedriveRequest;
import com.dev.models.RedriveResult;
import com.dev.utils.JsonToObject;
import com.dev.utils.ObjectToJson;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class DlqMessageReprocessor implements RequestHandler<String, String> {

    private LambdaLogger logger;
    private AmazonSQS sqsClient;
    public String handleRequest(String jsonString, Context context) {

        /********************************* STEP 0: unbox Redrive Request Object ************************************/

        logger = context.getLogger();
        sqsClient = AmazonSQSClientBuilder.defaultClient();
        RedriveRequest redriveRequest = JsonToObject.convertJsonToRedriveRequestObject(jsonString,logger);
        String mainQueueName = redriveRequest.getMainQueueName();
        String deadLetterQueueName = redriveRequest.getDeadLetterQueueName();
        Integer maxNoOfMessagesToRedrive = redriveRequest.getMaxNoOfMessagesToRedrive();


        /********************************* STEP 1: get dlqUrl and main queueUrl with names************************************/

        String mainQueueUrl = sqsClient.getQueueUrl(mainQueueName).getQueueUrl();
        String deadLetterQueueUrl = sqsClient.getQueueUrl(deadLetterQueueName).getQueueUrl();
        logger.log("MainQueueUrl: " + mainQueueUrl);
        logger.log("DeadLetterQueueUrl: " + deadLetterQueueUrl);
        logger.log("MaxNoOfMessagesToRedrive: " + maxNoOfMessagesToRedrive);

        /***************************************** STEP 2: read messages from dlq*******************************************/

        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(deadLetterQueueUrl)
                .withMaxNumberOfMessages(maxNoOfMessagesToRedrive).withAttributeNames("All")
                .withMessageAttributeNames("All");
        ReceiveMessageResult receiveMessageResult = sqsClient.receiveMessage(receiveMessageRequest);
        if (receiveMessageResult.getMessages().isEmpty()) {
            logger.log("no messages to redrive");
            RedriveResult redriveResult = new RedriveResult();
            redriveResult.setMessage("no messages to redrive");
            String result = ObjectToJson.convertRedriveResultObjectToJson(redriveResult,logger);
            return result;
        }
        logger.log("ReceiveMessageResult: " + receiveMessageResult.toString());

        /******************************** STEP 3: filter retryable errors from read message result *************************/


        String retryableErrors = "HTTP:TIMEOUT,HTTP:INTERNAL_SERVER_ERROR,HTTP:BAD_GATEWAY,HTTP:SERVICE_UNAVAILABLE,HTTP:CONNECTIVITY";
        List<Message> messages = receiveMessageResult.getMessages();
        List<Message> retryableMessages = messages
                .stream()
                .filter(s->retryableErrors.contains(s.getMessageAttributes().get("errorType").getStringValue()))
                .collect(Collectors.toList());
        retryableMessages.forEach(item->
        {
            item.getMessageAttributes().remove("errorType");
            item.getMessageAttributes().remove("errorDescription");
        });
        if (retryableMessages.isEmpty()) {
            logger.log("no retryable messages to redrive");
            RedriveResult redriveResult = new RedriveResult();
            redriveResult.setMessage("no retryable messages to redrive");
            String result = ObjectToJson.convertRedriveResultObjectToJson(redriveResult,logger);
            return result;
        }
        logger.log("======== Retryable Messages ready to redrive: " + retryableMessages.toString());

        /******************************** STEP 4: send batch messages to main queue ****************************************/


        List<SendMessageBatchRequestEntry> sendMessageBatchRequestEntries = new ArrayList<>();
        retryableMessages.forEach(item->
        {
            SendMessageBatchRequestEntry sendMessageBatchRequestEntry = new SendMessageBatchRequestEntry();
            sendMessageBatchRequestEntry.setId(item.getMessageId());
            sendMessageBatchRequestEntry.setMessageAttributes(item.getMessageAttributes());
            sendMessageBatchRequestEntry.setMessageBody(item.getBody());

            sendMessageBatchRequestEntries.add(sendMessageBatchRequestEntry);
        });

        SendMessageBatchRequest sendMessageBatchRequest = new SendMessageBatchRequest(mainQueueUrl, sendMessageBatchRequestEntries);
        SendMessageBatchResult sendMessageBatchResult = sqsClient.sendMessageBatch(sendMessageBatchRequest);
        logger.log("======== Send Message Batch Result: " + sendMessageBatchResult.toString());

        /******************************** STEP 5: delete batch message from dlq****************************************/


        List<DeleteMessageBatchRequestEntry> deleteMessageBatchRequestEntries = new ArrayList<>(); retryableMessages.forEach(item->
        {
            DeleteMessageBatchRequestEntry deleteMessageBatchRequestEntry = new DeleteMessageBatchRequestEntry();
            deleteMessageBatchRequestEntry.setId(item.getMessageId());
            deleteMessageBatchRequestEntry.setReceiptHandle(item.getReceiptHandle());

            deleteMessageBatchRequestEntries.add(deleteMessageBatchRequestEntry);
        });
        DeleteMessageBatchRequest deleteMessageBatchRequest = new DeleteMessageBatchRequest(deadLetterQueueUrl,deleteMessageBatchRequestEntries);
        DeleteMessageBatchResult deleteMessageBatchResult = sqsClient.deleteMessageBatch(deleteMessageBatchRequest);
        logger.log("======== Delete Message Batch Result: " + deleteMessageBatchResult.toString());

        /*********************************** STEP 6: form response object***************************************/


        String sendMessageBatchResultString = ObjectToJson.convertSendMessageBatchResultObjectToJson(sendMessageBatchResult,logger);
        String deleteMessageBatchResultString = ObjectToJson.convertDeleteMessageBatchResultObjectToJson(deleteMessageBatchResult,logger);
        RedriveResult redriveResult=new RedriveResult();
        redriveResult.setMessage("redrive successful");
        redriveResult.setDeleteMessageBatchResult(deleteMessageBatchResultString);
        redriveResult.setSendMessageBatchResult(sendMessageBatchResultString);
        String result = ObjectToJson.convertRedriveResultObjectToJson(redriveResult,logger);
        logger.log("Redrive Result: " + result);

        return result;
    }

}