window.xhr = new XMLHttpRequest();
window.buffer = new Array();

function send(value) {
	window.buffer.push(value);
	if(window.buffer.length == 200) {
		sendReally();	
	}
}

function sendReally() {
	window.xhr.open('POST', document.location.href + '?thisisajsdomexecutiontracingcall', false);
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
}


function getAllAttrs(element){
	var nodes=[];
	for (var attr, i=0, attrs=element.attributes, l=attrs.length; i<l; i++){
	    attr = attrs.item(i);
	    nodes.push(attr.nodeName + "::" + attr.nodeValue);
	  
	}
//	nodes.push("text" + "::" + $(element).text());
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
        

        if(element.nodeName.toLowerCase()=="body")
           break;
        
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
        
        paths.splice(0, 0, tagName + pathIndex+nodeAttrs);
    }
    return paths.length ? "/" + paths.join("/") : null;

}

function AddDomNodeProps(element,value,name){
	var date = new Date();
	time=date.getTime();
	var datas = new Array();
	
	for(i=0;i<$(element).length;i++){
		path=getElementXPath($(element).get(i));
	
	datas.push({
		id:$($(element).get(i)).prop('id'), 
		className: $($(element).get(i)).prop('className'),
		tagName: $($(element).get(i)).prop('tagName'),
		selector: $(element).selector,
		xpath: path
		});
	}
	
	return new Array("DOM",datas, value, name, time);

}

function stripScripts(html) {

    var div = document.createElement('div');
    div.innerHTML = html;
    var scripts = div.getElementsByTagName('script');
    var i = scripts.length;
    while (i--) {
    	scripts[i].parentNode.removeChild(scripts[i]);
    }

    return div.innerHTML.replace(/(\r\n|\n|\r)/gm,"");
 };
