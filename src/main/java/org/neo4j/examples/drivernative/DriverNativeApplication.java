package org.neo4j.examples.drivernative;

import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.net.URI;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Logging;
import org.neo4j.driver.MetricsAdapter;
import org.neo4j.driver.Session;
import org.neo4j.driver.async.AsyncSession;

@CommandLine.Command(name = "printmovies")
public class DriverNativeApplication implements Callable<Integer> {

	@Option(
		names = { "-a", "--address" },
		description = "The address this migration should connect to. The driver supports bolt, bolt+routing or neo4j as schemes.",
		required = true,
		defaultValue = "bolt://localhost:7687"
	)
	private URI address;

	@Option(
		names = { "-u", "--username" },
		description = "The login of the user connecting to the database.",
		required = true,
		defaultValue = "neo4j"
	)
	private String user;

	@Option(
		names = { "-p", "--password" },
		description = "The password of the user connecting to the database.",
		arity = "0..1", interactive = true,
		defaultValue = "secret"
	)
	private char[] password;

	@Option(
		names = { "-l" }, description = "Logging level"
	)
	private String loggingLevel;

	@Option(
		names = { "-m" }, defaultValue = "DEV_NULL"
	)
	private MetricsAdapter metricsAdapter;

	@Override
	public Integer call() throws Exception {

		System.out.println("Using metrics adapter " + metricsAdapter);

		AuthToken auth = AuthTokens.basic(user, new String(password));
		Config.ConfigBuilder builder = Config.builder();
		builder.withMetricsAdapter(metricsAdapter);

		if (loggingLevel != null) {
			builder.withLogging(Logging.console(Level.parse(loggingLevel)));
		}

		try (Driver driver = GraphDatabase.driver(address, auth, builder.build())) {
			try (Session session = driver.session()) {
				session.run("MATCH (m:Movie) RETURN m.title AS title")
					.list(r -> r.get("title").asString())
					.forEach(System.out::println);
			}

			AsyncSession session = driver.asyncSession();

			List<String> movieTitles = session.runAsync("MATCH (m:Movie) RETURN m.title AS title")
				.thenCompose(cursor -> cursor.listAsync(record -> record.get("title").asString()))
				.thenCompose(titles -> session.closeAsync()
					.thenApply(ignore -> titles))
				.toCompletableFuture()
				.get();

			System.out.println("Fetched " + movieTitles.size() + " async (and blocked doing so)");
			System.out.println("Metrics had been " + (driver.isMetricsEnabled() ? "on" : "off"));
			if (driver.isMetricsEnabled()) {
				driver.metrics().connectionPoolMetrics().forEach(System.out::println);
			}
		}

		return 0;
	}

	public static void main(String[] args) {

		int exitCode = new CommandLine(new DriverNativeApplication()).execute(args);
		System.exit(exitCode);
	}
}
