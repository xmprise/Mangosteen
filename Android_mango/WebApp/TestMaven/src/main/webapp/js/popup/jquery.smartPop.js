/**
 * smartPop
 *
 * Copyright (c) 2011 Cho Yong Gu (@inidu2)
 * Dual licensed under the MIT and GPL licenses:
 * http://www.opensource.org/licenses/mit-license.php
 * http://www.gnu.org/licenses/gpl.html
 *
 */

/**
 * �덉씠�대줈 �앹뾽 �꾩슦湲�- html or url
 *
 * �뱀쭠
 * 1. 釉뚮씪�곗� �명솚
 * 2. 源붾걫���ㅽ겕濡ㅻ컮 泥섎━
 * 3. 釉뚮씪�곗� �ш린 蹂�꼍���덉씠���앹뾽 �먮룞 以묒븰 �뺣젹
 * 4. url �섏씠吏�� �꾩슱 寃쎌슦 �꾨젅���ш린 �먮룞 議곗젅
 * 
 * 紐⑹쟻
 * �댁슜���곴��놁씠 源붾걫���ㅽ겕濡ㅻ컮 泥섎━�섍린
 * 釉뚮씪�곗� �ш린���곴��놁씠 以묒븰���꾩슦湲�
 * 
 * �쒗븳�ы빆
 * 媛�줈 理쒕� 1300, �몃줈 5000 - ���ш쾶��寃쎌슦 css �섏젙
 * 
 * �ъ슜踰�
 * 1. html �댁슜 蹂댁뿬二쇨린
 *      $.smartPop.open({title: '�ㅻ쭏�명뙘', width: 500, height: 500, html: '<h1>smartPop</h1> �ш린��蹂댁뿬以��댁슜' });
 * 2. url �섏씠吏��꾩슦湲�
 *      $.smartPop.open({title: '�ㅻ쭏�명뙘', width: 500, height: 500, url: 'smartPop.html �ш린��蹂댁뿬以��섏씠吏� });
 *      �몃줈 �ш린��遺덈윭�ㅻ뒗 �섏씠吏��ш린��留욊쾶 �먮룞�쇰줈 ��젅��
 * 3. �믪씠媛��뺤씤 濡쒓렇
 *      $.smartPop.open({title: '�ㅻ쭏�명뙘', width: 500, height: 500, log: true, url: 'smartPop.html �ш린��蹂댁뿬以��섏씠吏� });
 *      log: true �ㅼ젙
 *
 * 湲곕낯 �듭뀡
 * $.smartPop.defaults = {
    position    : 'center',
    left        : 310,
    top         : 10,
    bodyClose   : true,
    padding     : 10,
    background  : '#fff',
    border      : 5,
    borderColor : '#39a3e5',
    closeMargin : 5,
    opacity     : .7,
    width       : 720,
    height      : 500,
    html        : '',
    url         : '',
    log         : false
 * };
 * 
 * 
 * 
 * @name $.smartPop
 * @author Pharos @inidu2
 */
    
;(function($) {
    var ie     = $.browser.msie && ($.browser.version < 9);
    var innerH  = window.innerHeight;
    
    $.smartPop = {
        isInstall : false,
        opts : {},
        open : function(options) {
            this.opts = $.extend({}, $.smartPop.defaults, options);
            this.install();
            this.resize();

            $('html').css({ marginRight: '15px', display: 'block', overflow: 'hidden', overflowY: 'hidden' });
            $('#smartPop').show();
            if(this.opts.log) $('#smartPop_log').show();
        },
        resize : function() {
            this.log(this.opts.width + ' x ' + this.opts.height);
            this.log('background : ' + this.opts.background);
            this.log('padding : ' + this.opts.padding);
            this.log('border : ' + this.opts.border);
            this.log('borderColor : ' + this.opts.borderColor);
            this.log('closeMargin : ' + this.opts.closeMargin);
            this.log('opacity : ' + this.opts.opacity);
            this.log('');

            // 湲곕낯 �ㅼ젙
            $('#smartPop_container').css({ border: 'solid ' + this.opts.border + 'px ' + this.opts.borderColor });
            $('#smartPop_close').css({ top: this.opts.closeMargin + 'px', right: this.opts.closeMargin + 'px' });
            $('#smartPop_content').css({ padding: this.opts.padding + 'px' });
            $('#smartPop_container').width(this.opts.width);
            $('#smartPop_close_wrap').width(this.opts.width);
            this.resizeHeight(this.opts.height);
        },
        resizeHeight : function(h) {
            this.log('resizeHeight : ' + h);
            if(ie) {
                $('body').attr({ scroll: 'no' }); // ie7�먯꽌 overflow �곸슜�덈맖 
                innerH = document.documentElement.clientHeight;
            }

            // �꾩튂�ㅼ젙
            if(this.opts.position == 'center') {
                var t;
                t = (h < innerH) ? (innerH - h) / 2 : 10;
                $('#smartPop_container').css({ marginLeft: 'auto', marginTop: t + 'px' });
            } else {
                $('#smartPop_container').css({ marginLeft: this.opts.left + 'px', marginTop: this.opts.top + 'px' });
            }

            // �믪씠�ㅼ젙
            $('#smartPop_container').height(h);
            if(this.opts.url == '') {
                $('#smartPop_content').html(this.opts.html).height(h).show();
                $('#smartPop_frame').hide();
            } else {
                $('#smartPop_content').hide();
                $('#smartPop_frame').css({ padding: this.opts.padding + 'px', width: (this.opts.width - this.opts.padding * 2) + 'px', height: (h - this.opts.padding * 2) + 'px' }).show();
            }
            $('#smartPop_loading').hide();
            this.log('');
        },
        install : function() {
            if(this.isInstall == false) {
                var body                    = $('body');
                var smartPop_overlay        = $('<div />').attr('id', 'smartPop_overlay').css({ opacity: this.opts.opacity, background: this.opts.background });
                var smartPop                = $('<div />').attr('id', 'smartPop');
                var smartPop_container      = $('<div />').attr('id', 'smartPop_container');
                var smartPop_close_wrap     = $('<div />').attr('id', 'smartPop_close_wrap');
                var smartPop_close          = $('<div />').attr('id', 'smartPop_close');
                var smartPop_loading        = $('<div />').attr('id', 'smartPop_loading');
                var smartPop_content        = $('<div />').attr('id', 'smartPop_content');
                var smartPop_frame          = $('<iframe />').attr({ id: 'smartPop_frame', frameBorder: 0, scrolling: 'no' });

                smartPop_close_wrap.append(smartPop_close).appendTo(smartPop_container);
                smartPop_container.append(smartPop_loading).append(smartPop_content).append(smartPop_frame).appendTo(smartPop);
                smartPop.append($('<div />').attr('id', 'smartPop_log'));
                body.append(smartPop_overlay).append(smartPop);
                this.isInstall = true;
            } else {
                $('#smartPop').show();
                $('#smartPop_overlay').show();
            }

            // �リ린 踰꾪듉 �ㅼ젙
            if(this.opts.closeImg != undefined) {
                $('#smartPop_close').css({ width:this.opts.closeImg.width + 'px', height:this.opts.closeImg.height + 'px', backgroundImage:'url(' + this.opts.closeImg.src + ')'});
            }
            if(this.opts.url != '') {
                $('#smartPop_frame').attr({ src : this.opts.url }).load(function () {
                    var h = $(this).contents().height();
                    $.smartPop.resizeHeight(h);
                });
            }
            
            /*
            if(this.opts.bodyClose) {
                $('body').bind('click', function(event) {
                    if (!event) event = window.event;
                    var target = (event.target) ? event.target : event.srcElement;
                    event.stopPropagation(); // �대깽��踰꾨툝留��꾪뙆瑜�留됱쓬
                    if(target.id == 'smartPop') { $.smartPop.close(); }
                });
            }*/

            $('#smartPop_close').click(function() {
                $.smartPop.close();
            });
        },
        close : function() {
            if(ie) {
                $('body').attr({ scroll: 'yes' });
            }
            $('html').css({ marginRight: 0, display: '', overflowY: 'scroll'});

            $('#smartPop_frame').attr('src', '').unbind();
            $('#smartPop').hide();
            $('#smartPop_overlay').hide();
        },
        log : function(msg) {
            var log = $('#smartPop_log').html();
            $('#smartPop_log').html(msg + '<br />' + log);
        }
    };
    
    $.smartPop.defaults = {
        position    : 'center',
        left        : 310,
        top         : 10,
        bodyClose   : true,
        padding     : 10,
        background  : '#fff',
        border      : 5,
        borderColor : '#39a3e5',
        closeMargin : 3,
        closeImg    : {width:13, height:13, src:'images/popup/btn_close1.png'},
        opacity     : .7,
        width       : 720,
        height      : 500,
        html        : '',
        url         : '',
        log         : false
    };
})(jQuery);
