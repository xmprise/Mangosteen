function stickyFooter(){
        jQuery("#stickyFooter").css({position: "absolute",top:($(window).scrollTop()+$(window).height()-$("#stickyFooter").height())+"px"});
    }
jQuery(function(){
    stickyFooter();
    jQuery(window)
        .scroll(stickyFooter)
        .resize(stickyFooter);
});