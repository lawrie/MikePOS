import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.json.JSONException;
import org.json.JSONObject;


public class ReadBarcode extends JFrame {
	private static final long serialVersionUID = 1L;
	private static StringBuffer barcode = new StringBuffer();
	private JTextField text = new JTextField(0);
	private JLabel brandLabel = new JLabel("Brand:");
	private JLabel brand = new JLabel();
	private JLabel productLabel = new JLabel("Name:");
	private JLabel product = new JLabel();
	private JLabel energyLabel = new JLabel("Energy:");
	private JLabel energy =new JLabel();
	private JTextArea data = new JTextArea(32,40);
	private JLabel scalesLabel = new JLabel("Scales: ");
	static JLabel weight = new JLabel();
	
    private class MyDispatcher implements KeyEventDispatcher {
        @Override
        public boolean dispatchKeyEvent(KeyEvent e) {
            if (e.getID() == KeyEvent.KEY_TYPED) {
            	System.out.println("key is" + e.getKeyChar());
            	if (e.getKeyChar() == '\n') {
            		try {
						JSONObject json = readJsonFromUrl("http://uk.openfoodfacts.org/api/v0/product/" + barcode.toString() + ".json");
						brand.setText("");
						product.setText("");					
						energy.setText("");
						data.setText("");;
						try {
							//System.out.println(json.toString(2));
							data.setText(json.toString(2));
							brand.setText(((JSONObject) json.get("product")).get("brands").toString());
							System.out.println(((JSONObject) json.get("product")).get("product_name").toString());
							product.setText(((JSONObject) json.get("product")).get("product_name").toString());
							energy.setText(((JSONObject) ((JSONObject) json.get("product")).get("nutriments")).get("energy_value").toString());					
						} catch (JSONException je) {
							//System.out.println("Energy not available");
						}
					} catch (IOException | JSONException e1) {
						e1.printStackTrace();
					}
            		barcode.setLength(0);
            		text.setText("");
            	} else {
            		barcode.append(e.getKeyChar());
            	}
            }
            return false;
        }
    }
    
    public ReadBarcode() {
        setTitle("Mike's Calorie Calculator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(800,600));
        text.setVisible(false);
        JPanel p = new JPanel();
        JPanel q = new JPanel();
        p.setPreferredSize(new Dimension(300,550));
        q.setPreferredSize(new Dimension(450,550));
        q.add(new JScrollPane(data));
        getContentPane().add(p, BorderLayout.LINE_START);
        getContentPane().add(q, BorderLayout.LINE_END);
        p.add(text);
        JPanel brandPanel = new JPanel();
        brandPanel.setPreferredSize(new Dimension(280,20));
        p.add(brandPanel);
        brandPanel.add(brandLabel);
        brandPanel.add(brand);
        JPanel productPanel = new JPanel();
        productPanel.setPreferredSize(new Dimension(280,20));
        p.add(productPanel);
        productPanel.add(productLabel);
        productPanel.add(product);
        JPanel energyPanel = new JPanel();
        energyPanel.setPreferredSize(new Dimension(280,20));
        p.add(energyPanel);
        energyPanel.add(energyLabel);
        energyPanel.add(energy);
        JPanel scalesPanel = new JPanel();
        scalesPanel.setPreferredSize(new Dimension(280,20));
        p.add(scalesPanel);
        scalesPanel.add(scalesLabel);
        scalesPanel.add(weight);
        KeyboardFocusManager manager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        manager.addKeyEventDispatcher(new MyDispatcher());
    }
    
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
          sb.append((char) cp);
        }
        return sb.toString();
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
    }
    
    static class ReadScales implements Runnable {

		@Override
		public void run() {
			RFControl rf;
			StringBuffer buff = new StringBuffer();
			int i = 0;
			try {
				rf = new RFControl("COM44", 38400, 2000, "scales");
				
				for(;;) {	
					int b = rf.readByte();
					if (b == '\n') {
						weight.setText(buff.toString());
						//System.out.println(buff.toString());
						buff.setLength(0);
					} else if (b != '\r') buff.append((char) b);
				}
			
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}  	
    }
}