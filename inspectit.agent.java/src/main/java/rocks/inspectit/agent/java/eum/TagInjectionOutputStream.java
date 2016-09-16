package rocks.inspectit.agent.java.eum;


import java.io.IOException;
import java.io.OutputStream;

import rocks.inspectit.agent.java.proxy.IProxySubject;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.agent.java.proxy.ProxyFor;
import rocks.inspectit.agent.java.proxy.ProxyMethod;

/**
 * A ServletOutputStream which injects the given tag on the fly into the head (or another appropriate) section of the document.
 * Automatically detects non-html and then falls back to just piping the data through.
 *
 * @author Jonas Kunz
 */
@ProxyFor(superClass = "javax.servlet.ServletOutputStream")
public class TagInjectionOutputStream extends OutputStream implements IProxySubject {

	/**
	 * The actual stream to which the data will be written.
	 */
	private WServletOutputStream originalStream;

	/**
	 * The parser used for inejcting the tag.
	 */
	private HTMLScriptInjector parser;

	/**
	 * The new-line character.
	 */
	private static final String NL = System.getProperty("line.separator");


	/**
	 * Creates a tag injecting stream.
	 *
	 * @param originalStream
	 *            the wrapped stream, to which the data will be passed through.
	 * @param tagToInject
	 *            the tag to inject.
	 */
	public TagInjectionOutputStream(Object originalStream, String tagToInject) {
		this.originalStream = WServletOutputStream.wrap(originalStream);
		parser = new HTMLScriptInjector(tagToInject);
	}

	public Object[] getProxyConstructorArguments() {
		return new Object[]{};
	}

	/**
	 * {@inheritDoc}
	 */
	public void proxyLinked(Object proxyObject, IRuntimeLinker linker) {
		//nothing to do
	}

	/**
	 * Sets the Character encoding used by the data.
	 * Only this way the stream is able to decode / encode binary data.
	 * @param charsetName the name of the encoding
	 */
	public void setEncoding(String charsetName) {
		parser.setEncoding(charsetName);
	}


	@ProxyMethod
	public void print(boolean arg0) throws IOException {
		originalStream.print(parser.performInjection(String.valueOf(arg0)));
	}

	@ProxyMethod
	public void print(char c) throws IOException {
		originalStream.print(parser.performInjection(String.valueOf(c)));
	}

	@ProxyMethod
	public void print(double d) throws IOException {
		originalStream.print(parser.performInjection(String.valueOf(d)));
	}

	@ProxyMethod
	public void print(float f) throws IOException {
		originalStream.print(parser.performInjection(String.valueOf(f)));
	}

	@ProxyMethod
	public void print(int i) throws IOException {
		originalStream.print(parser.performInjection(String.valueOf(i)));
	}

	@ProxyMethod
	public void print(long l) throws IOException {
		originalStream.print(parser.performInjection(String.valueOf(l)));
	}

	@ProxyMethod
	public void print(String arg0) throws IOException {
		originalStream.print(parser.performInjection(arg0));
	}

	@ProxyMethod
	public void println() throws IOException {
		originalStream.print(parser.performInjection(NL));
	}

	@ProxyMethod
	public void println(boolean b) throws IOException {
		originalStream.print(parser.performInjection(String.valueOf(b)+NL));
	}

	@ProxyMethod
	public void println(char c) throws IOException {
		originalStream.print(parser.performInjection(String.valueOf(c)+NL));
	}

	@ProxyMethod
	public void println(double d) throws IOException {
		originalStream.print(parser.performInjection(String.valueOf(d)+NL));
	}

	@ProxyMethod
	public void println(float f) throws IOException {
		originalStream.print(parser.performInjection(String.valueOf(f)+NL));
	}

	@ProxyMethod
	public void println(int i) throws IOException {
		originalStream.print(parser.performInjection(String.valueOf(i)+NL));
	}

	@ProxyMethod
	public void println(long l) throws IOException {
		originalStream.print(parser.performInjection(String.valueOf(l)+NL));
	}

	@ProxyMethod
	public void println(String s) throws IOException {
		originalStream.print(parser.performInjection(String.valueOf(s)+NL));
	}


	@Override
	@ProxyMethod
	public void write(int b) throws IOException {
		originalStream.write(parser.performInjection(new byte[]{(byte)b}));
	}

	@Override
	@ProxyMethod
	public void write(byte[] b) throws IOException {
		originalStream.write(parser.performInjection(b));
	}

	@Override
	@ProxyMethod
	public void write(byte[] b, int off, int len) throws IOException {
		byte[] copiedData = new byte[len];
		System.arraycopy(b, off, copiedData, 0, len);
		originalStream.write(parser.performInjection(copiedData));
	}

	@Override
	@ProxyMethod
	public void flush() throws IOException {
		originalStream.flush();
	}

	@Override
	@ProxyMethod
	public void close() throws IOException {
		originalStream.close();
	}


	@ProxyMethod(isOptional = true)
	public boolean isReady() {
		return originalStream.isReady();
	}

	@ProxyMethod(parameterTypes = {"javax.servlet.WriteListener"}, isOptional = true)
	public void setWriteListener(Object listener) {
		originalStream.setWriteListener(listener);

	}





}
