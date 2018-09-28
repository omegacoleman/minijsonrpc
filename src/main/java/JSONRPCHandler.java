import com.sun.net.httpserver.*;
import java.io.*;
import java.net.URLDecoder;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;

public abstract class JSONRPCHandler implements HttpHandler {

    public enum Option {
        ALLOW_CROSS_ORIGIN,
        DISABLE_GET,
        DISABLE_POST
    }

    private EnumSet<Option> options;

    private static String charset = "utf-8";

    public static void setCharset(String charset)
    {
        JSONRPCHandler.charset = charset;
    }

    public static String getCharset() {
        return charset;
    }

    public JSONRPCHandler() {
        super();
        this.options = EnumSet.noneOf(Option.class);
    }

    public JSONRPCHandler(String charset, EnumSet<Option> options) {
        super();
        this.options = options;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Map<String, String> getParams = null;
        String reqCharset = getRequestCharset(exchange.getRequestHeaders());
        if(!this.options.contains(Option.DISABLE_GET))
        {
            getParams = queryToMap(exchange.getRequestURI().getRawQuery(), reqCharset);
        }
        Map<String, String> postParams = null;
        if(!this.options.contains(Option.DISABLE_POST))
        {
            postParams = queryToMap(getPOSTQuery(exchange.getRequestBody(), reqCharset), reqCharset);
        }

        String resultedString = this.run(getParams, postParams);

        Headers response_headers = exchange.getResponseHeaders();
        response_headers.add("Content-Type", "application/json");
        response_headers.add("charset", JSONRPCHandler.getCharset());
        if(this.options.contains(Option.ALLOW_CROSS_ORIGIN))
        {
            response_headers.add("Access-Control-Allow-Origin", "*");
        }
        exchange.sendResponseHeaders(200, 0);
        OutputStream os = exchange.getResponseBody();
        os.write(resultedString.getBytes(JSONRPCHandler.getCharset()));
        os.close();
    }

    public abstract String run(Map<String, String> getParams, Map<String, String> postParams);

    private static Map<String, String> queryToMap(String query, String reqCharset) {
        Map<String, String> result = new HashMap<>();
        if((query == null) || query.trim().equals(""))
        {
            return result;
        }
        for (String param : query.split("&")) {
            String pair[] = param.split("=");
            String value = "";
            String key;
            try {
                key = URLDecoder.decode(pair[0], reqCharset);
                if (pair.length > 1) {
                    value = URLDecoder.decode(pair[1], reqCharset);
                }
            } catch (UnsupportedEncodingException e)
            {
                key = pair[0];
                if (pair.length > 1) {
                    value = pair[1];
                }
            }
            result.put(key, value);
        }
        return result;
    }

    private static String getRequestCharset(Headers reqHeaders) {
        String contentType = reqHeaders.getFirst("Content-Type");
        String requestCharset = "utf-8";
        if (contentType != null) {
            Pattern findCharset = Pattern.compile("charset=([^;]*)");
            Matcher charsetMatcher = findCharset.matcher(contentType);
            if (charsetMatcher.find()) {
                requestCharset = charsetMatcher.group();
            }
        }
        return requestCharset;
    }

    private static String getPOSTQuery(InputStream reqBody, String reqCharset) throws IOException
    {
        StringBuilder postQueryBuilder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(reqBody, reqCharset));
        String line;
        while ((line = reader.readLine()) != null) {
            postQueryBuilder.append(line);
        }
        return postQueryBuilder.toString();
    }
}
