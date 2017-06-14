package supr.core.model.handler;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.async.Callback;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.concurrent.Future;

/**
 */
public class RestHandler implements Callback<JsonNode> {

    private JsonNode result;

    private Throwable error;

    final String url;

    private boolean completed;

    private Future<HttpResponse<JsonNode>> future;

    public RestHandler(String url){
        this.url = url ;
        completed = false;
    }

    public void get(){
        future = Unirest.get(url).asJsonAsync(this);
    }

    @Override
    public void completed(HttpResponse<JsonNode> httpResponse) {
        result = httpResponse.getBody();
        completed = true;
    }

    @Override
    public void failed(UnirestException e) {
        result = null;
        completed = true;
        error = e.getCause();
    }

    @Override
    public void cancelled() {
        result = null;
        completed = true;
    }

    public JsonNode result(){
        return result;
    }

    public Throwable error(){
        return error;
    }

    public JsonNode getAndWaitAsync(){
        get();
        while ( !completed ) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {

            }
        }
        return result;
    }
}
