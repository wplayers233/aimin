package com.oimc.aimin.mq.drug;


import org.springframework.amqp.rabbit.annotation.RabbitListener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@RabbitListener(queues = Constants.ES_QUEUE, concurrency = "5") // 封装 RabbitListener
public @interface EsWorkQueueListener {
}
