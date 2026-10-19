package com.oimc.aimin.mq.drug;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QueueConfiguration {

    /**
     * 队列持久化 是指队列本身的元数据（如队列名称、绑定关系等）在 RabbitMQ 重启后仍然存在,但不能保证队列中的消息不丢失。
     * 消息的持久化，在发送时设置；
     */
    @Bean
    public Queue workQueue() {
        return new Queue(Constants.ES_QUEUE,true);
    }

}
