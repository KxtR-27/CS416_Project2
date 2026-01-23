package config;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ConfigLoader {
	private static final Gson GSON = new Gson();

	// state will be updated in all public methods
	@SuppressWarnings("FieldMayBeFinal" )
	private static ConfigSingleton state = updateState();

	private static ConfigSingleton updateState() {
		// try-with-resources automatically closes the readers after using them
		try (JsonReader reader = new JsonReader(new FileReader("src/config/config.json"))) {
			return GSON.fromJson(reader, ConfigSingleton.class);
		}
		catch (Exception e) {
			System.err.printf("Config file not found.%n");

			String extraMessage = switch (e) {
				case JsonIOException _ -> "Unable to read config file.";
				case JsonSyntaxException _ -> "Could not correctly parse config file.";
				case FileNotFoundException _ -> "Config file not found.";
				case IOException _ -> "Unexpected file-related issue occurred.";

				default -> "";
			};

			if (!extraMessage.isBlank())
				System.err.printf("%s%n", extraMessage);

			e.printStackTrace(System.err);

			return state;
		}
	}

	static void main() {
		System.out.printf("%s%n", ConfigLoader.state);
	}
}
