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
 * /js/repoServer/repoServer.referenceData.js
 * 
 * Reference service data objects used for reading form data and compiling
 * resource state objects to send back to the service.
 */

(function(){

Sonatype.repoServer.referenceData = {
  globalSettingsState : {
    securityAnonymousUsername: "",
    securityAnonymousPassword: "",
    securityEnabled: false,
    securityAnonymousAccessEnabled: false,
    baseUrl: "",
    smtpSettings: {
      host: "",
      port: 25,
      username: "",
      password: "",
      systemEmailAddress: "",
      sslEnabled: "",
      tlsEnabled: ""
    },
    globalConnectionSettings: {
      connectionTimeout: 0,
      retrievalRetryCount: 0,
      queryString: "",
      userAgentString: ""
    },
    globalHttpProxySettings: {
      proxyHostname: "",
      proxyPort: 0,
      authentication: {
        username: "",
        password: "",
        ntlmHost: "",
        ntlmDomain: "",
        privateKey: "",
        passphrase: ""
      }
    }
  },
  
  repositoryState : {
    virtual : {
      repoType: "",
      id: "",
      name: "",
      shadowOf: "",
      format: "",
      syncAtStartup: false
      //realmId: ""
    },
  
    hosted : {
      repoType: "",
      id: "",
      name: "",
      allowWrite: true,
      browseable: true,
      indexable: true,
      notFoundCacheTTL: 0,
      repoPolicy: "",
      format: "",
      //realmId: "",
      overrideLocalStorageUrl: "",
      defaultLocalStorageUrl: "",
      downloadRemoteIndexes: true,
      checksumPolicy: ""
    },
  
    proxy : {
      repoType: "",
      id: "",
      name: "",
      browseable: true,
      indexable: true,
      notFoundCacheTTL: 0,
      artifactMaxAge: 0,
      metadataMaxAge: 0,
      repoPolicy: "",
      format: "",
      //realmId: "",
      overrideLocalStorageUrl: "",
      defaultLocalStorageUrl: "",
      downloadRemoteIndexes: true,
      checksumPolicy: "",
      remoteStorage:{
        remoteStorageUrl: "",
        authentication: {
          username: "",
          password: "",
          ntlmHost: "",
          ntlmDomain: "",
          privateKey: "",
          passphrase: ""
        },
        connectionSettings: {
          connectionTimeout: 0,
          retrievalRetryCount: 0,
          queryString: "",
          userAgentString: ""
        },
        httpProxySettings: {
          proxyHostname: "",
          proxyPort: 0,
          authentication: {
            username: "",
            password: "",
            ntlmHost: "",
            ntlmDomain: "",
            privateKey: "",
            passphrase: ""
          }
        }
      }
    } // end repositoryProxyState
  },
  
  group : {
    id : "",
    name: "",
    repositories:[]
      //note: internal record structure is the responsibility of data modifier func
      //{
      //  id:"central",
      //  name:"Maven Central",
      //  resourceURI:".../repositories/repoId"  // added URI to be able to reach repo
      //}
  },
  
  route : {
    id : "",
    ruleType : "",
    groupId : "",
    pattern : "",
    repositories : []
      //@todo: there's a discrepancy between routes list and state representation of
      //    the repo data inside routes data
  },
  
  schedule : {
    manual : {
      id : "",
      name : "",
      enabled : "",
      typeId : "",
      schedule : "",
      properties : [{
        id: "",
        value: ""
      }]
    },
    once : {
      id : "",
      name : "",
      enabled : "",
      typeId : "",
      schedule : "",
      properties : [{
        id: "",
        value: ""
      }],
      startDate : "",
      startTime : ""
    },
    daily : {
      id : "",
      name : "",
      enabled : "",
      typeId : "",
      schedule : "",
      properties : [{
        id: "",
        value: ""
      }],
      startDate : "",
      recurringTime : ""
    },
    weekly : {
      id : "",
      name : "",
      enabled : "",
      typeId : "",
      schedule : "",
      properties : [{
        id: "",
        value: ""
      }],
      startDate : "",
      recurringTime : "",
      recurringDay : []
    },
    monthly : {
      id : "",
      name : "",
      enabled : "",
      typeId : "",
      schedule : "",
      properties : [{
        id: "",
        value: ""
      }],
      startDate : "",
      recurringTime : "",
      recurringDay : []
    },
    advanced : {
      id : "",
      name : "",
      enabled : "",
      typeId : "",
      schedule : "",
      properties : [{
        id: "",
        value: ""
      }],
      cronCommand : ""
    }
  },
  
  upload : {
    r : "",
    g : "",
    a : "",
    v : "",
    p : "",
    c : "",
    e : ""
  },
  
  users : {
    userId : "",
    name : "",
    email : "",
    status : "",
    roles : []
  },
  
  roles : {
    id : "",
    name : "",
    description : "",
    sessionTimeout : 0,
    roles : [],
    privileges : []
  },
  
  privileges : {
    repositoryTarget : {
      name: "",
      description: "",
      type: "",
      repositoryTargetId: "",
      repositoryId: "",
      repositoryGroupId: "",
      method: []
    }
  },

  repoTargets : {
    id : "",
    name : "",
    contentClass : "",
    patterns : []
  },
  
  contentClasses : {
    contentClass : "",
    name : ""
  }
  
};

})();
