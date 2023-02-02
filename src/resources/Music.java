package resources;

import java.net.URL;
import javafx.animation.Animation;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

public final class Music {

	public static MediaPlayer menu_music1;	
	
	static {
		menu_music1 = getMedia("Bonus-Misty Mountains.mp3");
		menu_music1.setCycleCount(Animation.INDEFINITE);
	}


	private static URL url;
	public static MediaPlayer getMedia(String name) {
		url = Music.class.getResource("music/"+name);
		if (url == null)  {
			System.out.println("music "+name+" not found");
		}
		return new MediaPlayer(new Media(url.toString()));
	}
	
}