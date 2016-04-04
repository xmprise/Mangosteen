
// COPYRIGHT (c) 2010 Pavel Khlebovich
// Contact me at pas.anddev@gmail.com if you want to use parts of this code in your application or website

function createAudio()
{
	$("#jplayer").jPlayer({
		ready: function () {
			$(this).jPlayer("setMedia", { wav: "audio.wav" }).jPlayer("play");
		},
		ended: function (event) {
			$(this).jPlayer("play");
		},
		swfPath: "",
		supplied: "wav"
    });
    if (navigator.userAgent.indexOf("MSIE")>=0)
    {
        $("#jplayer").after($('<p>In-browser audio is not supported in your browser. Use <a href="http://google.com/chrome">Chrome</a> or <a href="http://firefox.com">Firefox</a>.</p>'));
    }
    else
    {
        $("#jplayer").after($("<p>In-browser audio is running.</p>"));
    }
}


function ipwajax(cmd)
{
    $.ajax({url: "/"+cmd});
}

function openRemote() 
{
	popupWindow = window.open("/remote.html",'popUpWindow','height=400,width=360,toolbar=no,menubar=no,location=no,directories=no,status=no');
}


var working = null;
var showing = null;
var rszOnAq = true;
var switchAspect = false;

function loadImage(e) {
  var oldshowing = showing;
  showing = working;
  working = oldshowing;
  showing.unbind()
  showing.css("zIndex", 1);
  if (rszOnAq)
  {
    rszOnAq = false;
    onJsResize();
  }
  loadFeed();
}

function loadJsWindowed()
{
  rszOnAq = false;
  initJsAq();
}

function initJsAq()
{
  working = $("#img1");
  showing = $("#img2");
  loadFeed();
}

function loadFeed() {
  working.css("zIndex", -1);
  working.load(loadImage);
  working.attr("src","/shot.jpg?rnd="+Math.floor(Math.random()*1000000));
}

function onJsResize() {
    if (showing == null)
        return;
    var width = $(window).width();
    var height = $(window).height();
    var paspect = (showing.width()+0.0)/showing.height();
    var waspect = ($(window).width()+0.0)/$(window).height();
    var aspectDep = (paspect > waspect)
    var pic1 = $("#img1");
    var pic2 = $("#img2");
    if (switchAspect)
        aspectDep = !aspectDep;
    if (aspectDep)
    {
        pic1.css('width', width)
        pic2.css('width', width)
        pic1.css('height', '')
        pic2.css('height', '')
    }
    else
    {
        pic1.css('height', height)
        pic2.css('height', height)
        pic1.css('width', '')
        pic2.css('width', '')
    }
}

function swAspect()
{
    switchAspect = !switchAspect;
    onJsResize();
}

function swBAspect()
{
    switchAspect = !switchAspect;
    onBrowserResize();
}

function loadJsFullscreen()
{
    $(window).resize(onJsResize);
    initJsAq();
}

function onBrowserResize() {
    var width = $(window).width();
    var height = $(window).height();
    var showing = $('#img1')
    var paspect = (showing.width()+0.0)/showing.height();
    var waspect = ($(window).width()+0.0)/$(window).height();
    var aspectDep = (paspect > waspect)
    if (switchAspect)
        aspectDep = !aspectDep;
    if (aspectDep)
    {
        showing.css('width', width)
        showing.css('height', '')
    }
    else
    {
        showing.css('height', height)
        showing.css('width', '')
    }
}

function loadBrowserFullscreen()
{
    setInterval(function() { onBrowserResize(); }, 1000);
    $(window).resize(onBrowserResize);
}


