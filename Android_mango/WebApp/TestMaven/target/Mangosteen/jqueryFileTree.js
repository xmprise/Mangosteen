// jQuery File Tree Plugin
//
// Version 1.01
//
// Cory S.N. LaViska
// A Beautiful Site (http://abeautifulsite.net/)
// 24 March 2008
//
// Visit http://abeautifulsite.net/notebook.php?article=58 for more information
//
// Usage: $('.fileTreeDemo').fileTree( options, callback )
//
// Options:  root           - root folder to display; default = /
//           script         - location of the serverside AJAX file to use; default = jqueryFileTree.php
//           folderEvent    - event to trigger expand/collapse; default = click
//           expandSpeed    - default = 500 (ms); use -1 for no animation
//           collapseSpeed  - default = 500 (ms); use -1 for no animation
//           expandEasing   - easing function to use on expand (optional)
//           collapseEasing - easing function to use on collapse (optional)
//           multiFolder    - whether or not to limit the browser to one subfolder at a time
//           loadMessage    - Message to display while initial tree loads (can be HTML)
//
// History:
//
// 1.01 - updated to work with foreign characters in directory/file names (12 April 2008)
// 1.00 - released (24 March 2008)
//
// TERMS OF USE
// 
// This plugin is dual-licensed under the GNU General Public License and the MIT License and
// is copyright 2008 A Beautiful Site, LLC. 
//
if(jQuery) (function($){
	
	$.extend($.fn, {
		fileTree: function(o, h) {
			// Defaults
			if( !o ) var o = {};
			if( o.root == undefined ) o.root = '/';
			if( o.script == undefined ) o.script = 'jqueryFileTree.php';
			if( o.folderEvent == undefined ) o.folderEvent = 'dblclick';
			if( o.expandSpeed == undefined ) o.expandSpeed= 500;
			if( o.collapseSpeed == undefined ) o.collapseSpeed= 500;
			if( o.expandEasing == undefined ) o.expandEasing = null;
			if( o.collapseEasing == undefined ) o.collapseEasing = null;
			if( o.multiFolder == undefined ) o.multiFolder = true;
			if( o.loadMessage == undefined ) o.loadMessage = 'Loading...';
			
			$(this).each( function() { // this는 등록한 태그
				
				function showTree(c, t) {
					//c 는 #fileTree, t는 루트 경로
					//처음에 로딩을 위한 wait 클래스 삽입
					$(c).addClass('wait');
					
					// jqueryFileTree start 하위 태그 전부 삭제
					$(".jqueryFileTree.start").remove();
				     
					// Ajax Post로 url과, 루트 경로 매개변수 넘기고...  전체 폴더와 파일에 대한 list 태그 받아옴..
					$.post(o.script, { dir: t }, function(data) {
						
						// start 태그의 내용 지우고
						$(c).find('.start').html('');
						
						// wait 클래스 지우고,   list 태그 다 붙여 넣고
						$(c).removeClass('wait').append(data);
						// 메인 뷰 쪽에 지워주고
						$('#MainFileView *').remove();
						selectItemNum = 0;
						//MainFileView 에도 똑같이 데이터 뿌려주고
						if(data =="") // 폴더 안에 아무것도 없다면.. 기본으로  상위폴더가는거 넣어주기 위해
						{
							$('#MainFileView').append("<ul class='jqueryFileTree' style='display: none;'>");
						}
						else
						{
							$('#MainFileView').append(data);
						}
							
						//상위폴더 이동 추가
						$('#MainFileView ul').attr('path',t);
						$('#MainFileView ul').prepend("<li class='UpFolder'><a href='javascript:;' rel='"+escape($('#MainFileView ul').attr('path'))+"'>...</a></li>");
						// li태그 있는 놈들 만큼 돌면서 
						$('#MainFileView').find('li').each(function(){
							$(this).attr('select', 'unselect');
						});
						mangoAPI.fileButtons.bindSelectAllBtn('#MainFileView LI');
						// 각 체크박스에 click이벤트 바인딩
						mangoAPI.fileButtons.bindItemBtn();
						// 파일탐색기에 컨텍스트 메뉴 등록
						mangoAPI.contextMenuBind($('#MainFileView'));
						//t는 더블 클릭한 폴더의 경로... 경로를 속성으로 추가해줘서.. 
						
						
						if( o.root == t )
						{
							$(c).find('UL:hidden').show();
						}
						else
						{
							$(c).find('UL:hidden').slideDown({ duration: o.expandSpeed, easing: o.expandEasing });
						}
						//$(c).children('UL').clone().appendTo('#MainFileView');
						$('#MainFileView ul.jqueryFileTree').show();
						$('#MainFileView ul.jqueryFileTree').css('display','block');
						// 이벤트 등록.  매개변수로는 #fileTree
						//console.log($(c));
						//console.log(t);
						bindTree(c, t); // t = 부모경로 넘겨주기						
						bindTree2();
					});
				}
				
				function bindTree(t, parentPath) {
					// 여기서 t는 #fileTree
					//#fileTree Li a 태그에 인벤트 등록.. 더블클릭이벤트
					//console.log($(t));
					$(t).find('li a').bind(o.folderEvent, function() {
						//console.log('call');
						if( $(this).parent().hasClass('directory') ) {
							if( $(this).parent().hasClass('collapsed') ) {
								// Expand 펼치기
								$('#MainFileView *').remove();
								selectItemNum= 0;
								//console.log('remove');
								// 이건 다시 닫는거 아닌감
								if( !o.multiFolder ) {
									//console.log('?');
									$(this).parent().parent().find('UL').slideUp({ duration: o.collapseSpeed, easing: o.collapseEasing });
									$(this).parent().parent().find('LI.directory').removeClass('expanded').addClass('collapsed');
								}
								
								// 이건 자식 폴더 및 파일 태그 다 지우는건데....
								$(this).parent().find('UL').remove(); // cleanup
								// 재귀????
								// 매개변수로 li 태그 넘겨주고..., 현재 선택된 태그의 경로 값 넘겨주고....
								showTree( $(this).parent(), escape($(this).attr('rel').match( /.*\// )) );
								// 닫힘 -> 열림으로 변경
								$(this).parent().removeClass('collapsed').addClass('expanded');
								//console.log('open');
								
							} else { // 열려있다면?
								// Collapse // 닫기
								//console.log('close');
								$(this).parent().find('UL').slideUp({ duration: o.collapseSpeed, easing: o.collapseEasing });
								$(this).parent().removeClass('expanded').addClass('collapsed');
								$('#MainFileView *').remove();
								selectItemNum = 0;
								 //showTree( $(this).parent(), escape($(this).attr('rel').match( /.*\// )) );
								//$(this).parent().parent().find('ul').remove();
								$(this).parent().parent().clone().appendTo('#MainFileView');
								$('#MainFileView ul').prepend("<li class='UpFolder'><a href='javascript:;' rel='"+escape($('#MainFileView ul').attr('path')) +"'>...</a></li>");
								$('#MainFileView ul').attr('path',parentPath);
								$('#MainFileView li').attr('select', 'unselect');
								$('#MainFileView ul').find('ul').remove();
								selectItemNum = 0;
								bindTree2();
							}
						} else {
							h($(this).attr('rel'));
						}
						return false;
					});

					// Prevent A from triggering the # on non-click events
					if( o.folderEvent.toLowerCase != 'click' ) $(t).find('LI A').bind('dblclick', function() { return false; });
				}
				
				function bindTree2() {
					// 각 폴더나 파일을 클릭했다면
					selectItemnum = 0; // 바인딩 할떄 선택된 숫자 초기화
					$('#MainFileView').find('LI').off('click');
					$('#MainFileView').find('LI').bind('click', function() {
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
					$('#MainFileView').find('LI A').bind(o.folderEvent, function() {
						$('#fileTree a[rel = "'+ $(this).attr('rel') +'"]').dblclick();
					});
				}

				if(o.root == '/sdcard/')
				{
					// Loading message
					$(this).html('<ul class="jqueryFileTree start"><li class="wait">' + o.loadMessage + '<li></ul>');
					// Get the initial file list
					showTree( $(this), escape(o.root) );
				}
				else
				{
					if(o.root =='sdcard')
						o.root = '/'+o.root+'/';
					$(this).find('li a').unbind("dblclick");
					bindTree(this, escape(o.root));
					bindTree2();
				}
			});
		}
	});
	
})(jQuery);