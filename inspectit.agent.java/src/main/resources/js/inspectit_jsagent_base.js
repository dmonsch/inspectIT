//configuration
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
			if (typeof window.inspectIT_eum_cookieId !== "undefined") {
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

//ACTION MODULE
//Identifying user actions and send if they're complete
//NEEDS : SESSIONSTORAGE
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

