package edu.stanford.bmir.protege.web.client.user;

import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import edu.stanford.bmir.protege.web.client.app.ClientObjectReader;
import edu.stanford.bmir.protege.web.client.app.UserInSessionDecoder;
import edu.stanford.bmir.protege.web.client.dispatch.DispatchErrorMessageDisplay;
import edu.stanford.bmir.protege.web.client.dispatch.DispatchServiceCallback;
import edu.stanford.bmir.protege.web.client.dispatch.DispatchServiceManager;
import edu.stanford.bmir.protege.web.shared.dispatch.actions.GetCurrentUserInSessionAction;
import edu.stanford.bmir.protege.web.shared.dispatch.actions.GetCurrentUserInSessionResult;
import edu.stanford.bmir.protege.web.shared.access.ActionId;
import edu.stanford.bmir.protege.web.shared.access.BuiltInAction;
import edu.stanford.bmir.protege.web.shared.app.UserInSession;
import edu.stanford.bmir.protege.web.shared.inject.ApplicationSingleton;
import edu.stanford.bmir.protege.web.shared.user.LogOutUserAction;
import edu.stanford.bmir.protege.web.shared.user.UserDetails;
import edu.stanford.bmir.protege.web.shared.user.UserId;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.util.Optional;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Author: Matthew Horridge<br>
 * Stanford University<br>
 * Bio-Medical Informatics Research Group<br>
 * Date: 05/04/2013
 */
@ApplicationSingleton
public class LoggedInUserManager {

    @Nonnull
    private final LoggedInUser loggedInUser;

    @Nonnull
    private final DispatchServiceManager dispatchServiceManager;

    @Nonnull
    private final DispatchErrorMessageDisplay errorDisplay;

    @Inject
    public LoggedInUserManager(@Nonnull LoggedInUser loggedInUser,
                               @Nonnull DispatchServiceManager dispatchServiceManager, @Nonnull DispatchErrorMessageDisplay errorDisplay) {
        this.loggedInUser = loggedInUser;
        this.dispatchServiceManager = checkNotNull(dispatchServiceManager);
        this.errorDisplay = checkNotNull(errorDisplay);
    }

    /**
     * Reads the initial logged in user from the web page source.
     * This should really only be called once on page load.
     */
    public void readInitialUserInSession() {
        readUserInSession();
    }

    /**
     * Gets the id of the currently logged in user.
     * @return The id of the currently logged in user.  Not {@code null}.  The returned id may correspond to the id
     * of the guest user.
     */
    @Nonnull
    public UserId getLoggedInUserId() {
        return loggedInUser.getCurrentUserId();
    }

    /**
     * Sets the logged in user.  An event will be fired (asynchronously) to indicate whether the user has logged in or out.
     * @param userInSession The user in the session that is the logged in user.
     * @throws NullPointerException if {@code userId} is {@code null}.
     */
    public void setLoggedInUser(@Nonnull final UserInSession userInSession) {
        loggedInUser.setLoggedInUser(userInSession);
    }

    /**
     * If the current user is not the guest user calling this method will log out the current user.  An event will
     * be fired (asynchronously) when the current user has been logged out.
     */
    public void logOutCurrentUser() {
        if(loggedInUser.getCurrentUserId().isGuest()) {
            return;
        }

        dispatchServiceManager.execute(new LogOutUserAction(), result -> {
            loggedInUser.setLoggedInUser(result.getUserInSession());

            UserDetails userDetails = loggedInUser.getUserInSession().getUserDetails();
            String idToken = userDetails.getIdToken().orElse("");
//            String protocol = Window.Location.getProtocol();
//            String hostname = Window.Location.getHostName();
//            String port = Window.Location.getPort();
//            if(protocol == "http:" && port == "80"){
//                port = "";
//            } else if(protocol == "https:" && port == "443"){
//                port = "";
//            } else {
//                port = ":" + port;
//            }
//            String redirectUrl = protocol + "//" + hostname + port + "/";

            String jsonString = result.logoutInfoJson;
            JSONValue jsonValue = JSONParser.parseStrict(jsonString);
            JSONObject jsonObject = jsonValue.isObject();
            UrlBuilder builder = Window.Location.createUrlBuilder();
            builder.setProtocol(jsonObject.get("scheme").isString().stringValue());
            builder.setHost(jsonObject.get("hostname").isString().stringValue());
            builder.setPort((int)jsonObject.get("port").isNumber().doubleValue());
            builder.setPath(jsonObject.get("path").isString().stringValue());
            builder.setParameter("id_token_hint", idToken);
            builder.setParameter("post_logout_redirect_uri", jsonObject.get("post_logout_redirect_uri").isString().stringValue());
            Window.Location.replace(builder.buildString());
        });
    }

    public Set<ActionId> getLoggedInUserApplicationActions() {
        return loggedInUser.getUserInSession().getAllowedApplicationActions();
    }

    public boolean isAllowedApplicationAction(ActionId actionId) {
        return loggedInUser.isAllowedApplicationAction(actionId);
    }

    public boolean isAllowedApplicationAction(BuiltInAction action) {
        return isAllowedApplicationAction(action.getActionId());
    }

    private void restoreUserFromServerSideSession(final Optional<AsyncCallback<UserDetails>> callback) {
        dispatchServiceManager.execute(new GetCurrentUserInSessionAction(), new DispatchServiceCallback<GetCurrentUserInSessionResult>(errorDisplay) {
            @Override
            public void handleExecutionException(Throwable cause) {
                callback.ifPresent(userDetailsAsyncCallback -> userDetailsAsyncCallback.onFailure(cause));
            }

            @Override
            public void handleSuccess(GetCurrentUserInSessionResult result) {
                loggedInUser.setLoggedInUser(result.getUserInSession());
                callback.ifPresent(userDetailsAsyncCallback -> userDetailsAsyncCallback.onSuccess(result.getUserInSession().getUserDetails()));
            }

        });
    }

    private void readUserInSession() {
        UserInSessionDecoder decoder = new UserInSessionDecoder();
        UserInSession userInSession  = ClientObjectReader.create("userInSession", decoder).read();
        loggedInUser.setLoggedInUser(userInSession);
    }
}
