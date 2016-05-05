package io.zeymo.network.management;

import io.zeymo.network.util.NetworkLogUtils;

import java.io.IOException;

public abstract class ConfigurationDelegate {

	public static interface ConfigurationListener {
		public void handle(final String configuration);
	}

	private static ConfigurationDelegate	INSTANCE;

	public static void addConfigurationListener(String groupId, String dataId, ConfigurationListener listener) {
		checkInstance();
		INSTANCE.doAddConfigurationListener(groupId, dataId, listener);
	}

	private static void checkInstance() {
		if (INSTANCE == null) {
			throw new RuntimeException("MatrixDelegate is not wired.");
		}
	}

	public static String getConfiguration(String groupId, String dataId, long timeout) throws IOException {
		checkInstance();
		return INSTANCE.doGetConfiguration(groupId, dataId, timeout);
	}

	public static void wire(ConfigurationDelegate delegate) {
		if (INSTANCE != null) {
			System.err.println("[WARN] wiring ConfigurationDelegate " + NetworkLogUtils.toString(delegate.getClass()));
			System.err.println("[WARN] ConfigurationDelegate is already wired to " + NetworkLogUtils.toString(INSTANCE.getClass()));
		}
		INSTANCE = delegate;
	}

	protected abstract void doAddConfigurationListener(String groupId, String dataId, ConfigurationListener listener);

	protected abstract String doGetConfiguration(String groupId, String dataId, long timeout) throws IOException;

}
