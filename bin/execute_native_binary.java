///usr/bin/env jbang "$0" "$@" ; exit $?
//JAVA 17
//DEPS org.testcontainers:neo4j:1.16.2
//DEPS org.slf4j:slf4j-simple:1.7.32
//DEPS org.neo4j.driver:neo4j-java-driver:4.4.3
//DEPS org.assertj:assertj-core:3.22.0

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.testcontainers.containers.Neo4jContainer;

/**
 * Needs to be called from the root of the project.
 * Required args:
 * - a[0]: Name of binary
 * - a[1]: Micrometer supposed to work or not (true|false)
 */
public class execute_native_binary {

	public static void main(String... a) throws Exception {

		assertThat(a).hasSize(3);

		var dockerImageTag = a[0];
		var executable = Paths.get(a[1]).toAbsolutePath().normalize().toString();

		// Let Ryuk take care of it, so no try/catch with autoclose
		var neo4j = new Neo4jContainer<>(String.format("neo4j:%s", dockerImageTag)).withReuse(true);
		neo4j.start();

		try (var driver = GraphDatabase.driver(neo4j.getBoltUrl(), AuthTokens.basic("neo4j", neo4j.getAdminPassword()));
			var session = driver.session()) {
			session.writeTransaction(tx -> {
				tx.run("MATCH (n) DETACH DELETE n").consume();
				tx.run("CREATE (m:Movie {title: 'Highlander'}) RETURN m.title AS title").consume();
				return null;
			});
		}

		List<String> expectedOutput;
		if (Boolean.parseBoolean(a[2])) {
			expectedOutput = List.of(
				"Trying to use metrics adapter MICROMETER",
				"Highlander",
				"Fetched 1 async (and blocked doing so)",
				"Metrics had been on"
			);
		} else {
			expectedOutput = List.of(
				"Trying to use metrics adapter MICROMETER",
				"Highlander",
				"Fetched 1 async (and blocked doing so)",
				"Metrics had been off"
			);
		}

		var p = new ProcessBuilder(executable,
			"--address", neo4j.getBoltUrl(),
			"--password", neo4j.getAdminPassword(),
			"-m", "MICROMETER"
		).redirectErrorStream(true).start();

		p.onExit().thenAccept(done -> {
			try (var in = new BufferedReader(new InputStreamReader(done.getInputStream()))) {
				var output = in.lines().collect(Collectors.toCollection(LinkedHashSet::new));
				assertThat(output).containsAll(expectedOutput);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}).get();
	}
}
