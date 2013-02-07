window.xhr = new XMLHttpRequest();
window.buffer = new Array();

function send(value) {
	window.buffer.push(value);
	if(window.buffer.length == 200) {
		sendReally();	
	}
}

function sendReally() {
	window.xhr.open('POST', document.location.href + '?thisisadomtracingcall', false);
	window.xhr.send(JSON.stringify(window.buffer));
	window.buffer = new Array();
}
function AddDomNodeProps(element,value){
	var datas = new Array();

	for(i=0;i<$(element).length;i++)
	datas.push({id:$($(element).get(i)).prop('id'), className: $($(element).get(i)).prop('className')
		, tagName: $($(element).get(i)).prop('tagName')});
	
	return new Array(element, value);

};