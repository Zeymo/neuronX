package io.zeymo.neuron.task;

import io.zeymo.commons.io.NeuronInputBuffer;
import io.zeymo.commons.io.NeuronOutputBuffer;

import java.io.IOException;

public interface Updater {
	public void init(UpdateTaskContext taskContext);

	public void initUpdateTask(NeuronInputBuffer paramInput) throws IOException;

	public void update(long nodeId, UpdateTaskContext taskContext) throws IOException;

	public void finishUpdateTask(NeuronOutputBuffer output) throws IOException;

}

// tasks:
// {
// "feedTask":{
// "type":"map-reduce",
// "mapperClass":"aa",
// "reducerClass":"aa"
// },
// "relationTask":{
// "type":"map",
// "mapperClass":"bb"
// }
// }
