package com.dev.models;

import lombok.Data;

@Data
public class RedriveRequest {
    String mainQueueName;
    String deadLetterQueueName;
    Integer maxNoOfMessagesToRedrive;

    @Override
    public String toString() {
        return "RedriveRequest [mainQueueName=" + mainQueueName + ", deadLetterQueueName=" + deadLetterQueueName
                + ", maxNoOfMessagesToRedrive=" + maxNoOfMessagesToRedrive + "]";
    }
}

