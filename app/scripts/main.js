// MAIN BOOTIQUE UI FUNC
//----------------------------------------------------
//
// CONTENT ///////////////////////////////////////////
//
// 0) functions
//
// 1) document.ready
//	1.1) SVG Support (IMG TAG)
//	1.2) GitHub Bootique
//	1.3) Current year in footer


// 0) functions
	
//	/.functions


// document.ready()
$( document ).ready(function() {

	// 0.1 SIDE MENU 
		var Menu = $('#menu'),
			MenuLink = $('#menuLink'),
			Html = $('html');

		MenuLink.on('click', function(e) {
			e.preventDefault();
			$(this).toggleClass('active');
			Menu.toggleClass('active');
			Html.toggleClass('inactive no-scroll');
		});


	// 1.1 SVG SUPPPORT (IMG TAG)
		if (!Modernizr.svg) {
			if ($('img').length) {
				$('img').each(function() {
					var svgExtension = /.*\.svg$/;
					if ($(this)[0].src.match(svgExtension)) {
						// $(this).attr("src", "images/logo.png");
						$(this)[0].src = $(this)[0].src.slice(0, -3) + 'png';
					}
				});
			}
		}

	// 1.2 GitHub Bootique
		function callbackFuncWithData(ghData) {
			// gh fork counter
			if ($('.fork-counter').length) {$('.fork-counter').text(': ' + ghData.forks_count);}
			// gh star counter
			if ($('.star-counter').length) {$('.star-counter').text(ghData.stargazers_count);}
		}

		if ($('.gh-btns').length) {
			var ghOwner = $('.gh-btns').attr('data-gh-owner'),
				ghRepo = $('.gh-btns').attr('data-gh-repo'),
				ghUrl;
				
			ghUrl = 'https://api.github.com/repos/' + ghOwner + '/' + ghRepo;
			
			$.getJSON(ghUrl, callbackFuncWithData);
		}

	// 1.3 Current year in footer
		var currentYear = new Date().getFullYear();
		if ($('.current-year').length) {$('.current-year').text(currentYear);}


	// 1.4 DOCS NAV (CONTENTS)
	if ($('.bs-docs-sidebar').length) {
		var docsContents = $('.bs-docs-sidebar');

		docsContents.affix({
			offset: {
				top: function() {
					var c = docsContents.offset().top,
						d = parseInt(docsContents.children(0).css("margin-top"), 10),
						e = $("#top-nav").height();

					return this.top = c - e - d;
				}
			}
		});
	}

	// 1.5 Hash links
	$(this).on('click', 'a[href^=#]:not([href=#menu])', function(e) {
		if ($(this).prop('hash').split()[0] != '') {
			e.preventDefault();
			$('html, body').animate({
				scrollTop: $($.attr(this, 'href')).offset().top - $('#top-nav').height() - 15   // + height of fixed header + padding from it
			}, 300);
		}
	});

	// 1.6 Pretty print
		$('pre').addClass('prettyprint');

	// 1.7 Tables 
		$('table').addClass('pure-table pure-table-bordered');

	// Docbook TOC -> add hash
		if ($('.docbook').length) {
			$('.docbook h1 > a, .docbook h2 > a, .docbook h3 > a, .docbook .container-fluid').each(function() {
				$(this).attr('id', $(this).attr('name'));
			});
		}

		// // refresh every [data-spy="scroll"] after DOM mods
		// $('[data-spy="scroll"]').each(function() {
		// 	var $spy = $(this).scrollspy('refresh')
		// }) 
	
	// add scrollspy classes
	if ($('.bs-docs-sidenav').length) {
		$('.bs-docs-sidenav ul').addClass('nav');
	}

	// unwrapping unnecessary els + add title attr
	if ($('.bs-docs-sidenav a').length) {

		$('.bs-docs-sidenav a').each(function() {
			// unwrap
			var thisParent = $(this).parent();
			if (thisParent.is('span')) {
				$(this).unwrap();
			};

			// title
			$(this).attr('title', $(this).text());
		});
	}
});
