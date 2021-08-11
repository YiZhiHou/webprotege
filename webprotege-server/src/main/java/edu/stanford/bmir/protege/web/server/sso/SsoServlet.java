package edu.stanford.bmir.protege.web.server.sso;

import com.alibaba.fastjson.JSONObject;
import com.google.api.client.auth.oauth2.*;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import edu.stanford.bmir.protege.web.server.access.AccessManager;
import edu.stanford.bmir.protege.web.server.auth.AuthenticationManager;
import edu.stanford.bmir.protege.web.server.download.DownloadFormat;
import edu.stanford.bmir.protege.web.server.download.FileDownloadParameters;
import edu.stanford.bmir.protege.web.server.session.WebProtegeSession;
import edu.stanford.bmir.protege.web.server.session.WebProtegeSessionImpl;
import edu.stanford.bmir.protege.web.server.user.UserDetailsManager;
import edu.stanford.bmir.protege.web.shared.auth.Salt;
import edu.stanford.bmir.protege.web.shared.auth.SaltedPasswordDigest;
import edu.stanford.bmir.protege.web.shared.inject.ApplicationSingleton;
import edu.stanford.bmir.protege.web.shared.project.ProjectId;
import edu.stanford.bmir.protege.web.shared.revision.RevisionNumber;
import edu.stanford.bmir.protege.web.shared.user.*;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Console;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import static edu.stanford.bmir.protege.web.server.logging.RequestFormatter.formatAddr;
import static edu.stanford.bmir.protege.web.server.logging.WebProtegeLogger.WebProtegeMarker;

/**
 * <p>
 * Try to use synyi sso.
 * </p>
 */
@ApplicationSingleton
public class SsoServlet extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(SsoServlet.class);

    @Nonnull
    private final AccessManager accessManager;

    @Nonnull
    private final UserDetailsManager userDetailsManager;

    @Nonnull
    private final AuthenticationManager authenticationManager;

    @Inject
    public SsoServlet(@Nonnull AccessManager accessManager, @Nonnull UserDetailsManager userDetailsManager, @Nonnull AuthenticationManager authenticationManager) {
        this.accessManager = accessManager;
        this.userDetailsManager = userDetailsManager;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final WebProtegeSession webProtegeSession = new WebProtegeSessionImpl(request.getSession());

        StringBuffer fullUrlBuf = request.getRequestURL();
        if (request.getQueryString() != null) {
            fullUrlBuf.append('?').append(request.getQueryString());
        }
        AuthorizationCodeResponseUrl authResponse =
                new AuthorizationCodeResponseUrl(fullUrlBuf.toString());
        // check for user-denied error
        if (authResponse.getError() != null) {
            response.sendError(500, "Failed to get sso info");
        } else {
            String code = authResponse.getCode();

//            String scheme = request.getScheme();
//            String servername = request.getServerName();
//            int port = request.getServerPort();
//            String portInfo = (port == 80? "" : (":" + port));
//            String redirectUri = scheme + "://" + servername + portInfo + "/sso";

            String tokenBody = requestAccessToken(code, SynyiSsoHelper.GetRedirectLogin());
            if (tokenBody == null) {
                logger.error(WebProtegeMarker, "Can not get user access token.");
                throw new IOException("Can not get user access token. Please try to login again.");
            }

            JSONObject tokenJson = JSONObject.parseObject(tokenBody);
            String accessToken = tokenJson.getString("access_token");
            logger.info(WebProtegeMarker, "Access token: " + accessToken);

            String userBody = requestUserInfo(accessToken);
            JSONObject jsonObject = JSONObject.parseObject(userBody);
            String username = jsonObject.getString("name");
            UserId userId = UserId.getUserId(username);
            String idToken = tokenJson.getString("id_token");
            webProtegeSession.setUserInSession(userId);

            String digestStr = "Not need SaltedPasswordDigest";
            byte[] digestBytes = digestStr.getBytes();

            String saltStr = "Not need Salt";
            byte[] saltBytes = saltStr.getBytes();

            UserDetails userDetails;
            try {
                userDetails = authenticationManager.registerUser(userId, new EmailAddress(userId + "@synyi.com"), new SaltedPasswordDigest(digestBytes), new Salt(saltBytes));
                //
            } catch (UserRegistrationException e) {
                userDetails = null;
            }

            if (userDetails == null) {
                userDetails = userDetailsManager.getUserDetails(userId).get();
            }
            userDetails.setIdToken(idToken);

//            String mainPageUrl = scheme + "://" + servername + portInfo + "/";
            String currentUrl = request.getRequestURL().toString();
            int index = currentUrl.lastIndexOf("/");
            String mainPageUrl = currentUrl.substring(0, index + 1);
            response.sendRedirect(mainPageUrl);
        }
    }

    private String requestUserInfo(String accessToken) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder().build();
        Request userInfoRequest = new Request.Builder().url(SynyiSsoHelper.GetUserInfoUrl())
                .addHeader("Authorization", "Bearer " + accessToken).build();
        Response userInfoResponse = client.newCall(userInfoRequest).execute();
        ResponseBody responseBody = userInfoResponse.body();
        String body = responseBody.string();
        return body;
    }

    private String requestAccessToken(String code, String redirectUri) throws IOException {
        try {
            HttpResponse response =
                    new AuthorizationCodeTokenRequest(new NetHttpTransport(), new JacksonFactory(),
                            new GenericUrl(SynyiSsoHelper.GetTokenUrl()), code)
                            .setGrantType(SynyiSsoHelper.GetGrantType())
                            .setRedirectUri(redirectUri)
                            .setClientAuthentication(new ClientParametersAuthentication(SynyiSsoHelper.GetClientID(), SynyiSsoHelper.GetClientSerect()))
                            .executeUnparsed();
            InputStream inputStream = response.getContent();
            String tokenResp = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
            return tokenResp;
        } catch (TokenResponseException e) {
            if (e.getDetails() != null) {
                logger.error(WebProtegeMarker, "Error: " + e.getDetails().getError());
                if (e.getDetails().getErrorDescription() != null) {
                    logger.error(WebProtegeMarker, e.getDetails().getErrorDescription());
                }
                if (e.getDetails().getErrorUri() != null) {
                    logger.error(WebProtegeMarker, e.getDetails().getErrorUri());
                }
            } else {
                logger.error(WebProtegeMarker, e.getMessage());
            }
            return null;
        }
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
