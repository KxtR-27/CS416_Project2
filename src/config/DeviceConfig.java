package config;

import java.util.Arrays;

/**
 * Stores configuration information for network devices except for the device ID.
 * The device ID is used as a map key in the {@code ConfigParser} class instead.
 * This is because the device ID must be provided in order to access the configuration
 * as it is outlined in the rubric.
 * <p>
 * <br>
 * <em>
 * A record is a special data storage class that is immutable/final and public.
 * It removes the need to write a boilerplate class with getters and constructors.
 * <p>
 * The attributes themselves are variables, but they are accessed with a method of the same name.
 * For example, instead of using {@code myDeviceConfig.port},
 * you might use {@code myDeviceConfig.port()} with parentheses.
 * </em>
 *
 * @param port      The port on which the host or switch operates
 * @param ipAddress The IP address on which the host or switch operates
 * @param neighbors The "linked" host and switches of this device in the topology
 *
 * @see ConfigParser
 */
public record DeviceConfig(
		int port,
		String ipAddress,
		String[] neighbors
) {
	@Override
	public String toString() {
		return String.format(
				"%s:%s | neighbors: %s",
				ipAddress, port, Arrays.toString(neighbors)
		);
	}
}
