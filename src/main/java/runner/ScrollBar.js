////////////////////////////////////////////////////////////////////////////////
// Adult Swim Back Channel Scrollbar
////////////////////////////////////////////////////////////////////////////////

//------------------------------------------------
// Constructor
//------------------------------------------------
JOINT.ScrollBar = function(selector, index) {
	
	var _public = {
		
		//------------------------------------------------
		// Init game
		//------------------------------------------------
		init:function() {
			_private.selector = selector;
			_private.setup();
		},
		
		//------------------------------------------------
		// Update scrollbar position and size
		//------------------------------------------------
		update:function() {
			if (!_private.active) {
				$(_private.selector).find(_private.HANDLE).unbind("mousedown");
				$(_private.selector).find(_private.HANDLE).unbind("mouseup");
				$(_private.selector).find(_private.HANDLE).unbind("click");
				_private.createListeners();
				_private.setDefaults();
			}
			_private.height = $(_private.selector).find(_private.TRACK).height();
			//console.log(_private.height);
			_private.setDefaults();
			var percentage = $(_private.selector).scrollTop() / ($(_private.selector).children(":first").outerHeight() - $(_private.selector).height());
			var newPos = ($(_private.selector).find(_private.TRACK).height() - $(_private.selector).find(_private.HANDLE).height()) * percentage;
			$(_private.selector).find(_private.HANDLE).css("top", newPos);
		}
	};
	
	var _private = {
		
		//------------------------------------------------
		// Variables
		//------------------------------------------------
		HANDLE: ".scrollbar a",
		TRACK: ".scrollbar",
		MIN_HANDLE_HEIGHT: 50,
		selector: "",
		height: 0,
		mouseY: 0,
		mouseBarOffset: 0,
		lastPercentage: 0,
		active: false,
		
		//------------------------------------------------
		// Set up game
		//------------------------------------------------
		setup:function() {
			this.height = $(this.selector).find(this.TRACK).height();
			if ($(this.selector).children(":first").outerHeight(true) > this.height) {
				//$(this.selector).find(this.HANDLE).height(this.height);
				this.createListeners();
				this.setDefaults();
			} else {
				//this.active = false;
				$(this.selector).find(this.HANDLE).height(this.height);
				$(this.selector).find(this.HANDLE).bind("mousedown", function(e) {_private.preventDefaultAction(e);});
				$(this.selector).find(this.HANDLE).bind("mouseup", function(e) {_private.preventDefaultAction(e);});
				$(this.selector).find(this.HANDLE).bind("click", function(e) {_private.preventDefaultAction(e);});
			}
		},
		
		//------------------------------------------------
		// Prevent browser defaults
		//------------------------------------------------
		preventDefaultAction:function(e) {
			e.preventDefault();
			return false;
		},
		
		//------------------------------------------------
		// Prepare scrollbar
		//------------------------------------------------
		setDefaults:function() {
			$(this.selector).find(this.HANDLE).css("top", 0);
			//console.log(this.height);
			var percentage = this.height / $(this.selector).children(":first").outerHeight(true);
			var scrollBarHeight = Math.floor(this.height * percentage);
			if (scrollBarHeight < this.MIN_HANDLE_HEIGHT) {scrollBarHeight = this.MIN_HANDLE_HEIGHT;}
			if (scrollBarHeight > $(this.selector).find(this.TRACK).height()) {scrollBarHeight=this.height}
			$(this.selector).find(this.HANDLE).height(scrollBarHeight);
		},
		
		//------------------------------------------------
		// Listen for mouse events
		//------------------------------------------------
		createListeners:function() {
			this.active = true;
			$(this.selector).find(this.HANDLE).bind("mousedown", function(e) {_private.onScrollBarPressed(e);});
			$(this.selector).find(this.HANDLE).bind("mouseup", function(e) {_private.preventDefaultAction(e);});
			$(this.selector).find(this.HANDLE).bind("click", function(e) {_private.preventDefaultAction(e);});
			$(this.selector).bind("mousewheel", function(e, delta) {_private.onScrollWheelUsed(e, delta);});
			$(this.selector).find(this.TRACK).bind("mousedown", function(e) {_private.onScrollBarMouseDown(e);});
			$(this.selector).find(this.HANDLE).bind("touchstart", function(e) {_private.onTouchStart(e);});
		},
		
		//------------------------------------------------
		// Reposition Content
		//------------------------------------------------
		repositionContent:function(percentage) {
			this.lastPercentage = percentage;
			var targetY = ($(this.selector).children(":first").outerHeight(true) - this.height) * percentage;
			$(this.selector).scrollTop(targetY);
		},

		//------------------------------------------------
		// Jump content to click position
		//------------------------------------------------
		onScrollBarMouseDown:function(e) {
			this.mouseY = (e.pageY - $(this.selector).find(this.TRACK).offset().top) - ($(this.selector).find(this.HANDLE).height()/2);
			var newPos = this.mouseY;
			if (newPos < 0) {
				newPos = 0;
			} else if (newPos > (this.height - $(this.selector).find(this.HANDLE).height())) {
				newPos = this.height - $(this.selector).find(this.HANDLE).height();
			}
			$(this.selector).find(this.HANDLE).css("top", newPos);
			var percentage = $(this.selector).find(this.HANDLE).position().top / (this.height - $(this.selector).find(this.HANDLE).height());
			this.repositionContent(percentage);
			e.preventDefault();
			return false;
		},
		
		//------------------------------------------------
		// Scroll content incrementally
		//------------------------------------------------
		onScrollWheelUsed:function(e, delta) {
			//console.log(this.HANDLE);
			var currentPercent = this.lastPercentage;
			var increment = $(this.selector).find(this.HANDLE).height() / 6;
			var newPos = (delta <= 0) ? $(this.selector).find(this.HANDLE).position().top + increment : $(this.selector).find(this.HANDLE).position().top - increment;
			if (newPos > (this.height-$(this.selector).find(this.HANDLE).height())) {
				newPos = this.height-$(this.selector).find(this.HANDLE).height();
			} else if (newPos < 0) {
				newPos = 0;
			}
			$(this.selector).find(this.HANDLE).css("top", newPos);
			var percentage = $(this.selector).find(this.HANDLE).position().top / (this.height - $(this.selector).find(this.HANDLE).height());
			this.repositionContent(percentage);
			e.preventDefault();
			return false;
		},
		
		//------------------------------------------------
		// Start scrolling content
		//------------------------------------------------
		onScrollBarPressed:function(e) {
			if ($(this.selector).children(":first").outerHeight(true) > this.height) {
				this.mouseY = e.pageY - $(this.selector).find(this.TRACK).offset().top;
				this.clickPos = ($(this.selector).find(this.HANDLE).offset().top - $(this.selector).find(this.TRACK).offset().top);
				this.mouseBarOffset = (e.pageY - ($(this.selector).find(this.TRACK).position().top)) - $(this.selector).find(this.HANDLE).position().top;
				this.repositionContent();
				$(document).bind("mousemove", function(e) {_private.onScrollBarDragged(e);});
				$(document).bind("mouseup", function(e) {_private.onScrollBarReleased(e);});
			}
			e.preventDefault();
			return false;
		},

		//------------------------------------------------
		// Start scrolling content
		//------------------------------------------------
		onTouchStart:function(e) {
			if ($(this.selector).children(":first").outerHeight(true) > this.height) {
				this.mouseY = e.originalEvent.changedTouches[0].pageY - $(this.selector).find(this.TRACK).offset().top;
				this.clickPos = ($(this.selector).find(this.HANDLE).offset().top - $(this.selector).find(this.TRACK).offset().top);
				this.mouseBarOffset = (e.originalEvent.changedTouches[0].pageY - ($(this.selector).find(this.TRACK).position().top)) - $(this.selector).find(this.HANDLE).position().top;
				this.repositionContent();
				$(window).bind("touchmove", function(e) {_private.onTouchMove(e);});
				$(window).bind("touchend", function(e) {_private.onTouchEnd(e);});
			}
			//e.preventDefault();
			//return false;
		},

		//------------------------------------------------
		// Set up game
		//------------------------------------------------
		onScrollBarReleased:function(e) {
			$(document).unbind("mousemove");
			$(document).unbind("mouseup");
			e.preventDefault();
			return false;
		},
		
		//------------------------------------------------
		// Set up game
		//------------------------------------------------
		onTouchEnd:function(e) {
			$(window).unbind("touchmove");
			$(window).unbind("touchup");
			e.preventDefault();
			return false;
		},
		
		//------------------------------------------------
		// Reposition content
		//------------------------------------------------
		onScrollBarDragged:function(e) {
			var newMouseY = e.pageY - ($(this.selector).find(this.TRACK).position().top);
			var scrollBarPos =  newMouseY - this.mouseBarOffset;
			if (scrollBarPos > (this.height-$(this.selector).find(this.HANDLE).height())) {
				scrollBarPos = this.height-$(this.selector).find(this.HANDLE).height();
			} else if (scrollBarPos < 0) {
				scrollBarPos = 0;
			}
			$(this.selector).find(this.HANDLE).css("top", scrollBarPos + "px");
			var percentage = scrollBarPos / (this.height - $(this.selector).find(this.HANDLE).height());
			this.repositionContent(percentage);
		},

		//------------------------------------------------
		// Reposition content
		//------------------------------------------------
		onTouchMove:function(e) {
			e.preventDefault();
			var newMouseY = e.originalEvent.changedTouches[0].pageY - ($(this.selector).find(this.TRACK).position().top);
			var scrollBarPos =  newMouseY - this.mouseBarOffset;
			if (scrollBarPos > (this.height-$(this.selector).find(this.HANDLE).height())) {
				scrollBarPos = this.height-$(this.selector).find(this.HANDLE).height();
			} else if (scrollBarPos < 0) {
				scrollBarPos = 0;
			}
			$(this.selector).find(this.HANDLE).css("top", scrollBarPos + "px");
			var percentage = scrollBarPos / (this.height - $(this.selector).find(this.HANDLE).height());
			this.repositionContent(percentage);
		}
	};
	_public.init();
	return _public;
};