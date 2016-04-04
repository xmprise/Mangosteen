$(document).ready (function () {

$(document).on("click", ".Menu",	function(e) {
							var id = "id=" + this.id;
							var url = this.id + ".do";
							$('#filelist * > *').remove();
							$('#mediaTool').remove();
							$('#explorerMenu').remove();
							$('#MainPhoneInfo').remove();
							$('[class=contextMenu]').remove();
							selectItemNum = 0;
							$('#fileTree').css({'overflow-x': ''});
							if (this.id == "FileExplorer") {
								$('#fileTree').css({'overflow-x': 'scroll'});
								// 이 부분에서 root 폴더 데이터를 보안상 문제가 있어 보이네..
								// 그리고 메인 페이지 로드시(중계서버에서 요청 할때 아이디에 맞는 페이지 로드)...
								// 각 아이디에 설정된 권한에 따라 설정된 폴더 값들 받아와서 fileTree 매개변수로 넘겨주기... 추후 구현
								
							        $("<div id='mediaTool' style='float: left'>" +
							        	"<div id='file-uploader-demo1'>" +		
										"<noscript>" +			
											"<p>Please enable JavaScript to use file uploader.</p>" +
											"<!-- or put a simple form for upload here -->" +
										"</noscript>" +         
									"</div>" +
									"<div class='icon-btns'>" +
									"<div class='btn-select-all i-cursor-hand'>" +
									"<span class='checkbox i-float-left' check='0'></span>" +
									"<span class='i-float-left ml6'>Select all</span></div>" +
									"<button type='button' class='disabled' menu='OpenFolder' id='openFolder' title='Open Folder'>"+
										"<span class='btn-openfolder'></span> </button>"+
									"<button type='button' id='newFolderBtn' title='New folder'>"+
										"<span class='icon-btn btn-newfolder'></span> </button>"+
									"<button type='button' class='disabled' menu='Rename' title='Rename'>"+
										"<span class='icon-btn btn-rename'></span></button>"+
									"<button type='button' class='disabled' menu='Cut' title='Cut'>"+
										"<span class='icon-btn btn-cut'></span></button>"+
									"<button type='button' class='disabled' menu='Copy' title='Copy'>"+
										"<span class='icon-btn btn-copy'></span></button>"+
									"<button type='button' class='disabled' menu='Paste' title='Paste'>"+
										"<span class='icon-btn btn-paste'></span></button>"+
									"<button type='button' class='disabled' menu='Delete' title='Delete'>"+
										"<span class='icon-btn btn-delete'></span></button>"+
									"<button type='button' class='disabled' menu='Play' title='Play'>"+
										"<span class='icon-btn btn-play'></span></button>"+
									"<button type='button' class='disabled' menu='WallPaper' title='WallPaper'>"+
										"<span class='icon-btn btn-wallpaper'></span></button>"+
									"<button type='button' class='disabled' menu='Ringtone' title='Ringtone'>"+
										"<span class='icon-btn btn-ringtone'></span></button>"+
									"<button type='button' class='disabled' menu='Download' title='Download'>"+
										"<span class='icon-btn btn-download'></span></button>"+
									"<button type='button' class='disabled' menu='Expand' title='Expand'>"+
										"<span class='icon-btn btn-expand'></span></button></div>"+
									"<div class='qq-upload-extra-drop-area'>Drop files here too</div>" +
							    	"</div>").insertBefore('#fileTree');
							    	
							    	mangoAPI.createUploader();
							    	
							       $("<div id='downloader'>" +
							    	"<div class='qq-download-button' style='position: relative; overflow: hidden; direction: ltr; '>Download a file</div>").insertBefore($('.qq-uploader'));
							        
							        
							        $('body').append($("<ul id='myMenu' class='contextMenu'>"+
							        						"<li class='download disabled'><a href='#download'>DownLoad</a></li>" +
							        						"<li class='upload'><a href='#upload'>UpLoad</a></li>" +
							        						"<li class='newfolder separator'><a href='#newfolder'>NewFolder</a></li>" +
															"<li class='edit disabled'><a href='#edit'>Edit</a></li>" +
															"<li class='cut disabled'><a href='#cut'>Cut</a></li>" +
															"<li class='copy disabled'><a href='#copy'>Copy</a></li>" +
															"<li class='paste disabled'><a href='#paste'>Paste</a></li>" +
															"<li class='delete disabled'><a href='#delete'>Delete</a></li></ul>"));
							       
//							    mangoAPI.fileButtons.bindAddFolderBtn();
//							    mangoAPI.fileButtons.bindCopyBtn();
//							    mangoAPI.fileButtons.bindPasteBtn();
//							    mangoAPI.fileButtons.bindDeleteBtn();
								$('#fileTree').fileTree({
									root : '/sdcard/',
									script : 'FileExplorer.do'
								}, function(file) {
									alert(file);
								});
							} else if(this.id == "image" || this.id == "video" || this.id == "audio") {
								var mediaType = this.id;
								//mangoAPI.contextMenuDisable($('#filelist')); // 컨텍스트 메뉴 언바이딩
								$.ajax({
											type : 'POST',
											url : url,
											data : id,
											dataType : 'json',
											success : function(json) {
												//json 메세지 중에 어떤 id 값인지 반환 받고 그 id값 태그에 
												$('#filelist * > *').remove();
												$("#MediaThumbList").append("<ul style=list-style-type:'none'></ul>");
												$(json).each(function(key, val) {
																	$("#MediaThumbList ul").append(
																					$("<li class='thumb element'><div class='mediaType' id='"+val.type+"' style='background-image : URL(ImgResource/loading.gif)' ><img onload='javascript:mangoAPI.imgOnload();' class='perFolderThumb' id='"+val.folderPath+"' src='"+val.type+val.thumbPath+"' />" +
																							"<span id='folderName'>"+ val.folderName +"</span>"+ 
																							"<span id='fileOfNum'>("+val.fileOfNum+")</span></div></li>"));
																});
												
												// 각 미디어 타입에 따라서 메뉴 구성 다르게..
												if(mediaType == 'image')
												{
													$('body').append($("<ul id='myMenu' class='contextMenu'>"+
															"<li class='download disabled'><a href='#download'>DownLoad</a></li>" +
							        						"<li class='upload'><a href='#upload'>UpLoad</a></li>" +
															"<li class='edit separator'><a href='#edit'>Edit</a></li>" +
															"<li class='cut '><a href='#cut'>Cut</a></li>" +
															"<li class='copy'><a href='#copy'>Copy</a></li>" +
															"<li class='paste'><a href='#paste'>Paste</a></li>" +
															"<li class='delete'><a href='#delete'>Delete</a></li>" +
															"<li class='wallpaper separator'><a href='#wallpaper'>WallPaper</a></li></ul>"));
												}
												else if(mediaType == 'video')
												{
													$('body').append($("<ul id='myMenu' class='contextMenu'>"+
															"<li class='download disabled'><a href='#download'>DownLoad</a></li>" +
							        						"<li class='upload'><a href='#upload'>UpLoad</a></li>" +
															"<li class='edit separator'><a href='#edit'>Edit</a></li>" +
															"<li class='cut'><a href='#cut'>Cut</a></li>" +
															"<li class='copy'><a href='#copy'>Copy</a></li>" +
															"<li class='paste'><a href='#paste'>Paste</a></li>" +
															"<li class='delete'><a href='#delete'>Delete</a></li>" +
															"<li class='play separator'><a href='#play'>Play</a></li></ul>"));
												}
												else if(mediaType == 'audio')
												{
													$('body').append($("<ul id='myMenu' class='contextMenu'>"+
															"<li class='download disabled'><a href='#download'>DownLoad</a></li>" +
							        						"<li class='upload'><a href='#upload'>UpLoad</a></li>" +
															"<li class='edit separator'><a href='#edit'>Edit</a></li>" +
															"<li class='cut'><a href='#cut'>Cut</a></li>" +
															"<li class='copy'><a href='#copy'>Copy</a></li>" +
															"<li class='paste'><a href='#paste'>Paste</a></li>" +
															"<li class='delete'><a href='#delete'>Delete</a></li>" +
															"<li class='play separator'><a href='#play'>Play</a></li>" +
															"<li class='ringtone separator'><a href='#ringtone'>Ringtone</a></li>"+
															"<li class='notification'><a href='#notification'>Notifi</a></li>"+
															"<li class='alarm'><a href='#alarm'>Alarm</a></li></ul>"));
												}
												//mangoAPI.contextMenuBind('perFolderThumb');
												$('.thumb.element').on("click", function(e){
													var url = 'SelectFolderList.bo';
													var param = 'path='+$(this).find('.perFolderThumb').attr('id')+'&type='+$(this).find('.mediaType').attr('id');
													var path = $(this).find('.perFolderThumb').attr('id');
													sendAjax(url, param, path, mediaType);
												});
											}
										});
							}
							else if(this.id == "mjpeg")
							{
								var requestUrl = location.href;
								var index = requestUrl.indexOf('55556');
								var url = requestUrl.substring(0, index);
								$('#filelist').children('.mjpeg').remove();
								$('#filelist').children('.hls').remove();
								$('#filelist').append("<div class='mjpeg' style='margin:0px auto; width:670px;height:490px;'><iframe src='"+url+"8555' width='670px' height='490px' frameborder=0></iframe></div>");
								//$.smartPop.open({ background: "gray", width: 640, height: 480, url:url+"8555" });
							}
							else if(this.id == "hls")
							{
								var requestUrl = location.href;
								var index = requestUrl.indexOf('55556');
								var url = requestUrl.substring(0, index);
								$('#filelist').children('.hls').remove();
								$('#filelist').children('.mjpeg').remove();
								$('#filelist').append("<div class='hls' style='margin:0px auto; width:352px;height:288px;margin-top:100px'><iframe src='"+url+"55556/Mangosteen/hls/video.m3u8' width='352px' height='288px' frameborder=0></iframe></div>");	
							
								//$.smartPop.open({ background: "gray", width: 352, height: 288,url:"hls/video.m3u8" });
//								$.ajax({
//									type : 'GET',
//									url : "/hls/video.m3u8",
//									dataType : 'data',
//									success : function(data) {
//										
//										
//									}
//								});
							}
						});

	function sendAjax(url, param, path, mediaType) // type은 메뉴 버튼 중 image/video/audio 세 가지중 하나, 타입을 받는 이유는 타입 별로 업로드 제한 하기 위해서
	{
		$.ajax({
			type : 'POST',
			url : url,
			data : param,
			dataType : 'json',
			success : function(json) {
				//json 메세지 중에 어떤 id 값인지 반환 받고 그 id값 태그에 
				$('#filelist * > *').remove();
				$("<div id='mediaTool'>"+
					"<div id='file-uploader-demo1'>" +		
					"<noscript>" +			
					"<p>Please enable JavaScript to use file uploader.</p>" +
					"<!-- or put a simple form for upload here -->" +
					"</noscript>" +         
					"</div>" +
					"<div class='icon-btns'>" +
					"<div class='btn-select-all i-cursor-hand'>" +
					"<span class='checkbox i-float-left' check='0'></span>" +
					"<span class='i-float-left ml6'>Select all</span></div>" +
					"<button type='button' class='disabled' id='newFolderBtn' title='New folder'>"+
						"<span class='icon-btn btn-newfolder'></span> </button>"+
					"<button type='button' class='disabled' menu='Rename' title='Rename'>"+
						"<span class='icon-btn btn-rename'></span></button>"+
					"<button type='button' class='disabled' menu='Cut' title='Cut'>"+
						"<span class='icon-btn btn-cut'></span></button>"+
					"<button type='button' class='disabled' menu='Copy' title='Copy'>"+
						"<span class='icon-btn btn-copy'></span></button>"+
					"<button type='button' class='disabled' menu='Paste' title='Paste'>"+
						"<span class='icon-btn btn-paste'></span></button>"+
					"<button type='button' class='disabled' menu='Delete' title='Delete'>"+
						"<span class='icon-btn btn-delete'></span></button>"+
					"<button type='button' class='disabled' menu='Play' title='Play'>"+
						"<span class='icon-btn btn-play'></span></button>"+
					"<button type='button' class='disabled' menu='WallPaper' title='WallPaper'>"+
						"<span class='icon-btn btn-wallpaper'></span></button>"+
					"<button type='button' class='disabled' menu='Ringtone' title='Ringtone'>"+
						"<span class='icon-btn btn-ringtone'></span></button>"+
					"<button type='button' class='disabled' menu='Download' title='Download'>"+
						"<span class='icon-btn btn-download'></span></button>"+
					"<button type='button' class='disabled' menu='Expand' title='Expand'>"+
						"<span class='icon-btn btn-expand'></span></button></div>"+
					"<div class='qq-upload-extra-drop-area'>Drop files here too</div>" +
					"</div>").insertBefore('#MediaThumbList');
				
				mangoAPI.createUploader();
				
				$("<div id='downloader'>" +
	        	"<div class='qq-download-button' style='position: relative; overflow: hidden; direction: ltr; '>Download a file</div>").insertBefore($('.qq-uploader'));
				
				mangoAPI.fileButtons.bindMediaDeleteBtn();
				$("#MediaThumbList").append("<ul style=list-style-type:'none'></ul>");
				
				// image일 때만 <a> 태그 붙여줘서 fancybox 적용
				if(mediaType == 'image')
				{
					$(json).each(function(key, val) {
						var src = val.type+val.path;
						var mediaPath = 'oriImage'+src.substring(5, src.length);
										$("#MediaThumbList ul").append($("<li class='thumb element' select='unselect'><div class='mediaType' folderPath='"+path+"'id='"+val.type+"' style='background-image : URL(ImgResource/loading.gif)'><a class='fancybox' rel='group' href='"+mediaPath+"'><img onload='javascript:mangoAPI.imgOnload();' class='perMediaThumb' src='"+src+"' /></a>" + 
											"<span id='fileName'>"+val.name+"</span></div></li>"));
									});
				}
				else
				{
					$(json).each(function(key, val) {
						var src = val.type+val.path;
										$("#MediaThumbList ul").append($("<li class='thumb element' select='unselect'><div class='mediaType' folderPath='"+path+"'id='"+val.type+"' style='background-image : URL(ImgResource/loading.gif)'><img onload='javascript:mangoAPI.imgOnload();' class='perMediaThumb' src='"+src+"' />" + 
											"<span id='fileName'>"+val.name+"</span></div></li>"));
									});
				}

				//컨텍스트 메뉴 바인딩
				mangoAPI.contextMenuBind($('#filelist'));
				// 각 아이템에 대한 이벤트등록
				mangoAPI.fileButtons.bindItemBtn(mediaType);
				mangoAPI.fileButtons.bindSelectAllBtn('#MediaThumbList LI');
				
				//fancybox 바인딩?
				$(".fancybox").fancybox();
				
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
							$('Button[menu="WallPaper"]').removeClass('disabled');
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
							$('Button[menu="WallPaper"]').removeClass('disabled');
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
			}
		});
	}
}); 