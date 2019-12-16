package org.neo4j.examples.drivernative;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Config;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Logging;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;
import org.neo4j.driver.async.AsyncSession;

public class DriverNativeApplication {

	public static void main(String[] args) throws ExecutionException, InterruptedException {
		Config config = Config.builder().withLogging(Logging.console(Level.ALL)).build();
		try (Driver driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("neo4j", "secret"), config)) {
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
		}
	}
}
