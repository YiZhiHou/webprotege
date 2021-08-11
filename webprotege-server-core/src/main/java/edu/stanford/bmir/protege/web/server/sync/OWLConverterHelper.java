package edu.stanford.bmir.protege.web.server.sync;

public class OWLConverterHelper {
    private final static String ENV_CONVERTOR_SCHEME = "CONVERTOR_SCHEME";
    private final static String ENV_CONVERTOR_HOST = "CONVERTOR_HOST";
    private final static String ENV_CONVERTOR_PORT = "CONVERTOR_PORT";
    private final static String ENV_CONVERTOR_PATH = "CONVERTOR_PATH";

    public static String GetScheme(){
        String scheme = GetEnvWithDefault(ENV_CONVERTOR_SCHEME, "http");
        return scheme;
    }

    public static String GetProtocolWithColon(){
        String scheme = GetEnvWithDefault(ENV_CONVERTOR_SCHEME, "http");
        return scheme + ":";
    }

    public static String GetHostname(){
        String hostname = GetEnvWithDefault(ENV_CONVERTOR_HOST, "medical-ai-knowledge-graph-external-owl2vowl.sy");
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
        String port = GetEnvWithDefault(ENV_CONVERTOR_PORT, defaultValue);
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
        int port = GetEnvWithDefault(ENV_CONVERTOR_PORT, defaultValue);
        return port;
    }

    public static String GetPath(){
        String path = GetEnvWithDefault(ENV_CONVERTOR_PATH, "owl2vowl");
        return path;
    }

    public static String GenerateUrlPrefix(){
        String protocol = GetScheme();
        String hostname = GetHostname();
        String port = GetPortString();

        if(protocol == "http" && port == "80"){
            port = "";
        } else if(protocol == "https" && port == "443"){
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
