package beautysalon.confirmation.msimpl;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import io.micronaut.configuration.rabbitmq.connect.ChannelInitializer;

import javax.inject.Singleton;
import java.io.IOException;

@Singleton
public class ChannelPoolListener extends ChannelInitializer {

    @Override
    public void initialize(Channel channel) throws IOException {
        channel.exchangeDeclare("beautysalon", BuiltinExchangeType.DIRECT, true);
        channel.queueDeclare("newappointments", true, false, false, null);
        channel.queueBind("newappointments", "beautysalon", "newappointments");
    }
}