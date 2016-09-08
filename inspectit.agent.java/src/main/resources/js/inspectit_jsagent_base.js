// STARTUP MODULE
var inspectIT = (function() {
	
	function init() {
		if (window.inspectIT_isRunning) {
			console.log("Injected script is already running, terminating new instance.");
			return;
		} else {
			window.inspectIT_isRunning = true;
		}
		
		// check if id exists and set if not
		inspectIT.cookies.checkCookieId();
		for (plugin in inspectIT.plugins) {
			inspectIT.plugins[plugin].init();
		}
		
		window.addEventListener("load", function() {
			for (plugin in inspectIT.plugins) {
				if (typeof inspectIT.plugins[plugin].onload !== "undefined") {
					inspectIT.plugins[plugin].onload();
				}
			}
		});
		
		document.addEventListener("DOMContentLoaded", function() {
			for (plugin in inspectIT.plugins) {
				if (typeof inspectIT.plugins[plugin].domready !== "undefined") {
					inspectIT.plugins[plugin].domready();
				}
			}
		});
	}
	
	return {
		start : init,
		plugins : {}
	};
})();

//UTILITY MODULE
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
		if (typeof navigator.sendBeacon !== "undefined") {
			navigator.sendBeacon(settings["eumManagementServer"], JSON.stringify(dataObject));
		} else {
			var xhrPost = new XMLHttpRequest();
			xhrPost.open("POST", settings["eumManagementServer"], !forceSynchronous);
			xhrPost.setRequestHeader("Content-Type", "application/json");
			xhrPost.send(JSON.stringify(dataObject));
		}
	}
	
	function getFunctionName(func) {
		if (!(typeof func === "function")) return null;
		if (func.hasOwnProperty("name")) return func.name; // ES6
		return "";
	}
	
	return {
		timestamp : getCurrentTimeStamp,
		os : getOS,
		browserinfo : getBrowserInformation,
		callback : sendToEUMServer,
		getFuncName : getFunctionName
	}
})();

//COOKIE MODULE
inspectIT.cookies = (function () {
	function hasCookie(name) {
		return getCookie(name) !== null;
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
		if (!inspectIT.action.hasActions()) {
			// NEW PAGELOADACTION
			var pageLoadAction = inspectIT.action.enterAction("pageLoad");
			window.addEventListener("load", function() {
				inspectIT.action.leaveAction(pageLoadAction);
			});
		}
		
		if (hasCookie("inspectIT_cookieId")) {
			var browserData = inspectIT.util.browserinfo();
			var sessionData = {
				type : "userSession",
				device : browserData.os,
				browser : browserData.name,
				language : browserData.lang,
				sessionId : inspectIT.cookies.getCurrentId()
			}
			inspectIT.util.callback(sessionData);
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

inspectIT.actionBundler = (function () {
	var currentBundle = [];
	var timeoutTask = null;
	var taskFinished = true;
	var lastRequest = null;
	var TIMEWINDOW = 2500;
	var MAXTIMEWINDOW = 15000;
	
	function addAction(action) {
		action.sessionId = inspectIT.cookies.getCurrentId();
		action.baseUrl = window.location.href;
		
		currentBundle.push(action);
		if (!taskFinished) {
			clearTimeout(timeoutTask);
		}
		
		var currStamp = inspectIT.util.timestamp();
		if (lastRequest != null && currStamp - lastRequest >= MAXTIMEWINDOW) {
			// send immediately
			finishBundle();
			taskFinished = true;
		} else {
			if (lastRequest == null) lastRequest = currStamp;
			timeoutTask = setTimeout(finishBundle, TIMEWINDOW);
			taskFinished = false;
		}
	}
	
	function finishBundle() {
		inspectIT.util.callback(currentBundle);
		currentBundle = [];
		lastRequest = inspectIT.util.timestamp();
	}
	
	return {
		addAction : addAction
	}
})();

//ACTION MODULE
//Identifying user actions and send if they're complete
inspectIT.action = (function () {
	var actions = [];
	var actionChildIds = [];
	var finishedChilds = [];
	
	var offset = 0;
	
	// For action capturing
	function enterAction(specType) {
		actions.push({
			type : "userAction",
			specialType : specType,
			contents : []
		});
		actionChildIds.push([++offset]);
		finishedChilds.push([]);
		return offset;
	}
	
	function leaveAction(enterId) {
		var actionId = getActionFromId(enterId);
		if (actionId >= 0) {
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
		
		inspectIT.actionBundler.addAction(actions[id]);
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
	}
	
	// submits data to a action
	function submitData(entrId, data) {
		var currentAction = getActionFromId(entrId);
		if (currentAction >= 0) {
			actions[currentAction].contents.push(data);
		} // otherwise we can't assign it to an action
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
	
	return {
		enterAction : enterAction,
		leaveAction : leaveAction,
		enterChild : enterChild,
		leaveChild : leaveChild,
		submitData : submitData,
		hasActions : hasActions
	}
})();
