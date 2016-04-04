$(document).ready (function () {
var uAgent = navigator.userAgent.toLowerCase();  //Mobile인 지 구분하기 위해정보 받음

    alert(document.URL);
/*아래는 모바일 장치들의 모바일 페이지 접속을위한 스크립트*/ 
    var mobilePhones = new Array('iphone', 'ipod', 'ipad', 'android', 'blackberry', 'windows ce','nokia', 'webos', 'opera mini', 'sonyericsson', 'opera mobi', 'iemobile');
    for (var i = 0; i < mobilePhones.length; i++)
        if (uAgent.indexOf(mobilePhones[i]) != -1)
           document.location = document.URL+"mobile/index.html"; 
});