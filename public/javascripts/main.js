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
				var wordArray = convertToWordArray(data.wordArray);
				printWordCloud(wordArray, data.maxCount);
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
				size : value
			});
		});
		return wordArray;
	}

	function printWordCloud(wordArray, maxCount) {
		// Print word cloud
		var fill = d3.scale.category20();

		d3.layout.cloud().size([800, 500]).words(wordArray).padding(1)
				.rotate(function(d) { 
					return ~~(Math.random() * 5) * 30 - 60;
				}).font("Impact").fontSize(function(d) {
					var size = (d.size / maxCount)*100;
					if(size < 10) {
						size = 10;
					}
					return size;
				}).on("end", draw).start();

		function draw(words) {
			d3.select("#cloud").append("svg").attr("width", 800).attr("height",
					500).append("g").attr("transform", "translate(400,250)")
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