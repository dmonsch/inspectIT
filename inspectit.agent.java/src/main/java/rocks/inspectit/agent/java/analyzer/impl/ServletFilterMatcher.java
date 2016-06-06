package rocks.inspectit.agent.java.analyzer.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;
import rocks.inspectit.agent.java.analyzer.IClassPoolAnalyzer;
import rocks.inspectit.agent.java.analyzer.IInheritanceAnalyzer;
import rocks.inspectit.agent.java.analyzer.IMatcher;

/**
 *
 * Matches the doFilter method of an javax.servlet.Filter
 *
 * @author Jonas Kunz
 *
 */
public class ServletFilterMatcher implements IMatcher {

	private static final String FILTER_CLASS = "javax.servlet.Filter";

	private static final String[] doFilterParams = { "javax.servlet.ServletRequest", "javax.servlet.ServletResponse", "javax.servlet.FilterChain" };

	private final IInheritanceAnalyzer inheritanceAnalyzer;
	private final IClassPoolAnalyzer classPoolAnalyzer;

	public ServletFilterMatcher(IInheritanceAnalyzer inheritanceAnalyzer, IClassPoolAnalyzer classPoolAnalyzer) {
		super();
		this.inheritanceAnalyzer = inheritanceAnalyzer;
		this.classPoolAnalyzer = classPoolAnalyzer;
	}

	public boolean compareClassName(ClassLoader classLoader, String className) throws NotFoundException {
		Iterator<CtClass> i = inheritanceAnalyzer.getInterfaceIterator(classLoader, className);
		while (i.hasNext()) {
			CtClass clazz = i.next();
			if (FILTER_CLASS.equals(clazz.getName())) {
				return true;
			}
		}
		return false;
	}

	public List<CtMethod> getMatchingMethods(ClassLoader classLoader, String className) throws NotFoundException {
		ArrayList<CtMethod> matchingMethods = new ArrayList<CtMethod>();
		CtMethod[] allMethods = classPoolAnalyzer.getMethodsForClassName(classLoader, className);

		for (CtMethod meth : allMethods) {
			// check for name and parameters
			if (meth.getName().equals("doFilter") && checkTypes(meth.getParameterTypes(), doFilterParams)) {
				matchingMethods.add(meth);
			}
		}
		return matchingMethods;
	}

	private boolean checkTypes(CtClass[] parameterTypes, String[] expectedTypes) {
		if (parameterTypes.length == expectedTypes.length) {
			for (int i = 0; i < parameterTypes.length; i++) {
				if (!parameterTypes[i].getName().equals(expectedTypes[i])) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	public List<CtConstructor> getMatchingConstructors(ClassLoader classLoader, String className) throws NotFoundException {
		return Collections.emptyList();
	}

	public void checkParameters(List<? extends CtBehavior> methods) throws NotFoundException {
		// TODO do i have todo anything here if the filtering already takes place in the
		// getMatchingMethods method?

	}

}
