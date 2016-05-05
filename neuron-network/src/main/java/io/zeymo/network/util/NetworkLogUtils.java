package io.zeymo.network.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.CodeSource;
import java.security.ProtectionDomain;

/**
 * Created By Zeymo at 14/12/26 13:40
 */
public class NetworkLogUtils {

	public static final Logger	log		= LoggerFactory.getLogger("network");

	public final static boolean	DEBUG	= false;

	public static Logger getLogger() {
		return log;
	}

	public static String toString(Class<?> clazz) {
		ProtectionDomain pd = clazz.getProtectionDomain();
		CodeSource cs = pd.getCodeSource();
		System.out.println(cs.getLocation());
		return clazz.getName() + " @ {" + cs.getLocation() + "}";
	}
}
