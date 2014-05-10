package jp.co.headwaters.webappos.controller.oauth;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jp.co.headwaters.webappos.controller.session.OAuthBean;

import org.scribe.builder.ServiceBuilder;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import org.scribe.oauth.OAuthService;

public class OAuthClient20Impl extends AbstractOAuthClient {
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

		String authorizationUrl = service.getAuthorizationUrl(null);
		response.sendRedirect(addParameter(authorizationUrl));
	}

	@Override
	public Token getAccessToken(HttpServletRequest request, OAuthService service, OAuthBean oAuthData)
			throws Exception {
		if (org.apache.commons.lang.StringUtils.isEmpty(request.getParameter("code"))) {
			return null;
		}
		Verifier verifier = new Verifier(request.getParameter("code")); //$NON-NLS-1$
		Token accessToken = service.getAccessToken(null, verifier);
		return accessToken;
	}
}
