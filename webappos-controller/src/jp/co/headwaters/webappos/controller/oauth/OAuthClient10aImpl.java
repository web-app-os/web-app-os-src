package jp.co.headwaters.webappos.controller.oauth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.headwaters.webappos.controller.session.OAuthBean;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

public class OAuthClient10aImpl extends AbstractOAuthClient {
	@Override
	public void oauth(HttpServletRequest request, HttpServletResponse response, OAuthBean oAuthData) throws Exception {
		String provider = oAuthData.getProviderName();
		OAuthConsumer consumer = ConsumerProperties.getConsumer(provider);
		OAuthService service = new ServiceBuilder()
				.provider(this.providerApi)
				.apiKey(consumer.getApiKey())
				.apiSecret(consumer.getApiSecret())
				.callback(getCallbackUrl(request))
				.build();

		Token requestToken = service.getRequestToken();
		oAuthData.setRequestToken(requestToken);
		String authorizationUrl = service.getAuthorizationUrl(requestToken);
		response.sendRedirect(addParameter(authorizationUrl));
	}

	@Override
	public Token getAccessToken(HttpServletRequest request, OAuthService service, OAuthBean oAuthData)
			throws Exception {
		String resonseToken = request.getParameter("oauth_token"); //$NON-NLS-1$
		if (resonseToken == null) {
			return null;
		}
		Token requestToken = oAuthData.getRequestToken();
		String expectedToken = oAuthData.getRequestToken().getToken();
		if (!requestToken.getToken().equals(expectedToken)) {
			return null;
		}

		Verifier verifier = new Verifier(request.getParameter("oauth_verifier")); //$NON-NLS-1$
		Token accessToken = service.getAccessToken(requestToken, verifier);
		return accessToken;
	}
}
