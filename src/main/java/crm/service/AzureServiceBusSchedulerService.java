package crm.service;

import com.azure.messaging.servicebus.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Azure Service Bus Scheduler Service
 * 
 * This service replaces java.util.Timer with Azure Service Bus scheduled message delivery
 * for distributed, timezone-agnostic task execution in cloud environments.
 * 
 * Benefits over java.util.Timer:
 * - Distributed: Works across multiple container instances
 * - Timezone-agnostic: All scheduling uses UTC timestamps
 * - Durable: Messages persist even if containers restart
 * - Scalable: Multiple consumers can process scheduled tasks
 * - Cloud-native: Integrates with Azure monitoring and diagnostics
 * 
 * Usage:
 * 1. Configure AZURE_SERVICEBUS_CONNECTION_STRING environment variable
 * 2. Call scheduleMessage() to schedule a task for future execution
 * 3. Implement message processing logic in processScheduledMessage()
 */
@Service
public class AzureServiceBusSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(AzureServiceBusSchedulerService.class);

    @Value("${azure.servicebus.connection-string:}")
    private String connectionString;

    @Value("${azure.servicebus.queue-name:scheduled-tasks}")
    private String queueName;

    private ServiceBusSenderClient senderClient;
    private ServiceBusProcessorClient processorClient;

    /**
     * Initialize Azure Service Bus clients
     * Only initializes if connection string is configured
     */
    @PostConstruct
    public void initialize() {
        if (connectionString == null || connectionString.isEmpty()) {
            logger.warn("Azure Service Bus connection string not configured. Scheduled message functionality disabled.");
            logger.warn("Set AZURE_SERVICEBUS_CONNECTION_STRING environment variable to enable.");
            return;
        }

        try {
            // Create sender client for scheduling messages
            senderClient = new ServiceBusClientBuilder()
                    .connectionString(connectionString)
                    .sender()
                    .queueName(queueName)
                    .buildClient();

            // Create processor client for receiving scheduled messages
            processorClient = new ServiceBusClientBuilder()
                    .connectionString(connectionString)
                    .processor()
                    .queueName(queueName)
                    .processMessage(this::processScheduledMessage)
                    .processError(this::processError)
                    .buildProcessorClient();

            // Start processing messages
            processorClient.start();

            logger.info("Azure Service Bus Scheduler initialized successfully for queue: {}", queueName);
        } catch (Exception e) {
            logger.error("Failed to initialize Azure Service Bus Scheduler", e);
        }
    }

    /**
     * Schedule a message for future delivery
     * 
     * @param messageBody The message content
     * @param scheduledTime The UTC time when the message should be delivered
     * @return true if scheduled successfully, false otherwise
     */
    public boolean scheduleMessage(String messageBody, Instant scheduledTime) {
        if (senderClient == null) {
            logger.warn("Service Bus sender not initialized. Cannot schedule message.");
            return false;
        }

        try {
            ServiceBusMessage message = new ServiceBusMessage(messageBody);
            
            // Schedule the message for future delivery using UTC timestamp
            OffsetDateTime scheduledDateTime = scheduledTime.atOffset(ZoneOffset.UTC);
            message.setScheduledEnqueueTime(scheduledDateTime);

            senderClient.sendMessage(message);
            
            logger.info("Message scheduled successfully for delivery at {} UTC", scheduledDateTime);
            return true;
        } catch (Exception e) {
            logger.error("Failed to schedule message", e);
            return false;
        }
    }

    /**
     * Schedule a message for delivery after a specified delay
     * 
     * @param messageBody The message content
     * @param delay The delay duration before message delivery
     * @return true if scheduled successfully, false otherwise
     */
    public boolean scheduleMessageWithDelay(String messageBody, Duration delay) {
        Instant scheduledTime = Instant.now().plus(delay);
        return scheduleMessage(messageBody, scheduledTime);
    }

    /**
     * Process a scheduled message when it's delivered
     * Override this method to implement custom message processing logic
     * 
     * @param context The message context
     */
    private void processScheduledMessage(ServiceBusReceivedMessageContext context) {
        ServiceBusReceivedMessage message = context.getMessage();
        
        try {
            logger.info("Processing scheduled message: {}", message.getBody().toString());
            logger.info("Message was scheduled for: {}", message.getScheduledEnqueueTime());
            logger.info("Message received at: {}", Instant.now());
            
            // TODO: Implement your scheduled task logic here
            // For example:
            // - Send notifications
            // - Process batch jobs
            // - Trigger data synchronization
            // - Execute maintenance tasks
            
            // Complete the message to remove it from the queue
            context.complete();
            
        } catch (Exception e) {
            logger.error("Error processing scheduled message", e);
            // Abandon the message so it can be retried
            context.abandon();
        }
    }

    /**
     * Handle processing errors
     * 
     * @param context The error context
     */
    private void processError(ServiceBusErrorContext context) {
        logger.error("Error occurred while processing message: {}", 
                context.getException().getMessage(), context.getException());
    }

    /**
     * Clean up resources on shutdown
     */
    @PreDestroy
    public void cleanup() {
        if (processorClient != null) {
            processorClient.close();
            logger.info("Service Bus processor client closed");
        }
        if (senderClient != null) {
            senderClient.close();
            logger.info("Service Bus sender client closed");
        }
    }
}
