import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.Thread.UncaughtExceptionHandler;
import java.net.URL;
import java.nio.charset.Charset;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.json.JSONException;
import org.json.JSONObject;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamDiscoveryEvent;
import com.github.sarxos.webcam.WebcamDiscoveryListener;
import com.github.sarxos.webcam.WebcamEvent;
import com.github.sarxos.webcam.WebcamListener;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamPicker;
import com.github.sarxos.webcam.WebcamResolution;


public class ReadBarcode extends JFrame implements WebcamListener,  WebcamDiscoveryListener, ItemListener, WindowListener, UncaughtExceptionHandler {
	private static final long serialVersionUID = 1L;
	private static StringBuffer barcode = new StringBuffer();
	private JLabel personLabel = new JLabel("Your name:");
	private JTextField person = new JTextField(16);
	private JLabel barcodeLabel = new JLabel("Barcode:");
	private JButton barcodeButton = new JButton("Enter");
	private JTextField barcodeField = new JTextField(13);
	private JLabel brandLabel = new JLabel("Brand:");
	private JLabel brand = new JLabel();
	private JLabel productLabel = new JLabel("Name:");
	private JLabel product = new JLabel();
	private JLabel proteinLabel = new JLabel("Protein per 100g:");
	private static JLabel protein = new JLabel();
	private JLabel fatLabel = new JLabel("Fat per 100g:");
	private static JLabel fat = new JLabel();
	private JLabel energyLabel = new JLabel("Energy per 100g:");
	private static JLabel energy = new JLabel();
	private JTextArea data = new JTextArea(10,40);
	private JLabel scalesLabel = new JLabel("Weight: ");
	private static JLabel weight = new JLabel();
	private JLabel totalEnergyLabel = new JLabel("Energy:");
	private static JLabel totalEnergy = new JLabel();
	private JLabel totalProteinLabel = new JLabel("Protein:");
	private static JLabel totalProtein = new JLabel();
	private JLabel totalFatLabel = new JLabel("Fat:");
	private static JLabel totalFat = new JLabel();
	private JButton identify = new JButton("Identify");
	private JButton reset = new JButton("Reset");
	private JButton portion = new JButton("Portion");
	private JButton hello = new JButton("Hello");
	private JButton silent = new JButton("Silent");
    private JPanel q = new JPanel();
    private JLabel portionLabel = new JLabel("Portion weight:");
    private static JLabel portionValue = new JLabel();
	
	private Webcam webcam = null;
	private WebcamPanel panel = null;
	private WebcamPicker picker = null;
	private static LocalSpeech speaker = new LocalSpeech();
	
	private static volatile boolean speak = true;
	private static float offset = 0;
	private static float scaleWeight = 0;
	private static float oldWeight = 0;
	private String productName;
	private static float portionStartWeight = 0;
	private static float portionWeight = 0;
	private static boolean weighingPortion;
	
	private static final Dimension WINDOW_SIZE = new Dimension(800, 600);
	private static final Dimension LEFT_PANEL_SIZE = new Dimension(300,550);
	private static final Dimension RIGHT_PANEL_SIZE = new Dimension(450,250);
	private static final Dimension ITEM_PANEL_SIZE = new Dimension(280,20);
	private static final Dimension BARCODE_PANEL_SIZE = new Dimension(280,30);
	private static final Dimension BUTTON_PANEL_SIZE = new Dimension(280,100);
	
    private class MyDispatcher implements KeyEventDispatcher {
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (e.getID() == KeyEvent.KEY_TYPED) {
            	if (e.getKeyChar() == '\n') {
            		scanned(barcode.toString());
            		barcode.setLength(0);
            	} else {
            		barcode.append(e.getKeyChar());
            	}
            }
            return false;
        }
    }
    
    public void scanned(String barcode) {
		try {
			JSONObject json = readJsonFromUrl("http://uk.openfoodfacts.org/api/v0/product/" + barcode.toString() + ".json");
			brand.setText("");
			product.setText("");					
			energy.setText("");
			protein.setText("");;
			fat.setText("");
			data.setText("");;
			try {
				//System.out.println(json.toString(2));
				data.setText(json.toString(2));
				brand.setText(((JSONObject) json.get("product")).get("brands").toString());
				String name = ((JSONObject) json.get("product")).get("product_name").toString();
				product.setText(name);
				if (speak) speaker.say(name);
				energy.setText(((JSONObject) ((JSONObject) json.get("product")).get("nutriments")).get("energy_value").toString());
				protein.setText(((JSONObject) ((JSONObject) json.get("product")).get("nutriments")).get("proteins_value").toString());
				fat.setText(((JSONObject) ((JSONObject) json.get("product")).get("nutriments")).get("fat").toString());
			} catch (JSONException je) {
				//System.out.println("Energy not available");
			}
		} catch (IOException | JSONException e1) {
			e1.printStackTrace();
		}
		
		barcodeField.setText("");	
    }
    
    public ReadBarcode() {
        setTitle("Mike's Calorie Calculator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        
        setPreferredSize(WINDOW_SIZE);
        JPanel p = new JPanel();
        p.setPreferredSize(LEFT_PANEL_SIZE);
        q.setPreferredSize(RIGHT_PANEL_SIZE);
        q.add(new JScrollPane(data));
        getContentPane().add(p, BorderLayout.LINE_START);
        getContentPane().add(q, BorderLayout.LINE_END);
        
        JPanel personPanel = new JPanel();
        personPanel.setPreferredSize(ITEM_PANEL_SIZE);
        p.add(personPanel);
        personPanel.add(personLabel);
        personPanel.add(person);

        JPanel brandPanel = new JPanel();
        brandPanel.setPreferredSize(ITEM_PANEL_SIZE);
        p.add(brandPanel);
        brandPanel.add(brandLabel);
        brandPanel.add(brand);
        
        JPanel productPanel = new JPanel();
        productPanel.setPreferredSize(ITEM_PANEL_SIZE);
        p.add(productPanel);
        productPanel.add(productLabel);
        productPanel.add(product);
        JPanel proteinPanel = new JPanel();
        proteinPanel.setPreferredSize(ITEM_PANEL_SIZE);
        p.add(proteinPanel);
        proteinPanel.add(proteinLabel);
        proteinPanel.add(protein);
        JPanel fatPanel = new JPanel();
        fatPanel.setPreferredSize(ITEM_PANEL_SIZE);
        p.add(fatPanel);
        fatPanel.add(fatLabel);
        fatPanel.add(fat);
        JPanel energyPanel = new JPanel();
        energyPanel.setPreferredSize(ITEM_PANEL_SIZE);
        p.add(energyPanel);
        energyPanel.add(energyLabel);
        energyPanel.add(energy);
        JPanel scalesPanel = new JPanel();
        scalesPanel.setPreferredSize(ITEM_PANEL_SIZE);
        p.add(scalesPanel);
        scalesPanel.add(scalesLabel);
        scalesPanel.add(weight);
        
        JPanel totalEnergyPanel = new JPanel();
        totalEnergyPanel.setPreferredSize(ITEM_PANEL_SIZE);
        p.add(totalEnergyPanel);
        totalEnergyPanel.add(totalEnergyLabel);
        totalEnergyPanel.add(totalEnergy);
        
        JPanel totalProteinPanel = new JPanel();
        totalProteinPanel.setPreferredSize(ITEM_PANEL_SIZE);
        p.add(totalProteinPanel);
        totalProteinPanel.add(totalProteinLabel);
        totalProteinPanel.add(totalProtein);
        
        JPanel totalFatPanel = new JPanel();
        totalFatPanel.setPreferredSize(ITEM_PANEL_SIZE);
        p.add(totalFatPanel);
        totalFatPanel.add(totalFatLabel);
        totalFatPanel.add(totalFat);
             
        JPanel portionPanel = new JPanel();
        portionPanel.setPreferredSize(ITEM_PANEL_SIZE);
        p.add(portionPanel);
        portionPanel.add(portionLabel);
        portionPanel.add(portionValue);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setPreferredSize(BUTTON_PANEL_SIZE);
        p.add(buttonPanel);
        buttonPanel.add(hello);
        buttonPanel.add(identify);
        buttonPanel.add(reset);
        buttonPanel.add(portion);
        buttonPanel.add(silent);
        
        JPanel barcodePanel = new JPanel();
        barcodePanel.setPreferredSize(BARCODE_PANEL_SIZE);
        p.add(barcodePanel);
        barcodePanel.add(barcodeLabel);
        barcodePanel.add(barcodeField);
        barcodePanel.add(barcodeButton);
        
        identify.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// get image
				BufferedImage image = webcam.getImage();

				// save image to PNG file
				try {
					System.out.println("Created test.png file");
					ImageIO.write(image, "PNG", new File("test.png"));
					productName = Identify.identify("test.png");
					product.setText(productName == null ? "Not identified" : productName);
					if (productName != null && speak) speaker.say("Product is " + productName);
				} catch (Exception e1) {
					e1.printStackTrace();
				}				
			}    	
        });
        
        hello.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				speaker.say("Hello," + person.getText() + " please scan an item or put it on the scales");			
			}        	
        });
        
        reset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reset();			
			}        	
        });
        
        silent.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				speak = !speak;	
				silent.setText(speak ? "Silent" : "Speak");
			}        	
        });
        
        barcodeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				scanned(barcodeField.getText());
			}        	
        });
        
        portion.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (Math.abs(scaleWeight) < 10) speaker.say("There is nothing on the scales");
				else {
					speaker.say("Take the item off the scales, remove the portion, and put it back");
					portionStartWeight = scaleWeight;
					weighingPortion = true;
				}
			}        	
        });
        
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new MyDispatcher());
        
    	Webcam.addDiscoveryListener(this);
    	
		picker = new WebcamPicker();
		picker.addItemListener(this);
		
		webcam = picker.getSelectedWebcam();

		if (webcam == null) {
			System.out.println("No webcams found...");
		}
		
		webcam.setViewSize(WebcamResolution.VGA.getSize());
		webcam.addWebcamListener(this);

		panel = new WebcamPanel(webcam, false);
		panel.setFPSDisplayed(true);

		q.add(panel);
		
		Thread t = new Thread() {

			@Override
			public void run() {
				panel.start();
			}
		};
		t.setName("example-starter");
		t.setDaemon(true);
		t.setUncaughtExceptionHandler(this);
		t.start(); 
    }
    
    private void reset() {
    	offset += scaleWeight;
    	System.out.println("Offset is " + offset);
    	weighingPortion = false;
    }
    
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
          sb.append((char) cp);
        }
        return sb.toString();
     }
    
    private static void weighed(String s) {
		scaleWeight = Float.parseFloat(s) - offset;
		
		if ((scaleWeight - oldWeight) > 10 ) {
			if (speak) speaker.say("Weight is " + ((int) scaleWeight) + " grams");	
			
			if (weighingPortion) {
				portionWeight = (portionStartWeight - scaleWeight);
				weighingPortion = false;
				portionValue.setText("" + portionWeight);
				speaker.say("Portion weight is " + portionWeight + " grams");
			}
		}

		oldWeight = scaleWeight;
		weight.setText("" + scaleWeight);
		
		try {
			float e = Float.parseFloat(energy.getText());
			totalEnergy.setText("" + (scaleWeight/100 * e));
			float p = Float.parseFloat(protein.getText());
			totalProtein.setText("" + (scaleWeight/100 * p));
			float f = Float.parseFloat(fat.getText());
			totalFat.setText("" + (scaleWeight/100 * f));
		} catch (NumberFormatException e) {}
    }
    
    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
          BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
          String jsonText = readAll(rd);
          JSONObject json = new JSONObject(jsonText);
          return json;
        } finally {
          is.close();
        }
      }

    public static void main(String[] args) throws AWTException {
        ReadBarcode f = new ReadBarcode();
        Thread readScales = new Thread(new ReadScales());
        readScales.setDaemon(true);
        readScales.start();
        f.setTitle("Mike's Calorie Calculator");
        f.setDefaultCloseOperation(EXIT_ON_CLOSE);
        f.pack();
        f.setVisible(true);
		speaker.say("Welcome to Mike's calorie counter");
    }
    
    static class ReadScales implements Runnable {

		@Override
		public void run() {
			RFControl rf;
			StringBuffer buff = new StringBuffer();
			try {
				rf = new RFControl("COM44", 38400, 2000, "scales");
				
				for(;;) {	
					int b = rf.readByte();
					if (b == '\n') {
						weighed(buff.toString());
						buff.setLength(0);
					} else if (b != '\r') buff.append((char) b);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}  	
    }


	@Override
	public void webcamOpen(WebcamEvent we) {
		System.out.println("webcam open");
	}

	@Override
	public void webcamClosed(WebcamEvent we) {
		System.out.println("webcam closed");
	}

	@Override
	public void webcamDisposed(WebcamEvent we) {
		System.out.println("webcam disposed");
	}

	@Override
	public void webcamImageObtained(WebcamEvent we) {
		// do nothing
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
		webcam.close();
	}

	@Override
	public void windowClosing(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		System.out.println("webcam viewer resumed");
		panel.resume();
	}

	@Override
	public void windowIconified(WindowEvent e) {
		System.out.println("webcam viewer paused");
		panel.pause();
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		System.err.println(String.format("Exception in thread %s", t.getName()));
		e.printStackTrace();
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getItem() != webcam) {
			if (webcam != null) {

				panel.stop();

				q.remove(panel);

				webcam.removeWebcamListener(this);
				webcam.close();

				webcam = (Webcam) e.getItem();
				webcam.setViewSize(WebcamResolution.VGA.getSize());
				webcam.addWebcamListener(this);

				System.out.println("selected " + webcam.getName());

				panel = new WebcamPanel(webcam, false);
				panel.setFPSDisplayed(true);

				q.add(panel);
				pack();

				Thread t = new Thread() {

					@Override
					public void run() {
						panel.start();
					}
				};
				t.setName("example-stopper");
				t.setDaemon(true);
				t.setUncaughtExceptionHandler(this);
				t.start();
			}
		}
	}

	@Override
	public void webcamFound(WebcamDiscoveryEvent event) {
		if (picker != null) {
			picker.addItem(event.getWebcam());
		}
	}

	@Override
	public void webcamGone(WebcamDiscoveryEvent event) {
		if (picker != null) {
			picker.removeItem(event.getWebcam());
		}
	}
}