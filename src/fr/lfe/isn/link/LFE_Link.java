package fr.lfe.isn.link;

import java.util.ArrayList; // Importation de tout ce qui est utile au système
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import fr.lfe.isn.link.ShakeDetector.OnShakeListener;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
 	
public class LFE_Link extends Activity implements TextToSpeech.OnInitListener {
	
		ImageButton btnRec; // Importation des différents affichages
		EditText IPadress;
		TextView Rep;
		TextView Recrep;
		TextView tts_pref_false;
		
        private TextToSpeech mTts; // Déclaration du TTS
        String A_dire;
        
        boolean TTS_box; // Déclaration des variables contenant les checkbox des config
        boolean Shake_box;
        float Shake_sens=3F;
	    
        // Juste des valeurs fixes de référence
        protected static final int RESULT_SPEECH = 1;
    	protected static final int OPTION = 2;
    	protected static final int TTS = 3;
    	protected static final int AIDE = 4;
    	
    	private ShakeDetector mShakeDetector; // Pour le Shake
        private SensorManager mSensorManager;
        private Sensor mAccelerometer;
        
        @Override
        public void onCreate(Bundle savedInstanceState){
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_lfe__link);
            
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build(); // Evite une erreur lors du transfert de données
    		StrictMode.setThreadPolicy(policy);
    		
    		IPadress = (EditText)findViewById(R.id.IPadress); // Identification des différents affichages
    		tts_pref_false = (TextView) findViewById(R.id.tts_pref_false);
    		Recrep = (TextView) findViewById(R.id.Recrep);
    		Rep = (TextView) findViewById(R.id.Rep);
    		btnRec = (ImageButton) findViewById(R.id.btnRec);
    		getConfig();
    		
    		btnRec.setOnClickListener(new View.OnClickListener() { // Propre au bouton
    			@Override
    			public void onClick(View v){
    				Initialisation();}
    			});
    		
    		 mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE); // Configure le Shake
    	     mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    	     mShakeDetector = new ShakeDetector();
    	     mShakeDetector.setOnShakeListener(new OnShakeListener() {
    	        @Override
    	        public void onShake(int count) {
    	        	if (Shake_box==true && Shake_sens>1){
    	        		A_dire = "Que voulez-vous, maitre ?";
			    	    getTTS();}}
    	        });
    	     mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);}
        
        public void onActivityResult(int requestCode, int resultCode, Intent data)
        {
            switch (requestCode) {
    		case RESULT_SPEECH: { // Au résultat du STT
    			if (resultCode == RESULT_OK && null != data) {

    				ArrayList<String> text = data
    						.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

    				Recrep.setText(text.get(0)); // Le marque sur le TextView Recrep
    				
    				String IPAdress = IPadress.getText().toString();
    				
    				// Début de l'envoie au serveur
    				HttpClient httpclient = new DefaultHttpClient();
    			    HttpPost httppost = new HttpPost("http://"+IPAdress);

    			    try {
    			        // Prépare les variables et les envoient
    			        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
    			        nameValuePairs.add(new BasicNameValuePair("message", Recrep.getText().toString()));
    			        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));

    			        // Reçoit la réponse
    			        HttpResponse responsePOST = httpclient.execute(httppost);
    			        HttpEntity httpreponse = responsePOST.getEntity();
    			        String result = EntityUtils.toString(httpreponse).trim(); // La transforme en string
    			        Rep.setText(result);}
    			    catch(Exception e){
    			        Log.e("log_tag", "Error in http connection"+e.toString());}
    		        
    		        A_dire = Rep.getText().toString();
    		        
    			    if (A_dire!=""){
    			    	getTTS();}
    		     }
    			break;}
    		
    			case OPTION:{ // Recharge la configuration
    				getConfig();}
    			
    			case TTS:{
    	                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
    	                    // S'il y a un TTS de disponible
    	                	mTts = new TextToSpeech(this, this);}
    			}
    	}}

        public void onInit(int i){
        	// S'exécute dès la création du mTts
        	mTts.speak(A_dire,TextToSpeech.QUEUE_FLUSH,null);
        	if(A_dire=="Que voulez-vous, maitre ?"){ // Si vous êtes venu ici avec le Shake, il lance l'initialisation après avoir dis la phrase
		    	Toast t = Toast.makeText(getApplicationContext(),A_dire,Toast.LENGTH_SHORT);
				t.show();
    			Initialisation();}}
          
        @Override
        public void onDestroy(){
        	// Quitte le TTS
        	if (mTts != null){
            	mTts.stop();
                mTts.shutdown();}
            super.onDestroy();}

        @Override
        protected void onPause() { // Met en pause le Shake lorsque l'on change d'application
            mSensorManager.unregisterListener(mShakeDetector);
            super.onPause();}  
        
        @Override
        protected void onResume() { // Remet en route après retour sur l'application du Shake
            super.onResume();
            mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);}
        
    	@Override
    	public boolean onCreateOptionsMenu(Menu menu) {
    		// Créer le menu
    		getMenuInflater().inflate(R.menu.lfe__link, menu);
    		return super.onCreateOptionsMenu(menu);}   
    	
    	public boolean onOptionsItemSelected(MenuItem item){
    		if(item.getItemId() == R.id.configuration){ // Envoie vers la config si on clique sur l'item du menu
    			startActivityForResult(new Intent(this, Configuration.class), OPTION);}
    		if(item.getItemId() == R.id.aide){ // Envoie vers l'aide si on clique sur l'item du menu
    			startActivity(new Intent(this, Aide.class));}
    		return super.onOptionsItemSelected(item);}
    	
		public void getConfig(){ // Charge la configuration
    		SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(this);
    		String V_string=preferences.getString("IPadress", "");
    		if(V_string != ""){
    			IPadress.setText(V_string);}
    		
    		TTS_box=preferences.getBoolean("tts_pref", true);
    		if(TTS_box==false){
    			tts_pref_false.setText("Attention ! Votre TTS est désactivé.");}
    		else{
    			tts_pref_false.setText("");}
    		
    		Shake_box=preferences.getBoolean("shake_pref", true);
    		Shake_sens=Float.parseFloat(preferences.getString("shake_sens", "3.0f"));
    		ShakeDetector.getConfig(Shake_sens);
    		if(Shake_sens<=1)   { 		
    			Toast t = Toast.makeText(getApplicationContext(),
    			"Attention ! Votre sensibilité de Shake est trop basse donc il ne fonctionnera pas. Veuillez la mettre à une valeur supérieure à 1.",
				Toast.LENGTH_SHORT);
	        	t.show();}}
    	
    	public void getTTS(){ // Charge le TTS
		    if (TTS_box==true){
	    		Intent checkIntent = new Intent();
	            checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA); // Va vérifier si il y a un TTS de disponible
	            startActivityForResult(checkIntent, TTS);}
		    }
    	
    	public void Initialisation(){ // Initialise la procédure STT
    		A_dire="";
    		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "fr-FR");
			
			try {
				startActivityForResult(intent, RESULT_SPEECH);
				Recrep.setText("");}
			
			catch (ActivityNotFoundException a) {
				Toast t = Toast.makeText(getApplicationContext(),
						"Oh bah zut alors ! Ton Android n'a pas installé le STT ou ne le supporte pas. Regarde les options (langue et saisie).",
						Toast.LENGTH_SHORT);
				t.show();}
    	}
}