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
});
