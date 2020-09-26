package fr.exiel.excelsia.bootstrap;

import java.io.File;

import fr.theshark34.openlauncherlib.LaunchException;
import fr.theshark34.openlauncherlib.external.ClasspathConstructor;
import fr.theshark34.openlauncherlib.external.ExternalLaunchProfile;
import fr.theshark34.openlauncherlib.external.ExternalLauncher;
import fr.theshark34.openlauncherlib.minecraft.util.GameDirGenerator;
import fr.theshark34.openlauncherlib.util.CrashReporter;
import fr.theshark34.openlauncherlib.util.SplashScreen;
import fr.theshark34.openlauncherlib.util.explorer.ExploredDirectory;
import fr.theshark34.openlauncherlib.util.explorer.Explorer;
import fr.theshark34.supdate.BarAPI;
import fr.theshark34.supdate.SUpdate;
import fr.theshark34.swinger.Swinger;
import fr.theshark34.swinger.colored.SColoredBar;

public class ExcelsiaBootstrap {
	
	private static SplashScreen splash;
	private static SColoredBar bar;
	private static Thread barThread;
	
	private static final File EX_B_DIR = new File(GameDirGenerator.createGameDir("Excelsia"), "Launcher");

	private static CrashReporter EX_B_CRASHREPORTER = new CrashReporter("Excelsia", EX_B_DIR);
	
	public static void main(String[] args) {
		Swinger.setResourcePath("/fr/exiel/excelsia/bootstrap/resources/");
		displaySplash();
		try {
			doUpdate();
		} catch (Exception e) {
			EX_B_CRASHREPORTER.catchError(e, "Impossible de mettre a jour le launcher !");
			barThread.interrupt();
		}
		try {
			launchLauncher();
		}catch (LaunchException e) {
			EX_B_CRASHREPORTER.catchError(e, "Impossible de lancer le launcher !");
		}
	}
	
	private static void displaySplash() {
		splash = new SplashScreen("Excelsia", Swinger.getResource("splash.png"));
		
		bar = new SColoredBar(Swinger.getTransparentWhite(100), Swinger.getTransparentWhite(175));
		bar.setBounds(52, 344, 391, 21);
		splash.add(bar);
		
		splash.setBackground(Swinger.TRANSPARENT);
		splash.setVisible(true);
		
	}
	
	private static void doUpdate() throws Exception{
		
		SUpdate su = new SUpdate("http://146.59.145.252/bootstrap/",EX_B_DIR);
		su.getServerRequester().setRewriteEnabled(true);
		
		barThread = new Thread() {
			@Override
			public void run() {
				while(!this.isInterrupted()) {
					bar.setValue((int) (BarAPI.getNumberOfTotalDownloadedBytes() / 1000));
					bar.setMaximum((int) (BarAPI.getNumberOfTotalBytesToDownload() / 1000));
				}
			}
		};
		barThread.start();
		
		su.start();
		barThread.interrupt();
	}
	
	private static void launchLauncher() throws LaunchException{
		ClasspathConstructor constructor = new ClasspathConstructor();
		ExploredDirectory gameDir = Explorer.dir(EX_B_DIR);
		constructor.add(gameDir.sub("Libs").allRecursive().files().match("^(.*\\.((jar)$))*$"));
		constructor.add(gameDir.get("launcher.jar"));
		
		
		ExternalLaunchProfile profile = new ExternalLaunchProfile("fr.exiel.excelsia.launcher.LauncherFrame", constructor.make());
		ExternalLauncher launcher = new ExternalLauncher(profile);
		
		Process p = launcher.launch();
		splash.setVisible(false);
		try {
			p.waitFor();
		}catch (InterruptedException ignored) {
		}
		System.exit(0);
	}
}
