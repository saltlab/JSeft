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

var deletedElemOrChangedAttr=false;
function deleteElement(xpath){
	if(deletedElemOrChangedAttr==false){
		evaluated=document.evaluate(xpath, document, null, XPathResult.ANY_TYPE,null);
		thisNode = $(evaluated.iterateNext());
		if(thisNode.length>0){
			thisNode.remove();
			deletedElemOrChangedAttr=true;
		}
	}
}

function changeCssAttr(xpath, cssProp){
	if(deletedElemOrChangedAttr==false){
		evaluated=document.evaluate(xpath, document, null, XPathResult.ANY_TYPE,null);
		thisNode = $(evaluated.iterateNext());
		if(thisNode.length>0){
			if(thisNode.css(cssProp)!=null && thisNode.css(cssProp)!=''){
				thisNode.css(cssProp,'');
				deletedElemOrChangedAttr=true;
			}
		}
		
	}
}

function changeAttr(xpath, attrType){
	if(deletedElemOrChangedAttr==false){
		evaluated=document.evaluate(xpath, document, null, XPathResult.ANY_TYPE,null);
		thisNode = $(evaluated.iterateNext());
		if(thisNode.length>0){
			if(thisNode.attr(attrType)!=undefined){
				attribute=thisNode.attr(attrType);
				thisNode.removeClass(attribute);
				deletedElemOrChangedAttr=true;
			}
		}
		
	}
}