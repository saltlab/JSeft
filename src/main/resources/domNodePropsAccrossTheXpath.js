function getAllAttrs(element){
	var nodes=[];
	for (var attr, i=0, attrs=element.attributes, l=attrs.length; i<l; i++){
	    attr = attrs.item(i);
	    nodes.push(attr.nodeName + ":" + attr.nodeValue);
	  
	}
	return nodes;
}

var getElementXPath = function(element) {
    
	return getElementTreeXPath(element);
};

var getElementTreeXPath = function(element) {
    var paths = [];

    // Use nodeName (instead of localName) so namespace prefix is included (if any).
    for (; element && element.nodeType == 1; element = element.parentNode)  {
        var index = 0;
        

        for (var sibling = element.previousSibling; sibling; sibling = sibling.previousSibling) {
            // Ignore document type declaration.
            if (sibling.nodeType == Node.DOCUMENT_TYPE_NODE)
              continue;

            if (sibling.nodeName == element.nodeName)
                ++index;
        }

        var tagName = element.nodeName.toLowerCase();
        var pathIndex = (index ? "[" + (index+1) + "]" : "");
        var nodes=getAllAttrs(element);
        var nodeAttrs="";
        for(var i=0;i<nodes.length;i++){
        	nodeAttrs+=","+nodes[i];
        }
        nodeAttrs=nodeAttrs.slice(0,nodeAttrs.length-1);
        paths.splice(0, 0, tagName + pathIndex+nodeAttrs);
    }
    return paths.length ? "/" + paths.join("/") : null;

}
