package config;

// only a skeleton, so "unused" warning can be disregarded for now.
@SuppressWarnings("unused")

record ConfigSingleton(
		ConfigDevice[] devices
) {

}

record ConfigDevice(
		String mac,
		int port,
		String ip,
		String[] links
) {}