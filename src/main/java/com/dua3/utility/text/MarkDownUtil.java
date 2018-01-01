package com.dua3.utility.text;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.AttributeProvider;
import org.commonmark.renderer.html.AttributeProviderContext;
import org.commonmark.renderer.html.AttributeProviderFactory;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.html.HtmlRenderer.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarkDownUtil {
	private static final Logger LOG = LoggerFactory.getLogger(MarkDownUtil.class);

	/**
	 * Convert markdown to RichText.
	 * @param source
	 * 		the markdown source
	 * @return
	 * 		the generated RichText
	 */
	public static RichText toRichText(String source) {
		Parser parser = Parser.builder().build();
		Node node = parser.parse(source);

		RichTextRenderer renderer = new RichTextRenderer();
		return renderer.render(node);
	}

	/**
	 * Convert markdown to HTML.
	 * @param source
	 * 		the markdown source
	 * @return
	 * 		the generated HTML
	 */
	public static String toHTML(String source) {
		Parser parser = Parser.builder().build();
		Node node = parser.parse(source);

		HtmlRenderer renderer = HtmlRenderer.builder().build();
		return renderer.render(node);
	}

	static class LinkResolvingAttributeProvider implements AttributeProvider {
		private final URI baseUri;

		public LinkResolvingAttributeProvider(URI baseUri) {
			this.baseUri = baseUri;
		}

		private void resolve(Map<String, String> attributes, String attribute) {
			attributes.computeIfPresent(attribute, (k, v) -> {
				try {
					return baseUri.resolve(new URI(v)).toString();
				} catch (URISyntaxException e) {
					LOG.warn("could not resolve value of attribute '{}', leaving unchanged: {}", attribute, v);
					return v;
				}
			});			
		}
		
		@Override
		public void setAttributes(Node node, String tagName, Map<String, String> attributes) {
			resolve(attributes, "href");
			resolve(attributes, "src");
		}
	}

	/**
	 * Retrieve HTML for a markdownresource through an URL.
	 * @param url
	 * 		the markdown document's URL
	 * @param resolveLinks
	 * 		if true, relative links will be resolved against the document's URL
	 * @return
	 * 		the generated HTML
	 * @throws IOException
	 * 		if the source could not be read
	 */
	public static String toHTML(URL url, boolean resolveLinks) throws IOException {
		try {
			URI uri = url.toURI();
			try (BufferedReader reader = Files.newBufferedReader(Paths.get(uri), StandardCharsets.UTF_8)) {
				Parser parser = Parser.builder().build();
				Node node = parser.parseReader(reader);

				Builder rendererBuilder = HtmlRenderer.builder();

				if (resolveLinks) {
					rendererBuilder = rendererBuilder.attributeProviderFactory(new AttributeProviderFactory() {
						@Override
						public AttributeProvider create(AttributeProviderContext context) {
							return new LinkResolvingAttributeProvider(uri);
						}
					});
				}

				HtmlRenderer renderer = rendererBuilder.build();
				return renderer.render(node);
			}
		} catch (URISyntaxException e) {
			throw new IOException("could not convert URI to URL: " + url, e);
		}
	}

	private MarkDownUtil() {
		// utility class
		throw new UnsupportedOperationException();
	}

}
