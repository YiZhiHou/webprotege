Oidc.Log.logger = console;
Oidc.Log.level = Oidc.Log.DEBUG;

var origin = window.location.protocol + '//' + window.location.hostname + (window.location.port? (':' + window.location.port) : '');

var settings = {
    authority: 'http://sso.sy',
    client_id: 'webprotege',
    client_secret: 'secret',
    redirect_uri: origin + '/html/signin-callback.html',
    popup_redirect_uri: origin + '/html/signin-callback.html',
    post_logout_redirect_uri: origin,
    response_type: 'id_token token',
    //response_mode:'fragment',
    scope: 'openid profile webprotege-api roles portal-api',

    // popup_redirect_uri:'http://localhost:15000/identityserver-sample-popup-signin.html',
    // popup_post_logout_redirect_uri:'http://localhost:15000/identityserver-sample-popup-signout.html',

    // silent_redirect_uri:'http://localhost:15000/identityserver-sample-silent.html',
    // automaticSilentRenew:true,
    //silentRequestTimeout:10000,

    // filterProtocolClaims: true,
    loadUserInfo: true
};
var mgr = new Oidc.UserManager(settings);

///////////////////////////////
// events
///////////////////////////////
mgr.events.addAccessTokenExpiring(function () {
    console.log("token expiring");
});

mgr.events.addAccessTokenExpired(function () {
    console.log("token expired");
});

mgr.events.addSilentRenewError(function (e) {
    console.log("silent renew error", e.message);
});

mgr.events.addUserLoaded(function (user) {
    console.log("user loaded", user);
    mgr.getUser().then(function(){
        console.log("getUser loaded user after userLoaded event fired");
    });
});

mgr.events.addUserUnloaded(function (e) {
    console.log("user unloaded");
});

///////////////////////////////
// functions for UI elements
///////////////////////////////

mgr.getUser().then(function(user) {
    console.log("got user", user);
    if (user && user.access_token) {
        // this.currentUser = user;
        // return this.currentUser;
    } else {
        mgr.signinRedirect();
        // mgr.signinPopup();
    }
}).catch(function(err) {
    console.error(err);
});