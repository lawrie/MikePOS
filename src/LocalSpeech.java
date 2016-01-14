

import javax.speech.EngineManager;
import javax.speech.synthesis.Synthesizer;
import javax.speech.synthesis.SynthesizerMode;

import org.jvoicexml.jsapi2.jse.synthesis.freetts.FreeTTSEngineListFactory;

public class LocalSpeech {
	private Synthesizer synth;
	
	public LocalSpeech() {
		//System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        try {
			EngineManager.registerEngineListFactory(FreeTTSEngineListFactory.class.getName());
			 // Create a synthesizer for the default Locale
            synth = (Synthesizer) EngineManager.createEngine(SynthesizerMode.DEFAULT);
            // Get it ready to speak
            synth.allocate();
            synth.resume();
            synth.waitEngineState(Synthesizer.RESUMED);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void say(String msg) {
		if (synth != null) {
			try {
				// Speak the message
				synth.speak(msg, null);

				// Wait till speaking is done
				synth.waitEngineState(Synthesizer.QUEUE_EMPTY);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		new LocalSpeech().say("Hello");
	}
}
