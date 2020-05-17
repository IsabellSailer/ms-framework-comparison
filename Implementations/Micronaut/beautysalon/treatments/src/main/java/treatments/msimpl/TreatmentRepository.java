package treatments.msimpl;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.RedisFuture;

import java.math.BigDecimal;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.inject.Singleton;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map;

import java.lang.Exception;

@Singleton
public class TreatmentRepository {

	private StatefulRedisConnection<String, String> redisConnection;

	public TreatmentRepository(StatefulRedisConnection<String, String> redisConnection) {
		this.redisConnection = redisConnection;
	}

	public Treatment save(@NotNull Integer id, @NotBlank String name, @NotNull BigDecimal price,
														@NotNull Integer minduration, @NotNull Integer maxduration)  {
		RedisCommands<String, String> redisCommands = redisConnection.sync();

		redisCommands.hset(Integer.toString(id), "name", name);
		redisCommands.hset(Integer.toString(id), "price", price.toString());
		redisCommands.hset(Integer.toString(id), "minduration", Integer.toString(minduration));
		redisCommands.hset(Integer.toString(id), "maxduration", Integer.toString(maxduration));

		return new Treatment(id, name, price, minduration, maxduration);
	}

	public List<Treatment> findAllTreatments() throws Exception {
		RedisAsyncCommands<String, String> redisCommands = redisConnection.async();
		RedisFuture<List<String>> allkeys = redisCommands.keys("*");
		List<String> keys = new ArrayList<String>();

		try {
			keys = allkeys.get();
		} catch (Exception e) {
			List<Treatment> treatments = new ArrayList<Treatment>();
			return treatments;
		}

		List<Treatment> treatments = new ArrayList<>();

		for (String k : keys) {
			treatments.add(findTreatmentById(Integer.parseInt(k)).get());
		}

		return treatments;
	}

	public Optional<Treatment> findTreatmentById(@NotNull int id)  {
		RedisCommands<String, String> redisCommands = redisConnection.sync();

		Map<String, String> treatment = redisCommands.hgetall(Integer.toString(id));
		treatment.remove("JSON");
		treatment.put("id", Integer.toString(id));
		ObjectMapper mapper = new ObjectMapper();

		return Optional.ofNullable(mapper.convertValue(treatment, Treatment.class));
	}

}
