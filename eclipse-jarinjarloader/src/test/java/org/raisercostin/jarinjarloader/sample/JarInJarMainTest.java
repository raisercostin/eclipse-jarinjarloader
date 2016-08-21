package org.raisercostin.jarinjarloader.sample;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.jdt.internal.jarinjarloader.JarRsrcLoader;
import org.junit.Test;

public class JarInJarMainTest {
	public static void main(String[] args) {
		try {
			JarRsrcLoader.main(new String[] {});
		} catch (Throwable e) {
			e.printStackTrace(System.err);
		}
	}
//	@Test
//	public void initAppTest() throws ClassNotFoundException, IllegalArgumentException, IllegalAccessException,
//			InvocationTargetException, SecurityException, NoSuchMethodException, IOException {
//		JarRsrcLoader.main(new String[] {});
//	}
}
