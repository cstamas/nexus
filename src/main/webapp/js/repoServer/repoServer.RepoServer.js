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

// Repository main Controller(conglomerate) Singleton
Sonatype.repoServer.RepoServer = function(){
  var cfg = Sonatype.config.repos;
  var sp = Sonatype.lib.Permissions;
  
  var defaultGroupPanel = {
    title: 'Default',
    id: 'default',
    cls: 'st-server-sub-container',
    layout: 'fit',
    frame: true,
    collapsible: false,
    collapsed: false,
    autoHeight: true,
    html: '<div>default</div>'
  };
  
  var bodyTpl = new Ext.XTemplate(
    '<ul class="group-links">',
    '<tpl for="links">',
      '<li><a id="{id}" href="#">{title}</a></li>',
    '</tpl>',
    '</ul>'
  );

  var linkTpl = new Ext.XTemplate(
    '<ul class="group-links">',
    '<tpl for="links">',
      '<li><a href="{href}" target="{href}">{title}</a></li>',
    '</tpl>',
    '</ul>'
  );


// ************************************  
  return {
    pomDepTmpl : new Ext.XTemplate('<dependency><groupId>{groupId}</groupId><artifactId>{artifactId}</artifactId><version>{version}</version></dependency>'),
    
    loginFormConfig : { 
      labelAlign: 'right',
      labelWidth:60,
      frame:true,  

      defaultType:'textfield',
      monitorValid:true,

      items:[
        { 
          id:'usernamefield',
          fieldLabel:'Username', 
          name:'username',
          tabIndex: 1,
          width: 150,
          allowBlank:false 
        },
        { 
          id:'passwordfield',
          fieldLabel:'Password', 
          name:'password',
          tabIndex: 2, 
          inputType:'password', 
          width: 150,
          allowBlank:false 
        }
      ]
      //buttons added later to provide scope to handler
       
    },

    addClickListeners: function( p ) {
      p.on('render', 
        function(panel){
          panel.body.on('click', Ext.emptyFn, null, {delegate:'a', preventDefault:true});
          panel.body.on('mousedown', this.doAction, this, {delegate:'a'});
        },
        this
      );
    },
    
    // Each Sonatype server will need one of these 
    initServerTab : function() {
      
      // Left Panel
      this.nexusPanel = new Ext.Panel({
        id: 'st-nexus-tab',
        title: 'Nexus',
        cls: 'st-server-panel',
        layout:'fit',
        border: false
        //items: groupConfigs
      });

      this.createSubComponents();
      
      Sonatype.view.serverTabPanel.add(this.nexusPanel);
      this.loginFormConfig.buttons = [{ 
        id:'loginbutton',
        text:'Log In',
        tabIndex: 3, 
        formBind: true,
        scope: this,
        handler:function(){
          this.loginWindow.getEl().mask("Logging you in...");

          Ext.Ajax.request({
            scope: this,
            method: 'GET',
            cbPassThru : {
              username : this.loginForm.find('name', 'username')[0].getValue()
            },
            headers: {'Authorization' : 'Basic ' + Sonatype.utils.base64.encode(this.loginForm.find('name', 'username')[0].getValue() + ':' + this.loginForm.find('name', 'password')[0].getValue())}, //@todo: send HTTP basic auth data
            url: Sonatype.config.repos.urls.login,
            success: function(response, options){
              //get user permissions
              var respObj = Ext.decode(response.responseText);
              var newUserPerms = respObj.data.clientPermissions;

              Sonatype.user.curr.username = options.cbPassThru.username;
              Sonatype.user.curr.authToken = respObj.data.authToken;
              Sonatype.user.curr.repoServer = newUserPerms;
              
              Sonatype.state.CookieProvider.set('authToken', Sonatype.user.curr.authToken);
              Sonatype.state.CookieProvider.set('username', Sonatype.user.curr.username);
              
              Ext.lib.Ajax.defaultHeaders.Authorization = 'NexusAuthToken ' + Sonatype.user.curr.authToken;
              
              Sonatype.user.curr.isLoggedIn = true;
              Sonatype.view.updateLoginLinkText();

              this.resetMainTabPanel();
              
              this.loginWindow.hide();
              this.loginWindow.getEl().unmask();
              this.loginForm.getForm().reset();
              
              this.createSubComponents(); //update left panel
            },
            failure: function(response, options){
              this.loginWindow.getEl().unmask();
              this.loginForm.find('name', 'password')[0].focus(true);
            }

          });

        } 
      }];
      
      this.loginFormConfig.keys = {
        key: Ext.EventObject.ENTER,
        fn: this.loginFormConfig.buttons[0].handler,
        scope: this
      };
      
      this.loginForm = new Ext.form.FormPanel(this.loginFormConfig);
      this.loginWindow = new Ext.Window({
        title:'Nexus Log In',
        animateTarget: 'login-link',
        closable: true,
        closeAction: 'hide',
        autoWidth: false,
        width: 250,
        autoHeight: true,
        modal:true,
        constrain: true,
        resizable: false,
        draggable: false,
        items: [this.loginForm]
      });
      
      this.loginWindow.on('show', function(){
        this.loginForm.find('name', 'username')[0].focus(true, 100);
      }, this);
      
      this.loginWindow.on('close', function(){
        this.loginForm.getForm().reset();
      }, this);
    },
    
    //Add/Replace Nexus left hand components
    createSubComponents : function() {
      var userPerms = Sonatype.user.curr.repoServer;
      var wasRendered = this.nexusPanel.rendered;
      
      if (wasRendered) {
        this.nexusPanel.getEl().mask('Updating...', 'loading-indicator');
        this.nexusPanel.items.each(function(item, i, len){
          this.remove(item, true);
        }, this.nexusPanel);
      }
      
      var groupConfigs = [];
      var panelConf;

      if(sp.checkPermission(userPerms.viewSearch, sp.READ)){
        this.addClickListeners( 
          this.nexusPanel.add( Ext.apply( {},
            {
              title: 'Artifact Search',
              id: 'st-nexus-search',
              items: [
                {
                  xtype: 'trigger',
                  id: 'quick-search--field',
                  triggerClass: 'x-form-search-trigger',
                  repoPanel: this,
                  width: 150,
                  listeners: {
                    'specialkey': {
                      fn: function(f, e){
                        if(e.getKey() == e.ENTER){
                          this.onTriggerClick();
                        }
                      }
                    }
                  },
                  onTriggerClick: function(a,b,c){
                    var v = this.getRawValue();
                    if ( v.length > 0 ) {
                      var panel = this.repoPanel.actions['open-search-all'](this.repoPanel);
                      panel.searchField.setRawValue( this.getRawValue() );
                      panel.startSearch( panel );
                    }
                  }
                }
              ],
              html: bodyTpl.apply({
                links: [
                  { id:'open-search-all', title: 'Advanced Search' }
                ]
              }) 
            },
            defaultGroupPanel )
          )
        );
      }
      
      //Views Group **************************************************
      var vTplData = {links:[]};
      
      if(sp.checkPermission(userPerms.maintRepos, sp.READ)) {
        vTplData.links.push({
          id: sp.checkPermission(userPerms.maintRepos, sp.EDIT) ?
            'open-repos-maint' : 'open-repos-maint-readonly',
          title:'Browse Repositories'
        });
      }
      if(sp.checkPermission(userPerms.viewSystemChanges, sp.READ)){
        vTplData.links.push( {id:'open-system-changes', title:'System Feeds'} );
      }
      if(sp.checkPermission(userPerms.maintLogs, sp.READ) || sp.checkPermission(userPerms.maintConfig, sp.READ)){
         vTplData.links.push( {id:'open-view-logs', title:'Logs and Config Files'} );
      }
      if(vTplData.links.length > 0){
        panelConf = Ext.apply({}, {title:'Views', id:'st-nexus-views', html: bodyTpl.apply(vTplData)}, defaultGroupPanel);
        this.addClickListeners( this.nexusPanel.add(panelConf) ); 
        //groupConfigs.push(panelConf);
      }

      //Config Group **************************************************
      var cTplData = {links:[]};
      if(sp.checkPermission(userPerms.configServer, sp.EDIT)){
        cTplData.links.push( {id:'open-config-server', title:'Server'} );
      }
      if(sp.checkPermission(userPerms.configRepos, sp.EDIT)){
        cTplData.links.push( {id:'open-config-repos', title:'Repositories'} );
      }
      if(sp.checkPermission(userPerms.configGroups, sp.EDIT)){
        cTplData.links.push( {id:'open-config-groups', title:'Groups'} );
      }
      if(sp.checkPermission(userPerms.configRules, sp.EDIT)){
        cTplData.links.push( {id:'open-config-rules', title:'Routing'} );
      }
      if(sp.checkPermission(userPerms.configSchedules, sp.EDIT)){
        cTplData.links.push( {id:'open-config-schedules', title:'Scheduled Tasks'} );
      }
      if(cTplData.links.length > 0){
        panelConf = Ext.apply({}, {title:'Administration', id:'st-nexus-config', html: bodyTpl.apply(cTplData)}, defaultGroupPanel);
        this.addClickListeners( this.nexusPanel.add(panelConf) ); 
        //groupConfigs.push(panelConf);
      }

      //Security Group **************************************************
      var sTplData = {links:[]};
      if( sp.checkPermission( userPerms.configUsers, sp.EDIT ) ) {
        sTplData.links.push( { id: 'open-security-users', title: 'Users' } );
      }
      if ( sp.checkPermission( userPerms.configRoles, sp.EDIT ) ) {
        sTplData.links.push( { id: 'open-security-roles', title: 'Roles' } );
      }
      if ( sTplData.links.length > 0 ){
        panelConf = Ext.apply( {}, { title:'Security', id: 'st-nexus-security', html: bodyTpl.apply( sTplData ) }, defaultGroupPanel );
        this.addClickListeners( this.nexusPanel.add(panelConf) );
      }

      this.nexusPanel.add( Ext.apply( {},
        {
          title: 'Help',
          id: 'st-nexus-docs',
          collapsible: true,
          collapsed: true,
          html: linkTpl.apply({
            links: [
              { href: 'http://nexus.sonatype.org/', title: 'Nexus Home' },
              { href: 'http://www.sonatype.com/book/reference/repository-manager.html', title: 'Getting Started' },
              { href: 'https://docs.sonatype.com/display/Nexus/Home', title: 'Nexus Wiki' },
              { href: 'http://www.sonatype.com/book/reference/public-book.html', title: 'Maven Book' },
              { href: 'http://nexus.sonatype.org/changes.html', title: 'Release Notes' },
              { href: 'http://issues.sonatype.org/browse/NEXUS', title: 'Report Issues' }
            ]
          }) 
        },
        defaultGroupPanel ) );

      if (wasRendered) {
        this.nexusPanel.doLayout();
        this.nexusPanel.getEl().unmask();
        //this.nexusPanel.enable();
      }
      
    },
    
    doAction : function(e, target){
      e.stopEvent();
      this.actions[target.id](this);
    },
    
    actions : {
      'open-search-all' : function(scope) {
        var id = 'st-nexus-search-panel';
        return Sonatype.view.mainTabPanel.addOrShowTab(id, Sonatype.repoServer.SearchPanel, {title: 'Search'});
      },
      'open-checksum-search' : function(scope) {
        var id = 'st-nexus-checksum-search-panel';
        Sonatype.view.mainTabPanel.addOrShowTab(id, Sonatype.repoServer.ChecksumSearchPanel, {title: 'Checksum Search'});
      },
      'open-system-changes' : function(scope) {
        var id = 'feed-view-system_changes';
        Sonatype.view.mainTabPanel.addOrShowTab(id, Sonatype.repoServer.FeedViewPanel, {title: 'System Feeds'});
      },      
      'open-view-logs' : function(scope) {
        var id = 'view-logs';
        Sonatype.view.mainTabPanel.addOrShowTab(id, Sonatype.repoServer.LogsViewPanel, {title: 'Logs and Configs'});
      },
      'open-repos-maint-readonly' : function(scope){
        var id = 'repos-maint-readonly';
        Sonatype.view.mainTabPanel.addOrShowTab(id, Sonatype.repoServer.RepoMaintPanel, {title: 'Repositories', editMode: false});
      },
      'open-repos-maint' : function(scope){
        var id = 'repos-maint';
        Sonatype.view.mainTabPanel.addOrShowTab(id, Sonatype.repoServer.RepoMaintPanel, {title: 'Repositories', editMode: true});
      },
      'open-config-server' : function(scope){
        var id = 'nexus-config';
        Sonatype.view.mainTabPanel.addOrShowTab(id, Sonatype.repoServer.ServerEditPanel, {title: 'Nexus'});
      },
      'open-config-repos' : function(scope){
        var id = 'repos-config';
        Sonatype.view.mainTabPanel.addOrShowTab(id, Sonatype.repoServer.RepoEditPanel, {title: 'Repository Config'});
      },
      'open-config-groups' : function(scope){
        var id = 'groups-config';
        Sonatype.view.mainTabPanel.addOrShowTab(id, Sonatype.repoServer.GroupsEditPanel, {title: 'Groups'});
      },
      'open-config-rules' : function(scope){
        var id = 'routes-config';
        Sonatype.view.mainTabPanel.addOrShowTab(id, Sonatype.repoServer.RoutesEditPanel, {title: 'Routing'});
      },
      'open-config-schedules' : function(scope){
        var id = 'schedules-config';
        Sonatype.view.mainTabPanel.addOrShowTab(id, Sonatype.repoServer.SchedulesEditPanel, {title: 'Scheduled Tasks'});
      },
      'open-security-users' : function(scope){
        var id = 'security-users';
        Sonatype.view.mainTabPanel.addOrShowTab(id, Sonatype.repoServer.UserEditPanel, {title: 'Users'});
      },
      'open-security-roles' : function(scope){
        var id = 'security-roles';
        Sonatype.view.mainTabPanel.addOrShowTab(id, Sonatype.repoServer.RoleEditPanel, {title: 'Roles'});
      }
    },
    
    loginHandler : function(){
      if (Sonatype.user.curr.isLoggedIn) {
        //do logout
        Ext.Ajax.request({
          scope: this,
          method: 'GET',
          url: Sonatype.config.repos.urls.logout,
          callback: function(options, success, response){
            //note: we don't care about success or failure from the server here.
            //      if the token was expired (403), we can still go to anonymous client state
            delete Ext.lib.Ajax.defaultHeaders.Authorization;

            Sonatype.state.CookieProvider.clear('authToken');
            Sonatype.state.CookieProvider.clear('username');
            
            this.resetMainTabPanel();
            
            Sonatype.user.curr = Sonatype.utils.cloneObj(Sonatype.user.anon);
            Sonatype.view.updateLoginLinkText();
            this.createSubComponents(); //update left panel
          }
        });
        
      }
      else {
        this.loginForm.getForm().clearInvalid();
        this.loginWindow.show();
      }
    },
    
    resetMainTabPanel: function() {
      Sonatype.view.mainTabPanel.items.each(function(item, i, len){
        this.remove( item, true );
      }, Sonatype.view.mainTabPanel);
      Sonatype.view.mainTabPanel.activeTab = null;
      
      Sonatype.view.mainTabPanel.add(Sonatype.view.welcomeTab);
      Sonatype.view.mainTabPanel.setActiveTab(Sonatype.view.welcomeTab);
    }
     
  };
}();


})();