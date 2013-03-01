window.xhr = new XMLHttpRequest();
window.buffer = new Array();

function send(value) {
	window.buffer.push(value);
	if(window.buffer.length == 200) {
		sendReally();	
	}
}

function sendReally() {
	window.xhr.open('POST', document.location.href + '?thisisanexecutiontracingcall', false);
	window.xhr.send(JSON.stringify(window.buffer));
	window.buffer = new Array();
}

function addVariable(name, value, variableUsage) {
	var date = new Date();
	time=date.getTime();
	var pattern=/[.]attr[(]/;
	var getAttrPattern=/[.]getAttribute[(]/;

	if(typeof(value) == 'object') {
		if(value instanceof Array) {
				if(value[0] instanceof Array){
					
					if(value[0].length > 0) 
						return new Array(name, typeof (value[0][0]) + '_array', value, time, variableUsage);
				
					else
						return new Array(name, 'object_array', value, time, variableUsage);
				}
				else
					if(value.length > 0)
						return new Array(name, typeof (value[0]) + '_array', value, time, variableUsage);
					else 
						return new Array(name, 'object_array', value, time, variableUsage);
		}
		else
			return new Array(name, 'object', value, time, variableUsage);
	
	} else if(typeof(value) != 'undefined' && typeof(value) != 'function') {
		return new Array(name, typeof(value), value, time, variableUsage);
	}
		else if (pattern.test(name) || getAttrPattern.test(name)){
			return new Array(name, 'string', value, time, variableUsage);//'java.lang.String');
		}
	else if (name.match(pattern)==".attr("){
		return new Array(name, 'string', 'java.lang.String', time, variableUsage);
	}
	return new Array(name, typeof(value), 'undefined', time, variableUsage);
};


