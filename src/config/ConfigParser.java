package config;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static config.ConfigTypes.*;

@SuppressWarnings("unused" /* because it's used later based on the rubric. */)
public class ConfigParser {
	/// The Gson object used in conjunction with a JsonReader to read data from the `config.json` file.
	private static final Gson GSON = new Gson();

	private static final Map<String, HostConfig> hostsMap = new HashMap<>();

	private static final Map<String, SwitchConfig> switchesMap = new HashMap<>();

	private static final Map<String, RouterConfig> routersMap = new HashMap<>();


	public static HostConfig getHostConfig(String id) {
		if (hostsMap.isEmpty())
			parseConfig();
		return hostsMap.get(id);
	}

	public static SwitchConfig getSwitchConfig(String id) {
		if (switchesMap.isEmpty())
			parseConfig();
		return switchesMap.get(id);
	}

	public static RouterConfig getRouterConfig(String id) {
		if (routersMap.isEmpty())
			parseConfig();
		return routersMap.get(id);
	}


	public static HostConfig getHostConfigFromVIP(String virtualIP) {
		String id = virtualIP.split("\\.")[1];
		return getHostConfig(id);
	}

	public static RouterConfig getRouterConfigFromVIP(String virtualIP) {
		String id = virtualIP.split("\\.")[1];
		return getRouterConfig(id);
	}


	private static void parseConfig() {
		ConfigSnapshot configuration = readConfigFile();
		populateDeviceMaps(configuration);
	}

	private static void populateDeviceMaps(ConfigSnapshot configuration) {
		hostsMap.clear();
		switchesMap.clear();
		routersMap.clear();

		for (HostConfig hostDevice : configuration.hosts())
			hostsMap.put(hostDevice.id(), hostDevice);
		for (SwitchConfig switchDevice : configuration.switches())
			switchesMap.put(switchDevice.id(), switchDevice);
		for (RouterConfig routerDevice : configuration.routers())
			routersMap.put(routerDevice.id(), routerDevice);
	}

	private static ConfigSnapshot readConfigFile() {
		try (JsonReader reader = newConfigReader()) {
			return GSON.fromJson(reader, ConfigSnapshot.class);
		}
		catch (IOException e) {
			System.err.printf("An unexpected error occurred while parsing the config file.%n");
			throw new RuntimeException(e);
		}
	}

	private static JsonReader newConfigReader() throws FileNotFoundException {
		return new JsonReader(new FileReader("src/config/config.json"));
	}


	private record ConfigSnapshot(
			HostConfig[] hosts,
			SwitchConfig[] switches,
			RouterConfig[] routers
	) {
	}
}
