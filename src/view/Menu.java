package view;

import resources.Images;
import resources.Music;
import util.Quote;
import util.Util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.ToggleSwitch;

import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import model.JSONParser;
import model.Movie;
import model.TextScriptParser;
import model.PdfScriptParser;
import model.Talker;

public class Menu extends Pane {

	private final Talker null_talker = new Talker("Select", "Select", new ArrayList<Quote>());
	private int max_characters_displayed = 500;

	private Window w;
	private MediaPlayer mp;
	private PdfScriptParser psp;
	public static final String font = "ChalkBoard SE";
	public boolean music = false, movie_mode = false;
	private LinkedList<String> history = new LinkedList<String>(); //prev talker responses
	private int history_size = 10, history_idx = 0;

	private ImageView menubg;
	private TextField user_field;
	private TextArea response; private DropShadow ds;
	private ComboBox<String> talker_menu;
	private CheckComboBox<String> movie_menu;
	private ToggleSwitch movie_mode_switch;
	private Button upload, remove_movies;

	private List<Movie> all_movies;
	private List<Movie> selected_movies;
	private List<Talker> talkers;
	private Talker talker = null_talker;


	public Menu(Window w) {	
		this.w = w;
		this.all_movies = new ArrayList<Movie>();
		this.selected_movies = new ArrayList<Movie>();
		this.talkers = new ArrayList<Talker>();  
		this.psp = new PdfScriptParser();

		addContent();
		addEvents();
		fixLayout();	

		initResponses();
		initMovies();
		//addMovie(new File(getClass().getResource("/resources/scripts/Harry-Hbp.pdf").getPath()));
		//Util.print(all_movies.get(0).getQuotes().keySet());
		
		//movie_menu.getCheckModel().check("Harry-Hbp");
		movie_menu.getCheckModel().check("Hobbit-Auj");
		movie_menu.getCheckModel().check("Hobbit-Bofa");
		movie_menu.getCheckModel().check("Lotr-Fotr");
		movie_menu.getCheckModel().check("Lotr-Tt");
		movie_menu.getCheckModel().check("Lotr-Rotk");

		setTalker(null_talker);
		setTalker("gandalf");
		movie_mode_switch.setSelected(movie_mode);
		//	talker_menu.setValue(Util.toCamelCase(talker.getName()));
	}

	private void addContent() {	
		movie_mode_switch = new ToggleSwitch("Movie mode");

		upload = new Button("Upload Screenplay Pdf");
		remove_movies = new Button("DELETE Selected Movies");
		user_field = new TextField();
		menubg = new ImageView(Images.default_bg);
		response = new TextArea();
		talker_menu = new ComboBox<String>();
		movie_menu = new CheckComboBox<String>();
		ds = new DropShadow();

		this.getChildren().addAll(menubg, user_field, response, talker_menu, 
				movie_menu, movie_mode_switch, upload, remove_movies);	
	}

	private void addEvents() {
		menubg.setOnMouseClicked(m->{
			this.requestFocus();
		});
		response.setOnKeyPressed(k->{
			//	Util.print(k.getCode()+" pressed on response");
			this.getOnKeyPressed().handle(k);
		});
		user_field.setOnKeyPressed(k->{
			//	Util.print(k.getCode()+" pressed on textfield");
			if (k.getCode() != KeyCode.ENTER && k.getCode() != KeyCode.M)
				this.getOnKeyPressed().handle(k);
		});
		movie_mode_switch.selectedProperty().addListener(e->{
			movie_mode = movie_mode_switch.isSelected();
			user_field.setVisible(!movie_mode);
		});		
		upload.setOnAction(e->{
			addMovie(Util.askFile());
		});
		remove_movies.setOnAction(e->{
			List<Integer> movie_idxes = new ArrayList<Integer>(movie_menu.getCheckModel().getCheckedIndices());
			movie_menu.getCheckModel().clearChecks(); //removes selected movies
			deleteMovies(movie_idxes);
		});
		this.setOnKeyPressed(k->{
			//	Util.print(k.getCode()+" pressed on this");
			if (k.getCode() == KeyCode.ENTER) {
				if (movie_mode) {
					setResponse(talker.readNext());
					addHistory(response.getText());
				}
				else {
					setResponse(talker.getResponse(user_field.getText()));
					addHistory(response.getText());
				}
			}
			else if (k.getCode() == KeyCode.LEFT) {
				if (movie_mode) {
					setResponse(talker.readPrev());
					addHistory(response.getText());	
				}
				else if (user_field.getCaretPosition() == 0 && history_idx > 0)
					setResponse(history.get((history_idx-=1)));
			}
			else if (k.getCode() == KeyCode.RIGHT) {
				if (movie_mode)
					setResponse(talker.readNext());
				else if (user_field.getCaretPosition() == user_field.getText().length() && history_idx+1 < history.size()) {	
					setResponse(history.get((history_idx+=1)));
				}
			}
			else if (k.getCode() == KeyCode.M)
				if (mp.getStatus() == Status.PAUSED || !music) {
					mp.play();
					music = true;
				}
				else {
					mp.pause();
					music = false;
				}
		});		
		talker_menu.getItems().addListener(new ListChangeListener<String>() {
			@Override
			public void onChanged(Change<? extends String> c) {
				c.next();
				//Util.print("talker menu items changed "+c);
				if (c.wasAdded() && talker_menu.getItems().size() == 1) {
					setTalker(talkers.get(0));	//change to all characters
				//	w.resizeHeight(menubg.getFitWidth());
					remove_movies.setDisable(false);
				} 
				else if (c.wasRemoved() && talker_menu.getItems().size() == 0) {
					setTalker(null_talker);
				//	w.resizeHeight(menubg.getFitWidth());
					remove_movies.setDisable(true);
				}
			}

		});

		talker_menu.setOnAction(e->{				
			int talker_idx = talker_menu.getItems().indexOf(talker_menu.getValue());

			if (talker_idx != -1 && !talker_menu.getValue().equals(talker.getName())) {
				setTalker(talkers.get(talker_idx));
				w.resizeHeight(menubg.getFitWidth());
			}
		});	
		movie_menu.getCheckModel().getCheckedIndices().addListener(new ListChangeListener<Integer>() {  
			@Override
			public void onChanged(Change<? extends Integer> c) {	
				c.next();
				//Util.print("movie menu checks changed "+movie_menu.getCheckModel().getCheckedIndices());	
				updateMovies();
				
				if (movie_menu.getCheckModel().getCheckedIndices().size() == 0) {
					setTalker(null_talker);
					remove_movies.setDisable(true);
				}
				else {
					setTalker(talkers.get(0));
					remove_movies.setDisable(false);
				}

			}
		});
	}

	private void fixLayout() {	
		menubg.getStyleClass().add("menubg");
		movie_mode_switch.getStyleClass().add("toggle");
		user_field.getStyleClass().add("user_field");
		response.getStyleClass().add("response");
		talker_menu.getStyleClass().add("talker_menu");
		movie_menu.getStyleClass().add("movie_menu");

		upload.getStyleClass().add("button1");
		remove_movies.getStyleClass().add("button1");
		movie_mode_switch.setWrapText(true);
		movie_mode_switch.setContentDisplay(ContentDisplay.CENTER);
		movie_mode_switch.setTextFill(Color.WHITE);

		//ds.setColor(Color.WHITE);
		//response.setEffect(ds);
		response.setEditable(false);
		response.setWrapText(true);       
	}


	public void my_resize(double width, double height) {		
		double k = getK(width, height);
		double h = k*2, w = k*6;

		response.setPrefWidth(width);
		response.setPrefRowCount(5);
		response.setPrefHeight(h*5);

		ds.setRadius(k);

		upload.setMaxWidth(w);
		remove_movies.setMaxWidth(w*1.3);
		//movie_mode_switch.setMaxHeight(k/4);

		movie_menu.setPrefWidth(3*(width/22)+1);
		talker_menu.setPrefWidth(3*(width/22+1));
		talker_menu.setMaxWidth(talker_menu.getPrefWidth());
		movie_menu.setMaxWidth(movie_menu.getPrefWidth());

		user_field.setPrefWidth(w*3);
		user_field.setPrefHeight(h);
		menubg.setFitWidth(width);
		menubg.setFitHeight(height);

		this.layout();

		user_field.setLayoutX(width/2-user_field.getLayoutBounds().getWidth()/2);
		user_field.setLayoutY(height/2-user_field.getLayoutBounds().getHeight()/2-h*2);
		response.setLayoutX(width/2-response.getLayoutBounds().getWidth()/2); //prefwidth
		response.setLayoutY(user_field.getLayoutY()+user_field.getLayoutBounds().getHeight()+h);
		movie_menu.setLayoutX(width-movie_menu.getLayoutBounds().getWidth()-5);
		movie_menu.setLayoutY(10);
		talker_menu.setLayoutX(movie_menu.getLayoutX()-talker_menu.getLayoutBounds().getWidth()-5);
		talker_menu.setLayoutY(movie_menu.getLayoutY());
		movie_mode_switch.setLayoutX(talker_menu.getLayoutX()-movie_mode_switch.getLayoutBounds().getWidth()-5);
		movie_mode_switch.setLayoutY(movie_menu.getLayoutY());
		upload.setLayoutX(movie_mode_switch.getLayoutX()-upload.getLayoutBounds().getWidth()-5);
		upload.setLayoutY(movie_menu.getLayoutY());
		remove_movies.setLayoutX(upload.getLayoutX()-remove_movies.getLayoutBounds().getWidth()-5);
		remove_movies.setLayoutY(movie_menu.getLayoutY());
		setResponse(response.getText());
	}

	private void addMovie(Movie m) {
		all_movies.add(m);
		movie_menu.getItems().add(m.getName());

		refreshChecks();
	}

	private void refreshChecks() {
		for (int checked_idx: movie_menu.getCheckModel().getCheckedIndices())
			movie_menu.getCheckModel().check(checked_idx);
	}
	
	private void updateMovies() {
		List<Integer> checked_idxes = movie_menu.getCheckModel().getCheckedIndices();
		selected_movies.clear();
		
		for (int idx: checked_idxes) {
			Movie m = all_movies.get(idx);
			if (!selected_movies.contains(m)) {
				selected_movies.add(m);
			}
		}
		updateTalkers();
	}

	private void addTalker(String... names) {
		List<Quote> quotes = new ArrayList<Quote>(0);
		for (Movie movie: selected_movies) {
			for (String name: names) {
				Util.mergeQuotes(quotes, movie.getQuotes(name));
			}		
		}
		Talker t = new Talker(names[0], "", quotes);
		addTalker(t);
	}

	private void addTalker(Talker t) {
		talkers.add(t);
		talker_menu.getItems().add(Util.toCamelCase(t.getName()));
	}

	private void updateTalkers() {
		talkers.clear();
		talker_menu.getItems().clear();

		if (selected_movies.size() == 0) return;

		int q = (int)Math.ceil(max_characters_displayed/selected_movies.size());
		List<Quote> all_movies_script = new ArrayList<Quote>();
		addTalker(new Talker("All Characters", "", all_movies_script)); //all characters
		
		for (Movie m: selected_movies) {
			Util.mergeQuotes(all_movies_script, m.getScript());
			List<String> top_talkers = m.getPopularCharacters(q+5);
			int i = 0, added = 0;
			while (i < top_talkers.size() && added < q) {
				String top_talker = top_talkers.get(i);
				if (!hasTalker(top_talker)) {
					addTalker(top_talker);
					added++;
				}
				i++;
			}
		}
	}

	private boolean hasTalker(String name) {
		name = name.toLowerCase();
		for (Talker t: talkers)
			if (t.getName().toLowerCase().equals(name))
				return true;
		return false;
	}

	private void setTalker(String name) {
		name = name.toLowerCase();
		for (Talker t: talkers) 
			if (t.getName().toLowerCase().equals(name)) {
				setTalker(t);
				break;
			}
	}

	private void setTalker(Talker t) {
		talker = t;
		t.resetRead();

		w.setTitle(Util.toCamelCase(talker.getSlogan()));
		user_field.setPromptText("Type to "+Util.toCamelCase(t.getName())+" and press Enter");
		talker_menu.setValue(Util.toCamelCase(t.getName()));

		setResponse(Util.toCamelCase(t.getName())+": Greetings!");
		addHistory(response.getText());
		//Util.print("talker now "+t.getName());
		List<Image> talker_bgs = Images.getBgs(talker.getName());
		if (talker_bgs.size() > 0) {
			int r = (int)(Math.random()*talker_bgs.size());
			menubg.setImage(talker_bgs.get(r));
		}
		else
			menubg.setImage(Images.default_bg);
	}

	private void setResponse(String resp) {
		double k = getK(menubg.getFitWidth(), menubg.getFitHeight()); //this is 0 to start with
		response.setFont(Font.font(font, 28));
		response.setText(resp);
	}

	private void addHistory(String text) {
		history.add(text);
		if (history.size() > history_size)
			history.remove();
		history_idx = history.size()-1; 
		user_field.clear();
	}

	@SuppressWarnings("unchecked")
	private void initResponses() {
		JSONParser jp = new JSONParser();
		File response_file = Paths.get(Util.getParentDir(), "responses", "responses.json").toFile();

		if (response_file != null && response_file.exists() && jp.parse(response_file))
			Talker.setWordMap(jp.word_map);
		else {
			Talker.setWordMap(Talker.makeResponses());
			Util.print(response_file+" does not exist or not in json format");
		}
	}

	private void initMovies() {
		try {
			File out_parent_dir = Paths.get(Util.getParentDir(), "movie_quotes").toFile();
			if (out_parent_dir.exists() && out_parent_dir.isDirectory()) {
				TextScriptParser tsp = new TextScriptParser();

				File[] text_scripts = out_parent_dir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return name.contains(".txt");
					}
				});
				for (File text_script: text_scripts) {
					if (tsp.parse(text_script)) {	
						Movie m = new Movie(Util.removeExt(text_script.getName()), tsp.script, tsp.quotes);
						addMovie(m);
						//Util.print(text_script.getName()+", size:"+tsp.script.size());
					}
					tsp.clear();
				}
			}
			else {
				Util.print(out_parent_dir+" directory does not exist");
				setResponse(out_parent_dir+" directory does not exist");
			}

			String in_parent_dir = Paths.get(File.separator, "resources", "scripts").toString();
			InputStream is = getClass().getResourceAsStream(in_parent_dir);
			BufferedReader r = new BufferedReader(new InputStreamReader(is));
                        Util.print("reading inputstream "+is.available());
			String[] files = {"Hobbit-Auj", "Hobbit-Bofa", "Lotr-Fotr", "Lotr-Tt", "Lotr-Rotk",
					"Harry-Cos", "Harry-Poa", "Harry-Hbp"};
			//String file_name;
			//while ((file_name = r.readLine()) != null) {
			for (String file_name: files) {
				//Util.print("file:"+file_name);
				file_name += ".pdf";
				//if (file_name.contains(".pdf")) {
					InputStream pdf_file = getClass().getResourceAsStream(
							Paths.get(in_parent_dir, file_name).toString());
					addMovie(file_name, pdf_file);
				//}
			}
			r.close();
			is.close();
		}
		catch (Exception e) {
                    e.printStackTrace();
			response.setText(response.getText()+"\n"+"Init movies Error:"+e.getMessage());
		}
	}

	public void addMovie(File pdf_script) { 
		if (pdf_script == null) { Util.print("null file"); return; }
		String name = Util.removeExt(pdf_script.getName());
		if (hasMovie(name)) {
			Util.print(name+" movie already parsed");
			return;
		}
		if (psp.parsePDF(pdf_script)) {
			Movie m = new Movie(name, psp.script, psp.quotes);
			Util.print("movie "+m.getName()+" has "+psp.script.size()+" quotes");
			addMovie(m);
		}
		else
			Util.print("could not parse pdf:"+pdf_script.getName());
		psp.clear();
	}

	public void addMovie(String file_name, InputStream pdf_script) { 
		if (pdf_script == null) { Util.print("null file"); return; }
		String name = Util.removeExt(file_name);
		if (hasMovie(name)) {
			Util.print(name+" movie already parsed");
			return;
		}
		if (psp.parsePDF(pdf_script)) {
			Movie m = new Movie(name, psp.script, psp.quotes);
			Util.print("movie "+m.getName()+" has "+psp.script.size()+" quotes, characters:"+m.getQuotes().keySet());
			addMovie(m);
		}
		else
			Util.print("could not parse pdf:"+file_name);
		psp.clear();
	}

	private void deleteMovies(List<Integer> movie_idxes) { //delete these from files, all_movies and menu items
		File out_parent_dir = Paths.get(Util.getParentDir(), "movie_quotes").toFile();
		if (out_parent_dir.exists() && out_parent_dir.isDirectory()) {		
			for (int idx: movie_idxes) {
				Movie m = all_movies.get(idx);
				File txt_file = Paths.get(out_parent_dir.getPath(), m.getName()+".txt").toFile();
				if (txt_file.exists()) {
					Util.print("deleted "+txt_file.getName());
					txt_file.delete();
				}
			}
		}
		
		for (int idx = movie_idxes.size()-1; idx > -1; idx--) {
			int remove_idx = movie_idxes.get(idx);
			Movie m = all_movies.get(remove_idx);
			movie_menu.getItems().remove(remove_idx);
			all_movies.remove(remove_idx);
		//	Util.print("deleted idx "+remove_idx +", movie: "+m.getName()+" from movie menu");
		}
	}

	private boolean hasMovie(String name) {
		name = name.toLowerCase();
		for (Movie m: all_movies)
			if (m.getName().toLowerCase().equals(name))
				return true;
		return false;
	}

	public void show() {
		this.requestFocus();
		mp = Music.menu_music1;
		if (music)
			mp.play();
	}

	public static double getK(double width, double height) {
		return height < width? height/22+1:width/22+1;
	}

	public double getPrefWidth(double height) {
		double ratio = menubg.getImage().getWidth()/menubg.getImage().getHeight();
		double pref_width = height*ratio;		
		return pref_width;
	}

	public double getPrefHeight(double width) {
		double ratio = menubg.getImage().getHeight()/menubg.getImage().getWidth();
		double pref_height = width*ratio;
		assert(getPrefWidth(pref_height) == width);
		return pref_height;
	}

	public void save() {
		JSONParser.save(Talker.getWordMap(), "responses");
		for (Movie movie: all_movies) {
			TextScriptParser.save(movie.getScript(), movie.getName());
		}
	}
}