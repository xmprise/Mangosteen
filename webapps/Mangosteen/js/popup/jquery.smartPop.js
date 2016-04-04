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
 * 占쎈뜆�좑옙��쨮 占쎌빘毓�占쎄쑴��묾占� html or url
 *
 * 占쎈�彛�
 * 1. �됰슢�わ옙怨쀯옙 占쎈챸��
 * 2. 繹먮뗀嫄ワ옙占쏙옙�쎄쾿嚥▲끇而�筌ｌ꼶��
 * 3. �됰슢�わ옙怨쀯옙 占싼덈┛ 癰귨옙瑗랃옙占쏙옙�됱뵠占쏙옙占쎌빘毓�占쎈Ŧ猷�餓λ쵐釉�占쎈베��
 * 4. url 占쎌꼷�좑쭪占쏙옙 占쎄쑴��野껋럩��占쎄쑬�낉옙占쏙옙�由�占쎈Ŧ猷�鈺곌퀣��
 * 
 * 筌뤴뫗��
 * 占쎈똻�쒙옙占쏙옙怨댐옙占쎈냱��繹먮뗀嫄ワ옙占쏙옙�쎄쾿嚥▲끇而�筌ｌ꼶�곻옙�띾┛
 * �됰슢�わ옙怨쀯옙 占싼덈┛占쏙옙占쎄낫占쏙옙�곸뵠 餓λ쵐釉곤옙占쏙옙袁⑹뒭疫뀐옙
 * 
 * 占쎌뮉釉놂옙�鍮�
 * 揶쏉옙以�筌ㅼ뮆占�1300, 占쎈챶以�5000 - 占쏙옙占싼덉쓺占쏙옙野껋럩��css 占쎌꼷��
 * 
 * 占싼딆뒠甕곤옙
 * 1. html 占쎈똻��癰귣똻肉т틠�⑤┛
 *      $.smartPop.open({title: '占썬끇彛륅옙紐낅솚', width: 500, height: 500, html: '<h1>smartPop</h1> 占싼덈┛占쏙옙癰귣똻肉т빳占쏙옙�곸뒠' });
 * 2. url 占쎌꼷�좑쭪占쏙옙袁⑹뒭疫뀐옙
 *      $.smartPop.open({title: '占썬끇彛륅옙紐낅솚', width: 500, height: 500, url: 'smartPop.html 占싼덈┛占쏙옙癰귣똻肉т빳占쏙옙�륁뵠筌욑옙 });
 *      占쎈챶以�占싼덈┛占쏙옙�븍뜄��옙�삳뮉 占쎌꼷�좑쭪占쏙옙�由곤옙占쏙쭕�딆쓺 占쎈Ŧ猷욑옙�곗쨮 占쏙옙�낉옙占� * 3. 占쎈��졾첎占쏙옙類ㅼ뵥 嚥≪뮄��
 *      $.smartPop.open({title: '占썬끇彛륅옙紐낅솚', width: 500, height: 500, log: true, url: 'smartPop.html 占싼덈┛占쏙옙癰귣똻肉т빳占쏙옙�륁뵠筌욑옙 });
 *      log: true 占썬끉��
 *
 * 疫꿸퀡��占쎈벊��
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
    var innerW = window.innerWidth;
    var imageWidth;
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
            // 疫꿸퀡��占썬끉��
            $('#smartPop_container').css({ border: 'solid ' + this.opts.border + 'px ' + this.opts.borderColor });
            $('#smartPop_close').css({ top: this.opts.closeMargin + 'px', right: this.opts.closeMargin + 'px' });
            $('#smartPop_content').css({ padding: this.opts.padding + 'px' });
            $('#smartPop_container').width(this.opts.width+20);
            $('#smartPop_close_wrap').width(this.opts.width);
            this.resizeHeight(this.opts.height+40);
        },
        resizeHeight : function(h) {
        	
        	
            this.log('resizeHeight : ' + h);
            if(ie) {
                $('body').attr({ scroll: 'no' }); // ie7占쎈Ŋ苑�overflow 占쎄낯�쒙옙�덈쭡 
                innerH = document.documentElement.clientHeight;
            }

            // 占쎄쑴�귨옙�쇱젟
            if(this.opts.position == 'center') {
                var t;
                t = (h < innerH) ? (innerH - h) / 2 : 10;
                $('#smartPop_container').css({ marginLeft: 'auto', marginTop: t + 'px' });
            } else {
                $('#smartPop_container').css({ marginLeft: this.opts.left + 'px', marginTop: this.opts.top + 'px' });
            }
            
            // 占쎈��좑옙�쇱젟
            $('#smartPop_container').height(h);
            if(this.opts.url == '') {
                $('#smartPop_content').html(this.opts.html).height(h).show();
                $('#smartPop_frame').hide();
            } else {
                $('#smartPop_content').hide();
                $('#smartPop_frame').css({ padding: this.opts.padding + 'px', width: ("693" - this.opts.padding * 2) + 'px', height: (h - this.opts.padding * 2) + 'px' }).show();
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
                //this.isInstall = true;
            } else {
                $('#smartPop').show();
                $('#smartPop_overlay').show();
            }

            // 占썬꺁由�甕곌쑵��占썬끉��
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
                    event.stopPropagation(); // 占쎈�源쏙옙占썼린袁⑦닜筌랃옙占쎄쑵�녺몴占쏙쭕�깆벉
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
            $('#smartPop').remove();
            $('#smartPop_overlay').remove();
//            $('#smartPop').hide();
//            $('#smartPop_overlay').hide();
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
        background  : '#000000',
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
