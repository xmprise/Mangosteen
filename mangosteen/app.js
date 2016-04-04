/**
 * Module dependencies.
 */

var express = require('express')
    fs = require('fs'),
    util = require('util'),
    im = require('imagemagick');

var app = express.createServer(
    //form({keepExtensions: true})
);
var LoginProvider = require('./static/javascripts/articleprovider-login').LoginProvider;
var sessionMgm = require('./static/javascripts/sessionManagement');
var userSessionMgm = require('./static/javascripts/UserSessionManagement')
var mobileSessionMgm = require('./static/javascripts/mobileSessionManagement')
var rest = require('restler');
var url_parser = require('url');
var io = require('socket.io').listen(app);

io.configure(function(){
    io.set('log level', 3);
    io.set('transports', [
        'websocket'
        , 'flashsocket'
        , 'htmlfile'
        , 'xhr-polling'
        , 'jsonp-polling'
    ]);
});
var nicknames = {};
var MemStore = express.session.MemoryStore;

app.configure(function(){
    // this logs in a Apache style
    // app.use(express.logger());
    app.use(express.bodyParser({uploadDir:__dirname + '/tmp'}));
    app.use(express.bodyParser());
    app.use(express.limit('5mb'));
    // this middleware will override our method
    // with what we passed into the hidden variable _method
    app.use(express.methodOverride());
    // methodOverride must be after the bodyParser
    app.use(express.static(__dirname + '/static'));
    app.use(express.cookieParser());
    app.use(express.session({secret: 'alessios', store: MemStore({
        reapInterval: 60000 * 10
    })}));
});

app.configure('development', function() {
    app.use(express.logger());
    // this is the error handler, uncomment #1 to see it in action
    app.use(express.errorHandler({
        dumpExceptions: true,
        showStack : true
    }))
});

app.configure('production', function() {
    // this is the error handler for the production env
    app.use(express.errorHandler({
        dumpExceptions: false,
        showStack: false
    }));
});

function requiresLogin(req, res, next) {
    if(req.session.user) {
        console.log(req.session.user);
        next();
    } else {
        res.redirect('/');
    }
}

app.set('views', __dirname + '/views');
app.set('view engine', 'jade')

//app.get('/', routes.index);

var loginProvider = new LoginProvider();

app.get('/', function(req, res) {
    // #1
    // throw new Error('this is just my custom error');
    // res.send('some text');
    res.render('login');
});

app.post('/login', function(req, res){

    var id = req.body.email;
    var pwd = req.body.pwd;

    userSessionMgm.remove(req.sessionID);
    delete req.session.user;

    loginProvider.login(id, pwd, function(error, result){
        //console.log('여기확인 =' + result.UserName + " : " + id);
        //console.log('line 74 = ' + id);
        try{
            var User_name = result.UserName; // Exception, if Login data null and undefind
        }catch(err) {
            console.log('fail loggin, and result null data');
        }

        var ssid = req.sessionID;
        if(id === User_name) {
            req.session.user = id;
            //console.log('라인98 : sessionID = ' + ssid + ' : userSessionMgm = ' + userSessionMgm.getSessionById(User_name));
            if(userSessionMgm.getSessionById(User_name) !== null) {
                delete userSessionMgm.getSessionById(User_name);
                userSessionMgm.removeByUserId(User_name);
            }
            // id값과 sessionID를 배열에 저장
            var session_data = new Object();
            session_data.ssid = req.sessionID;
            session_data.userId = id;
            session_data.username = id; //notUsed
            userSessionMgm.add(session_data);
            var session_user = userSessionMgm.getSessionByUserId(ssid);
            console.log('라인107 : '+ session_user.ssid + ' : ' + req.sessionID);
            res.redirect('/main');
            }else {
            //req.flush('warn', 'Login failed');
            console.log('warn...Login failed');
            res.redirect('/')
        }
    });
});

app.post('/join', function(req, res) {
    res.redirect('/join')
});

app.get('/join', function(req, res) {
    res.render('join');
});

app.post('/user-join', function(req, res) {
    var id = req.body.id;
    var pwd = req.body.pwd;
    var email = req.body.email;
    var nick = req.body.name;
    console.log('확인: ' + id + ' ' + pwd + ' ' + email + ' ' + nick);
    if(id.length>0 ){
        console.log('여기가 맞나');
    }else if(id.length<0) {
        console.log('널임');
    }
    //여기 다시 수정할 것.
    if(id.length<0 || pwd.length<0 || email.length<0 || nick.length< 0) {
        res. redirect('/join');
        console.log('fail join member')
    }else{
        loginProvider.join(id, pwd,email,nick, function(error, result){
            //req.flush('warn', 'Login failed');
            console.log('new Member join...');
            res.redirect('/')
        });
    }
});

app.get('/login/destroy', function(req, res) {
    userSessionMgm.remove(req.sessionId)
    delete req.session.user;
    res.redirect('/');
});

app.get('/main', requiresLogin,function(req, res){

    var all_my_messageVar;
    var my_message_UserName;
    var my_message_Message;
    var my_message_date;
    var session_user = userSessionMgm.getSessionByUserId(req.sessionID);
    var my_Pic;
    //console.log('실행순서.177');

    loginProvider.my_message(session_user.userId, function(error, my_result){

        //console.log('실행순서.180');
        try{
            console.Log('여기 머라고 나오지지~!' + my_result[0].Message);
            my_message_UserName = my_result[0].UserName;
            my_message_Message = my_result[0].Message;
            my_message_date = my_result[0].MessageDate;
            //console.log('여긴 아니겠지...' + my_message_UserName + ' : ' + my_message_Message + ' : ' +  my_message_date)
        }catch(err){
            console.log('new user, anything message. data null');
            my_message_UserName = session_user.userId;
            my_message_Message = 'Hi, My Friend.'
            my_message_date = 'Change Your Message.'
        }

        loginProvider.viewMyPicture(session_user.userId, function(error, p_result) {
            //console.log('실행순서.195');
            my_Pic = p_result[0].MemberPicture;
            //console.log('라인 147: ' + result[0].UserName);
        });

            loginProvider.message(session_user.userId, function(error, result){
                //console.log('실행순서.201');
                var jsonStr = JSON.stringify(result);
                var str = JSON.parse(jsonStr);
                var fri_List = [];
                var userName = session_user.userId;
                //my_Pic이 null이면 디폴트 사진으로 출력
                //console.log('라인208 ' + result.status);
                if(my_Pic === null) {
                    //console.log('라인156 : ' + my_Pic);
                    //console.log('라인158 : ' + __dirname + ' src+path' + src_path);
                    my_Pic = '/images/Icon-user.png';
                }

                for(var i=0;i<result.length;i++) {
                    if(result[i].MemberPicture === null) {
                        result[i].MemberPicture = '/images/Icon-user.png'
                    } else {
                        // DB에서 이미지 불러오기
                    }
                }

                res.render('main',{
                    email:userName,
                    User_Picture: my_Pic,
                    my_ID: 'Welcome ' + userName +' ' + ', Share your File with Friend.',
                    my_message_: my_result,
                    //my_message_date: my_result,
                    //my_message_: my_message_UserName + ' Said ' + my_message_Message ,
                    //my_message_date: 'MessageDate : ' + my_message_date ,
                    friend_message: result
                })
            });
    });
});

app.post('/Send', function(req, res){
    var msg = req.param('msg');
    var session_user = userSessionMgm.getSessionByUserId(req.sessionID);
    console.log('Message send it');

    loginProvider.Insert_Message(session_user.userId, msg, function(error, result){
        console.log('여기 머가 나오나~~~~' + result);

    });

});

app.post('/upload', function(req, res) {
    var images = [];
    var isImage = false;
    var session_user = userSessionMgm.getSessionByUserId(req.sessionID);

    //express.bodyParser 는 multipart 를 위해 req.files를 만들어 줍니다.
    //클라이언트에서 mutiple로 요청시 여러파일이 올라오기 때문에 array인지 확인해 줍니다.
    var image = req.files.imgs;
    var kb = image.size / 1024 | 0;
    isImage = checkType(image);
    images.push({name: image.name, size: kb, isImage: isImage});
    renameImg(image);
    var target_path = './static/upload/' + image.name;
    var timeStarted = new Date;
    im.resize({
        srcPath: target_path,
        dstPath: target_path,
        width: 128,
        height: 128
    }, function (err, stdout, stderr){
        if (err) return console.error(err.stack || err);
        console.log('resize(...) wrote ' + target_path);
        console.log('real time taken for convert: '+((new Date)-timeStarted)+' ms');
        im.identify(['-format', '%b', target_path], function (err, r){
            if (err) throw err;
            console.log("identify(['-format', '%b', 'test-resized.jpg']) ->", r);
            var id = session_user.userId;
            var url = '/upload/' + image.name;
            loginProvider.InsertMyPicture(id, url);
        })
    })
    console.log('image = : ' + req.files.imgs);

    res.redirect('/main');
});

app.get('/search',requiresLogin, function(req, res) {

    var all_my_messageVar;
    var my_message_UserName;
    var my_message_Message;
    var my_message_date;
    var session_user = userSessionMgm.getSessionByUserId(req.sessionID);
    var my_Pic;

    loginProvider.my_message(session_user.userId, function(error, result){
        //console.Log(result[0].UserName);
        try{
            my_message_UserName = result[0].UserName;
            my_message_Message = result[0].Message;
            my_message_date = result[0].MessageDate;
        }catch(err){
            console.log('new user, anything message. data null');
            my_message_UserName = session_user.userId;
            my_message_Message = 'Hi, My Friend.';
            my_message_date = 'Change Your Message.';
        }

        loginProvider.viewMyPicture(session_user.userId, function(error, result) {
            my_Pic = result[0].MemberPicture;
            //console.log('그림은 먼디: ' + result[0].MemberPicture);
            if(my_Pic === null) {
                //console.log('그림은머냐 : ' + my_Pic);
                //console.log('라인158 : ' + __dirname + ' src+path' + src_path);
                my_Pic = '/images/Icon-user.png';
            }

            var f_result=[];
            f_result.push({status:'Failed'});
            //console.log('그림 ' + my_Pic);

            res.render('friSearch', {
                email:session_user.userId,
                User_Picture: my_Pic,
                friend_search: f_result[0]
            })
        });
    });
});

app.post('/search', function(req, res) {

    var all_my_messageVar;
    var my_message_UserName;
    var my_message_Message;
    var my_message_date;
    var session_user = userSessionMgm.getSessionByUserId(req.sessionID);
    var my_Pic;
    var msg = req.param('msg');

    loginProvider.my_message(session_user.userId, function(error, result){
        //console.Log(result[0].UserName);
        try{
            my_message_UserName = result[0].UserName;
            my_message_Message = result[0].Message;
            my_message_date = result[0].MessageDate;
        }catch(err){
            console.log('new user, anything message. data null');
            my_message_UserName = session_user.userId;
            my_message_Message = 'Hi, My Friend.'
            my_message_date = 'Change Your Message.'
        }

        loginProvider.viewMyPicture(session_user.userId, function(error, result) {
            my_Pic = result[0].MemberPicture;
            //console.log('그림은 먼디: ' + result[0].MemberPicture);
            if(my_Pic === null) {
                //console.log('라인158 : ' + __dirname + ' src+path' + src_path);
                my_Pic = '/images/Icon-user.png';
            }

            var f_result=[];
            f_result.push({status:'Failed'});
            console.log('그림 ' + my_Pic);
        });

            loginProvider.searchFriend(msg, function(error, result){
                //자기 id를 검색하는 경우는 제외 할것.
                var jsonStr = JSON.stringify(result);
                var str = JSON.parse(jsonStr);
                var fri_List = [];
                var userName = msg;
                //my_Pic이 null이면 디폴트 사진으로 출력

                for(var i=0;i<result.length;i++) {
                    if(result[i].MemberPicture === null) {
                        result[i].MemberPicture = '/images/Icon-user.png'
                    } else {
                        // DB에서 이미지 불러오기
                    }
                }
                //수정
                if(msg === session_user.userId) {
                    console.log('same id...no search user');
                    result=null;
                    result = [];
                    result.push({status:'Failed'});
                    //console.log('그림 ' + result[0].status);

                    res.render('friSearch', {
                        email:session_user.userId,
                        User_Picture: my_Pic,
                        friend_search: result[0]
                    });
                }else{
                    res.render('friSearch', {
                        email:session_user.userId,
                        User_Picture: my_Pic,
                        friend_search: result
                    });
                }

            });
    });
});

app.post('/search_f', function(req, res) {

    var all_my_messageVar;
    var my_message_UserName;
    var my_message_Message;
    var my_message_date;
    var session_user = userSessionMgm.getSessionByUserId(req.sessionID);
    var my_Pic;
    var msg = req.param('msg');

    loginProvider.viewMyPicture(session_user.userId, function(error, result) {
        my_Pic = result[0].MemberPicture;
        //console.log('그림은 먼디: ' + result[0].MemberPicture);
        if(my_Pic === null) {
            //console.log('라인158 : ' + __dirname + ' src+path' + src_path);
            my_Pic = '/images/Icon-user.png';
        }

        var f_result=[];
        f_result.push({status:'Failed'});
        console.log('그림 ' + my_Pic);
    });

    loginProvider.searchFriend(msg, function(error, result){
        //자기 id를 검색하는 경우는 제외 할것.
        var jsonStr = JSON.stringify(result);
        var str = JSON.parse(jsonStr);
        var fri_List = [];
        var userName = msg;
        var countFri = 0;
        //my_Pic이 null이면 디폴트 사진으로 출력

        for(var i=0;i<result.length;i++) {
            if(result[i].MemberPicture === null) {
                result[i].MemberPicture = '/images/Icon-user.png'
            } else {
                // DB에서 이미지 불러오기
            }
        }
        //수정
        loginProvider.message(session_user.userId, function(error, fri_result){
            console.log("start..." + fri_result);

            for(var i=0;i<fri_result.length;i++){
                console.log('current friend...' + fri_result[i].FriendName + " : " + msg);
                if(msg === fri_result[i].FriendName || msg === session_user.userId) {
                    countFri = countFri + 1;
                    console.log('existed friend...no search user');
                    result=null;
                    result = [];
                    result.push({status:'Failed'});
                    //console.log('그림 ' + result[0].status);

                    res.render('request', {
                        email:session_user.userId,
                        User_Picture: my_Pic,
                        friend_search: result[0]
                    });

                    break;
                }
            }
            if(countFri == 0){
                console.log('no problem...');
                res.render('request', {
                    email:session_user.userId,
                    User_Picture: my_Pic,
                    friend_search: result
                });
            }
        });
  });
});

app.get('/request',requiresLogin,function(req, res) {

    var all_my_messageVar;
    var my_message_UserName;
    var my_message_Message;
    var my_message_date;
    var session_user = userSessionMgm.getSessionByUserId(req.sessionID);
    var my_Pic;
    var msg = req.param('msg');

    loginProvider.viewMyPicture(session_user.userId, function(error, result) {
        my_Pic = result[0].MemberPicture;
        //console.log('그림은 먼디: ' + result[0].MemberPicture);
        if(my_Pic === null) {
            //console.log('라인158 : ' + __dirname + ' src+path' + src_path);
            my_Pic = '/images/Icon-user.png';
        }

        var f_result=[];
        f_result.push({status:'Failed'});
        //console.log('그림 ' + my_Pic);
    });

    loginProvider.searchFriend(msg, function(error, result){
        //자기 id를 검색하는 경우는 제외 할것.
        var jsonStr = JSON.stringify(result);
        var str = JSON.parse(jsonStr);
        var fri_List = [];
        var userName = msg;
        //my_Pic이 null이면 디폴트 사진으로 출력

        for(var i=0;i<result.length;i++) {
            if(result[i].MemberPicture === null) {
                result[i].MemberPicture = '/images/Icon-user.png'
            } else {
                // DB에서 이미지 불러오기
            }
        }
        //수정
        if(msg === session_user.userId) {
            console.log('same id...no search user');
            result=null;
            result = [];
            result.push({status:'Failed'});
            //console.log('그림 ' + result[0].status);

            res.render('request', {
                friend_search: result[0]
            });
        }else{
            res.render('request', {
                friend_search: result
            });
        }
    });
});

app.post('/request_ok',function(req, res) {

    var friend = req.param('userN');
    var fri_pic = req.param('userP');
    var fri_name = req.param('userU');
    var session_user = userSessionMgm.getSessionByUserId(req.sessionID);

    loginProvider.requestState(friend, function(error, result){

        //console.log("여기 확인 : " + result[0].UserName + " : " + result[0].RequestState);
        if(result[0].RequestState === 1) {
            loginProvider.UpdateRequestSate(friend, 2, function(error, result) {
                console.log('UpdateRequestSate Result: ' + result + ' User : ' + friend);
            });
        }
    });

    //Friend request
    loginProvider.requestFriend(session_user.userId, friend, function(error, result){
        console.log(req.param('userN') + ' : ' + session_user.userId + ' : ' + req.param('userP'));
        res.render('request-ok', {
            friend:friend,
            fri_pic:fri_pic,
            fri_name:fri_name
        })
        //res.sendfile('views/request-ok.html');
    });
});

app.get('/accept',requiresLogin, function(req, res) {

    var session_user = userSessionMgm.getSessionByUserId(req.sessionID);

    loginProvider.invitationList(session_user.userId, function(error, result) {

        loginProvider.requestState(session_user.userId, function(error, result){

            //console.log("여기 확인 : " + result[0].UserName + " : " + result[0].RequestState);
            if(result[0].RequestState === 2) {
                loginProvider.UpdateRequestSate(session_user.userId, 1, function(error, result) {
                   console.log('UpdateRequestSate Result: ' + result + ' User : ' + session_user.userId);
                });
            }
        });

        var f_result=[];
        f_result.push({status:'Failed'});
        //console.log('확인: ' + result.status);
        //console.log('확인: ' + result[0].UserName);
        if(result.status == 'Failed'){
            res.render('accept', {
                friend_invitation: f_result[0]
            })
        }else{
            for(var i=0;i<result.length;i++) {
                if(result[i].MemberPicture === null) {
                    result[i].MemberPicture = '/images/Icon-user.png'
                } else {
                    // DB에서 이미지 불러오기
                }
            }
            res.render('accept', {
                friend_invitation: result
            })
        }

    });
});

app.post('/accept', requiresLogin, function(req, res) {
    //testCode
    //var session_user = userSessionMgm.getSessionByUserId(req.sessionID);
    //var friend = req.param('userN');
    //console.log(friend + ' : ' + session_user.userId);
    //res.end();

    //accptedRequestFriend
    var session_user = userSessionMgm.getSessionByUserId(req.sessionID);
    var friend = req.param('userN');

    loginProvider.accptedRequestFriend(session_user.userId, friend, function(error, result) {
        console.log(result.status);
        res.sendfile('views/request-ok.html')

    });

});

app.post('/reject', requiresLogin, function(req, res) {
    //accptedRequestFriend
    var session_user = userSessionMgm.getSessionByUserId(req.sessionID);
    var friend = req.param('userN');
    //var fri_pic = req.param('userP');
    //var fri_name = req.param('userU');
    //console.log(friend + ' : ' +session_user.userId);
    //res.end();

     loginProvider.rejectRequestFriend(session_user.userId, friend, function(error, result) {
         console.log(result.status);
         res.sendfile('views/request-cancel.html')
     });
});

app.post('/requestt', function(req, res) {
    console.log('리퀘스트');
    res.end();
});

function checkType(image){
    var isImage = false;
    console.log('->> image.type.indexOf : ' + image.type.indexOf('image'));
    //파일의 타입 비교
    if(image.type.indexOf('image') > -1){
        console.log('->>> req.files.img is img');
        isImage = true;
    }else{
        console.log('->>> req.files.img is not img');
        isImage = false;
    }
    return isImage;
}

function renameImg(image){
    var tmp_path = image.path;
    var target_path = './static/upload/' + image.name;
    console.log('->> tmp_path: ' + tmp_path );
    console.log('->> target_path: ' + target_path );
    //tmp_path -> target_path로 이동하면서 파일명을 바꿉니다.
    fs.rename(tmp_path, target_path, function(err){
        if(err) throw err;
        /* 어떤 예제에서 아래와 같이 tmp_path를 다시 unlink해주지만 이미 rename으로 이동시켰기 때문에 tmp_path가 없다는 오류가 나게 됨
         fs.unlink(tmp_path, function() {
         if (err) throw err;
         res.send('File uploaded to: ' + target_path + ' - ' + req.files.thumbnail.size + ' bytes');
         });*/
    });
}

io.sockets.on('connection', function(socket) {
    console.log('connected');

    socket.on('disconnect', function() {
        console.log('Good-bye : ' + socket.id);
        var s_id = socket.id;

        var session_id = mobileSessionMgm.getSessionByUserId(s_id);
        var remove_session = sessionMgm.getSessionByUserId(s_id);

        console.log("지우기 확인 : " + session_id + " : " + remove_session + " : " + s_id);
        if(session_id !== null) {
            loginProvider.message(session_id.userId, function(error, result){
                for(var i=0;i<result.length;i++){
                    var user = sessionMgm.getSessionById(result[i].FriendName);
                    if(user != null) {
                        io.sockets.socket(user.ssid).emit('close server', session_id.userId);
                        console.log('server close: web client');
                        sessionMgm.remove(socket.id);
                    } else {
                        var sysMsg = {type: "error", message: "User not found!"};
                        socket.emit('systemMessage', sysMsg);
                    }
                }
            });
                mobileSessionMgm.remove(socket.id);
                console.log('지워졌나?.. ' + mobileSessionMgm.getSessionByUserId(socket.id));
        }else if(remove_session !== null){
            console.log("들오오나???" + socket.id);
            loginProvider.message(remove_session.userId, function(error, result){
                for(var i=0;i<result.length;i++){
                    var user = sessionMgm.getSessionById(result[i].FriendName);
                    if(user != null) {
                        io.sockets.socket(user.ssid).emit('close_Online', remove_session.userId);
                    } else {
                        var sysMsg = {type: "error", message: "User not found!"};
                        socket.emit('systemMessage', sysMsg);
                    }
                }
            });
                sessionMgm.remove(socket.id);
                console.log('지워졌나?.. ' + sessionMgm.getSessionByUserId(socket.id));
        }
        //testcode
        if (!socket.nickname) return;

        delete nicknames[socket.nickname];
        socket.broadcast.emit('announcement', {user: socket.nickname, action: 'disconected'});
        socket.broadcast.emit('nicknames', nicknames);
    });

    socket.on('user', function(data) {
        console.log('라인180 ' + data);

        if(sessionMgm.getSessionById(data) !== null) {
            var Remove_session_id = sessionMgm.getSessionById(data)
            console.log('아웃 : ' + Remove_session_id.ssid);
            io.sockets.socket(Remove_session_id.ssid).emit('removeSession', 'Send_Data');
            sessionMgm.removeByUserId(data);
        }

        var session_data = new Object();
        session_data.ssid = socket.id;
        session_data.userId = data;
        session_data.username = data; //notUsed
        sessionMgm.add(session_data);
        var current_friend_list = [];
        var current_mobile_list = [];
        var session_id = sessionMgm.getSessionById(data);

        loginProvider.message(session_id.userId, function(error, result){
                for(var i=0;i<result.length;i++){
                    var user = sessionMgm.getSessionById(result[i].FriendName);
                    if(user != null) {
                        console.log('여기 확인...: ' + user.userId);
                        //console.log('new user online');
                        current_friend_list.push(user.userId);
                    }
                    //else {
                    //    var sysMsg = {type: "error", message: "User not found!"};
                    //    socket.emit('systemMessage', sysMsg);
                    //}
                }

                for(var i=0;i<result.length;i++){
                    var user = sessionMgm.getSessionById(result[i].FriendName);
                    if(user != null) {
                        //console.log('여기 확인...: ' + user.userId);
                        //console.log('new user online');
                        io.sockets.socket(user.ssid).emit('new_User_Online', session_id.userId);

                    } else {
                        var sysMsg = {type: "error", message: "User not found!"};
                        socket.emit('systemMessage', sysMsg);
                    }
                }

                io.sockets.socket(socket.id).emit('current_Friend', current_friend_list);
                //io.sockets.socket(socket.id).emit('current_Server', current_mobile_list);
                //sessionMgm은 현재 접속되 있는 사용자가 저장, loginProvider.message는 사용자의 친구가 저장
                //서로 비교를 해서 친구이면서 접속중인 사용자를 찾음. 사용자에게 접속한 친구 목록을 보냄.

        });

        loginProvider.requestState(session_id.userId, function(error, result){

            //console.log("여기 확인 : " + result[0].UserName + " : " + result[0].RequestState);
            if(result[0].RequestState === 2) {
                io.sockets.socket(socket.id).emit('current_request', result[0].UserName);
            }
        });

        loginProvider.message(session_id.userId, function(error, result){

            for(var i=0;i<result.length;i++){
                //console.log('758 확인' + result[i].FriendName);
                var m_user = mobileSessionMgm.getSessionById(result[i].FriendName);
                if(m_user != null) {
                    //console.log('모바일 여기 확인...: ' + m_user.userId);
                    if(m_user.nattype === 'private_ip') {
                        m_user.username = 'RelayServer'
                    }
                    current_mobile_list.push(m_user);
                }
            }
            io.sockets.socket(socket.id).emit('current_Server', current_mobile_list);
            //sessionMgm은 현재 접속되 있는 사용자가 저장, loginProvider.message는 사용자의 친구가 저장
            //서로 비교를 해서 친구이면서 접속중인 사용자를 찾음. 사용자에게 접속한 친구 목록을 보냄.
        });

        var address = socket.handshake.address;

        console.log("New connection from " + address.address + ":" + address.port + current_friend_list[0]);
        console.log('user : '+ session_id.ssid + ' : 같아야함 :' + socket.id);

        //io.sockets.socket(Remove_session_id.ssid).emit('new_user_online', 'Send_Data');

    });

    //send_msg_friend
    socket.on('send_msg_friend', function(data) {
        console.log('메세지 받음...' + data[0] + data[1]);
        var myID = sessionMgm.getSessionById(data[0]);
        loginProvider.Insert_Message(data[0], data[1], function(error, result){
            console.log('여기 머가 나오나~~~~' + result);

            loginProvider.message(data[0], function(error, result){

                loginProvider.my_message(data[0], function(my_error, my_result){

                    io.sockets.socket(myID.ssid).emit('new_My_Message', my_result);

                    for(var i=0;i<result.length;i++){
                        var user = sessionMgm.getSessionById(result[i].FriendName);
                        if(user != null) {
                            io.sockets.socket(user.ssid).emit('newMessage', my_result);
                            console.log('send message');
                        } else {
                            var sysMsg = {type: "error", message: "User not found!"};
                            socket.emit('systemMessage', sysMsg);
                        }
                    }
                });

            });

        });
    });

    socket.on('server open', function(data) {
        console.log('Server Open ' + data.nick);
        var address = socket.handshake.address;
        var NAT_type;

        if(mobileSessionMgm.getSessionById(data.nick) !== null) {
            var Remove_session_id = mobileSessionMgm.getSessionById(data.nick)
            console.log('Server Open 아웃 : ' + Remove_session_id.sessionId);
            //다른폰이 같은 아이디로 서버를 열려고 시도함.
            mobileSessionMgm.removeByUserId(data.nick);
        }

        console.log("New connection from " + address.address + " : " + data.m_addr);

        if(address.address !== data.m_addr){
            console.log('아이피가 다르므로 이거슨 private_ip');
            NAT_type = 'private_ip';
        }else{
            console.log('아이피가 같으므로 이거슨 public_ip');
            NAT_type = 'public_ip';
        }

        var session_data = new Object();
        session_data.ssid = socket.id;
        session_data.userId = data.nick;
        session_data.username = address.address; //ipAdress
        session_data.nattype = NAT_type;
        mobileSessionMgm.add(session_data);
        var session_id = mobileSessionMgm.getSessionById(data.nick)
        //console.log('Server Open SessionId : '+ session_id.ssid + ' Local IP : ' + data.m_addr);
        io.sockets.socket(session_id.ssid).emit('server open', {user:session_id.userId, message: NAT_type});
        //socket.broadcast.emit('user message', {user: socket.nickname, message: msg.message});

        loginProvider.message(data.nick, function(error, result){
            var return_data = new Object;
            if(session_id.nattype === 'public_ip') {
                return_data.nick = data.nick;
                return_data.link = address.address + ':55556/Mangosteen/';
            }else{
                return_data.nick = data.nick;
                return_data.link = address.address + '/RelayServer/';
            }

            for(var i=0;i<result.length;i++){
                var user = sessionMgm.getSessionById(result[i].FriendName);
                if(user != null) {
                    io.sockets.socket(user.ssid).emit('server open', return_data);
                    console.log('여기 테스트 보내기 : ' + user.ssid);
                    console.log('server open: web client');

                } else {
                    var sysMsg = {type: "error", message: "User not found!"};
                    socket.emit('systemMessage', sysMsg);
                }
            }
        });
    });

    socket.on('request_ok', function (data) {
        console.log('여기 들어오나요???' + data);

                var user = sessionMgm.getSessionById(data);
                if(user != null) {
                    io.sockets.socket(user.ssid).emit('newRequest', data);
                }
    });

    //test socket
    socket.on('user message', function (msg) {
        socket.broadcast.emit('user message', {user: socket.nickname, message: msg.message});
    });

    socket.on('nickname', function (nick, fn) {
        nickname = nick.nickname;
        if (nicknames[nickname]) {
            console.log("같은 닉네임을 입력 했을때, 여기는 fn이 트루");
            fn(true);
        } else {
            console.log("처음 닉네임을 입력 했을때, 여기는 fn이 펄스" + nicknames);
            fn(false);
            nicknames[nickname] = socket.nickname = nickname;
            socket.broadcast.emit('announcement', {user: nickname, action: 'connected'});
            io.sockets.emit('nicknames', nicknames);
        }
    });

});

app.listen(3000);

console.log('Server listening on port 3000');
