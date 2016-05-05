package io.zeymo.neuron.schema;

import io.zeymo.commons.properties.JsonProperties;
import io.zeymo.commons.properties.JsonPropertyConfigurable;
import io.zeymo.neuron.NeuronRequestType;
import io.zeymo.neuron.domain.Varchar;
import io.zeymo.neuron.task.Mapper;
import io.zeymo.neuron.task.Reducer;
import io.zeymo.neuron.task.Updater;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskLayout implements JsonPropertyConfigurable {

	private HashMap<Varchar, NeuronRequestType>	typeMap;

	public NeuronRequestType getType(Varchar taskName) {
		return typeMap.get(taskName);
	}

	public final static class TaskInstanceLayout {
		private final Class<? extends Mapper>	mapperClazz;
		private final Class<? extends Reducer>	reducerClazz;
		private final Class<? extends Updater>	updaterClazz;

		private final String					name;
		private final NeuronRequestType			type;

		public TaskInstanceLayout(String name, NeuronRequestType type, Class<? extends Mapper> mapperClazz, Class<? extends Reducer> reducerClazz, Class<? extends Updater> updaterClazz) {
			this.name = name;
			this.type = type;
			this.mapperClazz = mapperClazz;
			this.reducerClazz = reducerClazz;
			this.updaterClazz = updaterClazz;
		}

		public Class<? extends Mapper> getMapperClazz() {
			return mapperClazz;
		}

		public Class<? extends Reducer> getReducerClazz() {
			return reducerClazz;
		}

		public Class<? extends Updater> getUpdaterClazz() {
			return updaterClazz;
		}

		public NeuronRequestType getType() {
			return type;
		}

		public String getName() {
			return name;
		}

	}

	private ArrayList<TaskInstanceLayout>	templateLayoutList;

	public TaskLayout() {

	}

	@SuppressWarnings("unchecked")
	@Override
	public void configure(JsonProperties property) {

		ArrayList<TaskInstanceLayout> layoutList = new ArrayList<TaskInstanceLayout>();
		ArrayList<JsonProperties> templateList = property.getArray("tasks");
		HashMap<Varchar, NeuronRequestType> typeMap = new HashMap<Varchar, NeuronRequestType>();

		for (JsonProperties p : templateList) {
			String typeString = p.getStringNotNull("type");
			NeuronRequestType type = NeuronRequestType.valueOf(typeString);
			String name = p.getStringNotNull("name");

			Class<? extends Mapper> mapperClazz = null;
			Class<? extends Reducer> reducerClazz = null;
			Class<? extends Updater> updaterClazz = null;
			String clazzName = null;

			if (type == NeuronRequestType.MAP_ONLY || type == NeuronRequestType.MAP_REDUCE) {
				try {
					clazzName = p.getStringNotNull("mapper-class");
					mapperClazz = (Class<? extends Mapper>) Class.forName(clazzName);
				} catch (ClassNotFoundException e) {
					throw new IllegalArgumentException("could not find task-template classes : " + clazzName);
				}

				if (type == NeuronRequestType.MAP_REDUCE) {
					try {
						clazzName = p.getStringNotNull("reducer-class");
						reducerClazz = (Class<? extends Reducer>) Class.forName(clazzName);
					} catch (ClassNotFoundException e) {
						throw new IllegalArgumentException("could not find task-template classes : " + clazzName);
					}
				}
			}
			if (type == NeuronRequestType.UPDATE_ONLY) {
				try {
					clazzName = p.getStringNotNull("updater-class");
					updaterClazz = (Class<? extends Updater>) Class.forName(clazzName);
				} catch (ClassNotFoundException e) {
					throw new IllegalArgumentException("could not find task-template classes : " + clazzName);
				}
			}

			typeMap.put(new Varchar(name), type);
			TaskInstanceLayout layout = new TaskInstanceLayout(name, type, mapperClazz, reducerClazz, updaterClazz);
			layoutList.add(layout);
		}
		this.templateLayoutList = layoutList;
		this.typeMap = typeMap;
	}

	public ArrayList<TaskInstanceLayout> getTemplateLayoutList() {
		return templateLayoutList;
	}

	public void setTemplateLayoutList(ArrayList<TaskInstanceLayout> templateLayoutList) {
		this.templateLayoutList = templateLayoutList;
	}

}
