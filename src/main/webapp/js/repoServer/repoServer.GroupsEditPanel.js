/*
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
/*
 * Repository Groups Edit/Create panel layout and controller
 */

Sonatype.repoServer.RepositoryGroupEditor = function( config ) {
  var config = config || {};
  var defaultConfig = {
    dataModifiers: {
      load: {
        repositories: this.loadRepositories.createDelegate( this )
      },
      submit: { 
        repositories: this.saveRepositories.createDelegate( this )
      }
    },
    referenceData: Sonatype.repoServer.referenceData.group,
    uri: Sonatype.config.repos.urls.groups
  };
  Ext.apply( this, config, defaultConfig );

  var ht = Sonatype.repoServer.resources.help.groups;

  this.providerStore = new Ext.data.JsonStore( {
    root: 'data',
    id: 'provider',
    fields: [
      { name: 'description', sortType:Ext.data.SortTypes.asUCString },
      { name: 'format' },
      { name: 'provider' }
    ],
    sortInfo: { field: 'description', direction: 'asc' },
    url: Sonatype.config.repos.urls.groupRepoTypes
  } );

  this.repoStore = new Ext.data.JsonStore( {
    root: 'data',
    id: 'id',
    fields: [
      { name: 'id' },
      { name: 'resourceURI' },
      { name: 'format' },
      { name: 'name', sortType: Ext.data.SortTypes.asUCString }
    ],
    sortInfo: { field: 'name', direction: 'asc' },
    url: Sonatype.config.repos.urls.repositories
  } );
  
  this.checkPayload();

  Sonatype.repoServer.RepositoryGroupEditor.superclass.constructor.call( this, {
    dataStores: [this.providerStore, this.repoStore],
    items: [
      {
        xtype: 'textfield',
        fieldLabel: 'Group ID',
        itemCls: 'required-field',
        helpText: ht.id,
        name: 'id',
        width: 200,
        allowBlank:false,
        disabled: ! this.isNew,
        validator: Sonatype.utils.validateId
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Group Name',
        itemCls: 'required-field',
        helpText: ht.name,
        name: 'name',
        width: 200,
        allowBlank:false
      },
      {
        xtype: 'combo',
        fieldLabel: 'Provider',
        itemCls: 'required-field',
        helpText: ht.provider,
        name: 'provider',
        width: this.COMBO_WIDTH,
        store: this.providerStore,
        displayField:'description',
        valueField: 'provider',
        editable: false,
        forceSelection: true,
        mode: 'local',
        triggerAction: 'all',
        emptyText:'Select...',
        selectOnFocus:true,
        allowBlank: false,
        disabled: ! this.isNew,
        listeners: {
          select: this.providerSelectHandler,
          scope: this
        }
      },
      {
        xtype: 'textfield',
        fieldLabel: 'Format',
        itemCls: 'required-field',
        helpText: ht.format,
        name: 'format',
        width: 100,
        disabled: true,
        allowBlank: false
      },
      {
        xtype: 'twinpanelchooser',
        titleLeft: 'Ordered Group Repositories',
        titleRight: 'Available Repositories',
        name: 'repositories',
        valueField: 'id',
        store: this.repoStore,
        required: true
      }
    ],
    listeners: {
      submit: this.submitHandler,
      scope: this
    }
  } );
};

Ext.extend( Sonatype.repoServer.RepositoryGroupEditor, Sonatype.ext.FormPanel, {
	
	isValid: function(){
		if ( ! this.form.isValid() ){
			return false;
		}
		var repoBox = this.find( 'name', 'repositories' )[0];
		if ( ! repoBox.validate() ){
			return false;
		}
		return true;
	},
	
  loadRepositories: function( arr, srcObject, fpanel ) {
    var repoBox = fpanel.find( 'name', 'repositories' )[0];
    this.repoStore.filterBy( function( rec, id ) {
      return rec.data.format == srcObject.format;
    } );
    
    repoBox.setValue( arr );
  },

  providerSelectHandler: function( combo, rec, index ) {
    combo.ownerCt.find( 'name', 'format' )[0].setValue( rec.data.format );
    this.loadRepositories( [], rec.data, this );
  },

  saveRepositories: function( value, fpanel ) {
    var repoBox = fpanel.find( 'name', 'repositories' )[0];
    var repoIds = repoBox.getValue();

    var response = [];
    for ( var i = 0; i < repoIds.length; i++ ) {
      var rec = this.repoStore.getById( repoIds[i] );
      if ( rec ) {
        response.push( {
          id: rec.data.id,
          name: rec.data.name,
          resourceURI: rec.data.resourceURI
        } );
      }
    }

    return response;
  },

  submitHandler: function( form, action, receivedData ) {
    if ( this.isNew ) {
      if ( ! receivedData.resourceURI ) {
        // DP: there's no response when submitting a new group, so we have to pretend we have all the data
        receivedData.id = action.output.data.id;
        receivedData.name = action.output.data.name;
        receivedData.format = action.output.data.format;
        receivedData.displayStatus = Sonatype.utils.joinArrayObject( action.output.data.repositories, 'name' );
        receivedData.repoType = 'group';
        receivedData.exposed = true;
        receivedData.userManaged = true;
        receivedData.resourceURI =
          Sonatype.config.host + Sonatype.config.repos.urls.groups + '/' + action.output.data.id;
        receivedData.displayURI = Sonatype.config.repos.restToContentUrl( receivedData.resourceURI );
      }
      return;
    }
    
    var rec = this.payload;
    rec.beginEdit();
    rec.set( 'name', action.output.data.name );
    rec.set( 'format', action.output.data.format );
    rec.set( 'displayStatus', Sonatype.utils.joinArrayObject( action.output.data.repositories, 'name' ) ); 
    rec.commit();
    rec.endEdit();
  },

  //@override
  addSorted : function(store, rec) {
	var insertIndex;
	for (var i=0 ; i < store.getCount() ; i++) {
	  var tempRec = store.getAt(i);
	  if (tempRec.get('repoType') != 'group') {
	    insertIndex = i;
	    break;
	  }
	  if (tempRec.get('name').toLowerCase() > rec.get('name').toLowerCase() ) {
	  	insertIndex = i;
	  	break;
	  }
	}
	store.insert( insertIndex, [rec] );
  }

} );


Sonatype.Events.addListener( 'repositoryViewInit', function( cardPanel, rec ) {
  var sp = Sonatype.lib.Permissions;

  if ( rec.data.repoType == 'group' && sp.checkPermission( 'nexus:repogroups', sp.READ ) &&
      ( sp.checkPermission( 'nexus:repogroups', sp.CREATE ) ||
        sp.checkPermission( 'nexus:repogroups', sp.EDIT ) ) ) {
    cardPanel.add( new Sonatype.repoServer.RepositoryGroupEditor( {
      tabTitle: 'Configuration',
      payload: rec 
    } ) );
  }
} );

Sonatype.Events.addListener( 'repositoryAddMenuInit', function( menu ) {
  var sp = Sonatype.lib.Permissions;

  if ( sp.checkPermission( 'nexus:repogroups', sp.CREATE ) ) {
    menu.add( '-' );
    menu.add( {
      text: 'Repository Group',
      autoCreateNewRecord: true,
      handler: function( container, rec, item, e ) {
        rec.beginEdit();
        rec.set( 'repoType', 'group' );
        rec.commit();
        rec.endEdit();
      },
      scope: this
    } );
  }
} );
    