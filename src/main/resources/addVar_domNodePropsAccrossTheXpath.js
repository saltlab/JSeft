
window.xhr = new XMLHttpRequest();
window.buffer = new Array();
var stmCovgArray=new Array();
var brnCovgArray={};

function send(value) {
	window.buffer.push(value);
	if(window.buffer.length == 100) {
		sendReally();	
	}
}
function includeFunctions(key, value) {
	if (typeof(value) === 'function') {
		return value.toString();
	}
	return value;
}

function sendReally() {
	window.xhr.open('POST', document.location.href + '?thisisajsdomexecutiontracingcall', false);
	window.xhr.send(JSON.stringify(JSON.decycle(window.buffer),includeFunctions));
	window.buffer = new Array();
			

}

function addVariable(name, value, variableUsage) {
	var date = new Date();
	time=date.getTime();
	var pattern=/[.]attr[(]/;
	var getAttrPattern=/[.]getAttribute[(]/;
	var xpaths=new Array();

	var newValue;
	
	
	if(value==null){
		
		return new Array(name, typeof(value), null, time, variableUsage);
	}
	if(typeof value == "object" && "nodeType" in value &&
			   value.nodeType === 1 && value.cloneNode){
		var dojoXpath;
		xpaths=getXpathOfNodes($(value).clone());
		if(xpaths.length==1){
			var oneXpath=xpaths[0];
		
			return new Array(name, 'xpath', oneXpath, time, variableUsage);
		}
		
			return new Array(name, 'xpath', xpaths, time, variableUsage);
		
	}


	if(typeof(value) == 'object') {


		if(value instanceof Array) {
			
			
				if(value[0] instanceof Array){
					
					if(value[0].length > 0){
						newValue=$.extend(true,[],value);
				
						return new Array(name, typeof (newValue[0][0]) + '_array', newValue, time, variableUsage);
					}
					else{
						newValue=$.extend(true,[],value);
						return new Array(name, 'object_array', newValue, time, variableUsage);
					}
				}
				else
					if(value.length > 0){
						
						
						newValue=$.extend(true,[],value);
						var dojoValue;
					
						return new Array(name, typeof (newValue[0]) + '_array', newValue, time, variableUsage);
					}
					else {
						newValue=$.extend(true,[],value);
						var dojoValue;
						return new Array(name, 'object_array', newValue, time, variableUsage);
					}
		}
		else{
			
			newValue=$.extend(true,{},value);

			return new Array(name, 'object', newValue, time, variableUsage);
		}
	
	} else if(typeof(value) != 'undefined' && typeof(value) != 'function') {
		return new Array(name, typeof(value), value, time, variableUsage);
	}
	
	else if(typeof(value)=='function'){
		
		var funcName=functionName(value)
		return new Array(funcName, typeof(value), funcName, time, variableUsage);
		
	}
	else if (pattern.test(name) || getAttrPattern.test(name)){
		return new Array(name, 'string', value, time, variableUsage);//'java.lang.String');
	}
	else if (name.match(pattern)==".attr("){
		return new Array(name, 'string', 'java.lang.String', time, variableUsage);
	}
	
	return new Array(name, typeof(value), 'undefined', time, variableUsage);
}


function getXpathOfNodes(element){
	
	var path="";
	var xpaths=new Array();
	for(var i=0;i<$(element).length;i++){
		path=getElementXPath($(element).get(i));
		xpaths.push("$(document.evaluate" + "(" + "\"" + path + "\"" +", document, null, XPathResult.ANY_TYPE,null).iterateNext())");
		
	}
	return xpaths;
	
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
    if (element && element.id)
        return "//"+element.tagName.toLowerCase() + "[@id=" + "'"+ element.id + "'" + "]";
    
    else

    	return getElementTreeXPath(element);
};

var getElementTreeXPath = function(element) {
    var paths = [];

    // Use nodeName (instead of localName) so namespace prefix is included (if any).
    for (; element && element.nodeType == 1; element = element.parentNode)  {
        var index = 0;
        
        if (element && element.id) {
            paths.splice(0, 0, "/" + element.tagName.toLowerCase() + "[@id=" + "'"+ element.id + "'" + "]");
            break;
        }

  /*      if(element.nodeName.toLowerCase()=="body"){
          
        	return paths.length ? "//" + paths.join("/") : null;
        }
        
   */     for (var sibling = element.previousSibling; sibling; sibling = sibling.previousSibling) {
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

}

function AddDomNodeProps(elementArray){
	var date = new Date();
	time=date.getTime();
	var datas = new Array();
	var path;
	
	while(elementArray.length>0){
		element=elementArray.pop();
		
		for(var i=0;i<$(element).length;i++){
			path=getElementXPath($(element).get(i));
	        nodes=getAllAttrs($(element).get(i));
	        nodeAttrs="";
	        for(var j=0;j<nodes.length;j++){
	        	nodeAttrs+=nodes[j] + ":::";
	        }
	        nodeAttrs=nodeAttrs.slice(0,-3);//trim the last comma
	        datas.push({
	        	id:$($(element).get(i)).prop('id'), 
	        	className: $($(element).get(i)).prop('className'),
	        	tagName: $($(element).get(i)).prop('tagName'),
	        	attributes:nodeAttrs,
	        	selector: $(element).selector,
	        	xpath: path
	        });
		}
	}
	
	
	return new Array(datas);

}

function stripScripts(html) {

	
    var div = document.createElement('div');
    div.innerHTML = html;
    var scripts = div.getElementsByTagName('script');
    var i = scripts.length;
    while (i--) {
    	scripts[i].parentNode.removeChild(scripts[i]);
    }

    return div.innerHTML.replace(/(\r\n|\n|\r|\t)/gm,"");
 
	
 }

function pushIfItDoesNotExist(domNode,instrumentationArray){
	found=jQuery.inArray(domNode,instrumentationArray)
	if(found==-1){
		instrumentationArray.push(domNode);
	}
	else{
		instrumentationArray[found]=domNode;
	}
}

/*Array.prototype.clone = function() {

	return $.extend(true,[],this);
	
    var arr = new Array();
    arr=this.slice(0);
  
    for( var i = 0; i < this.length; i++ ) {
        if( this[i].clone ) {
            //recursion
            arr[i] = this[i].clone();
        }
    }
    
    return arr;

}

*/
function functionName(fun) {
	  var ret = fun.toString();
	  ret = ret.substr('function '.length);
	  ret = ret.substr(0, ret.indexOf('('));
	  return ret;
}

function initializeBranchCovgArray(functionName){
//	$.each(brnCovgArray, function(index){brnCovgArray[index]=-1;});
	$.each(brnCovgArray, 
			function(index){
				if (index.substring(0,functionName.length+1)==(functionName+"_"))
					delete brnCovgArray[index];
			}
	);
	
}

function detectCoveredBranch(currCondition, funcName_lineNo){
	if(brnCovgArray[funcName_lineNo]==1 || brnCovgArray[funcName_lineNo]==0)
		return currCondition;
	if(currCondition==true){
		brnCovgArray[funcName_lineNo]=1;
		
	}
	else{
		brnCovgArray[funcName_lineNo]=0;
		
	}
	return currCondition;
}

function adjustBranchCovgAfterFuncCall(calleeName, callerName){
	
	$.each(brnCovgArray, 
			function(index){
				if (index.substring(0,calleeName.length+1)==(calleeName+"_")){
					lineNo=index.split("_")[1];
					if(brnCovgArray[callerName+"_"+ lineNo]!=1 && brnCovgArray[callerName+"_"+ lineNo]!=0)
						brnCovgArray[callerName+"_"+ lineNo]=brnCovgArray[index];
				}
					
			}
	);
	
}

function getFunctionBrnCovgArray(funcName){
	var tempBrnCovgArray={};
	$.each(brnCovgArray, 
			function(index){
				if (index.substring(0,funcName.length+1)==(funcName+"_")){
					
					tempBrnCovgArray[index]=brnCovgArray[index];
				}
					
			}
	);

	return tempBrnCovgArray;
}
