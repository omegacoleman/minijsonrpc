import com.sun.net.httpserver.HttpServer;
import org.json.*;

import java.net.InetSocketAddress;
import java.util.Map;

public class SampleServer {
    public static void main(String[] arg) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress(8001), 0);

        server.createContext("/hello", new HelloHandler());
        server.createContext("/get_post", new GetPostHandler());

        server.start();
    }

    static class HelloHandler extends JSONRPCHandler{
        @Override
        public String run(Map<String, String> getParams, Map<String, String> postParams) {
            JSONObject json = new JSONObject();
            json.put("hello", "world");
            json.append("array", 3);
            json.append("array", 4);
            json.append("array", "peanut");
            return json.toString();
        }
    }

    static class GetPostHandler extends JSONRPCHandler{
        @Override
        public String run(Map<String, String> getParams, Map<String, String> postParams) {
            JSONObject json = new JSONObject();

            JSONObject getJson = new JSONObject();
            for(Map.Entry<String, String> entry : getParams.entrySet())
            {
                getJson.put(entry.getKey(), entry.getValue());
            }
            json.put("GET", getJson);

            JSONObject postJson = new JSONObject();
            for(Map.Entry<String, String> entry : postParams.entrySet())
            {
                postJson.put(entry.getKey(), entry.getValue());
            }
            json.put("POST", postJson);
            return json.toString();
        }
    }
}
