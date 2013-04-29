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


function getType(value) {

	var pattern=/[.]attr[(]/;
	var getAttrPattern=/[.]getAttribute[(]/;


	
	if(typeof(value) == 'object') {
		if(value instanceof Array) {
				if(value[0] instanceof Array){
					
					if(value[0].length > 0) 
						return new Array(typeof (value[0][0]) + '_array');
				
					else
						return new Array('object_array');
				}
				else
					if(value.length > 0)
						return new Array(typeof (value[0]) + '_array');
					else 
						return new Array('object_array');
		}
		else
			return new Array('object');
	
	} else if(typeof(value) != 'undefined' && typeof(value) != 'function') {
		return new Array(typeof(value));
	}
		
	return new Array(typeof(value));
	
	
	
}


function getXpathOfNodes(element){
	
	var path="";
	var xpaths=new Array();
	for(var i=0;i<$(element).length;i++){
		path=getElementXPath($(element).get(i));
		xpaths.push("$(document.evaluate" + "(" + path +", document, null, XPathResult.ANY_TYPE,null).iterateNext())");
		
	}
	return xpaths;
	
}

var getElementXPath = function(element) {
    
	return getElementTreeXPath(element);
};

var getElementTreeXPath = function(element) {
    var paths = [];

    // Use nodeName (instead of localName) so namespace prefix is included (if any).
    for (; element && element.nodeType == 1; element = element.parentNode)  {
        var index = 0;
        

        if(element.nodeName.toLowerCase()=="body"){
          
        	return paths.length ? "//" + paths.join("/") : null;
        }
        
        for (var sibling = element.previousSibling; sibling; sibling = sibling.previousSibling) {
            // Ignore document type declaration.
            if (sibling.nodeType == Node.DOCUMENT_TYPE_NODE)
              continue;

            if (sibling.nodeName == element.nodeName)
                ++index;
        }

        var tagName = element.nodeName.toLowerCase();
        var pathIndex = (index ? "[" + (index+1) + "]" : "");
 /*     var nodes=getAllAttrs(element);
        var nodeAttrs="";
        for(var i=0;i<nodes.length;i++){
        	nodeAttrs+=","+nodes[i];
        }
 */       
        paths.splice(0, 0, tagName + pathIndex);//+nodeAttrs);
    }
    return paths.length ? "/" + paths.join("/") : null;

};
