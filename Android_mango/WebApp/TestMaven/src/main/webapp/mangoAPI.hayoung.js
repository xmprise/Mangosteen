var req;
var fromUrl;
var toUrl;
var copySourceArray;
var cutSourceArray;
var selectItemNum = 0;
jQuery.extend(mangoAPI, {
	
	// 파일 탐색기의 버튼 함수 모음
	fileButtons : {

		//파일 탐색기의 전체 선택 체크박스 체크 했을 때
		bindSelectAllBtn : function(target) {
			$("[name=selectAll]").bind('click', function(event) {
				// name이 check_all 인 체크박스가 checked 가 되어 있다면
				if ($("[name=selectAll]").is(":checked")) {
					// class는 box_class 인 체크박스의 속성 checked는 checked이다
					$(target).attr("select", "selected");
					$(target).css('background-color','yellow'); //색상 변경
				} else {// 그렇지 않으면
					// class는 box_class인 체크박스의 속성 checked 는 "" 공백이다
					$(target).attr("select", "unselect");
					$(target).css('background-color',''); // 기본 색상
				}
			});
		},

		// 각 아이템의 선택 체크박스 체크 했을 때
		bindItemBtn : function(mediaType) {
			// 아이템 더블 클릭 했을 때 이벤트
			$('.thumb.element').on("dblclick", function(e){
				// 미디어 타입에 따라 다른 동작
				var mediaPath = $(this).find('img').attr('src').substring(5, $(this).find('img').attr('src').length);	
				switch(mediaType)
				{
				case 'image':
					var msg = '<img src="oriImage'+mediaPath+'"></img>';
					$.smartPop.open({ background: "red", width: 500, height: 500, html: msg });
					break;
				case 'video':
					var msg = '<video src="oriVideo'+mediaPath+'" loop="true" autoplay="true"></video>';
					$.smartPop.open({ background: "red", width: 500, height: 500, html: msg });
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
						$(this).css('background-color','yellow'); //색상 변경
						selectItemNum++;
					}
					else
					{
						$(this).attr('select','unselect'); //선택 됐으면
						$(this).css('background-color',''); // 기본 색상
						selectItemNum--;
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
				alert(createUrl);

				jQuery.Dav(createUrl).createFolder({
					complete : function(dat, stat) {
						//;;;console.log('#createFolder');
						callbackFn(returnValue);
					},
					async : false,
					success : function(dat, stat) {
						alert('dat' + dat);
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
						alert(deleteUrl);

						jQuery.Dav(deleteUrl).remove({
							complete : function(dat, stat) {
								//;;;console.log('delete file');
								callbackFn(returnValue);
							},
							async : true, //p10789
							success : function(dat, stat) {
								alert('dat' + dat);

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
						var relativePath =  $(this).parent().find('img').attr('src').substring(16, $(this).parent().find('img').attr('src').length);
						var removeTag = $(this).parent().parent();
						
						var deleteUrl = 'webdav'+relativePath;
						
						alert(deleteUrl);
						
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
						
						alert(fromUrl);
					}
				});
			});
		},
		
		bindCutBtn : function() {
			$('span.btn_cut').bind('click', function(event) {
				
			});
		},
		
		//파일 탐색기의 붙여넣기 버튼 눌렸을 때
		bindPasteBtn : function(){
			$('span.btn_paste').bind('click', function(event) {
				var name = fromUrl.split("/");
			    alert(name[0]);
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
						alert('copy success');
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
			alert(path);
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
	$("#MediaThumbList ul").append($("<li class='thumb element' select='unselect'><div class='mediaType' folderPath='"+val.path+"'id='"+val.type+"'><img class='perMediaThumb' src='"+val.type+val.path+val.name+"' />" + 
	"<span>"+val.name+"</span>" +						
		"</div></li>"));		
		// 이부분 최적화 하기...  개별아이템마다 호출해서.. 비효율.. 마지막 아이템 뿌려질때 한번만 호출하게 만들어보기
		mangoAPI.fileButtons.unBindItemBtn();
		mangoAPI.fileButtons.bindItemBtn(val.type);
	},
	
	updateAddData2: function(evt, pasteFolderPath){
		var copySource = copySourceArray.pop();
		var rootTag;
		var parentPath;
		 if($(evt).attr('id') != 'MainFileView')
		 {
			 $("#MediaThumbList ul").append($("<li class='thumb element' select='unselect'><div class='mediaType' folderPath='"+copySource.folderpath+"'id='"+copySource.id+"'><img class='perMediaThumb' src='"+copySource.src+"' />" + 
						"<span>"+copySource.name+"</span>" +						
							"</div></li>"));
							if(copySource.length == 0)
							{
								mangoAPI.fileButtons.bindItemBtn(val.type);
							}
		 }
		 else
		 {
			 parentPath = copySource.folderpath;
			 rootTag = $('#fileTree a[rel = "'+ pasteFolderPath +'"]').parent();
			 console.log(rootTag);
			 $('#MainFileView ul').append("<li class='" +copySource.className  +"' select='unselect'><a href='javascript:;' rel='" +copySource.src+ "'>"+ copySource.name + "</a></li>");
				rootTag.find('ul').append("<li class='" +copySource.className  +"'><a href='javascript:;' rel='" + copySource.src + "'>"+ copySource.name + "</a></li>");
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
		var rootTag = $('#fileTree a[rel = "'+ val.path +'"]').parent();
		var parentPath = val.path;
		$('#MainFileView ul').append("<li class='file ext_" + ext[1] +"' select='unselect'><a href='javascript:;' rel='" + val.path +val.name+ "'>"+ val.name + "</a></li>");
		rootTag.find('ul').append("<li class='file ext_" + ext[1] +"'><a href='javascript:;' rel='" + val.path+val.name + "'>"+ val.name + "</a></li>");
		
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
		fromUrl = new Array();
		var relativePath;
		copySourceArray = new Array();
		$('#filelist [select]').each(function() {
			if($(this).attr('select') == 'selected'){
				var copySource = new Object();
				
				if($(evt).attr('id') != 'MainFileView')
				{ 
					relativePath =  $(this).find('img').attr('src').substring(16, $(this).find('img').attr('src').length);
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
				copySourceArray.push(copySource);
			}
		});
	},
	
	contextMenuCut:function(evt){
		fromUrl = new Array();
		$('.thumb.element').each(function() {
			if($(this).attr('select') == 'selected'){
				var relativePath =  $(this).find('img').attr('src').substring(16, $(this).find('img').attr('src').length);
				fromUrl.push('webdav'+relativePath);
			}
		});
	},

	contextMenuPaste:function(element){
		$(fromUrl).each(function(){
			var name = this.split("/");
			var pasteFolderPath;
		    alert(name[0]);
		    
		    if($(element).attr('id') != 'MainFileView')
			{ 
		    	toUrl = $(element).find('.mediaType').attr('folderpath').substring(12, $(element).find('.mediaType').attr('folderpath').length)+name[name.length -1];
			}
		    else
		    {
		    	pasteFolderPath = $('#MainFileView ul').attr('path');
		    	if(name[name.length-1] == "")
		    		toUrl = $('#MainFileView ul').attr('path').substring(7, $('#MainFileView ul').attr('path').length)+name[name.length -2];
		    	else
		    		toUrl = $('#MainFileView ul').attr('path').substring(7, $('#MainFileView ul').attr('path').length)+name[name.length -1];
		    }
		    console.log(toUrl);
		    
			//toUrl = $('#MainFileView ul').attr('path').substring(7, $('#MainFileView ul').attr('path').length)+name[name.length -1];
			var fwrite;
			jQuery.Dav(this).copy({
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
					alert('copy success');
					mangoAPI.updateAddData2(element, pasteFolderPath);
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
	
	contextMenuEdit:function(evt){
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
							 sourceUrl =  'webdav'+selectItem.find('img').attr('src').substring(16, selectItem.find('img').attr('src').length);
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
									console.log('complete');
									inputTag.remove(); // input 태그 없애기
									selectItem.find('a').html(reName); // a태그 내용 수정
									selectItem.find('a').show(); // a태그 보이기
									
									},
									Destination_url:renameUrl,
									cache: false,
									async: true,
									success:  function(dat, stat) {
//										if(onCheckRcvLoginRequired(dat)) {
//											onRcvError();
//											return;
//										}
										returnValue = 1;
									},
									error:  function(dat, stat) {
										console.log('error');
//										if(onCheckRcvLoginRequired(dat.responseText)) {
//											onRcvError();
//											return;
//										}
//										returnValue = returnHttpErrorValue(dat.responseText);     
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
	},
	
	contextMenuDelete:function(evt){
		var removeTagArray = new Array();
		var relativePath;;
		var rootTag = new Array();
		$('#filelist [select]').each(function() {
			if($(this).attr('select') == 'selected'){
				rootTag.push($('#fileTree a[rel = "'+ $(this).find('a').attr('rel') +'"]').parent());
				if($(evt).attr('id') != 'MainFileView')
				  { 
					relativePath =  $(this).find('img').attr('src').substring(16, $(this).find('img').attr('src').length);
				  }
				else
				{
					relativePath = $(this).find('a').attr('rel').substring(7, $(this).find('a').attr('rel').length); 
				}
				var removeTag = $(this);
				
				var deleteUrl = 'webdav'+relativePath;
				alert(deleteUrl);
				
				jQuery.Dav(deleteUrl).remove({
					complete : function(dat, stat) {
						//;;;console.log('delete file');
						//callbackFn(returnValue);
						//removeTagArray.push(removeTag);
						removeTag.remove();
						rootTag.pop().remove();
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
});
