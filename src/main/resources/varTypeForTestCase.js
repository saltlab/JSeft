function areEqualArrays(array1, array2) {
	
	   var temp = new Array();
	   var tempArray = new Array(array2.length);
	   var count = 0;
	 /*the corresponding variable has not been assigned yet
        but later it will be assigned, so for now ignore the violation*/
	    if (array1==null) 
		   return true;
	    if ((array1 instanceof Array) && (array2 instanceof Array)){
		   if ((array1.length==0) && (array2.length==0))
			   return true;
		   if (array1[0] instanceof Array){
			   
	/*			if((!array1[0][0]) || (!array2[0][0])) {
			   
					return false;
				}
	*/		   for (var i=0; i<array1.length; i++){

				   for (var j=0; j<array1[i].length; j++){
			
					   tempArray[count] = array1[i][j];
					  
					   count++;
				  
				   }
			   }
	 	
			   for(var i = 0; i < tempArray.length; i++) {
				   key = (typeof tempArray[i]) + "~" + tempArray[i];
				   
				   if(temp[key]) {
					temp[key]++;
				   } else {
					temp[key] = 1;
				   }
				
				  
			   }
			   
			   for(var i = 0; i < array2.length; i++) {
				   key = (typeof array2[i]) + "~" + array2[i];
				 
				   if(temp[key]) {
					   
					   if(temp[key] == 0) {
						
						   return false;
					   } else {
						   temp[key]--;
					   }
				   } else {
					
					   return false;
				   }
			   }
		
			   return true;	   
		   
		   }
		   else{
		
	/*		   if((!array1[0]) || (!array2[0])) {
	
				   return false;
			   }

	*/		   if(array1.length != array2.length) {
	
				   return false;
			   }
			   for(var i = 0; i < array1.length; i++) {
				   key = (typeof array1[i]) + "~" + array1[i];
				   if(temp[key]) {
					   temp[key]++;
				   } else {
					   temp[key] = 1;
				   }
			   }
	   
			   for(var i = 0; i < array2.length; i++) {
				   key = (typeof array2[i]) + "~" + array2[i];
				   if(temp[key]) {
					   if(temp[key] == 0) {
	        
						   return false;
					   } else {
						   temp[key]--;
					   }
				   } else {
	    	  
					   return false;
				   }
			   }
  
			   return true;
		   }
	   }
	   else
		   if ((array1 instanceof Array) && (array1.length>0)){
//			   if ((typeof(array2))=="string"){
				   for (var i=0; i<array1.length; i++){
					   if (array1[i]!=array2)
						   return false;
					   
				   }
				   return true;
	   
//			   }
/*			   else
				   if ((typeof(array2))=="number"){
					   for (var i=0; i<array1.length; i++){
						   if (array1[i]!=array2)
							   return false;
						   
					   }
					   return true;
				   
				   }
*/		   }
	   return false;
}


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
