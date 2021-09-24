import com.rabbitmq.client.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class RoutingReceiverApp {

    private static final String EXCHANGE_NAME = "topic_exchange";

    public static void main(String[] argv) throws Exception {

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);
        String queueName = channel.queueDeclare().getQueue();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {

            while (true) {
                System.out.println("Если хотите подписаться введите '1'");
                System.out.println("Если хотите отписаться введите '0'");
                switch (reader.readLine()) {
                    case "1" :
                        System.out.println("Введите тему, которая вам интересна, например java");
                        String routingKeyAdd = reader.readLine();
                        channel.queueBind(queueName, EXCHANGE_NAME, routingKeyAdd);
                        System.out.println(" [*] Waiting for messages :" + routingKeyAdd);

                        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
                            System.out.println(
                                    "[x] Received '" +
                                    delivery
                                    .getEnvelope()
                                    .getRoutingKey() +
                                    "':'" + message + "'"
                            );
                        };
                        channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {
                        });
                        break;
                    case "0" :
                        System.out.println("Введите тему, от которой вы хотите отписаться, например java");
                        String routingKeyRm = reader.readLine();
                        channel.queueUnbind(queueName, EXCHANGE_NAME, routingKeyRm);
                        break;
                }
            }
        }
    }
}
