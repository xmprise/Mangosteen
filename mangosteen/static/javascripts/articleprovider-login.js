var sys =  require('util');
var rest = require('restler');

LoginProvider = function(){};

LoginProvider.prototype.login = function(id, pwd, callback) {
    rest.post('http://210.118.69.65/mangosteen_rest/mangosteen_rest/rest/login', {
        data: {email:id, pwd:pwd}
    }).on('complete', function(data, response) {
            if(data === null){
                console.log("몰라~~~~");
                callback(null, data);
            }else
            callback(null,data)
    });
};

LoginProvider.prototype.join = function(id, pwd, email, name, callback) {
    rest.post('http://210.118.69.65/mangosteen_rest/mangosteen_rest/rest/users', {
        data: {id:id, pwd:pwd, email:email, name:name}
    }).on('complete', function(data, response) {
           callback(null,data)
        });
};

LoginProvider.prototype.my_message = function(id, callback) {
    rest.post('http://210.118.69.65/mangosteen_rest/mangosteen_rest/rest/viewmylist', {
        data: {id:id}
    }).on('complete', function(data, response) {
            callback(null,data)
        });
};

LoginProvider.prototype.message = function(id, callback) {
    rest.post('http://210.118.69.65/mangosteen_rest/mangosteen_rest/rest/viewmyfriendlist', {
        data: {id:id}
    }).on('complete', function(data, response) {
            callback(null,data)
        });
};

LoginProvider.prototype.all_my_message = function(id, callback) {
    rest.post('http://210.118.69.65/mangosteen_rest/mangosteen_rest/rest/viewallmylist', {
       data: {id:id}
    }).on('complete', function(data, response) {
            callback(null, data)
        });
};

LoginProvider.prototype.Insert_Message = function(id, msg, callback) {
    rest.post('http://210.118.69.65/mangosteen_rest/mangosteen_rest/rest/insertmessage', {
        data: {id:id, msg:msg}
    }).on('complete', function(data, response) {
            callback(null, data);
        });
};

LoginProvider.prototype.viewMyPicture = function (id, callback) {
    rest.post('http://210.118.69.65/mangosteen_rest/mangosteen_rest/rest/viewMyPicture', {
        data: {id:id}
    }).on('complete', function(data, response) {
            callback(null, data);
        });
};

LoginProvider.prototype.InsertMyPicture = function(id, url) {
    rest.post('http://210.118.69.65/mangosteen_rest/mangosteen_rest/rest/InsertMyPicture', {
        data: {id:id, url:url}
    });
};

LoginProvider.prototype.searchFriend = function(id, callback) {
    rest.post('http://210.118.69.65/mangosteen_rest/mangosteen_rest/rest/searchFriend', {
        data: {id:id}
    }).on('complete', function(data, response) {
           callback(null, data);
        });
};

LoginProvider.prototype.requestFriend = function(id, friend,callback) {
    rest.post('http://210.118.69.65/mangosteen_rest/mangosteen_rest/rest/requestFriend', {
        data: {id:id, friend:friend}
    }).on('complete', function(data, response) {
            callback(null, data);
        });
};

LoginProvider.prototype.invitationList = function(id, callback) {
    rest.post('http://210.118.69.65/mangosteen_rest/mangosteen_rest/rest/invitationlist', {
        data: {id:id}
    }).on('complete', function(data, response) {
            callback(null, data);
        });
};

LoginProvider.prototype.accptedRequestFriend = function(id, friend,callback) {
    rest.post('http://210.118.69.65/mangosteen_rest/mangosteen_rest/rest/accptedRequestFriend', {
        data: {id:id, friend:friend}
    }).on('complete', function(data, response) {
            callback(null, data);
        });
};

LoginProvider.prototype.rejectRequestFriend = function(id, friend,callback) {
    rest.post('http://210.118.69.65/mangosteen_rest/mangosteen_rest/rest/rejectRequestFriend', {
        data: {id:friend, friend:id}
    }).on('complete', function(data, response) {
            callback(null, data);
        });
};

LoginProvider.prototype.requestState = function(id, callback) {
    rest.post('http://210.118.69.65/mangosteen_rest/mangosteen_rest/rest/requestState', {
        data: {id:id}
    }).on('complete', function(data, response) {
            callback(null,data)
        });
};

LoginProvider.prototype.UpdateRequestSate = function(id, data_r, callback) {
    rest.post('http://210.118.69.65/mangosteen_rest/mangosteen_rest/rest/UpdateRequestSate', {
        data: {id:id, data_r:data_r}
    }).on('complete', function(data, response) {
            callback(null, data);
        });
};

exports.LoginProvider = LoginProvider;