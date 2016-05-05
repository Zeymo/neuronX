package io.zeymo.neuron.task;

import io.zeymo.neuron.assembly.domain.XNode;

import java.io.IOException;

public interface UpdateTaskContext extends MapTaskContext {

	public boolean putNode(long nodeId, byte[] buffer, int offset, int length) throws IOException;

	public boolean putNode(XNode xnode) throws IOException;

	public XNode getXNode(long nodeId) throws IOException;

	public XNode getXNode();

}
