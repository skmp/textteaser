//This is what happens if you use the dev console too much

//try it on johndel's blog!
// http://johndel.gr/blog/crafting-rails-4-apps
// copy paste the function to console
// run tease($('.post_title').text(), $('.post_content').text());

function tease(title, body) {

	return $.ajax({ 
		url:"http://uncreative.korny.cc:8888/",
		type: "POST",
		data: JSON.stringify({
			id: encodeURIComponent(title),
			title: title,
			text: body
			.replace(/[^"':;/!?.>,<\[\]{}\-=\+\(\)|!@#$%^&*~`\u2019\u201C\u201D\w]/g," ")
			.replace(/([.,!?])[\s]*([^\s])/g,"$1 $2")
			.replace(/\s+/g," ")
			.replace(/(\w{2,100})\s([st])\s/g,"$1'$2 ")
			.replace(/(^\s*)|(\s*$)/g,"")
		}),
		contentType: 'application/json',
		dataType: 'json',
		crossDomain: true,
	})
	.done(function(brief) {
		$('body').append(
			'<style>' +
			'.short { z-index: 99999; position: fixed;top: 0;bottom: 0;left: 0;right: 0;background: rgba(0,0,0,0.3);}' +
			'.short div { overflow: auto; text-align: center; position: absolute; top: 0; bottom: 0; left: 0; right: 0; background: whitesmoke; margin: 10%; padding: 1em; border: 1px solid; border-radius: 7px; }' +
			'.short ul { text-align: left ; }' +
			'.short span { font-weight: 700; font-size: 1.2em; }' +
			'</style>' +
			'<div class="short"><div><span>In Brief</span><ul></ul></div></div>'
		);
            
		$.each(brief.sentences, function(x,y) { $('<li>').text(y).appendTo('.short ul') });

		$('.short').on('click', function(e) { $(e.target).remove() });
	});
}
