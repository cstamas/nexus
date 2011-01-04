/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.gwt.ui.client.repository;

import org.sonatype.gwt.client.handler.EntityResponseHandler;
import org.sonatype.gwt.client.handler.StatusResponseHandler;
import org.sonatype.gwt.client.resource.Representation;
import org.sonatype.nexus.gwt.ui.client.NexusUI;
import org.sonatype.nexus.gwt.ui.client.constants.RepositoryConstants;
import org.sonatype.nexus.gwt.ui.client.data.JSONArrayDataStore;
import org.sonatype.nexus.gwt.ui.client.table.ColumnModel;
import org.sonatype.nexus.gwt.ui.client.table.CompositeTable;
import org.sonatype.nexus.gwt.ui.client.table.DefaultColumnModel;
import org.sonatype.nexus.gwt.ui.client.table.JSONArrayTableModel;
import org.sonatype.nexus.gwt.ui.client.table.TableModel;
import org.sonatype.nexus.gwt.ui.client.table.ToolBar;
import org.sonatype.nexus.gwt.ui.client.table.ToolBarListener;
import org.sonatype.nexus.gwt.ui.client.widget.Header;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * 
 *
 * @author barath
 */
public class RepositoriesPage extends VerticalPanel {
    
    private static final RepositoryConstants i18n = (RepositoryConstants) GWT.create(RepositoryConstants.class);
    
    private final JSONArrayDataStore dataStore;

    private final CompositeTable repositoryTable;
    
    private class RepositoryResponseHandler implements EntityResponseHandler
    {
        public void onSuccess(Representation entity) {
            JSONValue json = (JSONValue) entity.getParsed();
            JSONArray repos = json.isObject().get("data").isArray();
            dataStore.setElements(repos);
        }

        public void onError(Request request, Throwable error) {
            Window.alert( "Error! Message: " + error.getMessage() );
        }
    }

    public RepositoriesPage(JSONArrayDataStore repositories) {
        this.dataStore = repositories;

        repositoryTable = createTable(dataStore,
            new String[]{"name", "repoType", "repoPolicy", "status.proxyMode", "status.localStatus", "status.remoteStatus"});
        
        repositoryTable.getToolBar().addToolBarListener(new ToolBarListener() {
			public void onRefresh(ToolBar sender) {
				sendRepositoryListRequest();
			}

			public void onAdd(ToolBar sender) {
                NexusUI.openCreateRepositoryPage(new JSONObject());
			}

			public void onEdit(ToolBar sender) {
			    String repoId = getSelectedRepoId();
                if (repoId != null) {
                    NexusUI.server.getLocalInstance().getRepositoriesService()
                            .getRepositoryById(repoId).read(new EntityResponseHandler() {
                        public void onSuccess(Representation entity) {
                            JSONValue json = (JSONValue) entity.getParsed();
                            JSONObject repo = json.isObject().get("data").isObject();
                            NexusUI.openEditRepositoryPage(repo);
                        }
                        public void onError(Request request, Throwable error) {
                            Window.alert("Error: " + error);
                        }
                    });
                }
			}

			public void onDelete(ToolBar sender) {
				String repoId = getSelectedRepoId();
				if (repoId != null) {
				    NexusUI.server.getLocalInstance().getRepositoriesService()
				            .getRepositoryById(repoId).delete(new StatusResponseHandler() {
				        public void onSuccess() {
				            Window.alert("Repository deleted");
				        }
				        public void onError(Request request, Throwable error) {
				            Window.alert("Delete repository failed");
				        }

				    });
				}
			}
        	
        });

        add(new Header("Repositories"));
        add(repositoryTable);
        repositoryTable.setWidth("100%");
        
        sendRepositoryListRequest();
    }
        
    public String getSelectedRepoId() {
        String repoId = null;
        JSONObject repo = (JSONObject) repositoryTable.getTable().getSelectedRow();
        if (repo != null) {
            String uri = repo.get("resourceURI").isString().stringValue();
            repoId = uri.substring(uri.lastIndexOf('/') + 1);
        }
        return repoId;
    }
    
    private void sendRepositoryListRequest() {
        NexusUI.server.getLocalInstance().getRepositoriesService()
                .getRepositories(new RepositoryResponseHandler());
    }
    
    private static CompositeTable createTable(JSONArrayDataStore ds, String[] cols) {
        TableModel model = new JSONArrayTableModel(ds, cols);
        ColumnModel columnModel = new DefaultColumnModel(cols, i18n);
        return new CompositeTable("repo-list", model, columnModel, 20);
    }

}
