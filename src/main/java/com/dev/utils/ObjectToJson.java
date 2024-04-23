package com.dev.utils;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.sqs.model.DeleteMessageBatchResult;
import com.amazonaws.services.sqs.model.SendMessageBatchResult;
import com.dev.models.RedriveResult;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectToJson {
    public static String convertSendMessageBatchResultObjectToJson(Object obj, LambdaLogger logger) {

        logger.log("convertSendMessageBatchResultObjectToJson method invoked");

        SendMessageBatchResult msg = (SendMessageBatchResult) obj;
        ObjectMapper objMapper= new ObjectMapper();
        objMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        String jsonObj="";

        try {
            jsonObj = objMapper.writeValueAsString(msg);
            logger.log("Deserialized Json Object: " + jsonObj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return jsonObj;

    }

    public static String convertDeleteMessageBatchResultObjectToJson(Object obj,LambdaLogger logger) {

        logger.log("convertDeleteMessageBatchResultObjectToJson method invoked");

        DeleteMessageBatchResult msg = (DeleteMessageBatchResult) obj;
        ObjectMapper objMapper= new ObjectMapper();
        objMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        String jsonObj="";

        try {
            jsonObj = objMapper.writeValueAsString(msg);
            logger.log("Deserialized Json Object: " + jsonObj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return jsonObj;
    }

    public static String convertRedriveResultObjectToJson(Object obj,LambdaLogger logger) {

        logger.log("convertRedriveResultResultObjectToJson method invoked");

        RedriveResult msg = (RedriveResult) obj;
        ObjectMapper objMapper= new ObjectMapper();
        objMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        String jsonObj="";

        try {
            jsonObj = objMapper.writeValueAsString(msg);
            logger.log("Deserialized Json Object: " + jsonObj);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return jsonObj;
    }
}
