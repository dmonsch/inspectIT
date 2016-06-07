// TODO writing tests
//configuration
window.diagnoseIT_eum_settings = {
	eumManagementServer : "/wps/contenthandler/eum_handler"
};

var inspectIT_eum = (function() {
	function init() {
		if (window.diagnoseIT_isRunning) {
			console.log("Injected script is already running, terminating new instance.");
			return;
		} else {
			window.diagnoseIT_isRunning = true;
		}

		inspectIT_eum.timings.collectResourceTimings();
		inspectIT_eum.timings.collectNavigationTimings();
		inspectIT_eum.ajax.instrument();

		var addEventListener = document.addEventListener;
		document.addEventListener = function(event, callback, bubble) {
			inspectIT_eum.util.printTrace();
			addEventListener.call(this, event, callback, bubble);
		}
	}
	
	return init;
})();

// AJAX MODULE
inspectIT_eum.ajax = (function () {
	var settings = window.diagnoseIT_eum_settings;
	
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
					type : "ajax",
					method : method,
					url : url,
					async : async,
					status : 200
				};
				
				//the "this" keyword will point to the actual XMLHTTPRequest object, so we overwrite the send method
				//of just this object (makes sure we use our specific ajaxRecord object)
				this.send = function(arg) {
					ajaxRecord.beginTime = inspectIT_eum.util.timestamp();
					if (arg != undefined) {
						ajaxRecord.requestContent = arg.toString();
					} else {
						ajaxRecord.requestContent = null;
					}
					//apply is a more safe way of calling the actual method, making sure that all
					//arguments passed to this method will be passed to the real method
					return XMLHttpRequest.prototype.uninstrumentedSend.apply(this, arguments);
				};
				
				// this will give us the time between sending the request
				// and getting back the response (better than below)
				// -> works in all modern browsers
				this.addEventListener("progress", function(oEvent) {
					 if (!ajaxRecord.sent && oEvent.lengthComputable) {
						 var percentComplete = oEvent.loaded / oEvent.total;
						 if (percentComplete >= 1) {
							 ajaxRecord.status = this.status;
							 ajaxRecord.endTime = inspectIT_eum.util.timestamp();
							 inspectIT_eum.util.callback(ajaxRecord);
							 ajaxRecord.sent = true;
						 }
					 }
				});
				
				// this will give us the time between send and finish
				// of all javascript tasks executed after the request
				// -> fallback solution (do we even want this?)
				this.addEventListener("loadend", function() {
					if (!ajaxRecord.sent) {
						ajaxRecord.status = this.status;
						ajaxRecord.endTime = inspectIT_eum.util.timestamp();
						inspectIT_eum.util.callback(ajaxRecord);
						ajaxRecord.sent = true;
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
inspectIT_eum.timings = (function () {
	function collectResourceTimings() {
		if (("performance" in window) && ("getEntriesByType" in window.performance) && (window.performance.getEntriesByType("resource") instanceof Array)) {
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
					inspectIT_eum.util.callback({
						type : "resourceTimings",
						data : timingsList
					});
				}
				//clear the timings to make space for new ones
				window.performance.clearResourceTimings();
			}


			window.addEventListener("load", sendAndClearResourceTimings);
		}
	}
	function collectNavigationTimings() {
		if (("performance" in window) && ("timing" in window.performance)) {
			//add event listener, which is called after the site has fully finished loading
			window.addEventListener("load", function() {
				inspectIT_eum.util.callback({
					type : "navigationTimings",
					data : window.performance.timing // TODO fix problems with safari
				});
			});
		}
	}
	
	return {
		collectResourceTimings : collectResourceTimings,
		collectNavigationTimings : collectNavigationTimings
	}
})();

// UTILITY MODULE
inspectIT_eum.util = (function () {
	
	var settings = window.diagnoseIT_eum_settings;
	
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
		if (navigator.appVersion.indexOf("Win") > -1) os="Windows";
		else if (navigator.appVersion.indexOf("Mac") > -1) os="Mac";
		else if (navigator.appVersion.indexOf("Linux") > -1 || navigator.appVersion.indexOf("X11") > -1) os="Linux";
		return os;
	}
	
	function getBrowserInformation() {
		// gets information about the browser of the user
		// TODO
	}
	
	function printTrace() {
		try {
			throw new Error("stack sampling - not a real error");
		} catch(e) {
			console.log(e.stack);
		}
	}
	
	function sendToEUMServer(dataObject, forceSynchronous) {
		// maybe append browser information?
		var xhrPost = new XMLHttpRequest();
		xhrPost.open("POST", settings["eumManagementServer"], !forceSynchronous);
		xhrPost.setRequestHeader("Content-Type", "application/json");
		xhrPost.send(JSON.stringify(dataObject));
	}
	
	return {
		timestamp : getCurrentTimeStamp,
		os : getOS,
		browserinfo : getBrowserInformation,
		printTrace : printTrace,
		callback : sendToEUMServer
	}
})();

// execute the injection
inspectIT_eum();