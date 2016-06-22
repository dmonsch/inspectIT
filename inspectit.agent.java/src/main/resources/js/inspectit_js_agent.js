//configuration
// TODO: outsource modules into single files and refactor
window.inspectIT_settings = {
	eumManagementServer : "/wps/contenthandler/eum_handler"
};

// STARTUP MODULE
var inspectIT = (function() {
	
	function init() {
		if (window.diagnoseIT_isRunning) {
			console.log("Injected script is already running, terminating new instance.");
			return;
		} else {
			window.diagnoseIT_isRunning = true;
		}
		
		// check if id exists and set if not
		inspectIT.cookies.checkCookieId();
		inspectIT.action.init();
		
		inspectIT.async.instrument();
		inspectIT.timings.collectResourceTimings();
		inspectIT.timings.collectNavigationTimings();
		inspectIT.ajax.instrument();
		inspectIT.listener.instrument();
		
		document.addEventListener("DOMContentLoaded", function() {
			inspectIT.listener.instrumentAfterLoad();
		});
	}
	
	return init;
})();

// LISTENER MODULE
inspectIT.listener = (function () {
	var instrumentedEvents = {
		"click" : true,
		"onchange" : true,
		"scroll" : true,
		"onmouseover" : true,
		"onmouseout" : true,
		"onkeydown" : true
	}
	
	// for removing events
	var activeEvents = {};
	var currId = 0;
	
	function instrumentDocumentListener() {
		var docListeners = [
		    [document, false],
		    [window, false]
		];
		
		for (var i = 0; i < docListeners.length; i++) {
			instrumentAddListener(docListeners[i][0], docListeners[i][1]);
			instrumentRemoveListener(docListeners[i][0], docListeners[i][1]);
		}
	}
	
	function instrumentListener() {
		// FOR COMMON ELEMENTS
		var commonListeners = [
		    [Element, true],
		    [EventTarget, true]
		];
		
		for (var i = 0; i < commonListeners.length; i++) {
			instrumentAddListener(commonListeners[i][0], commonListeners[i][1]);
			instrumentRemoveListener(commonListeners[i][0], commonListeners[i][1]);
		}
	}
	
	function instrumentAddListener(base, prot) {
		if (typeof base === "undefined") return;
		if (prot && typeof base.prototype === "undefined") return;
		if ((prot && typeof base.prototype.addEventListener === "undefined") || (!prot && typeof base.addEventListener === "undefined")) return;
		
		if (prot) {
			var addEvListener = base.prototype.addEventListener;
			base.prototype.addEventListener = function(event, callback, bubble) {
				addListenerInstrumentation.call(this, addEvListener, event, callback, bubble);
			}
		} else {
			var addEvListener = base.addEventListener;
			base.addEventListener = function(event, callback, bubble) {
				addListenerInstrumentation.call(this, addEvListener, event, callback, bubble);
			}
		}
	}
	
	function instrumentRemoveListener(base, prot) {
		if (typeof base === "undefined") return;
		if (prot && typeof base.prototype === "undefined") return;
		if ((prot && typeof base.prototype.removeEventListener === "undefined") || (!prot && typeof base.removeEventListener === "undefined")) return;
		
		if (prot) {
			var remEvListener = base.prototype.removeEventListener;
			base.prototype.removeEventListener = function(event, callback, opt) {
				removeListenerInstrumentation.call(this, remEvListener, event, callback, opt);
			}
		} else {
			var remEvListener = base.removeEventListener;
			base.removeEventListener = function(event, callback, opt) {
				removeListenerInstrumentation.call(this, remEvListener, event, callback, opt);
			}
		}
	}
	
	function removeListenerInstrumentation(realMethod, event, callback, opt) {
		if (event in instrumentedEvents && typeof callback.___id !== "undefined") {
			realMethod.call(this, event, activeEvents[callback.___id], opt);
			delete activeEvents[callback.___id];
		}
	}
	
	function addListenerInstrumentation(realMethod, event, callback, bubble) {
		var dataObj = {
			tagName : (typeof this.tagName !== "undefined" ? this.tagName : ""),
			elementId : (typeof this.id !== "undefined" ? this.id : ""),
			elementName : (typeof this.name !== "undefined" ? this.name : ""),
			methodName : inspectIT.util.getFuncName(callback),
			eventName : event,
			type : "clickAction"
		}
		
		if (event in instrumentedEvents) {
			// assign an id to the callback so we can access the instrumented function by the old func
			callback.___id = ++currId;
			activeEvents[currId] = function(e) {
				var currAction = inspectIT.action.enterAction("click");
				dataObj.beginTime = inspectIT.util.timestamp();
				inspectIT.action.submitData(currAction, dataObj, true);
				
				callback.call(this, e);
				
				dataObj.endTime = inspectIT.util.timestamp();
				inspectIT.action.submitData(currAction, dataObj);
				inspectIT.action.leaveAction(currAction);
			}
			
			realMethod.call(this, event, activeEvents[currId], bubble);
		} else {
			// we dont want to instrument
			realMethod.call(this, event, callback, bubble);
		}
	}
	
	return {
		instrument : instrumentListener,
		instrumentAfterLoad : instrumentDocumentListener
	}
})();

// AJAX MODULE
inspectIT.ajax = (function () {
	var settings = window.inspectIT_settings;
	
	function instrumentAjax() {
		// instrument xmlhttprequest
		XMLHttpRequest.prototype.uninstrumentedOpen = XMLHttpRequest.prototype.open;
		XMLHttpRequest.prototype.uninstrumentedSend = XMLHttpRequest.prototype.send;
		XMLHttpRequest.prototype.open = function(method, url, async, user, pw) {
			//only instrument if it wasnt a request to our management server
			if (url != settings["eumManagementServer"]) {
				//all methods in here will have access to this object (principle of closures)
				//a record is created for every open-call (should only happen once per XMLHTTPRequest)
				var ajaxRecord = {
					type : "AjaxRequest",
					method : method,
					url : url,
					async : async,
					status : 200
				};
				
				// action capturing
				var currAjaxAction = inspectIT.action.enterChild();
				var currAjaxCallbackAction = null;
				
				//the "this" keyword will point to the actual XMLHTTPRequest object, so we overwrite the send method
				//of just this object (makes sure we use our specific ajaxRecord object)
				this.send = function(arg) {
					ajaxRecord.beginTime = inspectIT.util.timestamp();
					if (arg != undefined) {
						ajaxRecord.requestContent = arg.toString();
					} else {
						ajaxRecord.requestContent = null;
					}
					//apply is a more safe way of calling the actual method, making sure that all
					//arguments passed to this method will be passed to the real method
					return XMLHttpRequest.prototype.uninstrumentedSend.apply(this, arguments);
				};
				
				// this gives us the time between sending the request
				// and getting back the response (better than below)
				// -> works in all modern browsers
				this.addEventListener("progress", function(oEvent) {
					 if (!ajaxRecord.sent && oEvent.lengthComputable) {
						 var percentComplete = oEvent.loaded / oEvent.total;
						 if (percentComplete >= 1) { // -> we're finished
							 ajaxRecord.status = this.status;
							 ajaxRecord.endTime = inspectIT.util.timestamp();
							 inspectIT.action.submitData(currAjaxAction, ajaxRecord);
							 ajaxRecord.sent = true;
							 currAjaxCallbackAction = inspectIT.action.enterChild(currAjaxAction);
						 }
					 }
				});
				
				// this gives us the time between send and finish of all
				// javascript tasks executed after the request
				// -> fallback solution if length not computable
				this.addEventListener("loadend", function() {
					if (!ajaxRecord.sent) {
						ajaxRecord.status = this.status;
						ajaxRecord.endTime = inspectIT.util.timestamp();
						inspectIT.action.submitData(currAjaxAction, ajaxRecord);
						ajaxRecord.sent = true;
					}
					
					// ajax finished
					inspectIT.action.leaveChild(currAjaxAction);
					if (currAjaxCallbackAction != null) {
						inspectIT.action.leaveChild(currAjaxCallbackAction);
					}
				});
			}
			return XMLHttpRequest.prototype.uninstrumentedOpen.apply(this, arguments);
		}
	}
	
	return {
		instrument : instrumentAjax
	};
})();

// TIMINGS MODULE
inspectIT.timings = (function () {
	
	function collectResourceTimings() {
		if (("performance" in window) && ("getEntriesByType" in window.performance) && (window.performance.getEntriesByType("resource") instanceof Array)) {
			var resourceTimingsBlock = inspectIT.action.enterChild();
			//increase the buffer size to make sure everythin is captured
			window.performance.setResourceTimingBufferSize(500);
			//add event listener, which is called after the site has fully finished loading
			function sendAndClearResourceTimings() {
				var timingsList = [];
				var resourceList = window.performance.getEntriesByType("resource");
				for ( i = 0; i < resourceList.length; i++) {
					timingsList.push({
						name : resourceList[i].name,
						startTime : Math.round(resourceList[i].startTime),
						endTime : Math.round(resourceList[i].responseEnd),
						initiatorType : resourceList[i].initiatorType
					});
				}
				if (timingsList.length > 0) {
					for (var i = 0; i < timingsList.length; i++) {
						timingsList[i].type = "ResourceLoadRequest";
						inspectIT.action.submitData(resourceTimingsBlock, timingsList[i]);
					}
				}
				//clear the timings to make space for new ones
				window.performance.clearResourceTimings();
				
				inspectIT.action.leaveChild(resourceTimingsBlock);
			}

			window.addEventListener("load", sendAndClearResourceTimings);
		}
	}
	
	function collectNavigationTimings() {
		if (("performance" in window) && ("timing" in window.performance)) {
			var navTimingBlock = inspectIT.action.enterChild();
			//add event listener, which is called after the site has fully finished loading
			window.addEventListener("load", function() {
				var objCallback = {
					type : "PageLoadRequest"
				}
				for (var key in window.performance.timing) {
					// this is really sad but otherwise toJSON doesn't work in all browsers
					objCallback["_" + String(key)] = window.performance.timing[key];
				}
				objCallback["url"] = document.location.href;
				
				inspectIT.action.submitData(navTimingBlock, objCallback);
				inspectIT.action.leaveChild(navTimingBlock);
			});
		}
	}
	
	return {
		collectResourceTimings : collectResourceTimings,
		collectNavigationTimings : collectNavigationTimings
	}
})();

// UTILITY MODULE
inspectIT.util = (function () {
	
	var settings = window.inspectIT_settings;
	
	function getCurrentTimeStamp() {
		if (window.performance) {
			if (performance.timing.navigationStart != 0) {
				return Math.round(performance.now() + performance.timing.navigationStart);
			}
		}
		return Date.now();
	}
	
	function getOS() { // gets the operation system, null if we can't recognize it
		var os = null;
		var userAgent = navigator.userAgent;
		// mobile detection
		if(userAgent.match(/iPad/i) || userAgent.match(/iPhone/i) || userAgent.match(/iPod/i)) {
			return "iOS";
		} else if(userAgent.match(/Android/i)) {
			return "Android";
		}
		// desktop detection
		if (navigator.appVersion.indexOf("Win") > -1) os="Windows";
		else if (navigator.appVersion.indexOf("Mac") > -1) os="Mac";
		else if (navigator.appVersion.indexOf("Linux") > -1 || navigator.appVersion.indexOf("X11") > -1) os="Linux";
		return os;
	}
	
	function getBrowserInformation() {
		// gets information about the browser of the user
		// feature detection
		var isOpera = (!!window.opr && !!opr.addons) || !!window.opera || navigator.userAgent.indexOf(' OPR/') >= 0;
		var isFirefox = typeof InstallTrigger !== 'undefined';
		var isSafari = Object.prototype.toString.call(window.HTMLElement).indexOf('Constructor') > 0;
		var isIE = /*@cc_on!@*/false || !!document.documentMode;
		var isEdge = !isIE && !!window.StyleMedia;
		var isChrome = Boolean(window.chrome);
		
		// get language
		var userLanguage = navigator.language || navigator.userLanguage;
		
		var retObj = {
			lang : userLanguage,
			os : getOS()
		};
		if (isOpera) {
			retObj.name = "Opera";
		} else if (isFirefox) {
			retObj.name = "Firefox";
		} else if (isSafari) {
			retObj.name = "Safari";
		} else if (isIE) {
			retObj.name = "Internet Explorer";
		} else if (isEdge) {
			retObj.name = "Edge";
		} else if (isChrome) {
			retObj.name = "Google Chrome";
		}
		return retObj;
	}
	
	function sendToEUMServer(dataObject, forceSynchronous) {
		// maybe append browser information?
		var xhrPost = new XMLHttpRequest();
		xhrPost.open("POST", settings["eumManagementServer"], !forceSynchronous);
		xhrPost.setRequestHeader("Content-Type", "application/json");
		// add sessionid
		dataObject.sessionId = inspectIT.cookies.getCurrentId();
		xhrPost.send(JSON.stringify(dataObject));
	}
	
	function getFunctionName(func) {
		if (!(typeof func === "function")) return null;
		if (func.hasOwnProperty("name")) return func.name; // ES6
		// FROM HERE ES5
		return "";
		// TODO doesn't work in IE
		var methodFull = func.toString().match(/^function\s*([^\s(]+)/)[1];
		return methodFull;
	}
	
	return {
		timestamp : getCurrentTimeStamp,
		os : getOS,
		browserinfo : getBrowserInformation,
		callback : sendToEUMServer,
		getFuncName : getFunctionName
	}
})();

// COOKIE MODULE
inspectIT.cookies = (function () {
	function hasCookie(name) {
		return getCookie(name) !== null;
	}
	
	function setCookie(key, value, expireMinutes) {
		var d = new Date();
		d.setTime(d.getTime() + (expireMinutes * 60 * 1000));
		document.cookie = key + "=" + value + "; expires=" + d.toUTCString(); 
	}
	
	function getCookie(key) {
		var name = key + "=";
	    var ca = document.cookie.split(';');
	    for(var i = 0; i <ca.length; i++) {
	        var c = ca[i];
	        while (c.charAt(0)==' ') {
	            c = c.substring(1);
	        }
	        if (c.indexOf(name) == 0) {
	            return c.substring(name.length,c.length);
	        }
	    }
	    return null;
	}
	
	function checkCookieId() {
		
		// check if we have an existing action
		inspectIT.action.restoreFromSession();
		if (!inspectIT.action.hasActions()) {
			// NEW PAGELOADACTION
			var pageLoadAction = inspectIT.action.enterAction("pageLoad");
			window.addEventListener("load", function() {
				inspectIT.action.leaveAction(pageLoadAction);
			});
		} else {
			// PAGELOAD because of useraction
			window.addEventListener("load", function() {
				inspectIT.action.finishRestoredActionRoot();
			});
		}
		
		if (!hasCookie("inspectIT_cookieId")) {
			// see if we have an id to set
			if (window.inspectIT_eum_cookieId != "undefined") {
				setCookie("inspectIT_cookieId", window.inspectIT_eum_cookieId, 60);
				
				// new UserSession
				var browserData = inspectIT.util.browserinfo();
				var sessionData = {
					type : "userSession",
					device : browserData.os,
					browser : browserData.name,
					language : browserData.lang
				}
				inspectIT.util.callback(sessionData);
			}
		}
	}
	
	function getCurrentId() {
		return getCookie("inspectIT_cookieId");
	}
	
	return {
		checkCookieId : checkCookieId,
		getCurrentId : getCurrentId,
	}
})();

// INSTRUMENTATION FIR TIMER (setTimeout etc.)
inspectIT.async = (function () {
	
	var originalSetTimeout = window.setTimeout;
	var originalClearTimeout = window.clearTimeout;
	
	function instrumentTimers() {
		var timerChildMap = {};
		
		window.setTimeout = function(f) {
			var enterAsync = inspectIT.action.enterChild();
			
			newFunction = function() {
				var innerEnter = inspectIT.action.enterChild(enterAsync);
				f.apply(this, arguments);
				inspectIT.action.leaveChild(innerEnter);
				inspectIT.action.leaveChild(enterAsync);
			}
			
			var args = Array.prototype.slice.call(arguments);
			args[0] = newFunction;
			
			var retVal = originalSetTimeout.apply(this, args);
			timerChildMap[retVal] = enterAsync;
			return retVal;
		}
		
		window.clearTimeout = function(id) {
			inspectIT.action.leaveChild(timerChildMap[id]);
			originalClearTimeout.apply(this, arguments);
		}
	}
	
	return {
		instrument : instrumentTimers
	}
})();

// ACTION MODULE
// Identifying user actions and send if they're complete
// NEEDS : SESSIONSTORAGE
// TODO invalidate if outside an action
inspectIT.action = (function () {
	var actions = [];
	var actionChildIds = [];
	var finishedChilds = [];
	var snapshotData = [];
	
	var offset = 0;
	var restoredActions = 0;
	var unloadInited = false;
	
	// For action capturing
	function enterAction(specType) {
		actions.push({
			type : "userAction",
			specialType : specType,
			contents : []
		});
		actionChildIds.push([++offset]);
		finishedChilds.push([]);
		snapshotData.push([]);
		return offset;
	}
	
	function leaveAction(enterId) {
		var actionId = getActionFromId(enterId);
		if (actionId >= 0 && !unloadInited) {
			finishedChilds[actionId].push(enterId);
			actionFinished(actionId); // check if finished
		}
	}
	
	function enterChild(parentId) {
		var currentAction;
		if (typeof parentId === "undefined") {
			currentAction = getActionFromId(offset);
		} else {
			currentAction = getActionFromId(parentId);
		}
		if (currentAction >= 0) {
			actionChildIds[currentAction].push(++offset);
			return offset;
		}
	}
	
	function leaveChild(enterId) {
		var actionId = getActionFromId(enterId);
		if (actionId >= 0) {
			finishedChilds[actionId].push(enterId);
			actionFinished(actionId); // check if finished
		} 
	}
	// END For actipn capturing
	
	// determines wheter the action has finished or not
	function actionFinished(id) {
		if (actionChildIds[id].length == finishedChilds[id].length) {
			//  the action is finished
			finishAction(id);
		}
	}
	
	function hasActions() {
		return actions.length > 0;
	}
	
	function finishAction(id, sync) {
		if (typeof sync === "undefined") sync = false;
		
		inspectIT.util.callback(actions[id], sync);
		forceRemove(id);
		
		if (actions.length == 0) {
			// reset offset because there is no action atm
			offset = 0;
		}
	}
	
	function forceRemove(id) {
		actions.splice(id, 1);
		finishedChilds.splice(id, 1);
		actionChildIds.splice(id, 1);
		snapshotData.splice(id, 1);
	}
	
	// finishes the action which caused a new page load
	function finishRestoredActionRoot() {
		if (restoredActions > 0) {
			for (var i = 0; i < restoredActions; i++) {
				// assign end time
				actions[i].contents[0].endTime = inspectIT.util.timestamp();
				finishedChilds[i].push(actionChildIds[i][0]);
			}
		}
	}
	
	// submits data to a action
	function submitData(entrId, data, isSnapshot) {
		if (typeof isSnapshot === 'undefined') { isSnapshot = false; }
		
		var currentAction = getActionFromId(entrId);
		if (currentAction >= 0) {
			if (!isSnapshot) {
				actions[currentAction].contents.push(data);
			} else {
				snapshotData[currentAction].push(data);
			}
		} // otherwise we can't assign it to an action
	}
	
	// adds the snapshot data to the action data
	// if the action wasn't finished and the site disappears or something similar
	function swapSnapshotData(id) {
		if (!(id < snapshotData.length)) return;
		for (var i = 0; i < snapshotData[id].length; i++) {
			actions[id].contents.push(snapshotData[id][i]);
		}
	}
	
	// gets the action id from child id
	function getActionFromId(id) {
		for (var i = 0; i < actionChildIds.length; i++) {
			for (var j = 0; j < actionChildIds[i].length; j++) {
				if (actionChildIds[i][j] == id) return i;
			}
		}
		return -1;
	}
	
	// determines wheter the root of the action was finished or not
	function actionRootComplete(actionId) {
		if (actionId >= actions.length) return true;
		for (var i = 0; i < finishedChilds[actionId].length; i++) {
			if (finishedChilds[actionId][i] == actionChildIds[actionId][0]) {
				return true;
			}
		}
		return false;
	}
	
	// finish all actions except of the one (or none) who "caused" the unload
	function beforeUnload() {
		unloadInited = true;
		
		var k = 0;
		while (k < actions.length) { // will terminate always
			if (actionRootComplete(k)) {
				// we need to do the ajax sync otherwise it will be cancelled
				finishAction(k, true);
			} else {
				swapSnapshotData(k);
				k++;
			}
		}
		
		saveToSession();
	}
	
	// handle an action who caused a new window creation
	function onNewWindow(reference) {
		// TODO is this efficient possible?
		var actionId = getActionFromId(offset); // action who caused the new window load
	}
	
	// SESSION STORAGE THINGS
	function saveToSession() {
		// max 1 action (the one which caused the location change)
		if (window.sessionStorage && actions.length > 0) {
			sessionStorage.setItem("inspectIT_actions", JSON.stringify(actions));
			sessionStorage.setItem("inspectIT_actionChilds", JSON.stringify(actionChildIds));
			sessionStorage.setItem("inspectIT_finishedChilds", JSON.stringify(finishedChilds));
			sessionStorage.setItem("inspectIT_offset", String(actionChildIds[0][0]));
		}
	}
	
	function restoreFromSession() {
		if (window.sessionStorage) {
			if (sessionStorage.getItem("inspectIT_actions") !== null
					&& sessionStorage.getItem("inspectIT_actionChilds") !== null
					&& sessionStorage.getItem("inspectIT_finishedChilds") !== null
					&& sessionStorage.getItem("inspectIT_offset") !== null) {
				
				actions = JSON.parse(sessionStorage.getItem("inspectIT_actions"));
				actionChildIds = JSON.parse(sessionStorage.getItem("inspectIT_actionChilds"));
				finishedChilds = JSON.parse(sessionStorage.getItem("inspectIT_finishedChilds"));
				offset = JSON.parse(sessionStorage.getItem("inspectIT_offset"));
				
				clearSessionStorage();
				
				restoredActions = actions.length;
			}
		}
	}
	
	function clearSessionStorage() {
		sessionStorage.removeItem("inspectIT_actions");
		sessionStorage.removeItem("inspectIT_actionChilds");
		sessionStorage.removeItem("inspectIT_finishedChilds");
		sessionStorage.removeItem("inspectIT_offset");
	}
	
	// CREATE LISTENERS AND INSTRUMENT SOME FUNCS
	function initialize() {
		// add listener for before unload
		window.addEventListener("beforeunload", function (event) {
			inspectIT.action.beforeUnload();
		});
		// add listener for window creation
		var uninstrumentedWinOpen = window.open;
		window.open = function() {
			var ret = uninstrumentedWinOpen.apply(this, arguments);
			onNewWindow(ret);
			return ret;
		}
	}
	
	return {
		enterAction : enterAction,
		leaveAction : leaveAction,
		enterChild : enterChild,
		leaveChild : leaveChild,
		submitData : submitData,
		restoreFromSession : restoreFromSession,
		hasActions : hasActions,
		beforeUnload : beforeUnload,
		finishRestoredActionRoot : finishRestoredActionRoot,
		init : initialize
	}
})();

// execute the injection
inspectIT();