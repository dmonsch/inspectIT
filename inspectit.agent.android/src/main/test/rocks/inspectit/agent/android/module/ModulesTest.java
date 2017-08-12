package rocks.inspectit.agent.android.module;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.HttpURLConnection;

import javax.net.ssl.HttpsURLConnection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import android.content.Context;
import rocks.inspectit.agent.android.callback.CallbackManager;
import rocks.inspectit.agent.android.core.AndroidDataCollector;
import rocks.inspectit.agent.android.core.TracerImplHandler;
import rocks.inspectit.agent.android.module.net.NetworkModule;
import rocks.inspectit.agent.android.util.DependencyManager;
import rocks.inspectit.shared.all.communication.data.mobile.CrashResponse;
import rocks.inspectit.shared.all.communication.data.mobile.MobileDefaultData;
import rocks.inspectit.shared.all.communication.data.mobile.NetRequestResponse;

/**
 * @author David Monschein
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class ModulesTest {

	@Mock
	Context mMockContext;

	@Mock
	CallbackManager callbackMock;

	@Mock
	AndroidDataCollector dataCollectorMock;

	TracerImplHandler tracerMock = new TracerImplHandler();

	@Captor
	ArgumentCaptor<MobileDefaultData> captor;

	@Test
	public void testCrashModule() {
		CrashModule module = new CrashModule();
		module.setCallbackManager(callbackMock);

		module.initModule(mMockContext);

		Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), new RuntimeException());

		verify(callbackMock, times(1)).pushData(captor.capture());
		assertTrue(captor.getValue() instanceof CrashResponse);
		reset(callbackMock);
	}

	@Test
	public void testNetworkModule() {
		DependencyManager.setTracerImplHandler(tracerMock);
		NetworkModule module = new NetworkModule();

		module.initModule(mMockContext);
		module.setAndroidDataCollector(dataCollectorMock);
		module.setCallbackManager(callbackMock);

		HttpURLConnection mockConnection = mock(HttpsURLConnection.class);
		module.openConnection(mockConnection);

		try {
			module.getOutputStream(mockConnection);
			verify(mockConnection, times(1)).getOutputStream();
		} catch (IOException e) {
			fail(e.getMessage());
		}

		try {
			module.getResponseCode(mockConnection);
			verify(mockConnection, times(1)).getResponseCode();
		} catch (IOException e) {
			fail(e.getMessage());
		}

		reset(mockConnection);

		module.shutdownModule(); // -> this calls collecting func
		verify(callbackMock, times(1)).pushData(captor.capture());
		assertTrue(captor.getValue() instanceof NetRequestResponse);
	}

}
