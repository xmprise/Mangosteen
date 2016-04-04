$(document).ready (function () {
	$.ajax({
		type : 'GET',
		url : 'PhoneInformation.do',
		dataType : 'json',
		success : function(json) {
				console.log(json);
			}
		});
});