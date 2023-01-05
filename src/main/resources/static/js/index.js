$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	// var token = $("meta[name = '_csrf']").attr("content");
	// var header = $("meta[name = '_csrf_header']").attr("content");
	// $(document).ajaxSend(function(e, xhr, options) {
	// 	xhr.setRequestHeader(header, token);
	// });

	// get title and content
	var title = $("#recipient-name").val();
	var content = $("#message-text").val();
	// send async request (POST)
	$.post(
		"/forum/discuss/add",
		{"title": title, "content": content},
		function (data) {
			data = $.parseJSON(data);
			$("#hintBody").text(data.msg);
			// show the hint modal
			$("#hintModal").modal("show");
			// after 2 seconds, hind the hint modal
			setTimeout(function(){
				$("#hintModal").modal("hide");
				// refresh the current page
				if (data.code == 0) {
					window.location.reload();
				}
			}, 2000);
		}
	);


}