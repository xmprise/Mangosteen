var req;
var fromUrl;
var toUrl;
var copySourceArray;
var cutSourceArray;
var isCopy = false;
var isCut = false;
var selectItemNum = 0;
jQuery.extend(mangoAPI, {
	
	// 파일 탐색기의 버튼 함수 모음
	fileButtons : {

		//파일 탐색기의 전체 선택 체크박스 체크 했을 때
		bindSelectAllBtn : function(target) {
			$(".checkbox").bind('click', function(event) {
				// name이 check_all 인 체크박스가 checked 가 되어 있다면
				if ($(".checkbox").attr('check') == 0) {
					$(".checkbox").attr('check', '1');
					$(".checkbox").addClass('checked');
					// class는 box_class 인 체크박스의 속성 checked는 checked이다
					if($(target).parent().attr('class') == "jqueryFileTree") // 파일 탐색기 쪽이면....
					{
						$(target).attr('select','selected'); //선택 됐으면
						$(target).css({'background-color' : '#594381','border-radius': '10px'});
					}
					else{
						$(target).attr("select", "selected");
						$(target).css('border-color','yellow'); //색상 변경
					}
					
					selectItemNum = 0; // 일단 0으로 초기화 후 다시 카운트
					
					$('#filelist [select]').each(function() {
						if($(this).attr('select') == 'selected'){
							selectItemNum++;
						}
					});
				} else {// 그렇지 않으면
					// class는 box_class인 체크박스의 속성 checked 는 "" 공백이다
					$(".checkbox").attr('check', '0');
					$(".checkbox").removeClass('checked');
					
					if($(target).parent().attr('class')== "jqueryFileTree") // 파일 탐색기 쪽이면....
					{
						$(target).attr('select','unselect'); //선택 됐으면
						$(target).css({'background-color' : '','border-radius': '10px'});
					}
					else{
						$(target).attr("select", "unselect");
						$(target).css('border-color','gray'); // 기본 색상
					}
					selectItemNum = 0 ;
				}
				
				$('Button[menu]').addClass('disabled'); // 기본 모두 비활성화(폴더추가, 전체선택 제외)
					if(selectItemNum ==0 && (copySourceArray != null || cutSourceArray != null))
					{
						$('Button[menu="Paste"]').removeClass('disabled');
					}
					else if(selectItemNum ==1 && (copySourceArray != null || cutSourceArray != null))
					{
						$('Button[menu="Paste"]').removeClass('disabled');
						$('Button[menu="Copy"]').removeClass('disabled');
						$('Button[menu="Cut"]').removeClass('disabled');
						$('Button[menu="Delete"]').removeClass('disabled');
						$('Button[menu="Download"]').removeClass('disabled');
						
						if($('.mediaType').attr('id') == "image") // mediaType에 따라서 지정
						{
							$('Button[menu="WallPaper"]').removeClass('disabled');
							$('Button[menu="Expand"]').removeClass('disabled');
						}
						else if($('.mediaType').attr('id') == "video")
							$('Button[menu="Play"]').removeClass('disabled');
						else if($('.mediaType').attr('id') == "audio")
						{
							$('Button[menu="Play"]').removeClass('disabled');
							$('Button[menu="Ringtone"]').removeClass('disabled');
						}	
					}
					else if(selectItemNum >=2 && (copySourceArray != null || cutSourceArray != null))
					{
						$('Button[menu="Paste"]').removeClass('disabled');
						$('Button[menu="Copy"]').removeClass('disabled');
						$('Button[menu="Cut"]').removeClass('disabled');
						$('Button[menu="Delete"]').removeClass('disabled');
						$('Button[menu="Download"]').removeClass('disabled');
					}
					else if(selectItemNum ==0) // 선택된 아이템이 없을 때
					{
						$('Button #newFolderBtn').removeClass('disabled');
						$('Button[menu]').addClass('disabled');
					}
					else if(selectItemNum ==1) // 선택된 아이템이 하나일 때
					{
						$('Button[menu="Rename"]').removeClass('disabled');
						$('Button[menu="Copy"]').removeClass('disabled');
						$('Button[menu="Cut"]').removeClass('disabled');
						$('Button[menu="Delete"]').removeClass('disabled');
						$('Button[menu="Download"]').removeClass('disabled');
						
						if($('.mediaType').attr('id') == "image") // mediaType에 따라서 지정
						{
							$('Button[menu="WallPaper"]').removeClass('disabled');
							$('Button[menu="Expand"]').removeClass('disabled');
						}
						else if($('.mediaType').attr('id') == "video")
							$('Button[menu="Play"]').removeClass('disabled');
						else if($('.mediaType').attr('id') == "audio")
						{
							$('Button[menu="Play"]').removeClass('disabled');
							$('Button[menu="Ringtone"]').removeClass('disabled');
						}
					}
					else if(selectItemNum >= 2) // 선택 아이템이 2개 이상일 때
					{
						$('Button[menu="Copy"]').removeClass('disabled');
						$('Button[menu="Cut"]').removeClass('disabled');
						$('Button[menu="Delete"]').removeClass('disabled');
						$('Button[menu="Download"]').removeClass('disabled');
					}
			});
		},

		bindItemBtn : function(mediaType) {
			// 아이템 더블 클릭 했을 때 이벤트
			$('.thumb.element').on("dblclick", function(e){
				// 미디어 타입에 따라 다른 동작
				var mediaPath = $(this).find('img').attr('src').substring(5, $(this).find('img').attr('src').length);	
				switch(mediaType)
				{
				case 'image':
					var msg = '<img id="preview" src="oriImage'+mediaPath+'"></img>';
					var path = 'oriImage'+mediaPath;
					var width;
					var height;
					var img = new Image();
	                img.src = path;
					$(img).load(function(){
						$.smartPop.open({ background: "gray", width: this.width, height: this.height,html:msg });
					});
					break;
				case 'video':
					//var msg = '<video src="oriVideo'+mediaPath+'" controls="true" loop="true" autoplay="true" width="640" height="480"></video>';
					var msg = '<div id="container">Loading the player ...</div>'+
										'<script type="text/javascript">'+
										'jwplayer("container").setup({'+
										'flashplayer: "js/jwplayer/player.swf",'+
										'file: "oriVideo'+mediaPath+'",'+
										'height: 480,'+
										'width: 640'+
										'});'+
										'</script>';
					$.smartPop.open({ background: "gray", width: 655, height: 500, html: msg  });
					break;
				case 'audio':
					var msg = '';
					break;
				}
			});
			
			// 아이템 클릭했을 때 이벤트
			$('.thumb.element').on("click", function(e){
					if($(this).attr('select') == 'unselect') // 선택안됐으면
					{
						$(this).attr('select','selected'); // 선택
						$(this).css('border-color','yellow'); //색상 변경
						selectItemNum++;
					}
					else
					{
						$(this).attr('select','unselect'); //선택 됐으면
						$(this).css('border-color','gray'); // 기본 색상
						selectItemNum--;
					}
					
					$('Button[menu]').addClass('disabled'); // 기본 모두 비활성화(폴더추가, 전체선택 제외)
					if(selectItemNum ==0 && (copySourceArray != null || cutSourceArray != null))
					{
						$('Button[menu="Paste"]').removeClass('disabled');
					}
					else if(selectItemNum ==1 && (copySourceArray != null || cutSourceArray != null))
					{
						$('Button[menu="Paste"]').removeClass('disabled');
						$('Button[menu="Copy"]').removeClass('disabled');
						$('Button[menu="Cut"]').removeClass('disabled');
						$('Button[menu="Delete"]').removeClass('disabled');
						$('Button[menu="Download"]').removeClass('disabled');
						
						if($('.mediaType').attr('id') == "image") // mediaType에 따라서 지정
						{
							$('Button[menu="WallPaper"]').removeClass('disabled');
							$('Button[menu="Expand"]').removeClass('disabled');
						}
						else if($('.mediaType').attr('id') == "video")
							$('Button[menu="Play"]').removeClass('disabled');
						else if($('.mediaType').attr('id') == "audio")
						{
							$('Button[menu="Play"]').removeClass('disabled');
							$('Button[menu="Ringtone"]').removeClass('disabled');
						}	
					}
					else if(selectItemNum >=2 && (copySourceArray != null || cutSourceArray != null))
					{
						$('Button[menu="Paste"]').removeClass('disabled');
						$('Button[menu="Copy"]').removeClass('disabled');
						$('Button[menu="Cut"]').removeClass('disabled');
						$('Button[menu="Delete"]').removeClass('disabled');
						$('Button[menu="Download"]').removeClass('disabled');
					}
					else if(selectItemNum ==0) // 선택된 아이템이 없을 때
					{
						$('Button #newFolderBtn').removeClass('disabled');
						$('Button[menu]').addClass('disabled');
					}
					else if(selectItemNum ==1) // 선택된 아이템이 하나일 때
					{
						$('Button[menu="Rename"]').removeClass('disabled');
						$('Button[menu="Copy"]').removeClass('disabled');
						$('Button[menu="Cut"]').removeClass('disabled');
						$('Button[menu="Delete"]').removeClass('disabled');
						$('Button[menu="Download"]').removeClass('disabled');
						
						if($('.mediaType').attr('id') == "image") // mediaType에 따라서 지정
						{
							$('Button[menu="WallPaper"]').removeClass('disabled');
							$('Button[menu="Expand"]').removeClass('disabled');
						}
						else if($('.mediaType').attr('id') == "video")
							$('Button[menu="Play"]').removeClass('disabled');
						else if($('.mediaType').attr('id') == "audio")
						{
							$('Button[menu="Play"]').removeClass('disabled');
							$('Button[menu="Ringtone"]').removeClass('disabled');
						}
					}
					else if(selectItemNum >= 2) // 선택 아이템이 2개 이상일 때
					{
						$('Button[menu="Copy"]').removeClass('disabled');
						$('Button[menu="Cut"]').removeClass('disabled');
						$('Button[menu="Delete"]').removeClass('disabled');
						$('Button[menu="Download"]').removeClass('disabled');
					}
					//
				});	

			$('.icon-btns button').on("click", function(e){
				var el;
				if(mediaType == null)
					el = $('#MainFileView');
				else
					el = $('#filelist');
				
				if($(this).attr('class') !='disabled' && $(this).attr('menu') == 'OpenFolder')	
				{
					$('#filelist [select]').each(function() {
						if($(this).attr('select') == 'selected')
							$(this).dblclick();
						});
				}
				else if($(this).attr('class') !='disabled' && $(this).attr('id')=="newFolderBtn")
				{
					mangoAPI.contextMenuNewFolder();
				}
				else if($(this).attr('class') !='disabled' && $(this).attr('menu') == 'Rename')
				{
					mangoAPI.contextMenuEdit(el);
				}
				else if($(this).attr('class') !='disabled' && $(this).attr('menu') == 'Cut')
				{
					mangoAPI.contextMenuCut(el);
				}
				else if($(this).attr('class') !='disabled' && $(this).attr('menu') == 'Copy')
				{
					mangoAPI.contextMenuCopy(el);
				}
				else if($(this).attr('class') !='disabled' && $(this).attr('menu') == 'Paste')
				{
					mangoAPI.contextMenuPaste(el);
				}
				else if($(this).attr('class') !='disabled' && $(this).attr('menu') == 'Delete')
				{
					mangoAPI.contextMenuDelete(el);
				}
				else if($(this).attr('class') !='disabled' && $(this).attr('menu') == 'Play')
				{
					mangoAPI.contextMenuPlay(el);
				}
				else if($(this).attr('class') !='disabled' && $(this).attr('menu') == 'WallPaper')
				{
					mangoAPI.contextMenuWallPaper(el);	
				}
				else if($(this).attr('class') !='disabled' && $(this).attr('menu') == 'Ringtone')	
				{
					mangoAPI.contextMenuRingtone('ringtone');
				}
				else if($(this).attr('class') !='disabled' && $(this).attr('menu') == 'Download')	
				{
					mangoAPI.contextMenuDownload(el);
				}
				else if($(this).attr('class') !='disabled' && $(this).attr('menu') == 'Expand')	
				{	
					var mediaPath;
					$('#filelist [select]').each(function() {
						if($(this).attr('select') == 'selected'){
							mediaPath = $(this).find('img').attr('src').substring(5, $(this).find('img').attr('src').length);	
						}
					});
					var msg = '<img id="preview" src="oriImage'+mediaPath+'"></img>';
					var path = 'oriImage'+mediaPath;
					var img = new Image();
					img.src = path;
					$(img).load(function(){
						$.smartPop.open({ background: "gray", width: this.width, height: this.height,html:msg });
					});
				}
			
			});
		},
		
		bindFileExplorerItem : function() {
			
		},

		unBindItemBtn : function() {
			$('.thumb.element').off("click");
		},
		
		// 파일 탐색기의 폴더 추가 버튼을 눌렸을 때
		bindAddFolderBtn : function() {
			$('span.btn_new').bind('click', function(event) {

				var folderName = prompt('폴더명을 입력하세요');
				
				var relativePath = $('#MainFileView ul').attr('path').substring(7, $('#MainFileView ul').attr('path').length);
				//var createUrl = '/Mangosteen/webdav'+;
				var createUrl = 'webdav'+relativePath;
				createUrl += folderName;
				//alert(createUrl);

				jQuery.Dav(createUrl).createFolder({
					complete : function(dat, stat) {
						//;;;console.log('#createFolder');
						callbackFn(returnValue);
					},
					async : false,
					success : function(dat, stat) {
						//alert('dat' + dat);
						/*if (onCheckRcvLoginRequired(dat)) {
						 onRcvError();
						 return;
						 }*/
						returnValue = 1;
					},
					error : function(dat, stat) {
						alert('error');
						/*if (onCheckRcvLoginRequired(dat.responseText)) {
						 onRcvError();
						 return;
						 }*/
						returnValue = returnHttpErrorValue(dat.responseText);
					}
				});
			});
		},

		// 파일 탐색기의 삭제 버튼을 눌렸을 때
		bindDeleteBtn : function() {
			$('span.btn_delete').bind('click', function(event) {

				$('input.item_box').each(function() {
					// this(현재선택된 input문의) 체크박스가 checked 되어 있다면
					if ($(this).is(":checked")) {
						
						var relativePath =  $(this).parent().find('a').attr('rel').substring(7, $(this).parent().find('a').attr('rel').length);
						var deleteUrl = 'webdav'+relativePath;
						//alert(deleteUrl);

						jQuery.Dav(deleteUrl).remove({
							complete : function(dat, stat) {
								//;;;console.log('delete file');
								callbackFn(returnValue);
							},
							async : true, //p10789
							success : function(dat, stat) {
								//alert('dat' + dat);

								//if(onCheckRcvLoginRequired(dat)) {
								//	onRcvError();
								//	return;
								//}
								returnValue = 1;
							},
							error : function(dat, stat) {
								//if(onCheckRcvLoginRequired(dat.responseText)) {
								//	onRcvError();
								//	return;
								//}
								if (dat.status == 204) {// for IE
									returnValue = 1;
								} else {
									returnValue = returnHttpErrorValue(dat.responseText);
								}
							}
						});
					}
				});
			});
		},

		// 각 미디어의 삭제 버튼을 눌렸을 때
		bindMediaDeleteBtn : function() {
			$('span.btn_delete').bind('click', function(event) {

				$('input.item_box').each(function() {
					// this(현재선택된 input문의) 체크박스가 checked 되어 있다면
					if ($(this).is(":checked")) {
						var relativePath =  $(this).parent().find('img').attr('src').substring(5+rootPath_len, $(this).parent().find('img').attr('src').length);
						var removeTag = $(this).parent().parent();
						
						var deleteUrl = 'webdav'+relativePath;
						
						//alert(deleteUrl);
						
						jQuery.Dav(deleteUrl).remove({
							complete : function(dat, stat) {
								//;;;console.log('delete file');
								//callbackFn(returnValue);
								removeTag.remove();
							},
							async : true, //p10789
							success : function(dat, stat) {
								returnValue = 1;
							},
							error : function(dat, stat) {
								//if(onCheckRcvLoginRequired(dat.responseText)) {
								//	onRcvError();
								//	return;
								//}
								if (dat.status == 204) {// for IE
									returnValue = 1;
								} else {
									returnValue = returnHttpErrorValue(dat.responseText);
								}
							}
						});
					}
				});
			});
		},

		//파일 탐색기의 복사 버튼을 눌렸을 때
		bindCopyBtn : function() {
			$('span.btn_copy').bind('click', function(event) {
				 
				$('input.item_box').each(function() {
					// this(현재선택된 input문의) 체크박스가 checked 되어 있다면
					if ($(this).is(":checked")) {
						
						var relativePath =  $(this).parent().find('a').attr('rel').substring(7, $(this).parent().find('a').attr('rel').length);
						//var removeTag = $(this).parent().parent();
						fromUrl = 'webdav'+relativePath;
						isCopy = true;
						//alert(fromUrl);
					}
				});
			});
		},
		
		bindCutBtn : function() {
			$('span.btn_cut').bind('click', function(event) {
				$('input.item_box').each(function() {
					// this(현재선택된 input문의) 체크박스가 checked 되어 있다면
					if ($(this).is(":checked")) {
						
						var relativePath =  $(this).parent().find('a').attr('rel').substring(7, $(this).parent().find('a').attr('rel').length);
						//var removeTag = $(this).parent().parent();
						fromUrl = 'webdav'+relativePath;
						isCut = true;
						//alert(fromUrl);
					}
				});
			});
		},
		
		//파일 탐색기의 붙여넣기 버튼 눌렸을 때
		bindPasteBtn : function(){
			$('span.btn_paste').bind('click', function(event) {
				var name = fromUrl.split("/");
			    //alert(name[0]);
				toUrl = $('#MainFileView ul').attr('path').substring(7, $('#MainFileView ul').attr('path').length)+name[name.length -1];

				var fwrite;
				jQuery.Dav(fromUrl).copy({
					complete : function(dat, stat) {
						//;;;console.log('#copy');
						//callbackFn(returnValue);
					},
					Destination_url : toUrl,
					Overwrite : fwrite,
					async : true,
					success : function(dat, stat) {
						//;;;console.log('#success');
						//if (onCheckRcvLoginRequired(dat)) {
						//	onRcvError();
						//	return;
						//}
						//alert('copy success');
						returnValue = 1;
					},
					error : function(dat, stat) {
						alert('copy fail');
						if (onCheckRcvLoginRequired(dat.responseText)) {
							onRcvError();
							return;
						}
						//;;;console.log('#error');
						returnValue = returnHttpErrorValue(dat.responseText);
					}
				});
			});
		},
		
		//파일 탐색기의 새이름 버튼을 눌렸을 때
		bindRenameBtn : function() {
			$('span.btn_rename').bind('click', function(event) {

				$('input.item_box').each(function() {
					// this(현재선택된 input문의) 체크박스가 checked 되어 있다면
					if ($(this).is(":checked")) {

						$(this).parent().find()
					}
				});

				jQuery.Dav(sourceUrl).rename({
					complete : function(dat, stat) {
						//;;;console.log('#rename');
						callbackFn(returnValue);
					},
					Destination_url : renameUrl,
					cache : false,
					async : true,
					success : function(dat, stat) {
						if (onCheckRcvLoginRequired(dat)) {
							onRcvError();
							return;
						}
						returnValue = 1;
					},
					error : function(dat, stat) {
						if (onCheckRcvLoginRequired(dat.responseText)) {
							onRcvError();
							return;
						}
						returnValue = returnHttpErrorValue(dat.responseText);
					}
				});
			});
		},
	},

	fileUpload : {
		ajaxFunction : function() {

			var url = "FileUploadServlet";
			var path = "path=" + $('#MainFileView ul').attr('path');
			//alert(path);
			if (window.XMLHttpRequest)// Non-IE browsers
			{

				req = new XMLHttpRequest();
				req.onreadystatechange = mangoAPI.fileUpload.processStateChange;

				try {
					req.open("POST", url, true);
					req.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
					req.setRequestHeader("Content-length", path.length);
					req.setRequestHeader("Connection", "close");
				} catch (e) {
					alert(e);
				}
				//post일 경우 파라미터를 넣는다.
				req.send(path);
			} else if (window.ActiveXObject)// IE Browsers
			{
				req = new ActiveXObject("Microsoft.XMLHTTP");
				if (req) {
					req.onreadystatechange = mangoAPI.fileUpload.processStateChange;
					req.open("POST", url, true);
					req.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
					req.setRequestHeader("Content-length", path.length);
					req.setRequestHeader("Connection", "close");
					req.send(path);
				}
			}
		},

		processStateChange : function() {
			/**
			 *	State	Description
			 *	0		The request is not initialized
			 *	1		The request has been set up
			 *	2		The request has been sent
			 *	3		The request is in process
			 *	4		The request is complete
			 */
			if (req.readyState == 4) {
				if (req.status == 200)// OK response
				{
					var xml = req.responseXML;

					// No need to iterate since there will only be one set of lines
					var isNotFinished = xml.getElementsByTagName("finished")[0];
					var myBytesRead = xml.getElementsByTagName("bytes_read")[0];
					var myContentLength = xml.getElementsByTagName("content_length")[0];
					var myPercent = xml.getElementsByTagName("percent_complete")[0];

					// Check to see if it's even started yet
					if ((isNotFinished == null) && (myPercent == null)) {
						document.getElementById("initializing").style.visibility = "visible";

						// Sleep then call the function again
						window.setTimeout("ajaxFunction();", 100);
					} else {
						document.getElementById("initializing").style.visibility = "hidden";
						document.getElementById("progressBarTable").style.visibility = "visible";
						document.getElementById("percentCompleteTable").style.visibility = "visible";
						document.getElementById("bytesRead").style.visibility = "visible";

						myBytesRead = myBytesRead.firstChild.data;
						myContentLength = myContentLength.firstChild.data;

						if (myPercent != null)// It's started, get the status of the upload
						{
							myPercent = myPercent.firstChild.data;

							document.getElementById("progressBar").style.width = myPercent + "%";
							document.getElementById("bytesRead").innerHTML = myBytesRead + " of " + myContentLength + " bytes read";
							document.getElementById("percentComplete").innerHTML = myPercent + "%";

							// Sleep then call the function again
							window.setTimeout("ajaxFunction();", 100);
						} else {
							document.getElementById("bytesRead").style.visibility = "hidden";
							document.getElementById("progressBar").style.width = "100%";
							document.getElementById("percentComplete").innerHTML = "Done!";
						}
					}
				} else {
					alert(req.statusText);
				}
			}
		},
	},
	
	updateAddData: function(json){
	var val = eval("("+json+")");
	var src = val.type+val.path+val.name;
	var mediaPath = 'oriImage'+src.substring(5, src.length);
	$("#MediaThumbList ul").append($("<li class='thumb element' select='unselect'><div class='mediaType' folderPath='"+val.path+"'id='"+val.type+"'><a class='fancybox' rel='group' href='"+mediaPath+"'><img onload='javascript:mangoAPI.imgOnload();' class='perMediaThumb' src='"+val.type+val.path+val.name+"' /></a>" + 
	"<span>"+val.name+"</span>" +						
		"</div></li>"));		
		// 이부분 최적화 하기...  개별아이템마다 호출해서.. 비효율.. 마지막 아이템 뿌려질때 한번만 호출하게 만들어보기
		mangoAPI.fileButtons.unBindItemBtn();
		mangoAPI.fileButtons.bindItemBtn(val.type);
		$(".fancybox").fancybox();
	},
	
	updateAddData2: function(evt, pasteFolderPath){
		var source;
		var sourceLength;
//		console.log('Add isCopy>>'+isCopy);
//		console.log('Add isCut>>'+isCut);
		if(isCopy == true)
		{
			source = copySourceArray.pop();
			sourceLength = copySourceArray.length;
//			console.log('copyPOP');
//			console.log('Copysource>>'+source)
		}
		else if(isCut == true)
		{
			source = cutSourceArray.pop();
			sourceLength = cutSourceArray.length;
//			console.log('cutPOP');
//			console.log('Cutsource>>'+source)
		}
		var rootTag;
		var parentPath;
		 if($(evt).attr('id') != 'MainFileView')  // 미디어 부분
		 {
			 // 이부분 pasteFolderPath source.id 이런거 좀 깔끔하게 수정하자!!!!!
			 
			 // 붙여넣기가 image 일 경우에는 fancybox 적용 위해서 <a> 태그 넣고 바인딩...
			 if(source.id == 'image')
			 {
				 var src = source.id+pasteFolderPath+source.name;
				 var mediaPath = 'oriImage'+src.substring(5, src.length);
				 $("#MediaThumbList ul").append($("<li class='thumb element' select='unselect'><div class='mediaType' folderPath='"+pasteFolderPath+"'id='"+source.id+"'><a class='fancybox' rel='group' href='"+mediaPath+"'><img onload='javascript:mangoAPI.imgOnload();' class='perMediaThumb' src='"+src+"' /></a>" + 
							"<span id='fileName'>"+source.name+"</span></div></li>")); // 추가된 미디어 엘리먼트 태그 추가 삽입
				 $(".fancybox").fancybox();
			 }
			 else{
				 $("#MediaThumbList ul").append($("<li class='thumb element' select='unselect'><div class='mediaType' folderPath='"+pasteFolderPath+"'id='"+source.id+"'><img onload='javascript:mangoAPI.imgOnload();' class='perMediaThumb' src='"+src+"' /></a>" + 
							"<span id='fileName'>"+source.name+"</span></div></li>")); // 추가된 미디어 엘리먼트 태그 추가 삽입	
			 }
			 
			if(sourceLength == 0) // 다 붙여 넣었다면???
			{
				// 아이템 재바인딩... 이부분도 최적화 필요
				mangoAPI.fileButtons.unBindItemBtn();
				mangoAPI.fileButtons.bindItemBtn(source.type);
				
				//Toolbar paste메뉴 비활성화
				$('Button[menu="Paste"]').addClass('disabled');
			}

		 }
		 else  // 파일 탐색기 부분
		 {
			 parentPath = source.folderpath;
			 
			 //원래 리소스 삭제하기..    
			 $('#filelist a[rel = "'+ source.src +'"]').parent().remove();				 

			 if(pasteFolderPath =='/sdcard/') // 이건 붙여넣기 하는 path가 최상위 sdcard이면 filetree 태그 바로 밑에 붙여넣어야 하므로
				 rootTag = $('#fileTree');
			 else
				 rootTag = $('#fileTree a[rel = "'+ pasteFolderPath +'"]').parent();
			 
			 $('#MainFileView ul').append("<li class='" +source.className  +"' select='unselect'><a href='javascript:;' rel='" +source.src+ "'>"+ source.name + "</a></li>");
				rootTag.find('ul').append("<li class='" +source.className  +"'><a href='javascript:;' rel='" + source.src + "'>"+ source.name + "</a></li>");
				if(parentPath == '/sdcard/') // /sdcard/ 바로 밑의 리소스를 복사 붙여넣기 한 경우 새로 뿌려지는거 방지 하기 위해 예외처리..
					parentPath = 'sdcard';
					
			    rootTag.fileTree({
					root : parentPath,
					script : 'FileExplorer.do'
				}, function(file) {
					alert(file);
				});
		 }
		},
	
	FileExplorerUpdateAddData:function(json){
		var val = eval("("+json+")");
		var ext =val.name.split('.');
		var rootTag;
		var parentPath = val.path;
		
		if(parentPath =='/sdcard/') // 이건 붙여넣기 하는 path가 최상위 sdcard이면 filetree 태그 바로 밑에 붙여넣어야 하므로
				 rootTag = $('#fileTree');
			 else
				 rootTag = $('#fileTree a[rel = "'+ val.path +'"]').parent();
		
		$('#MainFileView ul').append("<li class='file ext_" + ext[1] +"' select='unselect'><a href='javascript:;' rel='" + val.path +val.name+ "'>"+ val.name + "</a></li>");
		rootTag.find('ul').append("<li class='file ext_" + ext[1] +"'><a href='javascript:;' rel='" + val.path+val.name + "'>"+ val.name + "</a></li>");
		if(parentPath == '/sdcard/') // /sdcard/ 바로 밑의 리소스를 복사 붙여넣기 한 경우 새로 뿌려지는거 방지 하기 위해 예외처리..
			parentPath = 'sdcard';
		
		//console.log(rootTag);
		//console.log('parentPath =' + parentPath);
		
		rootTag.fileTree({
			root : parentPath,
			script : 'FileExplorer.do'
		}, function(file) {
			alert(file);
		});
	},
	
	FileExplorerDeleteData:function(){
		
	},
	
	FileExplorerAddDataBind:function(t, parentPath){},

	updateDelData:function(){
	
	},

	createUploader : function() {
		var uploader = new qq.FileUploader({
			element : document.getElementById('file-uploader-demo1'),
			action : 'FileUploadServlet2',
			debug : true,
			extraDropzones : [qq.getByClass(document, 'qq-upload-extra-drop-area')[0]]
		});
	},

	createDownloader : function() {
		
	},
	
	contextMenuBind : function(target){
		var menuName = $('[class="contextMenu"]').attr('id');
		/*
		var target = $('.'+tag).parent().parent();
		if(tag == 'perFolderThumb')
			menuName = $('[class=contextMenu]').attr('id');
		else if(tag == 'perMediaThumb')
			menuName = $('[class=contextMenu]').attr('id');
		*/
		
		//target.find('li').contextMenu({
		target.contextMenu({
				menu: menuName
		},
			function(action, el, pos) {
			switch(action){
				case 'upload':
					$('input[name=file]').click();
					break;
				case 'download':
					mangoAPI.contextMenuDownload(el);
					break;
				case 'newfolder':
					mangoAPI.contextMenuNewFolder();
					break;
				case 'paste':
					mangoAPI.contextMenuPaste(el);
					break;
				case 'copy':
					mangoAPI.contextMenuCopy(el);
					break;
				case 'cut':
					mangoAPI.contextMenuCut(el);
					break;
				case 'edit':
					mangoAPI.contextMenuEdit(el);
					break;
				case 'delete':
					mangoAPI.contextMenuDelete(el);
					break;
				case 'play':
					mangoAPI.contextMenuPlay(el);
					break;
				case 'wallpaper':
					mangoAPI.contextMenuWallPaper(el);
					break;
				case 'ringtone':
					mangoAPI.contextMenuRingtone(action);
					break;
				case 'notification':
					mangoAPI.contextMenuRingtone(action);
					break;
				case 'alram':
					mangoAPI.contextMenuRingtone(action);
					break;
			}
			
			/*
			alert(
				'Action: ' + action + '\n\n' +
				'Element ID: ' + $(el).attr('id') + '\n\n' + 
				'X: ' + pos.x + '  Y: ' + pos.y + ' (relative to element)\n\n' + 
				'X: ' + pos.docX + '  Y: ' + pos.docY+ ' (relative to document)'
				);*/
		});		
	},
	
	contextMenuDisable : function(target){
		target.disableContextMenu();
	},
	contextMenuEnable : function(target){
		target.enableContextMenu();
	},
	
	contextMenuCopy:function(evt){
		isCopy = false;
		isCut = false;
		fromUrl = new Array();
		var relativePath;
		copySourceArray = new Array();
		$('#filelist [select]').each(function() {
			if($(this).attr('select') == 'selected'){
				var copySource = new Object();
				
				if($(evt).attr('id') != 'MainFileView')
				{ 
					relativePath =  $(this).find('img').attr('src').substring(5+rootPath_len, $(this).find('img').attr('src').length);
					copySource.src = $(this).find('img').attr('src');
					copySource.folderpath = $(this).find('.mediaType').attr('folderpath');
					copySource.id = $(this).find('.mediaType').attr('id');
					copySource.name = $(this).find('span').html();
				}
				else
				{
					relativePath = $(this).find('a').attr('rel').substring(7, $(this).find('a').attr('rel').length);
					copySource.className = $(this).attr('class'); //클래스명 추출
					copySource.src = $(this).find('a').attr('rel');
					copySource.name = $(this).find('a').html();
					copySource.folderpath = $(this).parent().attr('path');
				}
				fromUrl.push('webdav'+relativePath);
				isCopy = true;				
				alert("Copy!!!");
				// Toolbar의 paste메뉴 활성화
				$('Button[menu="Paste"]').removeClass('disabled');
				copySourceArray.push(copySource);
			}
		});
	},
	
	contextMenuCut:function(evt){
		isCut = false;
		isCopy=false;
		fromUrl = new Array();
		var relativePath;
		cutSourceArray = new Array();
		$('#filelist [select]').each(function() {
			if($(this).attr('select') == 'selected'){
				var cutSource = new Object();
				
				if($(evt).attr('id') != 'MainFileView')
				{ 
					relativePath =  $(this).find('img').attr('src').substring(5+rootPath_len, $(this).find('img').attr('src').length);
					cutSource.src = $(this).find('img').attr('src');
					cutSource.folderpath = $(this).find('.mediaType').attr('folderpath');
					cutSource.id = $(this).find('.mediaType').attr('id');
					cutSource.name = $(this).find('span').html();
				}
				else
				{
					relativePath = $(this).find('a').attr('rel').substring(7, $(this).find('a').attr('rel').length);
					cutSource.className = $(this).attr('class'); //클래스명 추출
					cutSource.src = $(this).find('a').attr('rel');
					cutSource.name = $(this).find('a').html();
					cutSource.folderpath = $(this).parent().attr('path');
//					console.log('cutSource.folderpath>>'+cutSource.folderpath);
				}
				fromUrl.push('webdav'+relativePath);
				isCut = true;
				alert("Cut!!!");
				// Toolbar의 paste메뉴 활성화
				$('Button[menu="Paste"]').removeClass('disabled');
				cutSourceArray.push(cutSource);
			}
		});
	},

	contextMenuPaste:function(element){
		$(fromUrl).each(function(){
			var thisFromUrl = this;
			var name = this.split("/");
			var pasteFolderPath;
		    //alert(name[0]);
//		    console.log('Paste isCopy>>'+isCopy);
//		    console.log('Paste isCut>>'+isCut);
		    if($(element).attr('id') != 'MainFileView')
			{ 
				pasteFolderPath = $(element).find('.mediaType').attr('folderpath');
		    	toUrl = $(element).find('.mediaType').attr('folderpath').substring(rootPath_len, $(element).find('.mediaType').attr('folderpath').length)+name[name.length -1];
			}
		    else
		    {
		    	pasteFolderPath = $('#MainFileView ul').attr('path');
		    	if(name[name.length-1] == "")
		    		toUrl = $('#MainFileView ul').attr('path').substring(7, $('#MainFileView ul').attr('path').length)+name[name.length -2];
		    	else
		    		toUrl = $('#MainFileView ul').attr('path').substring(7, $('#MainFileView ul').attr('path').length)+name[name.length -1];
		    }
			//toUrl = $('#MainFileView ul').attr('path').substring(7, $('#MainFileView ul').attr('path').length)+name[name.length -1];
//		    console.log('toUrl>>>>'+toUrl);
//		    console.log('thisFromUrl>>>>'+thisFromUrl);
//		    console.log('pasteFolderPath>>>'+pasteFolderPath);
			var fwrite;
			jQuery.Dav(thisFromUrl).copy({
				complete : function(dat, stat) {;
					//callbackFn(returnValue);
				},
				Destination_url : toUrl,
				Overwrite : fwrite,
				async : true,
				success : function(dat, stat) {
					//if (onCheckRcvLoginRequired(dat)) {
					//	onRcvError();
					//	return;
					//}
					//alert('copy success');
					mangoAPI.updateAddData2(element, pasteFolderPath);
//					console.log('isCut>>>'+isCut);
					if(isCut == true){///// cut일경우 원본 삭제
//						console.log(thisFromUrl);
					jQuery.Dav(thisFromUrl).remove({
						complete : function(dat, stat) {
							//;;;console.log('delete file');
							//callbackFn(returnValue);
							//removeTagArray.push(removeTag);
							//removeTag.remove();
							//rootTag.pop().remove();
							selectItemNum = 0;
						},
						async : true, //p10789
						success : function(dat, stat) {
							alert("Cut Paste Success!");
							returnValue = 1;
						},
						error : function(dat, stat) {
							//if(onCheckRcvLoginRequired(dat.responseText)) {
							//	onRcvError();
							//	return;
							//}
							if (dat.status == 204) {// for IE
								returnValue = 1;
							} else {
								//returnValue = returnHttpErrorValue(dat.responseText);
							}
						}
					});
					}
					else{
						alert("Copy Paste Success!");
						returnValue = 1;
					}
				},
				error : function(dat, stat) {
					
					if(isCut == true){
						alert('cut paste fail');
						return;
					}
					else
					{
						alert('copy paste fail');
						return;
					}
					
//					if (onCheckRcvLoginRequired(dat.responseText)) {
//						onRcvError();
//					}
//					returnValue = returnHttpErrorValue(dat.responseText);
				}
			});
		});
	},
	
	contextMenuEdit:function(evt){
//		console.log(evt);
		var targetTag;
		if($(evt).attr('id') != 'MainFileView')
		{ 
			$('#MediaThumbList li').each(function() {
				if($(this).attr('select') == 'selected'){
					var sourceName = $(this).find('span').html(); // 원본 이름 추출
					var reName; // 새 이름
					var sourceUrl; // 원본 소스 주소
					var renameUrl; // 새이름으로 변경할 소스 주소
					var selectItem = $(this);
					$(this).find('span').hide(); // span태그 숨김..
					$(this).children('.mediaType').append("<div><input type='text' name='form1' style='bottom:5px; position: absolute; width:100px;' value='"+sourceName+"'></div>"); // input 태그 삽입
					$('input').on('keydown', function(e){
						 var key = e.which;
						  if(key == 13){ // 엔터키 치면..
							  var inputTag = $(this);
							  reName = $(this).val();
							  if($(evt).attr('id') != 'MainFileView')
							  { 
								 sourceUrl =  'webdav'+selectItem.find('img').attr('src').substring(5+rootPath_len, selectItem.find('img').attr('src').length);
								 renameUrl =  'webdav'+selectItem.find('img').attr('src').substring(5+rootPath_len, selectItem.find('img').attr('src').length - sourceName.length)+reName;
							  }
							  else
							  {
								  sourceUrl =  'webdav'+selectItem.find('a').attr('rel').substring(7, selectItem.find('a').attr('rel').length);
								  renameUrl =  'webdav'+selectItem.find('a').attr('rel').substring(7, selectItem.find('a').attr('rel').length - sourceName.length)+reName;
							  }
							  jQuery.Dav(sourceUrl).rename({
									complete:  function(dat, stat) {
											//;;;console.log('#rename');
											//callbackFn(returnValue);
//										console.log('complete');
										inputTag.remove(); // input 태그 없애기
										selectItem.find('span').html(reName); // a태그 내용 수정
										selectItem.find('span').show(); // a태그 보이기
										
										},
										Destination_url:renameUrl,
										cache: false,
										async: true,
										success:  function(dat, stat) {
//											if(onCheckRcvLoginRequired(dat)) {
//												onRcvError();
//												return;
//											}
											returnValue = 1;
										},
										error:  function(dat, stat) {
//											console.log('error');
//											if(onCheckRcvLoginRequired(dat.responseText)) {
//												onRcvError();
//												return;
//											}
//											returnValue = returnHttpErrorValue(dat.responseText);     
										}
									}); 
						  }
						  else if(key == 27)
						 {
							$(this).remove(); // input 태그 없애기
							selectItem.find('span').show(); // a태그 보이기
						 }
					});
				}
			});
		}
		else
		{
			$('#MainFileView li').each(function() {
				if($(this).attr('select') == 'selected'){
					var sourceName = $(this).find('a').html(); // 원본 이름 추출
					var reName; // 새 이름
					var sourceUrl; // 원본 소스 주소
					var renameUrl; // 새이름으로 변경할 소스 주소
					var selectItem = $(this);
					$(this).find('a').hide(); // a태그 숨김..
					$(this).append("<input type='text' name='form1' value='"+sourceName+"'>"); // input 태그 삽입
					$('input').on('keydown', function(e){
						 var key = e.which;
						  if(key == 13){ // 엔터키 치면..
							  var inputTag = $(this);
							  reName = $(this).val();
							  if($(evt).attr('id') != 'MainFileView')
							  { 
								 sourceUrl =  'webdav'+selectItem.find('img').attr('src').substring(5+rootPath_len, selectItem.find('img').attr('src').length);
								 //renameUrl =  'webdav'+
							  }
							  else
							  {
								  sourceUrl =  'webdav'+selectItem.find('a').attr('rel').substring(7, selectItem.find('a').attr('rel').length);
								  renameUrl =  'webdav'+selectItem.find('a').attr('rel').substring(7, selectItem.find('a').attr('rel').length - sourceName.length)+reName;
							  }
							  jQuery.Dav(sourceUrl).rename({
									complete:  function(dat, stat) {
											//;;;console.log('#rename');
											//callbackFn(returnValue);
//										console.log('complete');
										inputTag.remove(); // input 태그 없애기
										selectItem.find('a').html(reName); // a태그 내용 수정
										selectItem.find('a').show(); // a태그 보이기
										
										},
										Destination_url:renameUrl,
										cache: false,
										async: true,
										success:  function(dat, stat) {
//											if(onCheckRcvLoginRequired(dat)) {
//												onRcvError();
//												return;
//											}
											returnValue = 1;
										},
										error:  function(dat, stat) {
//											console.log('error');
//											if(onCheckRcvLoginRequired(dat.responseText)) {
//												onRcvError();
//												return;
//											}
//											returnValue = returnHttpErrorValue(dat.responseText);     
										}
									}); 
						  }
						  else if(key == 27)
						 {
							$(this).remove(); // input 태그 없애기
							selectItem.find('a').show(); // a태그 보이기
						 }
					});
				}
			});
		}
	},
	
	contextMenuDelete:function(evt){
		var check = confirm("Delete?!");
		if(check == true){
		var removeTagArray = new Array();
		var relativePath;;
		var rootTag = new Array();
		$('#filelist [select]').each(function() {
			if($(this).attr('select') == 'selected'){
				rootTag.push($('#fileTree a[rel = "'+ $(this).find('a').attr('rel') +'"]').parent());
				if($(evt).attr('id') != 'MainFileView')
				  { 
					relativePath =  $(this).find('img').attr('src').substring(5+rootPath_len, $(this).find('img').attr('src').length);
				  }
				else
				{
					relativePath = $(this).find('a').attr('rel').substring(7, $(this).find('a').attr('rel').length); 
				}
				var removeTag = $(this);
				
				var deleteUrl = 'webdav'+relativePath;
				//alert(deleteUrl);
				
				jQuery.Dav(deleteUrl).remove({
					complete : function(dat, stat) {
						//;;;console.log('delete file');
						//callbackFn(returnValue);
						//removeTagArray.push(removeTag);
						removeTag.remove();
						rootTag.pop().remove();
						if(rootTag.length == 0)
							alert("Delete Success!");
						selectItemNum = 0;
					},
					async : true, //p10789
					success : function(dat, stat) {
						returnValue = 1;
					},
					error : function(dat, stat) {
						//if(onCheckRcvLoginRequired(dat.responseText)) {
						//	onRcvError();
						//	return;
						//}
						if (dat.status == 204) {// for IE
							returnValue = 1;
						} else {
							returnValue = returnHttpErrorValue(dat.responseText);
						}
					}
				});
			}
		});
						}
		else
			return false;
	},
	contextMenuPlay:function(evt){
		var url;
		
		if($('.mediaType').attr('id')=='video')
		{
			$('#filelist [select]').each(function() {
				if($(this).attr('select') == 'selected'){
					url = $(this).find('img').attr('src').substring(5, $(this).find('img').attr('src').length);	
				}
			});
		
			var temp = url.split('/');
			var title = temp[temp.length-1]; // 파일명 추출
			var temp2 = title.split('.');
			var ext = temp2[temp2.length-1]; //확장자 추출
			
			if(ext=="mp4" || ext=="flv" || ext=="3gp")
			{
				var msg = '<div id="container">Loading the player ...</div>'+
				'<script type="text/javascript">'+
				'jwplayer("container").setup({'+
				'flashplayer: "js/jwplayer/player.swf",'+
				'file: "oriVideo'+url+'",'+
				'height: 480,'+
				'width: 640'+
				'});'+
				'</script>';
				$.smartPop.open({ background: "gray", width: 655, height: 500, html: msg  });
			}
			else
				alert("Not Support Format");
		}
		else if($('.mediaType').attr('id')=='audio'){
		$("#jquery_jplayer_1").jPlayer("destroy"); // 리셋하고	
		$('#filelist [select]').each(function() {
			if($(this).attr('select') == 'selected'){
				url = "oriAudio"+$(this).find('img').attr('src').substring(5, $(this).find('img').attr('src').length);	
			}
		});
		
		var temp = url.split('/');
		var title = temp[temp.length-1]; // 파일명 추출
		var temp2 = title.split('.');
		var ext = temp2[temp2.length-1]; //확장자 추출
		
		if(ext=="mp3" || ext=="wav" || ext=="wma")
		{
			$('.jp-title li').html(title);
			$("#jquery_jplayer_1").jPlayer({
		        ready: function () {
		          //audioPlaylist.playlistInit(true);
		          $(this).jPlayer("setMedia", {
		        	mp3: url,
		            // m4a: "http://www.jplayer.org/audio/m4a/Miaow-07-Bubble.m4a",
		            //oga: "http://www.jplayer.org/audio/ogg/Miaow-07-Bubble.ogg"
		          }).jPlayer("play");
		        },
		        swfPath: "js/jPlayer/",
		        supplied: "mp3, m4a, oga, flv",
		        solution: "html,flash",
		        wmode: "window"
		      });
	    }
	    else
	    	alert("Not Support Format.");
		}
	},
	contextMenuWallPaper: function(evt)
	{
		var path;
		$('#filelist [select]').each(function() {
			if($(this).attr('select') == 'selected'){
				path = $(this).find('img').attr('src').substring(5, $(this).find('img').attr('src').length);		
			}
		});
		$.ajax({
			type : 'Post',
			url : 'WallPaperSet.do',
			data: 'path='+path,
			dataType : 'text',
			success : function(text) {
					if(text == 'Success')
					{
						alert('WallPaper Change!! .');
					}
					else if(text =='Fail')
					{
						alert('Can not Change WallPaper');
					}
				}	
			});
	},
	
	contextMenuNewFolder:function(){
		var folderName = prompt('Enter the name of the folder');
		
		var relativePath = $('#MainFileView ul').attr('path').substring(7, $('#MainFileView ul').attr('path').length);
		//var createUrl = '/Mangosteen/webdav'+;
		var createUrl = 'webdav'+relativePath;
		createUrl += folderName;
		//alert(createUrl);

		jQuery.Dav(createUrl).createFolder({
			complete : function(dat, stat) {
				//;;;console.log('#createFolder');
				//callbackFn(returnValue);
			},
			async : false,
			success : function(dat, stat) {
				//alert('dat' + dat);
				/*if (onCheckRcvLoginRequired(dat)) {
				 onRcvError();
				 return;
				 }*/
				
				returnValue = 1;
			},
			error : function(dat, stat) {
				alert('error');
				/*if (onCheckRcvLoginRequired(dat.responseText)) {
				 onRcvError();
				 return;
				 }*/
				//returnValue = returnHttpErrorValue(dat.responseText);
			}
		});
	},
	contextMenuRingtone: function(type){
		var path;
		$('#filelist [select]').each(function() {
			if($(this).attr('select') == 'selected'){
				path = $(this).find('img').attr('src').substring(5, $(this).find('img').attr('src').length);		
			}
		});
		$.ajax({
			type : 'Post',
			url : 'RingtoneSet',
			data: {path: path, type:type},
			dataType : 'text',
			success : function(text) {
					if(text == 'Success')
					{
						alert(type+'Change!!.');
					}
					else if(text =='Fail')
					{
						alert(type+'Can not Change!');
					}
				}	
			});
	},
	
	imgOnload:function()
	{
		$('.mediaType').css('background',''); 
	},
	contextMenuDownload:function(evt)
	{
		$('#filelist [select]').each(function() {
			if($(this).attr('select') == 'selected'){
				
				if($(evt).attr('id') != 'MainFileView')
				{ 
					path = "path="+$(this).find('img').attr('src').substring(6, $(this).find('img').attr('src').length);
				}
				else
				{
					//path = "path=mnt"+$(this).find('a').attr('rel').substring(7, $(this).find('a').attr('rel').length);
					path = "path=mnt"+$(this).find('a').attr('rel');
				}
				$('body').append(
			    $('<iframe>', {
			    	id: 'fileDown',
			        src: 'FileDownloadServlet?'+path 
			    	}).hide()
				);
				
//				$.ajax({
//					type : 'GET',
//					url : 'FileDownloadServlet?'+path,
//					dataType : 'data',
//					success : function(data) {
//						
//					}	
//				});
			}
		});
	}
});
