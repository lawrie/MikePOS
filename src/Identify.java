import java.io.File;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.json.JSONException;
import org.json.JSONObject;

public class Identify {
	private static final String key = "N2L6AO7k5tHyBCpReppM0Q";
	
	public static void main(String[] args) throws Exception {
		System.out.println("Product is " + identify("test.png"));
	}

	public static String identify(String fileName) throws Exception {
		HttpClient client = new DefaultHttpClient();
		String token = null;
		String name = null;	
		HttpPost httpPost = new HttpPost("https://api.cloudsightapi.com/image_requests");
	
		httpPost.setHeader("Authorization", "CloudSight " + key);
		
		File file = new File(fileName);

		MultipartEntity mpEntity = new MultipartEntity();
		ContentBody cbFile = new FileBody(file, "image/png");
		mpEntity.addPart("image_request[locale", new StringBody("en-US"));
		mpEntity.addPart("image_request[image]", cbFile);
		
		httpPost.setEntity(mpEntity);
		HttpResponse response = client.execute(httpPost);

	    System.out.println(response.getStatusLine());
	    HttpEntity entity = response.getEntity();
	    if (entity != null) {
           String retSrc = EntityUtils.toString(entity); 
           // parsing JSON
           try {
				JSONObject result = new JSONObject(retSrc);
				System.out.println(result.toString(2));
				String status = token = result.getString("status").toString();
				System.out.println("Status is " + status);
				token = result.getString("token").toString();
				System.out.println("Token is " + token);
			} catch (JSONException e) {
				e.printStackTrace();
			} 
	    }
	    
	    EntityUtils.consume(entity);
	    
	    if (token != null) {
		    // Wait 6 seconds
		    Thread.sleep(6000);
		    
		    for(int i=0;i<10;i++) { // Try 10 times		    
			    HttpGet httpget = new HttpGet("https://api.cloudsightapi.com/image_responses/" + token);
			    httpget.setHeader("Authorization", "CloudSight " + key);
			    HttpResponse response2 = client.execute(httpget);
			    
			    HttpEntity entity2 = response2.getEntity();
			    
			    if (entity2 != null) {		    
				    String retSrc = EntityUtils.toString(entity2);
				    
		            try {
						JSONObject result = new JSONObject(retSrc);
						System.out.println(result.toString(2));
						String status = result.getString("status").toString();
						System.out.println("Status is " + status);
						if (status != null && status.equals("not completed")) {
							Thread.sleep(1000);
						} else if (status != null && status.equals("completed")) {
							name = result.getString("name").toString();
							break;
						} else {
							System.out.println("Identification failed");
							break;
						}	
					 } catch (JSONException e) {
						e.printStackTrace();
					 } 
			    }
			    
			    EntityUtils.consume(entity2);                
		    }	    
	    }
	    
	    return name;
	}
}
