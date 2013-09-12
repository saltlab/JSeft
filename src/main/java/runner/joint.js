var JOINT = (typeof JOINT === "undefined") ? {} : JOINT;

JOINT.MainView = function () {
	"use strict";
	// variables
	this.originalTitle = "";
	this.originalWordPath = "";
	this.originalWord = "";
	this.animating = false;
	this.browserHeight = 0;
	this.originalLocationPath = "";
	this.originalLocation = "";
	this.locationIndex = 0;
	this.usNavWord = "";
	this.usIndex = 0;
	this.originalUsNavPath = "";
	this.founderWord = "";
	this.founderIndex = 0;
	this.originalFounderPath = "";
	this.scrollBar = [];
	this.isiDevice = false;
	this.isiPhone = false;
	this.orientation = false;
	this.lastLocation = null;
	this.lastFounder = null;
	var scope = this;

	////////////////////////////////////////////////
	// CONTACT
	////////////////////////////////////////////////

	// contact clicked
	this.showLocations = function () {
		var scope = this;
		$('#contact').animate({'width': '100%'}, 500, function () {});
		$('#us').animate({'width': '0'}, 500, function () { $(this).css('display', 'none'); });
		$('#contact-header').animate({'width': '50%'}, 500);
		$('#locations-nav-container').css({'display': 'block'}).animate({'width': '50%'}, 500, function () { scope.animating = false; });
	};

	this.hideLocations = function (hash) {
		if (hash === undefined) {
			$('#contact-header span').removeClass('active').text("CONTACT");
		}
		var scope = this;
		$('#contact').animate({'width': '50%'}, 500, function () {});
		$('#us').css({'display': 'block'}).animate({'width': '50%'}, 500);
		$('#contact-header').animate({'width': '100%'}, 500);
		$('#locations-nav-container').animate({'width': '0'}, function () { $(this).css('display', 'none'); scope.animating = false; });
	};

	this.contactClicked = function (e, hash) {
		if (e === undefined) {
			if (this.animating === false) {
				this.animating = true;
				this.address.title(this.originalTitle);
				this.originalWordPath = hash.substring(3);
				if (this.originalWordPath === "contact") {
					this.originalWord = "CONTACT";
					this.showLocations();
					if (this.isiDevice) {
						$('#contact-header span').text("BACK");
						if (this.oldPath === "/london") {
							$('#locations-nav-container a').eq(0).text("LONDON");
						} else if (this.oldPath === "/newyork") {
							$('#locations-nav-container a').eq(1).text("NEW YORK");
						} else if (this.oldPath === "/shanghai") {
							$('#locations-nav-container a').eq(2).text("SHANGHAI");
						} else if (this.oldPath === "/saopaulo") {
							$('#locations-nav-container a').eq(3).text("SAO PAULO");
						}
					}
				}
			}
			$('#contact-header span').addClass('active');
		} else {
			if (this.animating === false) {
				this.animating = true;
				this.originalWord = $(e.currentTarget).text();
				this.address.title(this.originalTitle);
				if ($(e.currentTarget).hasClass('active')) {
					this.address.path('');
					$('#contact-header span').removeClass('active').text("CONTACT");
					this.hideLocations();
				} else {
					this.address.path('contact');
					$('#contact-header span').addClass('active').text("BACK");
					this.showLocations();
				}
			}
			e.preventDefault();
			return false;
		}
	};

	//location clicked
	this.showLocationsInfo = function () {
		$('#contact-header').animate({'width': '0'}, 500, function () { $(this).css('display', 'none'); });
		$('#locations-container').css({'display': 'block'}).animate({'width': '50%'}, 500);
	};

	this.hideLocationsInfo = function () {
		var scope = this;
		$('#locations-container').animate({'width': '0'}, 500, function () { $(this).css('display', 'none'); });
		$('#contact-header').css({'display': 'block'}).animate({'width': '50%'}, 500, function () {scope.animating = false; });
	};

	this.displayLocations = function () {
		if ($('#locations-container').css('display') === "none") {
			$('#locations-list').css({'top': -(this.locationIndex * this.browserHeight)});
			this.animating = false;
		} else if ($('#locations-container').css('display') === "block") {
			$('#locations-list').animate({'top': -(this.locationIndex * this.browserHeight)}, 500, function () {scope.animating = false; });
		}
	};

	this.locationClick = function (e) {
		if (this.lastLocation) {
			$('#locations-nav-container a').eq(this.locationIndex).text(this.lastLocation);
			this.lastLocation = null;
		}
		if ($(e.currentTarget).text() !== 'BACK') {
			this.originalLocation = $(e.currentTarget).text();
		}
		if ($(e.currentTarget).hasClass('active')) {
			$(e.currentTarget).text(this.originalLocation);
			$('#locations-nav-container a').removeClass('active');
			this.address.path('contact');
			this.hideLocationsInfo();
		} else {
			$('#locations-nav-container a').removeClass('active');
			this.address.path(this.removeSpace(this.originalLocation));
			$(e.currentTarget).addClass('active');
			$(e.currentTarget).text("BACK");
			this.locationIndex = ($('#locations-nav-container').find('a').index(e.currentTarget));
			this.displayLocations();
			this.showLocationsInfo();
		}
	};

	this.locationCheck = function () {
		if (this.oldPath === "/london") {
			$('#locations-nav-container a').eq(0).text("LONDON");
		} else if (this.oldPath === "/newyork") {
			$('#locations-nav-container a').eq(1).text("NEW YORK");
		} else if (this.oldPath === "/shanghai") {
			$('#locations-nav-container a').eq(2).text("SHANGHAI");
		} else if (this.oldPath === "/saopaulo") {
			$('#locations-nav-container a').eq(3).text("SAO PAULO");
		}
	};

	this.locationClicked = function (e, hash) {
		if (e === undefined) {
			if (this.animating === false) {
				this.animating = true;
				$('#locations-nav-container a').removeClass('active');
				this.originalLocationPath = hash.substring(3);
				if (this.originalLocationPath === "london") {
					this.originalLocation = "LONDON";
					this.locationIndex = 0;
					this.displayLocations();
					this.showLocationsInfo();
				} else if (this.originalLocationPath === "newyork") {
					this.originalLocation = "NEW YORK";
					this.locationIndex = 1;
					this.displayLocations();
					this.showLocationsInfo();
				} else if (this.originalLocationPath === "shanghai") {
					this.originalLocation = "SHANGHAI";
					this.locationIndex = 2;
					this.displayLocations();
					this.showLocationsInfo();
				} else if (this.originalLocationPath === "saopaulo") {
					this.originalLocation = "SAO PAULO";
					this.locationIndex = 3;
					this.displayLocations();
					this.showLocationsInfo();
				}
				if (this.isiDevice) {
					$('#contact-header span').text("BACK");
					this.lastLocation = this.originalLocation;
					$('#locations-nav-container a').eq(this.locationIndex).text('BACK');
					this.locationCheck();
				}
			}
			$('#locations-nav-container a').eq(this.locationIndex).addClass('active');
		} else {
			if (this.animating === false) {
				this.animating = true;
				if (this.isiDevice) {
					this.locationCheck();
					this.locationClick(e);
				} else {
					this.locationClick(e);
				}	
			}
			e.preventDefault();
			return false;
		}
	};

	////////////////////////////////////////////////
	// US
	///////////////////////////////////////////////

	//us clicked
	this.showUsNav = function () {
		var scope = this;
		$('#us').animate({'width': '100%'}, 500);
		$('#contact').animate({'width': '0'}, 500, function () { $(this).css('display', 'none'); scope.animating = false; });
		$('#us-header').animate({'width': '50%'}, 500);
		$('#us-nav').css({'display': 'block'}).animate({'width': '50%'}, 500);
	};

	this.hideUsNav = function (hash) {
		if (hash === undefined) {
			$('#us-header span').removeClass('active').text("US");
		}
		var scope = this;
		$('#us').animate({'width': '50%'}, 500, function () {});
		$('#contact').css({'display': 'block'}).animate({'width': '50%'}, 500);
		$('#us-header').animate({'width': '100%'}, 500);
		$('#us-nav').animate({'width': '0'}, 500, function () { $(this).css('display', 'none'); scope.animating = false; });
	};

	this.usClicked = function (e, hash) {
		if (e === undefined) {
			if (this.animating === false) {
				this.address.title("Joint London - About Us");
				this.animating = true;
				this.originalWordPath = hash.substring(3);
				if (this.originalWordPath === "us") {
					this.originalWord = "US";
					this.showUsNav();
					if (this.isiDevice) {
						$('#us-header span').text("BACK");
					}
				}
				$('#us-header span').addClass('active');
			}
		} else {
			if (this.animating === false) {
				this.animating = true;
				this.address.title("Joint London - About Us");
				this.originalWord = $(e.currentTarget).text();
				if ($(e.currentTarget).hasClass('active')) {
					this.address.path('');
					$('#us-header span').removeClass('active').text("US");
					this.hideUsNav();
				} else {
					this.address.path('us');
					$('#us-header span').addClass('active').text("BACK");
					this.showUsNav();
				}
			}
			e.preventDefault();
			return false;
		}
	};

	// us nav clicked
	this.showFoundersBlog = function () {
		$('#us-header').animate({'width': '0'}, 500, function () { $(this).css('display', 'none'); });
		$('#founders-nav').css({'display': 'block'}).animate({'width': '50%'}, 500);
	};

	this.hideFoundersBlog = function () {
		var scope = this;
		$('#founders-nav').animate({'width': '0'}, 500, function () { $(this).css('display', 'none'); scope.animating = false; });
		$('#us-header').css({'display': 'block'}).animate({'width': '50%'}, 500);
		if (this.isiDevice) {
			$('#us-nav a').eq(0).text('FOUNDERS');
		}
	};

	this.displayUsNav = function () {
		if ($('#founders-nav').css('display') === "none") {
			this.address.title("Joint London - " + this.changeToTitle(this.usNavWord));
			$('#founders-blog').css({'top': -(this.usIndex * this.browserHeight)});
			this.animating = false;
		} else if ($('#founders-nav').css('display') === "block") {
			this.address.title("Joint London - " + this.changeToTitle(this.usNavWord));
			$('#founders-blog').animate({'top': -(this.usIndex * this.browserHeight)}, 500, function () {scope.animating = false; });
		}
	};

	this.openBlog = function () {
		var blog = "http://jointlondon.tumblr.com";
		window.open(blog);
	};

	this.usNavClick = function (e) {
		if ($(e.currentTarget).text() !== 'BACK') {
			this.usNavWord = $(e.currentTarget).text();
		}
		if ($(e.currentTarget).hasClass('active')) {
			this.address.title("Joint London - About Us");
			$(e.currentTarget).text(this.usNavWord);
			this.address.path('us');
			$('#us-nav a').removeClass('active');
			this.hideFoundersBlog();
		} else {
			$('#us-nav a').removeClass('active');
			this.address.title("Joint London - Founders");
			this.address.path(this.usNavWord.toLowerCase());
			$(e.currentTarget).addClass('active');
			$(e.currentTarget).text("BACK");
			this.usIndex = $('#us-nav').find('a').index(e.currentTarget);
			this.displayUsNav(e);
			this.showFoundersBlog();
		}
	};

	this.usNavClicked = function (e, hash) {
		if (e === undefined) {
			if (this.animating === false) {
				this.animating = true;
				$('#us-nav a').removeClass("active");
				this.originalUsNavPath = hash.substring(3);
				if (this.originalUsNavPath === "founders") {
					this.usNavWord = "FOUNDERS";
					this.usIndex = 0;
					this.displayUsNav(e);
					this.showFoundersBlog();
					if (this.isiDevice) {
						$('#us-header span').text("BACK");
						$('#us-nav a').eq(0).text("BACK");
						if (this.oldPath === "/damoncollins") {
							$("#founders-blog a").eq(0).text("DAMON COLLINS");
						} else if (this.oldPath === "/richardexon") {
							$("#founders-blog a").eq(1).text("RICHARD EXON");
						} else if (this.oldPath === "/lorimeakin") {
							$("#founders-blog a").eq(2).text("LORI MEAKIN");
						} else if (this.oldPath === "/nikupton") {
							$("#founders-blog a").eq(3).text("NIK UPTON");
						}
					}
				} else if (this.originalUsNavPath === "blog") {
					this.usNavWord = "BLOG";
					this.usIndex = 1;
					this.displayUsNav(e);
					this.showFoundersBlog();
				}
				$('#us-nav a').eq(this.usIndex).addClass("active");
			}
		} else {
			if (this.animating === false) {
				this.animating = true;
				if (this.isiDevice) {
					this.usIndex = $('#us-nav').find('a').index(e.currentTarget);
					if (this.usIndex === 1) {
						this.openBlog();
						this.animating = false;
					} else {
						this.usNavClick(e);
					}
				} else {
					this.usNavClick(e);
				}
			}
			e.preventDefault();
			return false;
		}
	};

	//founder blog clicked
	this.showFoundersInfo = function () {
		var scope = this;
		$('#us-nav').animate({'width': '0'}, 500, function () { $(this).css('display', 'none'); });
		if ($('body').hasClass('medium') || $('body').hasClass('small')) {
			$('#founders-container').css({'display': 'block'}).animate({'width': '49.7%'}, 500, function () {scope.checkBrowserSize(); });
		} else {
			$('#founders-container').css({'display': 'block'}).animate({'width': '49.9%'}, 500, function () {scope.checkBrowserSize(); });
		}
	};

	this.hideFoundersInfo = function () {
		var scope = this;
		$('#founders-container').animate({'width': '0'}, 500, function () { $(this).css('display', 'none'); scope.animating = false; });
		$('#us-nav').css({'display': 'block'}).animate({'width': '50%'}, 500);
	};

	this.displayFounder = function () {
		var scope = this;
		if ($('#founders-container').css('display') === "none") {
			this.address.title("Joint London - " + this.changeToTitle(this.founderWord));
			$('#founders-container').show();
			$('#founders-list').css({'top': -(this.founderIndex * this.browserHeight)});
			$('.scrollbar').show();
			$('.scrollbar').css('z-index', '');
			$('.scrollbar').eq(this.founderIndex).css('z-index', '1');
			this.scrollBar[this.founderIndex].update();
			this.animating = false;
			this.showFoundersInfo();
		} else if ($('#founders-container').css('display') === "block") {
			this.address.title("Joint London - " + this.changeToTitle(this.founderWord));
			$('.founder-blurb-container').each(function (index, element) {
				scope.scrollBar[index].update();
			});
			$('#founders-list').animate({'top': -(this.founderIndex * this.browserHeight)}, 500, function () { scope.animating = false; });
			$('.scrollbar').css('z-index', '');
			$('.scrollbar').eq(this.founderIndex).css('z-index', '1');
		}
	};

	this.founderCheck = function () {
		if (this.oldPath === "/damoncollins") {
			$("#founders-blog a").eq(0).text("DAMON COLLINS");
		} else if (this.oldPath === "/richardexon") {
			$("#founders-blog a").eq(1).text("RICHARD EXON");
		} else if (this.oldPath === "/lorimeakin") {
			$("#founders-blog a").eq(2).text("LORI MEAKIN");
		} else if (this.oldPath === "/nikupton") {
			$("#founders-blog a").eq(3).text("NIK UPTON");
		}
	};

	this.founderClick = function (e) {
		if ($(e.currentTarget).text() !== 'BACK') {
			this.founderWord = $(e.currentTarget).text();
		}
		if ($(e.currentTarget).hasClass('active')) {
			$(e.currentTarget).text(this.founderWord);
			$('#founders-blog ul li a').removeClass('active');
			this.address.title(this.originalTitle);
			this.address.path('founders');
			this.hideFoundersInfo();
		} else {
			this.address.title("Joint London - " + this.changeToTitle(this.founderWord));
			this.address.path(this.removeSpace(this.founderWord));
			$('#founders-blog ul li a').removeClass('active');
			$(e.currentTarget).addClass('active');
			$(e.currentTarget).text("BACK");
			this.founderIndex = $('#founders-blog ul li').find('a').index(e.currentTarget);
			this.displayFounder(e);
			this.showFoundersInfo();
		}
	};

	this.founderClicked = function (e, hash) {
		if (e === undefined) {
			if (this.animating === false) {
				this.animating = true;
				$("#founders-blog a").removeClass("active");
				this.originalFounderPath = hash.substring(3);
				if (this.originalFounderPath === "damoncollins") {
					this.founderWord = "DAMON COLLINS";
					this.founderIndex = 0;
					this.displayFounder(e);
				} else if (this.originalFounderPath === "richardexon") {
					this.founderWord = "RICHARD EXON";
					this.founderIndex = 1;
					this.displayFounder(e);
				} else if (this.originalFounderPath === "lorimeakin") {
					this.founderWord = "LORI MEAKIN";
					this.founderIndex = 2;
					this.displayFounder(e);
				} else if (this.originalFounderPath === "nikupton") {
					this.founderWord = "NIK UPTON";
					this.founderIndex = 3;
					this.displayFounder(e);
				}
			}
			if (this.isiDevice) {
				$('#us-header span').text("BACK");
				$('#us-nav a').eq(0).text("BACK");
				this.lastFounder = this.founderWord;
				this.founderCheck();
				$('#founders-blog a').eq(this.founderIndex).text('BACK');
			}
			$("#founders-blog a").eq(this.founderIndex).addClass("active");
		} else {
			if (this.animating === false) {
				this.animating = true;
				if (this.isiDevice) {
					this.founderCheck();
					this.founderClick(e);
				} else {
					if (this.lastFounder) {
						$('#founders-blog a').eq(this.founderIndex).text(this.lastFounder);
						this.lastFounder = null;
					}
					this.founderClick(e);
				}
			}
			e.preventDefault();
			return false;
		}
	};

	// mouseovers
	this.back = function (e) {
		if ($(e.currentTarget).hasClass('active')) {
			$(e.currentTarget).text('BACK');
		}
	};

	this.backOff = function (e) {
		if ($(e.currentTarget).hasClass('active')) {
			$(e.currentTarget).text(this.originalWord);
		}
	};

	this.locationBackOff = function (e) {
		if ($(e.currentTarget).hasClass('active')) {
			$(e.currentTarget).text(this.originalLocation);
		}
	};

	this.usNavBackOff = function (e) {
		if ($(e.currentTarget).hasClass('active')) {
			$(e.currentTarget).text(this.usNavWord);
		}
	};

	this.founderBackOff = function (e) {
		if ($(e.currentTarget).hasClass('active')) {
			$(e.currentTarget).text(this.founderWord);
		}
	};

	// listeners
	this.createListeners = function () {
		var scope = this;
		$(window).on('resize', function (e) {scope.getBrowserSize(e); scope.checkBrowserSize(e); });
		$('#contact-header span').on('click', function (e) {scope.contactClicked(e); });
		$('#us-header span').on('click', function (e) {scope.usClicked(e); });
		$('#founders-blog ul li a').on('click', function (e) {scope.founderClicked(e); });
		$('#us-nav ul li a').on('click', function (e) {scope.usNavClicked(e); });
		$('#locations-nav-container a').on('click', function (e) {scope.locationClicked(e); });
		if (!this.isiDevice) {
			$('#locations-nav-container a').on('mouseover', function (e) {scope.back(e); });
			$('#locations-nav-container a').on('mouseout', function (e) {scope.locationBackOff(e); });
			$('#founders-blog ul li a').on('mouseover', function (e) {scope.back(e); });
			$('#founders-blog ul li a').on('mouseout', function (e) {scope.founderBackOff(e); });
			$('#contact-header span').on('mouseover', function (e) {scope.back(e); });
			$('#contact-header span').on('mouseout', function (e) {scope.backOff(e); });
			$('#us-header span').on('mouseover', function (e) {scope.back(e); });
			$('#us-header span').on('mouseout', function (e) {scope.backOff(e); });
			$('#us-nav ul li a').on('mouseover', function (e) {scope.back(e); });
			$('#us-nav ul li a').on('mouseout', function (e) {scope.usNavBackOff(e); });
		}
	};

	this.removeSplash = function () {
		$('#top, #bottom').show().delay(2000).animate({'height': '0'}, 500, function () {$(this).remove(); });
	};

	this.getBrowserSize = function () {
		this.browserHeight = $(window).innerHeight();
		this.browserWidth = $(window).innerWidth();
		if (this.isiPhone === true) {
			if (this.orientation === 0) {
				this.browserHeight = 416;
				this.browserWidth = 320;
			} else {
				this.browserHeight = 268;
				this.browserWidth = 480;
			}
		}
	};

	this.checkBrowserSize = function () {
		var that = this;
		if (this.browserWidth <= 1140 && this.browserWidth >= 768) {
			$('body').removeClass('small');
			$('body').addClass('medium');
			if ($('#founders-container').css('display') === 'block' && $('#founders-container').css('width') !== '49.7%') {
				$('#founders-container').css('width', '49.7%');
			}
		} else if (this.browserWidth < 768) {
			if ($('#founders-container').css('display') === 'block') {
				$('#founders-container').css('width', '49.7%' && $('#founders-container').css('width') !== '49.7%');
			}
			$('body').addClass('small');
			$('body').removeClass('medium');
			if (this.browserWidth < 500) {
				$('#founders-list .social-links, ul.social').addClass('iphone');
			} else {
				$('#founders-list .social-links, ul.social').removeClass('iphone');
			}
		} else {
			if ($('#founders-container').css('display') === 'block') {
				$('#founders-container').css('width', '49.9%');
			}
			$('body').removeClass('small');
			$('body').removeClass('medium');
		}
		if (this.browserHeight % 4 === 0) {
			this.browserHeight += 0;
		} else if (this.browserHeight % 4 === 1) {
			this.browserHeight += 3;
		} else if (this.browserHeight % 4 === 2) {
			this.browserHeight += 2;
		} else {
			this.browserHeight += 1;
		}
		$('#container').css('height', this.browserHeight);
		if (this.browserWidth < 822) {
			$('.founder-blurb').css('width', ((this.browserWidth / 2) - 40));
		} else {
			$('.founder-blurb').css('width', ((this.browserWidth / 2) - 100));
		}
		$('#locations-list').css('width', (this.browserWidth / 2));
		$('#locations-nav-container ul li').css('width', (this.browserWidth / 2));
		$('#founders-blog').css('width', (this.browserWidth / 2));
		$('#founders-list').css({'top': -(this.founderIndex * this.browserHeight)});
		$('#locations-list').css({'top': -(this.locationIndex * this.browserHeight)});
		$('#founders-blog').css({'top': -(this.usIndex * this.browserHeight)});
		$('#founders-links').css({'height': (this.browserHeight)});
		$('#locations-links').css({'height': (this.browserHeight)});
		$('.location').css({'height': (this.browserHeight)});
		if (this.browserHeight / 2 > 640) {
			$('.map img').css({'height': (this.browserHeight / 2), 'margin-top': ''});
		} else if (this.browserWidth / 2 > 1280) {
			$('.map img').css({'width': (this.browserWidth / 2), 'margin-left': ''});
		} else {
			$('.map img').css({'width': '', 'height': '', 'margin-top': (this.browserHeight / 4) - (640 / 2), 'margin-left': (this.browserWidth / 4) - (1280 / 2)});
		}
		$('#founders-list').show();
		if (this.run === true) {
			$('.founder-blurb-container').each(function (index, element) {
				that.scrollBar[index].update();
			});
		}
		// if iPhone set viewport
		if (this.isiPhone === true) {
			document.getElementById("viewport").setAttribute('content', 'width = ' + this.browserWidth + ', height = ' + this.browserHeight + ', maximum-scale = 1.0, minimum-scale = 1.0');
		} else {
			document.getElementById("viewport").setAttribute('content', 'width = device-width, maximum-scale = 1.0, minimum-scale = 1.0');
		}
	};

	this.changeToTitle = function (str) {
		var newstr = str.replace(/\w\S*/g, function (txt) {return txt.charAt(0).toUpperCase() + txt.substr(1).toLowerCase(); });
		return newstr;
	};

	this.removeSpace = function (str) {
		var newstr = str.toString().toLowerCase().replace(/ /g, '');
		return newstr;
	};

	this.createScrollBars = function (that) {
		this.run = true;
		$('#founders-container').show();
		$('.founder-blurb-container').each(function (index, element) {
			that.scrollBar[index] = new JOINT.ScrollBar(element, index);
		});
		$('#founders-container').hide();
	};

	this.checkLocation = function () {
		this.hash = window.location.hash;
		if (this.hash === "/" || this.hash === "" || this.hash === "!") {
			this.address.title(this.originalTitle);
			if (this.oldPath === "/contact") {
				this.hideLocations();
			} else if (this.oldPath === "/us") {
				this.hideUsNav();
			} else {
				$('#us-header span, #contact-header span').removeClass('active');
				if ($('#us').css('display') === "none") {
					$('#contact').animate({'width': '50%'}, 500);
					$('#us').css({'display': 'block'}).animate({'width': '50%'}, 500);
					$('#contact-header').animate({'width': '100%'}, 500);
				} else if ($('#contact').css('display') === "none") {
					$('#us').animate({'width': '50%'}, 500);
					$('#contact').css({'display': 'block'}).animate({'width': '50%'}, 500);
					$('#us-header').animate({'width': '100%'}, 500);
				}
			}
		} else if (this.hash === '#!/contact') {
			this.address.title("Joint London - Contact");
			if (this.oldPath === "" || this.oldPath === "/" || this.oldPath === undefined || this.oldPath === "/contact") {
				this.contactClicked(undefined, this.hash);
			} else if (this.oldPath === "/london" || this.oldPath === "/newyork" || this.oldPath === "/shanghai" || this.oldPath === "/saopaulo") {
				this.contactClicked(undefined, this.hash);
				this.hideLocationsInfo();
			} else {
				this.hideLocationsInfo();
				$('#locations-nav-container a').removeClass('active');
			}
		} else if (this.hash === "#!/london" || this.hash === "#!/newyork" || this.hash === "#!/shanghai" || this.hash === "#!/saopaulo") {
			if (this.oldPath === "" || this.oldPath === "/" || this.oldPath === undefined || this.oldPath === "/contact") {
				this.originalWord = "CONTACT";
				this.showLocations();
				this.locationClicked(undefined, this.hash);
				$('#contact-header span').addClass('active');
			} else if (this.oldPath === "/london" || this.oldPath === "/newyork" || this.oldPath === "/shanghai" || this.oldPath === "/saopaulo") {
				this.locationClicked(undefined, this.hash);
			} else {
				this.contactClicked(undefined, this.hash);
			}
		} else if (this.hash === "#!/us") {
			if (this.oldPath === "" || this.oldPath === "/" || this.oldPath === undefined || this.oldPath === "/us") {
				this.usClicked(undefined, this.hash);
			} else {
				this.hideFoundersBlog();
				$('#us-nav a').removeClass('active');
			}
		} else if (this.hash === "#!/founders" || this.hash === "#!/blog") {
			this.originalWord = "US";
			$("#us-header span").addClass('active');
			this.showUsNav();
			this.usNavClicked(undefined, this.hash);
			$("#founders-nav a").removeClass('active');
			$('.scrollbar').css('z-index', '');
		} else if (this.hash === "#!/damoncollins" || this.hash === "#!/richardexon" || this.hash === "#!/lorimeakin" || this.hash === "#!/nikupton") {
			if (this.oldPath === "/damoncollins" || this.oldPath === "/richardexon" || this.oldPath === "/lorimeakin" || this.oldPath === "/nikupton") {
				this.founderClicked(undefined, this.hash);
			} else {
				this.originalWord = "US";
				$("#us-header span").addClass('active');
				this.showUsNav();
				this.usNavWord = "FOUNDERS";
				$("#us-nav a").eq(0).addClass('active');
				this.showFoundersBlog();
				this.showFoundersInfo();
				this.founderClicked(undefined, this.hash);
			}
		} else {
			this.removeSplash();
		}
	};

	this.iPhoneOrientationChange = function () {
		scope.getBrowserSize();
		scope.checkBrowserSize();
		window.scrollTo(0, 1);
	};

	this.checkForiDevice = function () {
		if ((navigator.userAgent.match(/iPhone/i)) || (navigator.userAgent.match(/iPod/i)) || (navigator.userAgent.match(/iPad/i))) {
			this.isiDevice = true;
			if (navigator.userAgent.match(/iPhone/i)) {
				this.isiPhone = true;
			}
			this.orientation = (window.orientation);
			window.onorientationchange = function () {
				scope.orientation = (window.orientation);
				scope.iPhoneOrientationChange();
			}
		}
	};

	// init
	this.init = function () {
		var that = this;
		this.checkForiDevice();
		this.getBrowserSize();
		this.originalTitle = $('title').text();
		this.address = $.address;
		this.checkBrowserSize();
		this.createScrollBars(that);
		this.checkLocation();
		if (this.hash === "") {
			this.removeSplash();
		}
		this.createListeners();
		if ('ontouchstart' in window) {
			$('a, span').not('.scrollbar a').hover(
				function () {
					$(this).css({'color': '#E1E1E1', 'backgroundColor': '#3D3D3D'});
				},
				function () {
					$(this).css({'color': '#E1E1E1', 'backgroundColor': '#2C2C2C'});
				}
			);
		}
		// iPhone chrome remover
		setTimeout(function () { window.scrollTo(0, 1); }, 100);
	};

	this.init();

	this.address.internalChange(function () {
		scope.oldPath = scope.address.path();
	});

	// forward or back clicked on browser
	this.address.externalChange(function () {
		scope.hash = scope.address.hash();
		scope.checkLocation();
		scope.oldPath = scope.address.path();
	});

};

$(document).ready(function () {
	JOINT.application = new JOINT.MainView();
});