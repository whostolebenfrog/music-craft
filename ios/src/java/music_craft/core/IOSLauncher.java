package music_craft.core;

import clojure.lang.RT;
import clojure.lang.Symbol;

import com.badlogic.gdx.*;
import com.badlogic.gdx.backends.iosrobovm.*;

import org.robovm.apple.foundation.*;
import org.robovm.apple.uikit.*;

public class IOSLauncher extends IOSApplication.Delegate {
	protected IOSApplication createApplication() {
		IOSApplicationConfiguration config = new IOSApplicationConfiguration();
		RT.var("clojure.core", "require").invoke(Symbol.intern("music-craft.core"));
		try {
			Game game = (Game) RT.var("music-craft.core", "music-craft").deref();
			return new IOSApplication(game, config);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] argv) {
		NSAutoreleasePool pool = new NSAutoreleasePool();
		UIApplication.main(argv, null, IOSLauncher.class);
		pool.close();
	}
}
