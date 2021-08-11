package edu.stanford.bmir.protege.web.server.sso;

import com.alibaba.fastjson.JSONObject;
import com.google.gwt.http.client.URL;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.List;

public class SynyiSsoHelper {

    private final static String ENV_SSO_SCHEME = "SSO_SCHEME";
    private final static String ENV_SSO_HOSTNAME = "SSO_HOSTNAME";
    private final static String ENV_SSO_PORT = "SSO_PORT";
    private final static String ENV_SSO_CLIENT_ID = "SSO_CLIENT_ID";
    private final static String ENV_SSO_CLIENT_SECRET = "SSO_CLIENT_SECRET";
    private final static String ENV_SSO_CORS_ORIGIN = "SSO_CORS_ORIGIN";
    private final static String ENV_SSO_REDIRECT_LOGIN = "SSO_REDIRECT_LOGIN";
    private final static String ENV_SSO_REDIRECT_LOGOUT = "SSO_REDIRECT_LOGOUT";

    public static String GetScheme(){
        String scheme = GetEnvWithDefault(ENV_SSO_SCHEME, "http");
        return scheme;
    }

    public static String GetProtocolWithColon(){
        String scheme = GetEnvWithDefault(ENV_SSO_SCHEME, "http");
        return scheme + ":";
    }

    public static String GetHostname(){
        String hostname = GetEnvWithDefault(ENV_SSO_HOSTNAME, "sso.sy");
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
        String port = GetEnvWithDefault(ENV_SSO_PORT, defaultValue);
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
        int port = GetEnvWithDefault(ENV_SSO_PORT, defaultValue);
        return port;
    }

    public static String GetCORSOrigin(){
        String corsOrigin = GetEnvWithDefault(ENV_SSO_CORS_ORIGIN, "http://172.16.0.133:8080");
        return corsOrigin;
    }

    public static String GetRedirectLogin(){
        String loginUrl = GetEnvWithDefault(ENV_SSO_REDIRECT_LOGIN, "http://172.16.0.133:8080/webprotege/sso");
        return loginUrl;
    }

    public static String GetRedirectLogout(){
        String logoutUrl = GetEnvWithDefault(ENV_SSO_REDIRECT_LOGOUT, "http://172.16.0.133:8080/webprotege");
        return logoutUrl;
    }

    public static String GetClientID(){
        String clientID = GetEnvWithDefault(ENV_SSO_CLIENT_ID, "webprotege");
        return clientID;
    }

    public static String GetClientSerect(){
        String secret = GetEnvWithDefault(ENV_SSO_CLIENT_SECRET, "secret");
        return secret;
    }

    public static String GetAuthorizationUrl(){
        String url =  GenerateUrlPrefix() + "/" + GetAuthorizationPath();
        return url;
    }

    public static String GetTokenUrl(){
        String url =  GenerateUrlPrefix() + "/" + GetTokenPath();
        return url;
    }

    public static String GetUserInfoUrl(){
        String url =  GenerateUrlPrefix() + "/" + GetUserInfoPath();
        return url;
    }

    public static String GetEndSessionUrl(){
        String url =  GenerateUrlPrefix() + "/" + GetEndSessionPath();
        return url;
    }

    public static String GetAuthorizationPath(){
        return "connect/authorize";
    }

    public static String GetTokenPath(){
        return "connect/token";
    }

    public static String GetUserInfoPath(){
        return "connect/userinfo";
    }

    public static String GetEndSessionPath(){
        return "connect/endsession";
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

    public static List<String> GetTypeList(){
        List<String> types = new ArrayList<>();
        types.add("code");
        return types;
    }

    public static String GetTypes(){
        String types = "code";
        return types;
    }

    public static String GetGrantType(){
        String type = "authorization_code";
        return type;
    }

    public static List<String> GetScopeList(){
        List<String> scopes = new ArrayList<>();
        scopes.add("openid");
        scopes.add("roles");
        scopes.add("profile");
        return scopes;
    }

    public static String GetScopes(){
        String scopes = "openid roles profile";
        return scopes;
    }

    public static String GetState(){
        String state = RandomStringUtils.randomAlphanumeric(16);
        return state;
    }

    public static String GetLogoutInfoJson(){
        JSONObject object = new JSONObject();
        object.put("scheme", GetScheme());
        object.put("hostname", GetHostname());
        object.put("port", GetPortNumber());
        object.put("path", GetEndSessionPath());
        object.put("post_logout_redirect_uri", GetRedirectLogout());
        return object.toJSONString();
    }
}
