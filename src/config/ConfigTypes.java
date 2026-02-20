package config;

import java.util.Map;

public class ConfigTypes {
	public record HostConfig(
			String id,
			String realIP,
			int realPort,
			String virtualIP,
			String gatewayVIP,
			String[] neighbors
	) {}
	public record SwitchConfig(
			String id,
			String ipAddress,
			int port,
			String[] neighbors
	) {}
	public record RouterConfig(
			String id,
			String realIP,
			int realPort,
			String[] virtualIPs,
			Map<String, String> neighborsPerVirtualIP,
			Map<String, String> forwardingTable
	) {}
}
