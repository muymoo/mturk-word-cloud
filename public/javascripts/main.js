$(function() {
	/**
	 * Create a new hit with URL and # of assignments
	 */
	$('#hit_form').submit(function(e) {
		console.log($('#url').val());
		e.preventDefault();
		// preventing default click action
		$.ajax({
			url : '/api/hit',
			type : 'post',
			dataType : 'json',
			contentType : 'application/json',
			data : JSON.stringify({
				url : $('#url').val(),
				assignments : $('#assignments').val(),
				reward: $('#reward').val()
			}),
			success : function(data) {
				// TODO: Print URL where they can see the hits
				alert('success ' + JSON.stringify(data));
				console.log('Success: ' + JSON.stringify(data));

				// Set the value of the HIT input box to this data.
				$('#id').val(data.hitId);

				// Open the new HIT in a new window
				window.open(data.hitURL, '_blank');
			},
			error : function(data) {
				alert('ajax failed' + data);
			},
		});
	});

	/**
	 * Gets the answers for a HIT id and creates a cloud.
	 */
	$('#get_cloud').click(function(e) {
		e.preventDefault();
		// preventing default click action
		$.ajax({
			url : '/api/words/' + $('#id').val(),
			type : 'get',
			dataType : 'json',
			contentType : 'application/json',
			success : function(data) {
				var wordArray = convertToWordArray(data);
				printWordCloud(wordArray);
			},
			error : function(data) {
				alert('ajax failed' + data);
			},
		});
	});

	function convertToWordArray(data) {
		var wordArray = new Array();
		$.each(data, function(key, value) {
			wordArray.push({
				text : key,
				size : value + Math.random() * 90
			});
		});
		return wordArray;
	}

	function printWordCloud(wordArray) {
		// Print word cloud
		var fill = d3.scale.category20();

		d3.layout.cloud().size([ 300, 300 ]).words(wordArray).padding(5)
				.rotate(function() {
					return ~~(Math.random() * 2) * 90;
				}).font("Impact").fontSize(function(d) {
					return d.size;
				}).on("end", draw).start();

		function draw(words) {
			d3.select("#cloud").append("svg").attr("width", 300).attr("height",
					300).append("g").attr("transform", "translate(150,150)")
					.selectAll("text").data(words).enter().append("text")
					.style("font-size", function(d) {
						return d.size + "px";
					}).style("font-family", "Impact").style("fill",
							function(d, i) {
								return fill(i);
							}).attr("text-anchor", "middle").attr(
							"transform",
							function(d) {
								return "translate(" + [ d.x, d.y ] + ")rotate("
										+ d.rotate + ")";
							}).text(function(d) {
						return d.text;
					});
		}
	}
});