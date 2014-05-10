package jp.co.headwaters.webappos.controller.oauth;

import java.io.IOException;

import jp.co.headwaters.webappos.controller.enumation.ServiceProviderEnum;

import org.scribe.builder.api.FacebookApi;
import org.scribe.builder.api.TwitterApi;

public abstract class OAuthClientFactory {

	public static AbstractOAuthClient create(String name) throws IOException {
		AbstractOAuthClient client = null;
		ServiceProviderEnum sp = ServiceProviderEnum.getServiceProvider(name);
		if (sp.equals(ServiceProviderEnum.TWITTER)) {
			client = new OAuthClient10aImpl();
			client.setProviderApi(new TwitterApi.Authenticate());
			client.setResourceUrl("https://api.twitter.com/1.1/account/verify_credentials.json"); //$NON-NLS-1$
		} else if (sp.equals(ServiceProviderEnum.FACEBOOK)) {
			client = new OAuthClient20Impl();
			client.setProviderApi(new FacebookApi());
			client.setResourceUrl("https://graph.facebook.com/me"); //$NON-NLS-1$
		}
		client.setProvider(sp);
		return client;
	}
}
