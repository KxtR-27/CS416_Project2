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
 * Parses device configurations from a {@code config.json} file in the same directory.<br>
 * <em>As a static utility class, this cannot be instanced.</em>
 * <p>
 * <b><em><u>If you are not a developer of this package, you only need to worry about {@code getConfigForDevice()}.</u></em></b>
 * <p>
 * The raw configuration loader uses Google Gson to read the {@code config.json} file.<br> From there, loaded
 * information is parsed into a {@code Map} ({@code Hashmap}), as maps are optimized for getting and setting operations,
 * which is what this class is for.
 * <p>
 * Every call to a public method also refreshes the map, so any changes made to the {@code config.json} file will
 * reflect immediately.
 *
 * @author KxtR-27 (Kat)
 * @see #getConfigForDevice(String)
 * @see DeviceConfig
 */
@SuppressWarnings("unused" /* because it's used later based on the rubric. */)
class ConfigParser {
	/**
	 * The Gson object used in conjunction with a reader to read data from the {@code config.json} file.
	 */
	private static final Gson GSON = new Gson();

	/**
	 * A parsed map of keys (device IDs) and values ({@code DeviceConfig}s)
	 * that serves as a functional representation of the JSON structure itself.
	 */
	private static final Map<String, DeviceConfig> CONFIGS_MAP = new HashMap<>(7);

	/**
	 * Gets the configuration information for a virtual network device.
	 * Calling this method also updates the parsed configuration,
	 * so you can call this multiple times to pull potential changes to the config file.
	 *
	 * @param id ID/"MAC" for a host or switch device in the virtual network, (ex. "S1" or "A")
	 *
	 * @return a {@code DeviceConfig} object with the device's port, IP address, and neighbors,
	 * 		<em>or <b>null</b> if no configuration exists for the ID.</em>
	 *
	 * @see DeviceConfig
	 */
	@SuppressWarnings("unused" /* because it's used later on based upon the rubric.*/)
	public static DeviceConfig getConfigForDevice(String id) {
		updateConfigMap();
		return CONFIGS_MAP.get(id);
	}

	/**
	 * Parses config file information into entries which are put into the config map.
	 * Device IDs serve as keys, and the other device configuration information are encapsulated in the value.
	 * This effectively "reloads" the {@code config.json} file and updates existing entries.
	 *
	 * @see #loadConfigFile()
	 * @see RawDeviceConfig
	 */
	private static void updateConfigMap() {
		RawDeviceConfig[] rawConfigs = loadConfigFile();

		// abort update if something went wrong
		if (rawConfigs == null) {
			System.err.printf("Config file loaded, but valid configuration is not present.%n");
			return;
		}

		for (RawDeviceConfig rawConfig : rawConfigs)
			CONFIGS_MAP.put(
					rawConfig.id, new DeviceConfig(
							rawConfig.port,
							rawConfig.ipAddress,
							rawConfig.neighbors
					)
			);
	}

	/**
	 * Using a JsonReader from gson, converts the {@code config.json}
	 * file's JSON structure into an effectively identical array of objects.
	 *
	 * @return an array of {@code RawDeviceConfig}
	 *
	 * @see RawDeviceConfig
	 */
	private static RawDeviceConfig[] loadConfigFile() {
		// try-with-resources automatically closes the readers after using them
		try (JsonReader reader = new JsonReader(new FileReader("src/config/config.json"))) {
			return GSON.fromJson(reader, RawDeviceConfig[].class);
		}
		catch (Exception e) {
			printErrorWithMessage(e);
			return null;
		}
	}

	/**
	 * Prints a more human-readable indication of any error in {@code loadConfigFile()}, should an error occur.
	 * Then prints the error stacktrace as well.
	 *
	 * @param e the caught exception to map to a message and print
	 *
	 * @see #loadConfigFile()
	 */
	private static void printErrorWithMessage(Exception e) {
		String extraMessage = switch (e) {
			case JsonIOException _ -> "Unable to read config file.";
			case JsonSyntaxException _ -> "Could not correctly parse config file.";
			case FileNotFoundException _ -> "Config file not found.";
			case IOException _ -> "Unexpected file-related issue occurred.";

			default -> "Unhandled exception occurred.";
		};

		System.err.printf("%s%n", extraMessage);
	}

	/**
	 * Used internally to map the config.json file's contents to an object of effectively identical form.
	 * A RawDeviceConfig is later used to create a map entry for a parsed configuration structure.
	 * For usable information, use a DeviceConfig record instead.
	 * <p>
	 * This class provides no functionality aside from storing data in a record to be parsed later.
	 *
	 * @param id        The ID/"MAC" of a host or switch device
	 * @param port      The port on which the host or switch operates
	 * @param ipAddress The IP address on which the host or switch operates
	 * @param neighbors The "linked" host and switches of this device in the topology
	 *
	 * @see #loadConfigFile()
	 * @see DeviceConfig
	 */
	private record RawDeviceConfig(
			String id,
			int port,
			String ipAddress,
			String[] neighbors
	) {
	}
}
