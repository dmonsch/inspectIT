
// INSTRUMENTATION FOR TIMER (setTimeout etc.)
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
	
	inspectIT.plugins.asnyc = {
		init : instrumentTimers
	};
})();