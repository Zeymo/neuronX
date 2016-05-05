package io.zeymo.network.message;

import io.zeymo.network.customize.socket.NetworkClient;
import io.zeymo.network.management.MatrixDelegate;
import io.zeymo.network.schema.NotificationLayout;

import java.util.List;

/**
 * Created By Zeymo at 15/1/22 14:49
 */
public class Notification {

	public static class Publisher {

		private NotificationLayout layout;

		public Publisher(NotificationLayout layout) {
			this.layout = layout;
			// PublisherRegistration<String> registration = new PublisherRegistration<String>(layout.getPublisherName(), layout.getDataId());
			// registration.setGroup(layout.getGroupId());
			// publisher0 = PublisherRegistrar.register(registration);
		}

		public void publish(String json) {
			MatrixDelegate.publishMatrixNode(layout.getPublisherName(), layout.getGroupId(), layout.getDataId(), json);
		}

	}

	public static Publisher asPushlisher(NotificationLayout layout) {
		return new Publisher(layout);
	}

	public static class Subscriber implements MatrixDelegate.MatrixListener {

		private final NetworkClient network;

		public Subscriber(NotificationLayout layout, NetworkClient network) {
			this.network = network;

			MatrixDelegate.addMatrixListener(layout.getSubscriberName(), layout.getGroupId(), layout.getDataId(), this);
			// SubscriberRegistration registration = new SubscriberRegistration(layout.getSubscriberName(), layout.getDataId());
			// registration.setGroup(layout.getGroupId());
			// com.taobao.config.client.Subscriber subscriber = SubscriberRegistrar.register(registration); // Do register
			// subscriber.setDataObserver(this);
		}

		@Override
		public void handleData(String s, List<String> list) {
			network.handleData(list);
		}
	}

	public static Subscriber asSubscriber(NotificationLayout layout, NetworkClient networkClient) {
		return new Subscriber(layout, networkClient);
	}

}
