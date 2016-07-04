// TIMINGS MODULE
inspectIT.timings = (function () {
	var rum_speedindex = null;
	var navTimingBlock = -1;
	
	function collectNavigationTimings() {
		if (("performance" in window) && ("timing" in window.performance)) {
			navTimingBlock = inspectIT.action.enterChild();
		}
	}
	
	function sendTimings() {
		if (("performance" in window) && ("timing" in window.performance)) {
			// try to get speedindex
			if (typeof RUMSpeedIndex !== "undefined") {
				rum_speedindex = RUMSpeedIndex();
			}
			
			// get nav timings
			var objCallback = {
					type : "PageLoadRequest"
			}
			for (var key in window.performance.timing) {
				// this is really sad but otherwise toJSON doesn't work in all browsers
				objCallback[String(key) + "W"] = window.performance.timing[key];
			}
			objCallback["url"] = document.location.href;
			
			// add rum speedindex if possible
			if (rum_speedindex !== null && rum_speedindex["speedindex"] !== null && rum_speedindex["firstpaint"] !== null) {
				objCallback["speedindex"] = rum_speedindex["si"];
				objCallback["firstpaint"] = rum_speedindex["fp"];
			}
		
			inspectIT.action.submitData(navTimingBlock, objCallback);
			inspectIT.action.leaveChild(navTimingBlock);
		}
	}
	
	inspectIT.plugins.navTimings = {
		init : collectNavigationTimings,
		onload : sendTimings
	};
})();