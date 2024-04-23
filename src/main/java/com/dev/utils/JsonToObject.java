package com.dev.utils;

import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.dev.models.RedriveRequest;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonToObject {
    public static RedriveRequest convertJsonToRedriveRequestObject(String jsonString, LambdaLogger logger) {


        logger.log("convertJsonToRedriveRequestObject method invoked");

        ObjectMapper objMapper= new ObjectMapper();
        RedriveRequest redriveRequest = null;
        objMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        try {
            redriveRequest = objMapper.readValue(jsonString, RedriveRequest.class);
            logger.log("Serailized Java Object: " + redriveRequest.toString());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return redriveRequest;
    }

}
