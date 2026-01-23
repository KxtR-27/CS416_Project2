package config;

import java.util.Arrays;

record ConfigSingleton(
		ConfigDevice[] devices
) {
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(String.format("Loaded Devices:%n"));

		for (ConfigDevice device : devices)
			builder.append(String.format("- %s%n", device));

		return builder.toString();
	}
}

record ConfigDevice(
		String mac,
		int port,
		String ip,
		String[] links
) {
	@Override
	public String toString() {
		return String.format(
				"%-2s | %s:%s | links: %s",
				mac, ip, port, Arrays.toString(links)
		);
	}
}