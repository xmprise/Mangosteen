module.exports = mobileSessionManagement;

var sessions = [];

var mobileSessionManagement = {
    indexOf: function(sessionId) {
        for(var i=0;i<sessions.length;i++) {
            if(sessions[i].ssid == sessionId)
                return i;
        }

        return null;
    },

    indexOfUser: function(userId) {
        for(var i in sessions) {
            if(sessions[i].userId == userId)
            return i;
        }

        return null;
    },

    add: function(sessionData) {
        sessions.push(sessionData);
    },

    remove: function(sessionId) {
        var index = this.indexOf(sessionId);
        if(index != null) {
            sessions.splice(index, 1);
        } else {
            return null;
        }
    },

    removeByUserId: function(userId) {
        var index = this.indexOfUser(userId);
        if(index != null) {
            sessions.splice(index, 1);
        } else {
            return null;
        }
    },

    getSessionById: function(userId) {
        var index = this.indexOfUser(userId);

        if(index != null) {
            return sessions[index];
        } else {
            return null;
        }
    },

    getSessionByUserId: function(sessionId) {
        var index = this.indexOf(sessionId);

        if(index != null) {
            return sessions[index];
        } else {
            return null;
        }
    }
};

module.exports = mobileSessionManagement;