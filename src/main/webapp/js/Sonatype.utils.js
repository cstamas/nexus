/*
 * Nexus: Maven Repository Manager
 * Copyright (C) 2008 Sonatype Inc.                                                                                                                          
 * 
 * This file is part of Nexus.                                                                                                                                  
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 */
(function(){

Sonatype.utils = {
  passwordPlaceholder : '|$|N|E|X|U|S|$|',
  version : '',
  lowercase : function(str){
    if (Ext.isEmpty(str)) {return str;}
    str = str.toString();
    return str.toLowerCase();
  },
  lowercaseFirstChar : function(str){
    if (Ext.isEmpty(str)) {return str;}
    str = str.toString();
    return str.charAt(0).toLowerCase() + str.slice(1);
  },
  capitalize : function(str){
    if (Ext.isEmpty(str)) {return str;}
    str = str.toString();
    return str.charAt(0).toUpperCase() + str.slice(1);
  },
  returnEmptyStr : function(){
    return '';
  },
  returnValidStr : function(str){
    if (str != null) {
      return str;
    }
    else {
      return Sonatype.utils.returnEmptyStr();
    }
  },
  convert : {
    stringContextToBool : function(str){
      return (str.toLowerCase() === 'true');
    },
    passwordToString : function(str){
      if (Sonatype.utils.passwordPlaceholder === str){
        return null;
      }
      else if (str){
        return str;
      }
      else{
        return Sonatype.utils.returnEmptyStr();
      }
    }
  },
  //deep copy of an object.  All references independent from object passed in.
  cloneObj : function(o){
    if(typeof(o) != 'object' || o === null) {
      return o;
    }
    
    var newObj = {};

    for(var i in o){
        newObj[i] = Sonatype.utils.cloneObj(o[i]);
    }

    return newObj;
  },
  
  // (Array : arr, string : child, [string seperator])
  // array to join, name of element of contained object, seperator (defaults to ", ")
  joinArrayObject : function (arr, child, seperator){
    var sOut = '';
    var sep = (seperator) ? seperator : ', ';
    
    for(var i=0; i<arr.length; i++){
      if((arr[i])[child]){
        sOut += (arr[i])[child] + sep;
      }
    }
    
    return sOut.substring(0, sOut.length - sep.length);
  },

  connectionError: function( response, message, offerRestart, options, responseText ) {
    var serverMessage = ''; 
    var r = response.responseText;
    if ( r ) {
      var n1 = r.toLowerCase().indexOf( '<h3>' ) + 4;
      var n2 = r.toLowerCase().indexOf( '</h3>' );
      if ( n2 > n1 ) {
        serverMessage = '<br /><br />' + r.substring( n1, n2 );
      }
    }

    if ( response.status == 403 || response.status == 401 ) {
      if ( options && options.options && options.options.ignore401 ) {
        return;
      }
      
      if ( Sonatype.repoServer.RepoServer.loginWindow.isVisible() ) {
        var nexusReason = response.getResponseHeader['X-Nexus-Reason']; 
        if ( nexusReason && nexusReason.substring(0,7) == 'expired' ) {
          Sonatype.repoServer.RepoServer.loginWindow.hide();
          Sonatype.utils.changePassword( Sonatype.repoServer.RepoServer.loginForm.find('name', 'username')[0].getValue() );
        }
        else {
          Sonatype.MessageBox.show( {
            title: 'Login Error',
            msg: 'Incorrect username, password or no permission to use the Nexus User Interface.<br />Try again.' + serverMessage,
            buttons: Sonatype.MessageBox.OK,
            icon: Sonatype.MessageBox.ERROR,
            animEl: 'mb3'
          } );
        }
      }
      else {
        Sonatype.utils.clearCookie('JSESSIONID');
        Sonatype.utils.clearCookie('nxRememberMe');

        Sonatype.MessageBox.show( {
          title: 'Authentication Error',
          msg: 'Your login is incorrect or your session has expired.<br />' +
            'Please login again.' + serverMessage,
          buttons: Sonatype.MessageBox.OK,
          icon: Sonatype.MessageBox.ERROR,
          animEl: 'mb3',
          fn: function(button) {
            window.location.reload();
          }
        } );
      }
    }
    else {
      Sonatype.MessageBox.show( {
        title: "Error",
        msg: (
          ( message ? message + serverMessage + '<br /><br />' : '' ) +
          ( response.status == '400' && responseText ? 
              response.responseText
              :
          ( response.status ?
              'Nexus returned an error: ERROR ' + response.status + ': ' + response.statusText
              :
              'There was an error communicating with the Nexus server: ' + response.statusText + '<br />' +
              'Check the status of the server, and log in to the application again.' ) ) +
          ( offerRestart ?
              '<br /><br />Click OK to reload the console or ' +
              'CANCEL if you wish to retry the same action in a little while.'
              : '' )
        ),
        buttons: offerRestart ? Sonatype.MessageBox.OKCANCEL : Sonatype.MessageBox.OK,
        icon: Sonatype.MessageBox.ERROR,
        animEl: 'mb3',
        fn: function(button) {
          if ( offerRestart && button == "ok" ) {
            window.location.reload();
          }
        }
      } );
    }
  },
  
  /**
  *  Base64 encode / decode
  *  http://www.webtoolkit.info/
  **/
  base64 : function(){
    // private property
    var _keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
    
    // private method for UTF-8 encoding
    var _utf8_encode = function (string) {
        string = string.replace(/\r\n/g,"\n");
        var utftext = "";

        for (var n = 0; n < string.length; n++) {
            var c = string.charCodeAt(n);

            if (c < 128) {
                utftext += String.fromCharCode(c);
            }
            else if((c > 127) && (c < 2048)) {
                utftext += String.fromCharCode((c >> 6) | 192);
                utftext += String.fromCharCode((c & 63) | 128);
            }
            else {
                utftext += String.fromCharCode((c >> 12) | 224);
                utftext += String.fromCharCode(((c >> 6) & 63) | 128);
                utftext += String.fromCharCode((c & 63) | 128);
            }
        }

        return utftext;
    };

    // private method for UTF-8 decoding
    var _utf8_decode = function (utftext) {
        var string = "";
        var i = 0;
        var c = c1 = c2 = 0;

        while ( i < utftext.length ) {
            c = utftext.charCodeAt(i);

            if (c < 128) {
                string += String.fromCharCode(c);
                i++;
            }
            else if((c > 191) && (c < 224)) {
                c2 = utftext.charCodeAt(i+1);
                string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
                i += 2;
            }
            else {
                c2 = utftext.charCodeAt(i+1);
                c3 = utftext.charCodeAt(i+2);
                string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
                i += 3;
            }
        }

        return string;
    };
    
    return {
        // public method for encoding
        encode : function (input) {
            var output = "";
            var chr1, chr2, chr3, enc1, enc2, enc3, enc4;
            var i = 0;

            input = _utf8_encode(input);

            while (i < input.length) {
                chr1 = input.charCodeAt(i++);
                chr2 = input.charCodeAt(i++);
                chr3 = input.charCodeAt(i++);

                enc1 = chr1 >> 2;
                enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
                enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
                enc4 = chr3 & 63;

                if (isNaN(chr2)) {
                    enc3 = enc4 = 64;
                } else if (isNaN(chr3)) {
                    enc4 = 64;
                }

                output = output +
                _keyStr.charAt(enc1) + _keyStr.charAt(enc2) +
                _keyStr.charAt(enc3) + _keyStr.charAt(enc4);
            }

            return output;
        },

        // public method for decoding
        decode : function (input) {
            var output = "";
            var chr1, chr2, chr3;
            var enc1, enc2, enc3, enc4;
            var i = 0;

            input = input.replace(/[^A-Za-z0-9\+\/\=]/g, "");

            while (i < input.length) {
                enc1 = _keyStr.indexOf(input.charAt(i++));
                enc2 = _keyStr.indexOf(input.charAt(i++));
                enc3 = _keyStr.indexOf(input.charAt(i++));
                enc4 = _keyStr.indexOf(input.charAt(i++));

                chr1 = (enc1 << 2) | (enc2 >> 4);
                chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
                chr3 = ((enc3 & 3) << 6) | enc4;

                output = output + String.fromCharCode(chr1);

                if (enc3 != 64) {
                    output = output + String.fromCharCode(chr2);
                }
                if (enc4 != 64) {
                    output = output + String.fromCharCode(chr3);
                }
            }

            output = _utf8_decode(output);

            return output;
        }
    }
  }(),
  
  defaultToNo: function() {
    //@note: this handler selects the "No" button as the default
    //@todo: could extend Sonatype.MessageBox to take the button to select as a param
    Sonatype.MessageBox.getDialog().on('show', function(){
        this.focusEl = this.buttons[2]; //ack! we're offset dependent here
        this.focus();
      },
      Sonatype.MessageBox.getDialog(),
      {single:true}
    );
  },
  
  getCookie: function(cookieName) {
    var c = document.cookie + ";";
    var re = /\s?(.*?)=(.*?);/g;
    var matches;
    while((matches = re.exec(c)) != null){
      if ( matches[1] == cookieName ) {
    	return matches[2];
      }
    }
    return null;
  },
  
  setCookie: function(cookieName, value) {
    document.cookie = cookieName + "=" + value +
	    "; path=" + Sonatype.config.resourcePath
  },

  clearCookie : function(cookieName){
    document.cookie = cookieName + "=null; expires=Thu, 01-Jan-70 00:00:01 GMT" +
      "; path=" + Sonatype.config.resourcePath
  },
  
  recoverUsername: function() {
    var w = new Ext.Window({
      title: 'Username Recovery',
      closable: true,
      autoWidth: false,
      width: 300,
      autoHeight: true,
      modal:true,
      constrain: true,
      resizable: false,
      draggable: false,
      items: [
        {
          xtype: 'form',
          labelAlign: 'right',
          labelWidth:60,
          frame:true,  
          defaultType:'textfield',
          monitorValid:true,
          items:[
            {
              xtype: 'panel',
              style: 'padding-left: 70px; padding-bottom: 10px',
              html: 'Please enter the e-mail address you used to register your account and we will send you your username.'
            },
            {
              fieldLabel: 'E-mail', 
              name: 'email',
              width: 200,
              allowBlank: false 
            }
          ],
          buttons: [
            {
              text: 'E-mail Username',
              formBind: true,
              scope: this,
              handler: function(){
                var email = w.find('name', 'email')[0].getValue();

                Ext.Ajax.request({
                  scope: this,
                  method: 'POST',
                  jsonData: {
                    data: {
                      email: email
                    }
                  },
                  url: Sonatype.config.repos.urls.usersForgotId + '/' + email,
                  success: function(response, options){
                    w.close();
                    Sonatype.MessageBox.show( {
                      title: 'Username Recovery',
                      msg: 'Username request completed successfully.' +
                        '<br /><br />' +
                        'Check your mailbox, the username reminder should arrive in a few minutes.',
                      buttons: Sonatype.MessageBox.OK,
                      icon: Sonatype.MessageBox.INFO,
                      animEl: 'mb3'
                    } );
                  },
                  failure: function(response, options){
                    Sonatype.utils.connectionError( response, 'There is a problem retrieving your username.' )
                  }
                });
              }
            },
            {
              text: 'Cancel',
              formBind: false,
              scope: this,
              handler: function(){
                w.close();
              }
            }
          ]
        }
      ]
    });

    w.show();
  },
  
  recoverPassword: function() {
    var w = new Ext.Window({
      title: 'Password Recovery',
      closable: true,
      autoWidth: false,
      width: 300,
      autoHeight: true,
      modal:true,
      constrain: true,
      resizable: false,
      draggable: false,
      items: [
        {
          xtype: 'form',
          labelAlign: 'right',
          labelWidth:60,
          frame:true,  
          defaultType:'textfield',
          monitorValid:true,
          items:[
            {
              xtype: 'panel',
              style: 'padding-left: 70px; padding-bottom: 10px',
              html: 'Please enter your username and e-mail address below. We will send you a new password.'
            },
            { 
              fieldLabel: 'Username', 
              name: 'username',
              width: 200,
              allowBlank: false 
            },
            { 
              fieldLabel: 'E-mail', 
              name: 'email',
              width: 200,
              allowBlank: false 
            }
          ],
          buttons: [
            {
              text: 'Reset Password',
              formBind: true,
              scope: this,
              handler: function(){
                var username = w.find('name', 'username')[0].getValue();
                var email = w.find('name', 'email')[0].getValue();

                Ext.Ajax.request({
                  scope: this,
                  method: 'POST',
                  jsonData: {
                    data: {
                      userId: username,
                      email: email
                    }
                  },
                  url: Sonatype.config.repos.urls.usersForgotPassword,
                  success: function(response, options){
                    w.close();
                    Sonatype.MessageBox.show( {
                      title: 'Reset Password',
                      msg: 'Password request completed successfully.' +
                        '<br /><br />' +
                        'Check your mailbox, your new password should arrive in a few minutes.',
                      buttons: Sonatype.MessageBox.OK,
                      icon: Sonatype.MessageBox.INFO,
                      animEl: 'mb3'
                    } );
                  },
                  failure: function(response, options){
                    Sonatype.utils.connectionError( response, 'There is a problem resetting your password.' )
                  }
                });
              }
            },
            {
              text: 'Cancel',
              formBind: false,
              scope: this,
              handler: function(){
                w.close();
              }
            }
          ]
        }
      ]
    });

    w.show();
  },
  
  changePassword: function( expiredUsername ) {
    var w = new Ext.Window({
      title: 'Change Password',
      closable: true,
      autoWidth: false,
      width: 350,
      autoHeight: true,
      modal:true,
      constrain: true,
      resizable: false,
      draggable: false,
      items: [
        {
          xtype: 'form',
          labelAlign: 'right',
          labelWidth:110,
          frame:true,  
          defaultType:'textfield',
          monitorValid:true,
          items:[
            {
              xtype: 'panel',
              style: 'padding-left: 70px; padding-bottom: 10px',
              html: ( expiredUsername ? 'Your password has expired, you need to reset it. ' : '' ) +
                'Please enter your current password and then the new password twice to confirm.'
            },
            { 
              fieldLabel: 'Current Password', 
              inputType: 'password',
              name: 'currentPassword',
              width: 200,
              allowBlank: false 
            },
            { 
              fieldLabel: 'New Password', 
              inputType: 'password',
              name: 'newPassword',
              width: 200,
              allowBlank: false 
            },
            { 
              fieldLabel: 'Confirm Password', 
              inputType: 'password',
              name: 'confirmPassword',
              width: 200,
              allowBlank: false,
              validator: function( s ) {
                var firstField = this.ownerCt.find( 'name', 'newPassword' )[0];
                if ( firstField && firstField.getRawValue() != s ) {
                  return "Passwords don't match";
                }
                return true;
              }
            }
          ],
          buttons: [
            {
              text: 'Change Password',
              formBind: true,
              scope: this,
              handler: function(){
                var currentPassword = w.find('name', 'currentPassword')[0].getValue();
                var newPassword = w.find('name', 'newPassword')[0].getValue();

                Ext.Ajax.request({
                  scope: this,
                  method: 'POST',
                  jsonData: {
                    data: {
                      userId: expiredUsername ? expiredUsername : Sonatype.user.curr.username,
                      oldPassword: currentPassword,
                      newPassword: newPassword
                    }
                  },
                  url: Sonatype.config.repos.urls.usersChangePassword,
                  success: function(response, options){
                    if ( expiredUsername ) {
                      Sonatype.utils.doLogin( w, expiredUsername, newPassword );
                      w.close();
                    }
                    else {
                      w.close();
                      Sonatype.MessageBox.show( {
                        title: 'Password Changed',
                        msg: 'Password change request completed successfully.',
                        buttons: Sonatype.MessageBox.OK,
                        icon: Sonatype.MessageBox.INFO,
                        animEl: 'mb3'
                      } );
                    }
                  },
                  failure: function(response, options){
                    Sonatype.utils.connectionError( response, 'There is a problem changing your password.' )
                  }
                });
              }
            },
            {
              text: 'Cancel',
              formBind: false,
              scope: this,
              handler: function(){
                w.close();
              }
            }
          ]
        }
      ]
    });

    w.show();
  },
  
  doLogin: function( activeWindow, username, password ) {
    activeWindow.getEl().mask("Logging you in...");

    var token = Sonatype.utils.base64.encode(username + ':' + password); 
    Ext.Ajax.request({
      method: 'GET',
      cbPassThru : {
        username : username
      },
      headers: {'Authorization' : 'Basic ' + token}, //@todo: send HTTP basic auth data
      url: Sonatype.config.repos.urls.login,
      success: function(response, options){
        activeWindow.getEl().unmask();
        if ( Sonatype.repoServer.RepoServer.loginWindow.isVisible() ) {
          Sonatype.repoServer.RepoServer.loginWindow.hide();
          Sonatype.repoServer.RepoServer.loginForm.getForm().reset();
        }

        Sonatype.utils.loadNexusStatus();
      },
      failure: function(response, options){
        activeWindow.getEl().unmask();
        if ( Sonatype.repoServer.RepoServer.loginWindow.isVisible() ) {
          Sonatype.repoServer.RepoServer.loginForm.find('name', 'password')[0].focus(true);
        }
      }

    });
  },
  
  loadNexusStatus: function() {
    Sonatype.user.curr = Sonatype.utils.cloneObj(Sonatype.user.anon);

    Ext.Ajax.request({
      method: 'GET',
      options: { ignore401: true },
      url: Sonatype.config.repos.urls.status,
      callback: function(options, success, response){
        if ( success ) {
          var respObj = Ext.decode(response.responseText);
  
          Sonatype.utils.version = respObj.data.version;
          
          Sonatype.user.curr.repoServer = respObj.data.clientPermissions.permissions;
          Sonatype.user.curr.isLoggedIn = respObj.data.clientPermissions.loggedIn;
          Sonatype.user.curr.username = respObj.data.clientPermissions.loggedInUsername;
          
          var availSvrs = Sonatype.config.installedServers;
          for(var srv in availSvrs) {
            if (availSvrs[srv] && typeof(Sonatype[srv]) != 'undefined') {
              Sonatype[srv][Sonatype.utils.capitalize(srv)].statusComplete(respObj);
            }
          }
        }
        else {
          Sonatype.utils.version = 'Version unavailable';
        }
        
        Ext.get('version').update(Sonatype.utils.version);
        Sonatype.view.updateLoginLinkText();
        Sonatype.repoServer.RepoServer.resetMainTabPanel();
        Sonatype.repoServer.RepoServer.createSubComponents();
      }
    });
  }
};

})();


Ext.apply(Ext.form.VTypes, {
  password: function(val, field) {
    if (field.initialPasswordField != null && field.initialPasswordField != 'undefined') {
      var pwd = field.ownerCt.find('name', field.initialPasswordField)[0].getValue();
      return (val == pwd);
    }
    return true;
  },
  
  passwordText: 'Passwords do not match.'
});
