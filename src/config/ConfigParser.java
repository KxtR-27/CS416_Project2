package config;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles loading and parsing of the configuration files.
 * More Javadoc soon.
 * @author Kat
 */
class ConfigParser {
	private static final Gson GSON = new Gson();
	private static Map<String, DeviceConfig> configMap;

	/**
	 * Returns an object with a port, IP address, and list of neighbors, in that order.
	 * The only method you need to worry about.
	 * Calling this method more than once effectively refreshes the config.
	 * More Javadoc soon.
	 * @author KxtR-27
	 * @see DeviceConfig
	 */
	public static DeviceConfig getConfigForDevice(String id) {
		updateConfigMap();
		return configMap.get(id);
	}


	private static void updateConfigMap() {
		Map<String, DeviceConfig> updatedMap = new HashMap<>();
		RawDeviceConfig[] rawConfigs = loadConfigFile();

		// abort update if something went wrong
		if (rawConfigs == null)
			return;

		for (RawDeviceConfig rawConfig : rawConfigs)
			updatedMap.put(rawConfig.id, new DeviceConfig(rawConfig.port, rawConfig.ipAddress, rawConfig.neighbors));

		configMap = updatedMap;
	}

	private static RawDeviceConfig[] loadConfigFile() {
		// try-with-resources automatically closes the readers after using them
		try (JsonReader reader = new JsonReader(new FileReader("src/config/config.json"))) {
			return GSON.fromJson(reader, RawDeviceConfig[].class);
		}
		catch (Exception e) {
			printExtraErrorMessage(e);
			e.printStackTrace(System.err);

			return null;
		}
	}

	private static void printExtraErrorMessage(Exception e) {
		String extraMessage = switch (e) {
			case JsonIOException _ -> "Unable to read config file.";
			case JsonSyntaxException _ -> "Could not correctly parse config file.";
			case FileNotFoundException _ -> "Config file not found.";
			case IOException _ -> "Unexpected file-related issue occurred.";

			default -> "Unhandled exception occurred.";
		};

		System.err.printf("%s%n", extraMessage);
	}


	private record RawDeviceConfig(
			String id,
			int port,
			String ipAddress,
			String[] neighbors
	) {}
}
