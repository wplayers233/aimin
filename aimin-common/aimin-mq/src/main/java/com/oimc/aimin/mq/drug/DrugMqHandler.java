package com.oimc.aimin.mq.drug;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DrugMqHandler {

    private final RabbitTemplate rabbitTemplate;

    public void syncDataToES(Object obj) {
        rabbitTemplate.convertAndSend(Constants.ES_QUEUE,obj, message -> {
            message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
            return message;
        });
    }
}
