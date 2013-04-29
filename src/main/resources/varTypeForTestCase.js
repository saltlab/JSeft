function areEqualArrays(array1, array2) {
	
	  
	   return array1.compareArrays(array2);
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
