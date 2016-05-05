package io.zeymo.network.management;

import io.zeymo.network.util.NetworkLogUtils;

import java.util.List;

public abstract class MatrixDelegate {

	public static interface MatrixListener {
		public void handleData(String dataId, List<String> data);
	}

	private static MatrixDelegate	INSTANCE;

	public static void addMatrixListener(String subscriberName, String groupId, String dataId, MatrixListener listener) {
		checkInstance();
		INSTANCE.doAddMatrixListener(subscriberName, groupId, dataId, listener);
	}

	private static void checkInstance() {
		if (INSTANCE == null) {
			throw new RuntimeException("MatrixDelegate is not wired.");
		}
	}

	public static void publishMatrixNode(String publisherName, String groupId, String dataId, String data) {
		checkInstance();
		INSTANCE.doPublishMatrixNode(publisherName, groupId, dataId, data);

	}

	public static void wire(MatrixDelegate delegate) {
		if (INSTANCE != null) {
			System.err.println("[WARN] wiring MatrixDelegate " + NetworkLogUtils.toString(delegate.getClass()));
			System.err.println("[WARN] MatrixDelegate is already wired to " + NetworkLogUtils.toString(INSTANCE.getClass()));
		}
		INSTANCE = delegate;
	}

	protected abstract void doAddMatrixListener(String subscriberName, String groupId, String dataId, MatrixListener listener);

	protected abstract void doPublishMatrixNode(String publisherName, String groupId, String dataId, String data);
}
