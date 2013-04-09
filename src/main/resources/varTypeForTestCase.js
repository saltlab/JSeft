function getType(value, name) {

	var pattern=/[.]attr[(]/;
	var getAttrPattern=/[.]getAttribute[(]/;

	if(typeof(value) == 'object') {
		if(value instanceof Array) {
				if(value[0] instanceof Array){
					
					if(value[0].length > 0) 
						return (typeof (value[0][0]));
				
					else
						return ('object_array');
				}
				else
					if(value.length > 0)
						return (typeof (value[0]) + '_array');
					else 
						return ('object_array');
		}
		else
			return ('object');
	
	} 
	else if(typeof(value) != 'undefined' && typeof(value) != 'function') {
		return (typeof(value));
	}
		
	else if (pattern.test(name) || getAttrPattern.test(name)){
			return ('string');
	}
	else if (name.match(pattern)==".attr("){
		return ('string');
	}
	return (typeof(value));
};
