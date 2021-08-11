package edu.stanford.bmir.protege.web.server.sync;

public class WebOWLHelper {
    private final static String ENV_OWL_SCHEME = "OWL_SCHEME";
    private final static String ENV_OWL_HOST = "OWL_HOST";
    private final static String ENV_OWL_PORT = "OWL_PORT";
    private final static String ENV_OWL_PATH = "OWL_PATH";

    public static String GetScheme(){
        String scheme = GetEnvWithDefault(ENV_OWL_SCHEME, "http");
        return scheme;
    }

    public static String GetProtocolWithColon(){
        String scheme = GetEnvWithDefault(ENV_OWL_SCHEME, "http");
        return scheme + ":";
    }

    public static String GetHostname(){
        String hostname = GetEnvWithDefault(ENV_OWL_HOST, "webvowl-2083-develop.sy");
        return hostname;
    }

    public static String GetPortString(){
        String defaultValue;
        if(GetScheme().equals("http")){
            defaultValue = "80";
        }else if(GetScheme().equals("https")){
            defaultValue = "443";
        }else{
            defaultValue = "-1";
        }
        String port = GetEnvWithDefault(ENV_OWL_PORT, defaultValue);
        return port;
    }

    public static int GetPortNumber(){
        int defaultValue;
        if(GetScheme().equals("http")){
            defaultValue = 80;
        }else if(GetScheme().equals("https")){
            defaultValue = 443;
        }else{
            defaultValue = -1;
        }
        int port = GetEnvWithDefault(ENV_OWL_PORT, defaultValue);
        return port;
    }

    public static String GetPath(){
        String path = GetEnvWithDefault(ENV_OWL_PATH, "upload");
        return path;
    }

    public static String GenerateUrlPrefix(){
        String protocol = GetScheme();
        String hostname = GetHostname();
        String port = GetPortString();

        if(protocol.equals("http") && port.equals("80")){
            port = "";
        } else if(protocol.equals("https") && port.equals("443")){
            port = "";
        } else {
            port = ":" + port;
        }

        String urlPrefix = protocol + "://" + hostname + port;
        return urlPrefix;
    }

    public static String GetEnvWithDefault(String envName, String defaultValue){
        String result = System.getenv(envName);
        if(result == null || result.isEmpty()){
            result = defaultValue;
        }
        return result;
    }

    public static int GetEnvWithDefault(String envName, int defaultValue){
        int result;
        String value = System.getenv(envName);
        if(value == null || value.isEmpty()){
            result = defaultValue;
        } else {
            try{
                result = Integer.parseInt(value);
            } catch (NumberFormatException e){
                result = defaultValue;
            }
        }
        return result;
    }
}
