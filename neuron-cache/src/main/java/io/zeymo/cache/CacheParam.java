package io.zeymo.cache;

public interface CacheParam {

	public abstract byte[] getKey();

	public abstract byte[] getValue();

	public abstract int getValueLength();

	public abstract void setValueLength(int valueLength);

	public abstract byte[] getEvictKey();

	public abstract byte[] getEvictValue();

	public abstract int getEvictValueLength();

	public abstract boolean isEvicted();

	public abstract void setEvicted(boolean evicted);

	public abstract void setEvictValueLength(int lenght);

}
