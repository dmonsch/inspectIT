package rocks.inspectit.agent.java.eum;


import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import rocks.inspectit.agent.java.proxy.IProxySubject;
import rocks.inspectit.agent.java.proxy.IRuntimeLinker;
import rocks.inspectit.agent.java.proxy.ProxyFor;
import rocks.inspectit.agent.java.proxy.ProxyMethod;

/*
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
 */

/**
 *
 * Detects html content and injects a script tag at the correct point on-the-fly
 *
 * @author Jonas Kunz
 *
 */
@ProxyFor(superClass = "javax.servlet.http.HttpServletResponseWrapper", constructorParameterTypes={"javax.servlet.http.HttpServletResponse"})
public class TagInjectionResponseWrapper implements IProxySubject{

	private OutputStream wrappedStream;

	private PrintWriter wrappedWriter;

	private Object linkedThis;

	private W_HttpServletResponse wrappedResponse;
	private IRuntimeLinker linker;

	private String tagToInject;

	private Integer contentLengthSet = null;

	public TagInjectionResponseWrapper(Object responseObject, String tagToInject) {
		this.tagToInject = tagToInject;
		wrappedResponse = W_HttpServletResponse.wrap(responseObject);
	}

	public Object[] getProxyConstructorArguments() {
		return new Object[]{wrappedResponse.getWrappedElement()};
	}

	public void proxyLinked(Object proxyObject,IRuntimeLinker linker) {
		linkedThis = proxyObject;
		this.linker = linker;
	}



	@ProxyMethod
	public PrintWriter getWriter() throws IOException {
		if(wrappedWriter == null) {
			commitContentLength();
			PrintWriter originalWriter = wrappedResponse.getWriter();
			//avoid rewrapping or unnecessary wrapping
			if(isNonHTMLContentTypeSet() || (originalWriter instanceof TagInjectionPrintWriter)) {
				wrappedWriter =  originalWriter;
			} else {
				wrappedWriter = new TagInjectionPrintWriter(originalWriter,tagToInject);
			}
		}
		return wrappedWriter;
	}


	@ProxyMethod(returnType = "javax.servlet.ServletOutputStream")
	public OutputStream getOutputStream() throws IOException {

		if (wrappedStream == null) {
			commitContentLength();
			OutputStream originalStream = wrappedResponse.getOutputStream();
			//avoid rewrapping or unncessary wrapping
			if (isNonHTMLContentTypeSet() || linker.isProxyInstance(originalStream, TagInjectionOutputStream.class)) {
				wrappedStream = originalStream;
			} else {
				TagInjectionOutputStream resultStr = new TagInjectionOutputStream(originalStream, tagToInject);
				resultStr.setEncoding(wrappedResponse.getCharacterEncoding());

				ClassLoader cl = wrappedResponse.getWrappedElement().getClass().getClassLoader();
				wrappedStream = (OutputStream) linker.createProxy(TagInjectionOutputStream.class, resultStr, cl);
				if(wrappedStream == null) {
					//fallback to the normal stream if it can not be linked
					wrappedStream = originalStream;
				}
			}
		}
		return wrappedStream;
	}



	@ProxyMethod
	public void setContentLength(int len) {
		//we do not delegate this call at this moment- maybe we have to used chunked encoding for the request
		this.contentLengthSet = len;
	}

	@ProxyMethod
	public void setContentLengthLong(long len) {
		// we do not delegate this call at this moment- maybe we have to used chunked encoding for
		// the request
		this.contentLengthSet = (int) len;
	}


	/**
	 * Called when the headers are commited. At this point of time we have to decide whether we force chunked encoding.
	 */
	private void commitContentLength() {
		if(contentLengthSet != null) {
			if(isNonHTMLContentTypeSet()) {
				wrappedResponse.setContentLength(contentLengthSet);
			}
		}
	}

	private boolean isNonHTMLContentTypeSet() {
		String contentMime = wrappedResponse.getContentType();
		if(contentMime != null) {

			if(contentMime.startsWith("text/html")
					|| contentMime.startsWith("application/xhtml+xml")) {
				return false;
			} else {
				return true;
			}
		} else {
			//unset content type, it could still be HTML
			return false;
		}
	}










}