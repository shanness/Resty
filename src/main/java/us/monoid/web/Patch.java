/**
 * 
 */
package us.monoid.web;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Used in PATCH operations. Wraps nicely around Content and makes sure that PATCH is used instead of POST,
 * effectively replacing the existing resource (or creating a new one, according to the holy HTTP spec.
 * 
 * @author beders
 *
 */
public class Patch extends AbstractContent {
	private Content wrappedContent;

	static {
		allowMethods("PATCH");
	}

	public Patch(Content someContent) {
		wrappedContent = someContent;
	}
	
	@Override
	protected	void addContent(URLConnection con) throws IOException {
		con.setDoOutput(true);
		// PATCH is non standard apparently (but heavily used in REST)
		((HttpURLConnection)con).setRequestMethod("PATCH"); // https://stackoverflow.com/questions/25163131/httpurlconnection-invalid-http-method-patch
		// This work around will only work if the server supports the override..
//		((HttpURLConnection)con).setRequestProperty("X-HTTP-Method-Override", "PATCH");
//		((HttpURLConnection)con).setRequestMethod("POST");
		wrappedContent.addContent(con);
	}

	private static void allowMethods(String... methods) {
		try {
			Field methodsField = HttpURLConnection.class.getDeclaredField("methods");

			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(methodsField, methodsField.getModifiers() & ~Modifier.FINAL);

			methodsField.setAccessible(true);

			String[] oldMethods = (String[]) methodsField.get(null);
			Set<String> methodsSet = new LinkedHashSet(Arrays.asList(oldMethods));
			methodsSet.addAll(Arrays.asList(methods));
			String[] newMethods = methodsSet.toArray(new String[0]);

			methodsField.set(null/*static field*/, newMethods);
		} catch (NoSuchFieldException e) {
			throw new IllegalStateException(e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException(e);
		}
	}
	@Override
	public void writeContent(OutputStream os) throws IOException {
		// no own content
	}

	@Override
	public void writeHeader(OutputStream os) throws IOException {
		// no header
	}

	
}
