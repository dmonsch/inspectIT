
// LISTENER MODULE
inspectIT.listener = (function () {
	var instrumentedEvents = {
		"click" : true,
		"onchange" : true,
		// "scroll" : true,
		"onmouseover" : true,
		"onmouseout" : true,
		"onkeydown" : true,
		"onkeyup" : true,
		"onkeypress" : true
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
		var commonListeners = null;
		if (typeof EventTarget !== "undefined") {
			commonListeners = [
			    [Element, true],
			    [EventTarget, true]
			];
		} else {
			// for IE eventtarget is undefined
			commonListeners = [
			    [Element, true]
			];
		}
		
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
	
	inspectIT.plugins.listener = {
		init : instrumentListener,
		domready : instrumentDocumentListener
	}
})();