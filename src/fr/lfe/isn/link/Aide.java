package fr.lfe.isn.link;

import java.util.ArrayList;
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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Aide extends Activity implements OnInitListener{
	
	Button btn_aide;
	
	private TextToSpeech mTts; // Déclaration du TTS
    String A_dire;
	
	@Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aide);
        
        btn_aide = (Button) findViewById(R.id.btn_aide);
        btn_aide.setOnClickListener(new View.OnClickListener() { // Propre au bouton
			@Override
			public void onClick(View v){
				HttpClient httpclient = new DefaultHttpClient();
			    HttpPost httppost = new HttpPost("http://novares.free.fr/andro/LFE_Link/index.php");

			    try {
			        // Prépare les variables et les envoient
			        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			        nameValuePairs.add(new BasicNameValuePair("message", "Android_Aide_test"));
			        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));

			        // Reçoit la réponse
			        HttpResponse responsePOST = httpclient.execute(httppost);
			        HttpEntity httpreponse = responsePOST.getEntity();
			        A_dire = EntityUtils.toString(httpreponse).trim();}
			    catch(Exception e){
			        Log.e("log_tag", "Error in http connection"+e.toString());}

		        
			    if (A_dire!=""){
			    	Intent checkIntent = new Intent();
		            checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA); // Va vérifier si il y a un TTS de disponible
		            startActivityForResult(checkIntent, 1);}
				}
		});
    }
	
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		switch (requestCode) {
		case 1:{
	        if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
	            // S'il y a un TTS de disponible
	        	mTts = new TextToSpeech(this,this);}}
    }}
	
	public void onInit(int i){
    	// S'exécute dès la création du mTts
    	mTts.speak(A_dire,TextToSpeech.QUEUE_FLUSH,null);
	    Toast t = Toast.makeText(getApplicationContext(),A_dire,Toast.LENGTH_SHORT);
		t.show();}
      
    @Override
    public void onDestroy(){
    	// Quitte le TTS
    	if (mTts != null){
        	mTts.stop();
            mTts.shutdown();}
        super.onDestroy();}
	
}