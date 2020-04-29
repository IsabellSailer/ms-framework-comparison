package beautysalon.treatments.msimpl;

import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.authentication.AuthenticationFailed;
import io.micronaut.security.authentication.UserDetails;
import java.util.ArrayList;
import io.reactivex.Flowable;
import javax.inject.Singleton;
import org.reactivestreams.Publisher;

@Singleton
public class AuthenticationProviderBasic implements AuthenticationProvider {

	@Override
	public Publisher<AuthenticationResponse> authenticate(AuthenticationRequest authenticationRequest) {
		if (authenticationRequest.getIdentity().equals("admin")
				&& authenticationRequest.getSecret().equals("admin")) {
			return Flowable.just(new UserDetails((String) authenticationRequest.getIdentity(), new ArrayList<>()));
		}
		return Flowable.just(new AuthenticationFailed());
	}
}