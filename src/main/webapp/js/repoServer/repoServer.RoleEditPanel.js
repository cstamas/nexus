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
/*
 * Role Edit/Create panel layout and controller
 */
  
Sonatype.repoServer.RoleEditPanel = function(config){
  var config = config || {};
  var defaultConfig = {};
  Ext.apply(this, config, defaultConfig);
  
  var ht = Sonatype.repoServer.resources.help.roles;
    
  //List of privileges
  //TODO: This will be a list retrieved from server via REST, currently no privilege api, so hardcoding
  this.privilegeList = [{id : 'privid', name : 'privname'},{id : 'privid2', name : 'privname2'}];
  
  this.actions = {
    refresh : new Ext.Action({
      text: 'Refresh',
      iconCls: 'st-icon-refresh',
      scope:this,
      handler: this.reloadAll
    }),
    deleteAction : new Ext.Action({
      text: 'Delete',
      scope:this,
      handler: this.deleteHandler
    })
  };
  
  //Methods that will take the incoming json data and map over to the ui controls
  this.loadDataModFunc = {
    "roles" : this.loadRolesTreeHelper.createDelegate(this),
    "privileges" : this.loadPrivilegesTreeHelper.createDelegate(this)
  };
  
  //Methods that will take the data from the ui controls and map over to json
  this.submitDataModFunc = {
    "roles" : this.saveRolesTreeHelper.createDelegate(this),
    "privileges" : this.savePrivilegesTreeHelper.createDelegate(this)
  };
  
  this.roleRecordConstructor = Ext.data.Record.create([
    {name:'resourceURI'},
    {name:'id'},
    {name:'name', sortType:Ext.data.SortTypes.asUCString},
    {name:'description'},
    {name:'sessionTimeout'},
    {name:'privileges'},
    {name:'roles'}
  ]);
  
  
  //Reader and datastore that queries the server for the list of currently defined roles
  this.rolesReader = new Ext.data.JsonReader({root: 'data', id: 'resourceURI'}, this.roleRecordConstructor );
  this.rolesDataStore = new Ext.data.Store({
    url: Sonatype.config.repos.urls.roles,
    reader: this.rolesReader,
    sortInfo: {field: 'id', direction: 'ASC'},
    autoLoad: true
  });
  
  this.COMBO_WIDTH = 300;
  
  //Build the form
  this.formConfig = {
    region: 'center',
    width: '100%',
    height: '100%',
    autoScroll: true,
    border: false,
    frame: true,
    collapsible: false,
    collapsed: false,
    labelWidth: 200,
    layoutConfig: {
      labelSeparator: ''
    },
        
    items: [
      {
        xtype: 'hidden',
        name: 'id'
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Name',
        itemCls: 'required-field',
        labelStyle: 'margin-left: 15px; width: 185px;',
        helpText: ht.name,
        name: 'name',
        allowBlank: false,
        width: this.COMBO_WIDTH
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Description',
        itemCls: 'required-field',
        labelStyle: 'margin-left: 15px; width: 185px;',
        helpText: ht.description,
        name: 'description',
        allowBlank: false,
        width: this.COMBO_WIDTH
      },
      {
        xtype: 'numberfield',
        fieldLabel: 'Session Timeout',
        itemCls: 'required-field',
        labelStyle: 'margin-left: 15px; width: 185px;',
        helpText: ht.sessionTimeout,
        name: 'sessionTimeout',
        allowBlank: false,
        width: this.COMBO_WIDTH,
        value: 60
      },
      {
        xtype: 'panel',
        id: 'roles_privs_tree_panel',
        layout: 'column',
        autoHeight: true,
        style: 'padding: 10px 0 0 0',
        
        items: [
          {
            xtype: 'treepanel',
            id: 'roles_privs_tree', //note: unique ID is assinged before instantiation
            title: 'Selected Roles / Privileges',
            border: true, //note: this seem to have no effect w/in form panel
            bodyBorder: true, //note: this seem to have no effect w/in form panel
            //note: this style matches the expected behavior
            bodyStyle: 'background-color:#FFFFFF; border: 1px solid #B5B8C8',
            style: 'padding: 0 20px 0 0',
            width: 225,
            height: 300,
            animate:true,
            lines: false,
            autoScroll:true,
            containerScroll: true,
            //@note: root node must be instantiated uniquely for each instance of treepanel
            //@ext: can TreeNode be registerd as a component with an xtype so this new root node
            //      may be instantiated uniquely for each form panel that uses this config?
            rootVisible: false,

            enableDD: true,
            ddScroll: true,
            dropConfig: {
              allowContainerDrop: true,
              onContainerDrop: function(source, e, data){
                this.tree.root.appendChild(data.node);
                return true;
              },
              onContainerOver:function(source, e, data){return this.dropAllowed;},
              // passign padding to make whole treePanel the drop zone.  This is dependent
              // on a sonatype fix in the Ext.dd.DropTarget class.  This is necessary
              // because treepanel.dropZone.setPadding is never available in time to be useful.
              padding: [0,0,274,0]
            }
          },
          {
            xtype: 'treepanel',
            id: 'all_roles_privs_tree', //note: unique ID is assinged before instantiation
            title: 'Available Roles / Privileges',
            border: true, //note: this seem to have no effect w/in form panel
            bodyBorder: true, //note: this seem to have no effect w/in form panel
            //note: this style matches the expected behavior
            bodyStyle: 'background-color:#FFFFFF; border: 1px solid #B5B8C8',
            width: 225,
            height: Ext.isGecko ? 315 : 300,
            animate:true,
            lines: false,
            autoScroll:true,
            containerScroll: true,
            //@note: root node must be instantiated uniquely for each instance of treepanel
            //@ext: can TreeNode be registerd as a component with an xtype so this new root node
            //      may be instantiated uniquely for each form panel that uses this config?
            rootVisible: false,

            enableDD: true,
            ddScroll: true,
            dropConfig: {
              allowContainerDrop: true,
              onContainerDrop: function(source, e, data){
                this.tree.root.appendChild(data.node);
                return true;
              },
              onContainerOver:function(source, e, data){return this.dropAllowed;},
              // passign padding to make whole treePanel the drop zone.  This is dependent
              // on a sonatype fix in the Ext.dd.DropTarget class.  This is necessary
              // because treepanel.dropZone.setPadding is never available in time to be useful.
              padding: [0,0,274,0]
            }
          }
          ]
        }
      ],
    buttons: [
      {
        id: 'savebutton',
        text: 'Save'
      },
      {
        id: 'cancelbutton',
        text: 'Cancel'
      }
    ]
  };

  this.rolesGridPanel = new Ext.grid.GridPanel({
    title: 'Roles',
    id: 'st-roles-grid',
    
    region: 'north',
    layout:'fit',
    collapsible: true,
    split:true,
    height: 200,
    minHeight: 150,
    maxHeight: 400,
    frame: false,
    autoScroll: true,
    tbar: [
      {
        id: 'role-refresh-btn',
        text: 'Refresh',
        icon: Sonatype.config.resourcePath + '/images/icons/arrow_refresh.png',
        cls: 'x-btn-text-icon',
        scope: this,
        handler: this.reloadAll
      },
      {
        id: 'role-add-btn',
        text:'Add',
        icon: Sonatype.config.resourcePath + '/images/icons/add.png',
        cls: 'x-btn-text-icon',
        scope: this,
        handler: this.addResourceHandler
      },
      {
        id: 'role-delete-btn',
        text: 'Delete',
        icon: Sonatype.config.resourcePath + '/images/icons/delete.png',
        cls: 'x-btn-text-icon',
        scope:this,
        handler: this.deleteHandler
      }
    ],

    //grid view options
    ds: this.rolesDataStore,
    sortInfo:{field: 'id', direction: "ASC"},
    loadMask: true,
    deferredRender: false,
    columns: [
      {header: 'Name', dataIndex: 'name', width:175, id: 'role-config-name-col'},
      {header: 'Session Timeout', dataIndex: 'sessionTimeout', width:175, id: 'role-config-session-timeout-col'},
      {header: 'Description', dataIndex: 'description', width:175, id: 'role-config-description-col'}      
    ],
    autoExpandColumn: 'role-config-description-col',
    disableSelection: false,
    viewConfig: {
      emptyText: 'Click "Add" to create a new Role.'
    }
  });
  this.rolesGridPanel.on('rowclick', this.rowClick, this);
  this.rolesGridPanel.on('rowcontextmenu', this.contextClick, this);

  Sonatype.repoServer.RoleEditPanel.superclass.constructor.call(this, {
    layout: 'border',
    autoScroll: false,
    width: '100%',
    height: '100%',
    items: [
      this.rolesGridPanel,
      {
        xtype: 'panel',
        id: 'role-config-forms',
        title: 'Role Configuration',
        layout: 'card',
        region: 'center',
        activeItem: 0,
        deferredRender: false,
        autoScroll: false,
        frame: false,
        items: [
          {
            xtype: 'panel',
            layout: 'fit',
            html: '<div class="little-padding">Select a role to edit it, or click "Add" to create a new one.</div>'
          }
        ]
      }
    ]
  });

  this.formCards = this.findById('role-config-forms');
};


Ext.extend(Sonatype.repoServer.RoleEditPanel, Ext.Panel, {
  //Dump the currently stored data and requery for everything
  reloadAll : function(){
    this.rolesDataStore.removeAll();
    this.rolesDataStore.reload();
    this.formCards.items.each(function(item, i, len){
      if(i>0){this.remove(item, true);}
    }, this.formCards);
    
    this.formCards.getLayout().setActiveItem(0);
  },
  
  saveHandler : function(formInfoObj){
    if (formInfoObj.formPanel.form.isValid()) {
      var isNew = formInfoObj.isNew;
      var createUri = Sonatype.config.repos.urls.roles;
      var updateUri = (formInfoObj.resourceUri) ? formInfoObj.resourceUri : '';
      var form = formInfoObj.formPanel.form;
    
      form.doAction('sonatypeSubmit', {
        method: (isNew) ? 'POST' : 'PUT',
        url: isNew ? createUri : updateUri,
        waitMsg: isNew ? 'Creating Role...' : 'Updating Role...',
        fpanel: formInfoObj.formPanel,
        dataModifiers: this.submitDataModFunc,
        serviceDataObj : Sonatype.repoServer.referenceData.roles,
        isNew : isNew //extra option to send to callback, instead of conditioning on method
      });
    }
  },
  
  cancelHandler : function(formInfoObj) {
    var formLayout = this.formCards.getLayout();
    var gridSelectModel = this.rolesGridPanel.getSelectionModel();
    var store = this.rolesGridPanel.getStore();
    
    this.formCards.remove(formInfoObj.formPanel.id, true);
    //select previously selected form, or the default view (index == 0)
    var newIndex = this.formCards.items.length - 1;
    newIndex = (newIndex >= 0) ? newIndex : 0;
    formLayout.setActiveItem(newIndex);

    //delete row from grid if canceling a new repo form
    if(formInfoObj.isNew){
      store.remove( store.getById(formInfoObj.formPanel.id) );
    }
    
    //select the coordinating row in the grid, or none if back to default
    var i = store.indexOfId(formLayout.activeItem.id);
    if (i >= 0){
      gridSelectModel.selectRow(i);
      this.rowClick(this.rolesGridPanel, i, null);
    }
    else{
      gridSelectModel.clearSelections();
    }
  },
  
  addResourceHandler : function() {
    var id = 'new_role_' + new Date().getTime();

    var config = Ext.apply({}, this.formConfig, {id:id});
    
    config = this.initializeTreeRoots(id, config);
    
    var formPanel = new Ext.FormPanel(config);
    
    formPanel.form.on('actioncomplete', this.actionCompleteHandler, this);
    formPanel.form.on('actionfailed', this.actionFailedHandler, this);
    formPanel.on('beforerender', this.beforeFormRenderHandler, this);
    formPanel.on('afterlayout', this.afterLayoutFormHandler, this, {single:true});
        
    var buttonInfoObj = {
        formPanel : formPanel,
        isNew : true
      };
    
    //save button event handler
    formPanel.buttons[0].on('click', this.saveHandler.createDelegate(this, [buttonInfoObj]));
    //cancel button event handler
    formPanel.buttons[1].on('click', this.cancelHandler.createDelegate(this, [buttonInfoObj]));
    
    this.loadRolesTreeHelper([], {}, formPanel);
    this.loadPrivilegesTreeHelper([], {}, formPanel);
    
    //add place holder to grid
    var newRec = new this.roleRecordConstructor({
        id : 'new_role_',
        name : 'New Role',
        resourceURI : 'new'
      },
      id); //use "new_role_" id instead of resourceURI like the reader does
    this.rolesDataStore.insert(0, [newRec]);
    this.rolesGridPanel.getSelectionModel().selectRow(0);
    
    //add new form
    this.formCards.add(formPanel);
    
    //always set active and re-layout
    this.formCards.getLayout().setActiveItem(formPanel);
    formPanel.doLayout();
  },
  
  afterLayoutFormHandler : function(formPanel, fLayout){
    // register required field quicktip, but have to wait for elements to show up in DOM
    var temp = function(){
      var els = Ext.select('.required-field .x-form-item-label, .required-field .x-panel-header-text', this.getEl());
      els.each(function(el, els, i){
        Ext.QuickTips.register({
          target: el,
          cls: 'required-field',
          title: '',
          text: 'Required Field',
          enabled: true
        });
      });
    }.defer(300, formPanel);
  },
    
  deleteHandler : function(){
    if (this.ctxRecord || this.rolesGridPanel.getSelectionModel().hasSelection()){
      var rec = this.ctxRecord ? this.ctxRecord : this.rolesGridPanel.getSelectionModel().getSelected();

      if(rec.data.resourceURI == 'new'){
        this.cancelHandler({
          formPanel : Ext.getCmp(rec.id),
          isNew : true
        });
      }
      else {
        //@note: this handler selects the "No" button as the default
        //@todo: could extend Sonatype.MessageBox to take the button to select as a param
        Sonatype.MessageBox.getDialog().on('show', function(){
          this.focusEl = this.buttons[2]; //ack! we're offset dependent here
          this.focus();
        },
        Sonatype.MessageBox.getDialog(),
        {single:true});
        
        Sonatype.MessageBox.show({
          animEl: this.rolesGridPanel.getEl(),
          title : 'Delete Role?',
          msg : 'Delete the ' + rec.get('id') + ' Role?',
          buttons: Sonatype.MessageBox.YESNO,
          scope: this,
          icon: Sonatype.MessageBox.QUESTION,
          fn: function(btnName){
            if (btnName == 'yes' || btnName == 'ok') {
              Ext.Ajax.request({
                callback: this.deleteCallback,
                cbPassThru: {
                  resourceId: rec.id
                },
                scope: this,
                method: 'DELETE',
                url:rec.data.resourceURI
              });
            }
          }
        });
      }
    }
  },
  
  deleteCallback : function(options, isSuccess, response){
    if(isSuccess){
      var resourceId = options.cbPassThru.resourceId;
      var formLayout = this.formCards.getLayout();
      var gridSelectModel = this.rolesGridPanel.getSelectionModel();
      var store = this.rolesGridPanel.getStore();

      if(formLayout.activeItem.id == resourceId){
        this.formCards.remove(resourceId, true);
        //select previously selected form, or the default view (index == 0)
        var newIndex = this.formCards.items.length - 1;
        newIndex = (newIndex >= 0) ? newIndex : 0;
        formLayout.setActiveItem(newIndex);
      }
      else {
        this.formCards.remove(resourceId, true);
      }

      store.remove( store.getById(resourceId) );

      //select the coordinating row in the grid, or none if back to default
      var i = store.indexOfId(formLayout.activeItem.id);
      if (i >= 0){
        gridSelectModel.selectRow(i);
      }
      else{
        gridSelectModel.clearSelections();
      }
    }
    else {
      Sonatype.MessageBox.alert('The server did not delete the role.');
    }
  },
      
  //(Ext.form.BasicForm, Ext.form.Action)
  actionCompleteHandler : function(form, action) {
    //@todo: handle server error response here!!

    if (action.type == 'sonatypeSubmit'){
      var isNew = action.options.isNew;
      var receivedData = action.handleResponse(action.response).data;
      if (isNew) {
        //successful create
        var sentData = action.output.data;
        
        var dataObj = {
          id : receivedData.id,
          name : receivedData.name,
          resourceURI : receivedData.resourceURI,
          description : receivedData.description,
          privileges : receivedData.privileges,
          roles : receivedData.roles
        };
        
        var newRec = new this.roleRecordConstructor(
          dataObj,
          action.options.fpanel.id);
        
        this.rolesDataStore.remove(this.rolesDataStore.getById(action.options.fpanel.id)); //remove old one
        this.rolesDataStore.addSorted(newRec);
        this.rolesDataStore.getSelectionModel().selectRecords([newRec], false);

        //set the hidden id field in the form for subsequent updates
        action.options.fpanel.find('name', 'id')[0].setValue(receivedData.resourceURI);
        //remove button click listeners
        action.options.fpanel.buttons[0].purgeListeners();
        action.options.fpanel.buttons[1].purgeListeners();

        var buttonInfoObj = {
            formPanel : action.options.fpanel,
            isNew : false,
            resourceUri : dataObj.resourceURI
          };

        //save button event handler
        action.options.fpanel.buttons[0].on('click', this.saveHandler.createDelegate(this, [buttonInfoObj]));
        
        //cancel button event handler
        action.options.fpanel.buttons[1].on('click', this.cancelHandler.createDelegate(this, [buttonInfoObj]));
      }
      else {
        var sentData = action.output.data;

        var i = this.rolesDataStore.indexOfId(action.options.fpanel.id);
        var rec = this.rolesDataStore.getAt(i);

        this.updateRoleRecord(rec, receivedData);
        
        var sortState = this.rolesDataStore.getSortState();
        this.rolesDataStore.sort(sortState.field, sortState.direction);
      }
    }
  },
  
  updateRoleRecord : function(rec, receivedData){
        rec.beginEdit();
        rec.set('id', receivedData.id);
        rec.set('name', receivedData.name);
        rec.set('resourceURI', receivedData.resourceURI);
        rec.set('description', receivedData.description);
        rec.set('privileges', receivedData.privileges);
        rec.set('roles', receivedData.roles);
        rec.commit();
        rec.endEdit();
  },

  //(Ext.form.BasicForm, Ext.form.Action)
  actionFailedHandler : function(form, action){
    if(action.failureType == Ext.form.Action.CLIENT_INVALID){
      Sonatype.MessageBox.alert('Missing or Invalid Fields', 'Please change the missing or invalid fields.').setIcon(Sonatype.MessageBox.WARNING);
    }
//@note: server validation error are now handled just like client validation errors by marking the field invalid
//  else if(action.failureType == Ext.form.Action.SERVER_INVALID){
//    Sonatype.MessageBox.alert('Invalid Fields', 'The server identified invalid fields.').setIcon(Sonatype.MessageBox.ERROR);
//  }
    else if(action.failureType == Ext.form.Action.CONNECT_FAILURE){
      Sonatype.utils.connectionError( action.response, 'There is an error communicating with the server.' )
    }
    else if(action.failureType == Ext.form.Action.LOAD_FAILURE){
      Sonatype.MessageBox.alert('Load Failure', 'The data failed to load from the server.').setIcon(Sonatype.MessageBox.ERROR);
    }

    //@todo: need global alert mechanism for fatal errors.
  },
  
  beforeFormRenderHandler : function(component){
    var sp = Sonatype.lib.Permissions;
    if(sp.checkPermission(Sonatype.user.curr.repoServer.configRoles, sp.EDIT)){
      component.buttons[0].disabled = false;
    }
  },

  formDataLoader : function(formPanel, resourceUri, modFuncs){
    formPanel.getForm().doAction('sonatypeLoad', {url:resourceUri, method:'GET', fpanel:formPanel, dataModifiers: modFuncs, scope: this});
  },

  rowClick : function(grid, rowIndex, e){
    var rec = grid.store.getAt(rowIndex);
    var id = rec.id; //note: rec.id is unique for new resources and equal to resourceURI for existing ones
    var formPanel = this.formCards.findById(id);
    
    //assumption: new route forms already exist in formCards, so they won't get into this case
    if(!formPanel){ //create form and populate current data
      var config = Ext.apply({}, this.formConfig, {id:id});
      
      config = this.initializeTreeRoots(id, config);
      
      formPanel = new Ext.FormPanel(config);
      formPanel.form.on('actioncomplete', this.actionCompleteHandler, this);
      formPanel.form.on('actionfailed', this.actionFailedHandler, this);
      formPanel.on('beforerender', this.beforeFormRenderHandler, this);
      formPanel.on('afterlayout', this.afterLayoutFormHandler, this, {single:true});
      
      var buttonInfoObj = {
        formPanel : formPanel,
        isNew : false, //not a new route form, see assumption
        resourceUri : rec.data.resourceURI
      };
      
      formPanel.buttons[0].on('click', this.saveHandler.createDelegate(this, [buttonInfoObj]));
      formPanel.buttons[1].on('click', this.cancelHandler.createDelegate(this, [buttonInfoObj]));
  
      this.formDataLoader(formPanel, rec.data.resourceURI, this.loadDataModFunc);
      
      this.formCards.add(formPanel);
      
      //always set active
      this.formCards.getLayout().setActiveItem(formPanel);
      
      formPanel.doLayout();
    }
    else{
      //always set active
      this.formCards.getLayout().setActiveItem(formPanel);
    }
  },
  
  contextClick : function(grid, index, e){
    this.contextHide();
    
    if ( e.target.nodeName == 'A' ) return; // no menu on links
    
    this.ctxRow = this.rolesGridPanel.view.getRow(index);
    this.ctxRecord = this.rolesGridPanel.store.getAt(index);
    Ext.fly(this.ctxRow).addClass('x-node-ctx');

    //@todo: would be faster to pre-render the six variations of the menu for whole instance
    var menu = new Ext.menu.Menu({
      id:'roles-grid-ctx',
      items: [
        this.actions.refresh,
        this.actions.deleteAction
      ]
    });
    
    //TODO: Add additional menu items
    
    menu.on('hide', this.contextHide, this);
    e.stopEvent();
    menu.showAt(e.getXY());
  },
  
  contextHide : function(){
    if(this.ctxRow){
      Ext.fly(this.ctxRow).removeClass('x-node-ctx');
      this.ctxRow = null;
      this.ctxRecord = null;
    }
  },
  
  initializeTreeRoots : function(id, config){
    //@note: there has to be a better way to do this.  Depending on offsets is very error prone
    var newConfig = config;

    newConfig.items[4].items[0].root = new Ext.tree.TreeNode({text: 'root'});
    newConfig.items[4].items[1].root = new Ext.tree.TreeNode({text: 'root'});

    return newConfig;
  },
    
  loadRolesTreeHelper : function(arr, srcObj, fpanel){
    var selectedTree = fpanel.find('id', 'roles_privs_tree')[0];
    var allTree = fpanel.find('id', 'all_roles_privs_tree')[0];

    var role;

    for(var i=0; i<arr.length; i++){
      role = arr[i];
      selectedTree.root.appendChild(
        new Ext.tree.TreeNode({
          id: role.id,
          text: role.name,
          payload: role, //sonatype added attribute
          allowChildren: false,
          draggable: true,
          leaf: true,
          nodeType: 'role'
        })
      );
    }
    
    this.rolesDataStore.each(function(item, i, len){
      if(typeof(selectedTree.getNodeById(item.data.id)) == 'undefined'){
        allTree.root.appendChild(
          new Ext.tree.TreeNode({
            id: item.data.id,
            text: item.data.name,
            payload: item.data, //sonatype added attribute
            allowChildren: false,
            draggable: true,
            leaf: true,
            nodeType: 'role'
          })
        );
      }
    },this);
    
    return arr; //return arr, even if empty to comply with sonatypeLoad data modifier requirement
  },
  
  loadPrivilegesTreeHelper : function(arr, srcObj, fpanel){
    var selectedTree = fpanel.find('id', 'roles_privs_tree')[0];
    var allTree = fpanel.find('id', 'all_roles_privs_tree')[0];

    var priv;

    for(var i=0; i<arr.length; i++){
      priv = arr[i];
      selectedTree.root.appendChild(
        new Ext.tree.TreeNode({
          id: priv.id,
          text: priv.name,
          payload: priv, //sonatype added attribute
          allowChildren: false,
          draggable: true,
          leaf: true,
          nodeType: 'priv'
        })
      );
    }
    
    for(var i=0; i<this.privilegeList.length; i++){
      priv = this.privilegeList[i];
      if(typeof(selectedTree.getNodeById(priv.id)) == 'undefined'){
        allTree.root.appendChild(
          new Ext.tree.TreeNode({
            id: priv.id,
            text: priv.name,
            payload: priv, //sonatype added attribute
            allowChildren: false,
            draggable: true,
            leaf: true,
            nodeType: 'priv'
          })
        );
      }
    }
    
    return arr; //return arr, even if empty to comply with sonatypeLoad data modifier requirement
  },
  
  saveRolesTreeHelper : function(val, fpanel){
    var tree = fpanel.find('id', 'roles_privs_tree')[0];

    var outputArr = [];
    var nodes = tree.root.childNodes;

    for(var i = 0; i < nodes.length; i++){
      if (nodes[i].nodeType == 'role'){
        outputArr[i] = nodes[i].attributes.payload;
        Ext.apply(outputArr[i], {'@class':'org.sonatype.nexus.rest.model.RoleContainedRoleResource'});
      }
    }

    return outputArr;
  },
  
  savePrivilegesTreeHelper : function(val, fpanel){
    var tree = fpanel.find('id', 'roles_privs_tree')[0];

    var outputArr = [];
    var nodes = tree.root.childNodes;

    for(var i = 0; i < nodes.length; i++){
      if (nodes[i].nodeType == 'priv'){
        outputArr[i] = nodes[i].attributes.payload;
        Ext.apply(outputArr[i], {'@class':'org.sonatype.nexus.rest.model.RoleContainedPrivilegeResource'});
      }
    }

    return outputArr;
  }
});
