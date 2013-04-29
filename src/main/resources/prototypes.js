Number.max = function (a,b) {
    return a<b?b:a;
}

Number.min = function (a,b) {
    return a>b?b:a;
}

Math.mod = function(val,mod) {
    if (val < 0) {
        while(val<0) val += mod;
        return val;
    } else {
        return val%mod;
    }
}

window.getInnerWidth = function() {
    if (window.innerWidth) {
        return window.innerWidth;
    } else if (document.body.clientWidth) {
        return document.body.clientWidth;
    } else if (document.documentElement.clientWidth) {
        return document.documentElement.clientWidth;
    }
} 

window.getInnerHeight = function() {
    if (window.innerHeight) {
        return window.innerHeight;
    } else if (document.body.clientHeight) {
        return document.body.clientHeight;
    } else if (document.documentElement.clientHeight) {
        return document.documentElement.clientHeight;
    }
} 

String.prototype.chop = function() {
    if (this.length == 0) return "";
    return this.substring(0,this.length-1);
}

String.prototype.endsWith = function(str) {
    return (this.length-str.length)==this.lastIndexOf(str);
}

String.prototype.reverse = function() {
    var s = "";
    var i = this.length;
    while (i>0) {
        s += this.substring(i-1,i);
        i--;
    }
    return s;
}

// this trim was suggested by Tobias Hinnerup
String.prototype.trim = function() {
    return(this.replace(/^\s+/,'').replace(/\s+$/,''));
}

String.prototype.toInt = function() {
    var a = new Array();
    for (var i = 0; i < this.length; i++) {
        a[i] = this.charCodeAt(i);
    }
    return a;
}

Array.prototype.intArrayToString = function() {
    var a = new String();
    for (var i = 0; i < this.length; i++) {
        if(typeof this[i] != "number") {
            throw new Error("Array must be all numbers");
        } else if (this[i] < 0) {
            throw new Error("Numbers must be 0 and up");
        }
        a += String.fromCharCode(this[i]);
    }
    return a;    
}

Array.prototype.compareArrays = function(arr) {
    if (this.length != arr.length) return false;
    for (var i = 0; i < arr.length; i++) {
        if (this[i].compareArrays) { //likely nested array
            if (!this[i].compareArrays(arr[i])) return false;
            else continue;
        }
        if (this[i] != arr[i]) return false;
    }
    return true;
}

Array.prototype.map = function(fnc) {
    var a = new Array(this.length);
    for (var i = 0; i < this.length; i++) {
        a[i] = fnc(this[i]);
    }
    return a;
}

Array.prototype.foldr = function(fnc,start) {
    var a = start;
    for (var i = this.length-1; i > -1; i--) {
        a = fnc(this[i],a);
    }
    return a;
}

Array.prototype.foldl = function(fnc,start) {
    var a = start;
    for (var i = 0; i < this.length; i++) {
        a = fnc(this[i],a);
    }
    return a;
}

Array.prototype.exists = function (x) {
    for (var i = 0; i < this.length; i++) {
        if (this[i] == x) return true;
    }
    return false;
}

Array.prototype.filter = function(fnc) {
    var a = new Array();
    for (var i = 0; i < this.length; i++) {
        if (fnc(this[i])) {
            a.push(this[i]);
        }
    }
    return a;
}

Array.prototype.random = function() {
    return this[Math.floor((Math.random()*this.length))];
}