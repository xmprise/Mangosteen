$(document).ready (function () {
	$.ajax({
		type : 'GET',
		url : 'PhoneInformation.do',
		dataType : 'json',
		success : function(json) {
			var phone = json;
			var interAvailable = phone.internalAvailableMem;
			var interTotal = phone.internalTotalMem;
			var exterAvailable = phone.externalAvailableMem;
			var exterTotal = phone.externalTotalMem;
			var interUse = phone.internalUseMem;
			var exterUse = phone.externalUseMem;
			var inUsingPixel = phone.inUsingPixel;
			var exUsingPixel = phone.exUsingPixel;
			rootPath_len = phone.rootPath.length;
//			console.log('rootPath>>>>'+phone.rootPath);
//			console.log('rootPath_len>>>>'+rootPath_len);

			$("<div class='phoneTitle' style='position: absolute;  font-size: 35px;  width: 500px;  left: 150px;''><span class='phoneTitle model'>"+phone.Brand+' ' +phone.Model+"</span>" +
					"<span class='phoneTitle os'>Android "+phone.version+"</span>"+
//					"<span class='phoneTitle number'>"+phone.phoneNum+"</span>"+
					"</div>"+
					"<div class='interStorage'> <div class='stitle'>Internal Storage (Available: "+interAvailable+")</div> <div class='bar'> <div class='using' style='width: "+inUsingPixel+"px; '> </div> <div class='mask_text'>"+interUse+" / "+interTotal+"</div> </div> </div>"+
					"<div class='s_sd_storage'> <div class='stitle'>SD Card (Available: "+exterAvailable+")</div> <div class='bar'> <div class='using' style='width: "+exUsingPixel+"px; '> </div> <div class='mask_text'>"+exterUse+" / "+exterTotal+"</div> </div> <div class='stip'> </div></div>").insertBefore($("#s3"));		
			}
	
	
		});
});