import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import org.eclipse.jdt.internal.jarinjarloader.JarRsrcLoader;

public class LauncherMain {
	public static void main(String[] args) {
		try {
			JarRsrcLoader.main(args);
		} catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException | InvocationTargetException
				| SecurityException | NoSuchMethodException | IOException e) {
			e.printStackTrace();
		}
	}
}
