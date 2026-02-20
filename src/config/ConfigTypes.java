package config;

import java.util.Map;

/// Remember: there are two ways to call these classes in your code.
/// 1. type the whole class - `ConfigTypes.HostConfig`
/// 2. or......<br>
///    `import static config.ConfigTypes.*` at the top of your file,<br>
///    then just type `HostConfig`<br>
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
			Map<String, String[]> neighborsPerVirtualIP,
			Map<String, String> forwardingTable
	) {}
}