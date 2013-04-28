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


function deleteElement(xpath){

	evaluated=document.evaluate(xpath, document, null, XPathResult.ANY_TYPE,null);
	thisNode = $(evaluated.iterateNext());
	if(thisNode.length>0){
		thisNode.remove();
//		deletedElemOrChangedAttr=true;
	}
	
}

function changeCssAttr(xpath, cssProp){

	evaluated=document.evaluate(xpath, document, null, XPathResult.ANY_TYPE,null);
	thisNode = $(evaluated.iterateNext());
	if(thisNode.length>0){
		if(thisNode.css(cssProp)!=null && thisNode.css(cssProp)!=''){
			thisNode.css(cssProp,'');
//			deletedElemOrChangedAttr=true;
		}
	}
		
	
}

function changeAttr(xpath, attrType){

	evaluated=document.evaluate(xpath, document, null, XPathResult.ANY_TYPE,null);
	thisNode = $(evaluated.iterateNext());
	if(thisNode.length>0){
		if(thisNode.attr(attrType)!=undefined){
			
			if(attrType=='class'){
				attribute=thisNode.attr('class');
				thisNode.removeClass("'" + attribute + "'");
			}
/*			attribute=thisNode.attr(attrType);
			if(thisNode.hasClass("'"+attribute + "'")){
				thisNode.removeClass("'" + attribute + "'");
			}

*/			else{
				thisNode.attr(attrType,'');
				
			}
			

		}
	}
		
	
}


function changeProp(xpath, propType){

	evaluated=document.evaluate(xpath, document, null, XPathResult.ANY_TYPE,null);
	thisNode = $(evaluated.iterateNext());
	if(thisNode.length>0){
		if(thisNode.prop(propType)!=undefined){
//			attribute=thisNode.attr(attrType);
			thisNode.prop(propType,'');

		}
	}
		
	
}


function changeAddClass(xpath, className){

	evaluated=document.evaluate(xpath, document, null, XPathResult.ANY_TYPE,null);
	thisNode = $(evaluated.iterateNext());
	if(thisNode.length>0){
		if(thisNode.hasClass(className)==true){
		
			thisNode.removeClass(className);
//			deletedElemOrChangedAttr=true;	
		}
	}
		
	
}

function changeRemoveClass(xpath, className){

	evaluated=document.evaluate(xpath, document, null, XPathResult.ANY_TYPE,null);
	thisNode = $(evaluated.iterateNext());
	if(thisNode.length>0){
		if(thisNode.hasClass(className)==false){
		
			thisNode.addClass(className);
//			deletedElemOrChangedAttr=true;	
		}
	}
		
	
}

function changeWidth(xpath){

	evaluated=document.evaluate(xpath, document, null, XPathResult.ANY_TYPE,null);
	thisNode = $(evaluated.iterateNext());
	if(thisNode.length>0){

		thisNode.width(0);
		
	}
		
	
}

function changeHeight(xpath){

	evaluated=document.evaluate(xpath, document, null, XPathResult.ANY_TYPE,null);
	thisNode = $(evaluated.iterateNext());
	if(thisNode.length>0){

		thisNode.height(0);
		
	}
		
	
}

