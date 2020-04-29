package beautysalon.appointments;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

@OpenAPIDefinition(
        info = @Info(
                title = "Beauty Salon - Appointments",
                version = "1.0",
                description = "API of the Appointments Microservice",
                contact = @Contact(name = "Isabell Sailer", email = "isabell.sailer@stud.uni-bamberg.de")
        )
)
public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class);
    }
}