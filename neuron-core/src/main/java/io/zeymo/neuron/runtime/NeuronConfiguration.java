package io.zeymo.neuron.runtime;

import io.zeymo.cache.CacheLayout;
import io.zeymo.commons.properties.JsonProperties;
import io.zeymo.commons.properties.JsonPropertyConstructable;
import io.zeymo.commons.properties.JsonPropertyUtils;
import io.zeymo.network.schema.NetworkLayout;
import io.zeymo.neuron.schema.*;

public class NeuronConfiguration implements JsonPropertyConstructable {

	private final EngineLayout engineLayout;
	// private final AllocPolicyLayout allocPolicyLayout;
	// private final CacheLayout cacheLayout;
	// private final FieldLayout fieldLayout;
	// private final MatcherLayout matcherLayout;
	private final NodeLayout nodeLayout;
	// private final RankLayout rankLayout;
	// private final StatLayout statLayout;
	private final TaskLayout taskLayout;
	private final DatabaseLayout databaseLayout;
	private final NetworkLayout networkLayout;
	private final CacheLayout cacheLayout;

	public CacheLayout getCacheLayout() {
		return cacheLayout;
	}

	public ClusterLayout getClusterLayout() {
		return clusterLayout;
	}

	private final ClusterLayout	clusterLayout;

	public NetworkLayout getNetworkLayout() {
		return networkLayout;
	}

	public DatabaseLayout getDatabaseLayout() {
		return databaseLayout;
	}

	private final JsonProperties property;

	public JsonProperties getProperty() {
		return property;
	}

	public NeuronConfiguration(JsonProperties property) {
		try {
			this.property = property;
			this.nodeLayout = JsonPropertyUtils.newInstance(NodeLayout.class, property.getSubProperties("node-layout"));
			// this.fieldLayout =
			// JsonPropertyUtils.newInstance(FieldLayout.class,
			// property.getSubProperties("field-layout"));
			// this.matcherLayout =
			// JsonPropertyUtils.newInstance(MatcherLayout.class,
			// property.getSubProperties("matcher-layout"));
			// this.statLayout = JsonPropertyUtils.newInstance(StatLayout.class,
			// property.getSubProperties("stat-layout"));
			// this.rankLayout = JsonPropertyUtils.newInstance(RankLayout.class,
			// property.getSubProperties("rank-layout"));
			// this.allocPolicyLayout =
			// JsonPropertyUtils.newInstance(AllocPolicyLayout.class,
			// property.getSubProperties("alloc-policy-layout"));
			// this.cacheLayout =
			// JsonPropertyUtils.newInstance(CacheLayout.class,
			// property.getSubProperties("cache-layout"));
			this.taskLayout = JsonPropertyUtils.newInstance(TaskLayout.class, property.getSubProperties("task-layout"));
			this.engineLayout = JsonPropertyUtils.newInstance(EngineLayout.class, property.getSubProperties("engine-layout"));
			this.networkLayout = JsonPropertyUtils.newInstance(NetworkLayout.class, property.getSubProperties("network-layout"));
			this.databaseLayout = JsonPropertyUtils.newInstance(DatabaseLayout.class, property.getSubProperties("database-layout"));
			this.cacheLayout = JsonPropertyUtils.newInstance(CacheLayout.class, property.getSubProperties("cache-layout"));
			this.clusterLayout = JsonPropertyUtils.newInstance(ClusterLayout.class, property.getSubProperties("cluster-layout"));

		} catch (Exception e) {
			throw new RuntimeException("unable to initialize NeuronConfiguration, " + property.toPrettyString(), e);
		}
	}

	// public int getFieldIndex(String name) {
	// return this.fieldLayout.getIndex(name);
	// }

	// public AllocPolicyLayout getAllocPolicyLayout() {
	// return allocPolicyLayout;
	// }

	// public FieldLayout getFieldLayout() {
	// return fieldLayout;
	// }

	// public String getFieldName(int typeIndex) {
	// return this.fieldLayout.getFieldName(typeIndex);
	// }
	//
	// public MatcherLayout getMatcherLayout() {
	// return matcherLayout;
	// }
	//
	// public String getMatcherName(int typeIndex) {
	// return this.matcherLayout.getMatcherName(typeIndex);
	// }
	//
	// public int getMatcherTypeIndex(String name) {
	// return this.matcherLayout.getMatcherTypeIndex(name);
	// }

	public EngineLayout getEngineLayout() {
		return engineLayout;
	}

	public NodeLayout getNodeLayout() {
		return nodeLayout;
	}

	// public RankLayout getRankLayout() {
	// return rankLayout;
	// }

	public int getSectorIndex(String sectorName) {
		return this.nodeLayout.getSectorIndex(sectorName);
	}

	// public StatLayout getStatLayout() {
	// return statLayout;
	// }

	public TaskLayout getTaskLayout() {
		return taskLayout;
	}

}
