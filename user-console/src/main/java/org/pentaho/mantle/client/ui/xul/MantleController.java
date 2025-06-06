/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2029-07-20
 ******************************************************************************/


package org.pentaho.mantle.client.ui.xul;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Widget;
import org.pentaho.gwt.widgets.client.dialogs.MessageDialogBox;
import org.pentaho.gwt.widgets.client.menuitem.CheckBoxMenuItem;
import org.pentaho.gwt.widgets.client.menuitem.MenuCloner;
import org.pentaho.gwt.widgets.client.menuitem.PentahoMenuItem;
import org.pentaho.gwt.widgets.client.toolbar.ToolbarButton;
import org.pentaho.gwt.widgets.client.utils.ElementUtils;
import org.pentaho.gwt.widgets.client.utils.MenuBarUtils;
import org.pentaho.gwt.widgets.client.utils.i18n.ResourceBundle;
import org.pentaho.gwt.widgets.client.utils.string.StringTokenizer;
import org.pentaho.mantle.client.MantleApplication;
import org.pentaho.mantle.client.admin.ContentCleanerPanel;
import org.pentaho.mantle.client.admin.EmailAdminPanelController;
import org.pentaho.mantle.client.admin.ISysAdminPanel;
import org.pentaho.mantle.client.admin.JsSysAdminPanel;
import org.pentaho.mantle.client.admin.ServiceCallback;
import org.pentaho.mantle.client.admin.UserRolesAdminPanelController;
import org.pentaho.mantle.client.commands.ShowBrowserCommand;
import org.pentaho.mantle.client.commands.SwitchLocaleCommand;
import org.pentaho.mantle.client.commands.SwitchThemeCommand;
import org.pentaho.mantle.client.events.EventBusUtil;
import org.pentaho.mantle.client.events.FavoritesChangedEvent;
import org.pentaho.mantle.client.events.PerspectivesLoadedEvent;
import org.pentaho.mantle.client.events.PerspectivesLoadedEventHandler;
import org.pentaho.mantle.client.events.RecentsChangedEvent;
import org.pentaho.mantle.client.events.UserSettingsLoadedEvent;
import org.pentaho.mantle.client.events.UserSettingsLoadedEventHandler;
import org.pentaho.mantle.client.messages.Messages;
import org.pentaho.mantle.client.solutionbrowser.SolutionBrowserPanel;
import org.pentaho.mantle.client.solutionbrowser.filelist.FileCommand.COMMAND;
import org.pentaho.mantle.client.solutionbrowser.filepicklist.AbstractFilePickList;
import org.pentaho.mantle.client.solutionbrowser.filepicklist.FavoritePickItem;
import org.pentaho.mantle.client.solutionbrowser.filepicklist.FavoritePickList;
import org.pentaho.mantle.client.solutionbrowser.filepicklist.IFilePickItem;
import org.pentaho.mantle.client.solutionbrowser.filepicklist.IFilePickListListener;
import org.pentaho.mantle.client.solutionbrowser.filepicklist.RecentPickItem;
import org.pentaho.mantle.client.solutionbrowser.filepicklist.RecentPickList;
import org.pentaho.mantle.client.ui.BurgerMenuBar;
import org.pentaho.mantle.client.ui.BurgerMenuPopup;
import org.pentaho.mantle.client.ui.PerspectiveManager;
import org.pentaho.mantle.client.ui.UserDropDown;
import org.pentaho.mantle.client.usersettings.IMantleUserSettingsConstants;
import org.pentaho.mantle.client.usersettings.JsSetting;
import org.pentaho.mantle.client.usersettings.UserSettingsManager;
import org.pentaho.ui.xul.XulComponent;
import org.pentaho.ui.xul.XulException;
import org.pentaho.ui.xul.binding.BindingFactory;
import org.pentaho.ui.xul.components.XulMenuitem;
import org.pentaho.ui.xul.components.XulToolbarbutton;
import org.pentaho.ui.xul.containers.XulMenubar;
import org.pentaho.ui.xul.gwt.GwtXulDomContainer;
import org.pentaho.ui.xul.gwt.binding.GwtBindingFactory;
import org.pentaho.ui.xul.gwt.tags.GwtConfirmBox;
import org.pentaho.ui.xul.gwt.tags.GwtMessageBox;
import org.pentaho.ui.xul.impl.AbstractXulEventHandler;
import org.pentaho.ui.xul.stereotype.Bindable;
import org.pentaho.ui.xul.util.XulDialogCallback;

import java.util.HashMap;
import java.util.Map;

public class MantleController extends AbstractXulEventHandler {

  private static final String PUC_VALIDATION_ERROR_MESSAGE = "PUC_VALIDATION_ERROR_MESSAGE";
  private static final String CHANGE_PASS_ERROR_TITLE = "changePasswordErrorTitle";
  private static final String CHANGE_PASS_ERROR_MESSAGE = "changePasswordErrorMessage";
  private static final String HTTP_REQUEST_ACCEPT_HEADER = "accept";
  private static final String JSON_REQUEST_HEADER = "application/json";

  private static final String BURGER_MODE_MEDIA_QUERY = "(max-height: 500px), (max-width: 650px)";

  private MantleModel model;

  private XulMenubar mainMenubar;

  private XulToolbarbutton burgerBtn;

  private XulToolbarbutton openBtn;

  private XulToolbarbutton newBtn;

  private XulToolbarbutton saveBtn;

  private XulToolbarbutton saveAsBtn;

  private XulToolbarbutton newAdhocBtn;

  private XulToolbarbutton contentEditBtn;

  private XulToolbarbutton printBtn;

  private XulMenuitem saveMenuItem;

  private XulMenuitem saveAsMenuItem;

  private XulMenuitem useDescriptionsMenuItem;

  private XulMenuitem showHiddenFilesMenuItem;

  private XulMenubar languageMenu;

  private XulMenubar themesMenu;

  private XulMenubar toolsMenu;

  private XulMenubar recentMenu;

  private XulMenubar favoriteMenu;

  private BindingFactory bf;

  private String overrideContentPanelId;

  private String overrideContentUrl;

  private BurgerMenuPopup burgerMenuPopup;

  HashMap<String, ISysAdminPanel> sysAdminPanelsMap = new HashMap<String, ISysAdminPanel>();

  RecentPickList recentPickList = RecentPickList.getInstance();

  FavoritePickList favoritePickList = FavoritePickList.getInstance();

  class SysAdminPanelInfo {
    String id;

    String url;

    public SysAdminPanelInfo() {

    };

    public SysAdminPanelInfo( String panelId, String panelUrl ) {
      id = panelId;
      url = panelUrl;
    };
  }

  SysAdminPanelInfo adminPanelAwaitingActivation = null;

  public MantleController( MantleModel model ) {
    this.model = model;
  }

  /**
   * Called when the Xul Dom is ready, grab all Xul references here.
   */
  @Bindable
  public void init() {
    mainMenubar = (XulMenubar) document.getElementById( "mainMenubar" );

    burgerBtn = (XulToolbarbutton) document.getElementById( "burgerButton" );
    openBtn = (XulToolbarbutton) document.getElementById( "openButton" ); //$NON-NLS-1$
    newBtn = (XulToolbarbutton) document.getElementById( "newButton" ); //$NON-NLS-1$
    saveBtn = (XulToolbarbutton) document.getElementById( "saveButton" ); //$NON-NLS-1$
    saveAsBtn = (XulToolbarbutton) document.getElementById( "saveAsButton" ); //$NON-NLS-1$
    printBtn = (XulToolbarbutton) document.getElementById( "printButton" );

    contentEditBtn = (XulToolbarbutton) document.getElementById( "editContentButton" ); //$NON-NLS-1$

    bf = new GwtBindingFactory( document );
    bf.createBinding( model, "saveEnabled", saveBtn, "visible" ); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding( model, "saveAsEnabled", saveAsBtn, "visible" ); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding( model, "contentEditEnabled", contentEditBtn, "visible" ); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding( model, "contentEditSelected", this, "editContentSelected" ); //$NON-NLS-1$ //$NON-NLS-2$
    bf.createBinding( model, "printVisible", printBtn, "visible" );

    saveMenuItem = (XulMenuitem) document.getElementById( "saveMenuItem" ); //$NON-NLS-1$
    saveAsMenuItem = (XulMenuitem) document.getElementById( "saveAsMenuItem" ); //$NON-NLS-1$
    useDescriptionsMenuItem = (XulMenuitem) document.getElementById( "useDescriptionsMenuItem" ); //$NON-NLS-1$
    showHiddenFilesMenuItem = (XulMenuitem) document.getElementById( "showHiddenFilesMenuItem" ); //$NON-NLS-1$
    languageMenu = (XulMenubar) document.getElementById( "languagemenu" ); //$NON-NLS-1$
    themesMenu = (XulMenubar) document.getElementById( "themesmenu" ); //$NON-NLS-1$
    toolsMenu = (XulMenubar) document.getElementById( "toolsmenu" ); //$NON-NLS-1$
    recentMenu = (XulMenubar) document.getElementById( "recentmenu" ); //$NON-NLS-1$
    favoriteMenu = (XulMenubar) document.getElementById( "favoritesmenu" ); //$NON-NLS-1$

    initializeBurgerMenu();

    if ( PerspectiveManager.getInstance().isLoaded() ) {
      PerspectiveManager.getInstance().enablePerspective( PerspectiveManager.OPENED_PERSPECTIVE, false );
    } else {
      EventBusUtil.EVENT_BUS.addHandler( PerspectivesLoadedEvent.TYPE, new PerspectivesLoadedEventHandler() {
        public void onPerspectivesLoaded( PerspectivesLoadedEvent event ) {
          PerspectiveManager.getInstance().enablePerspective( PerspectiveManager.OPENED_PERSPECTIVE, false );
        }
      } );
    }

    // install language sub-menus
    ResourceBundle resourceBundle = Messages.getResourceBundle();
    Map<String, String> supportedLanguages = resourceBundle.getSupportedLanguages();
    if ( supportedLanguages != null && !supportedLanguages.isEmpty() ) {

      String currentLanguage = resourceBundle.getLanguage();

      MenuBar langMenu = (MenuBar) languageMenu.getManagedObject();
      langMenu.insertSeparator( 0 );
      for ( String lang : supportedLanguages.keySet() ) {
        CheckBoxMenuItem langMenuItem = new CheckBoxMenuItem( supportedLanguages.get( lang ), new SwitchLocaleCommand( lang ) );
        langMenuItem.getElement().setId( supportedLanguages.get( lang ) + "_menu_item" );
        langMenuItem.setChecked( lang.equalsIgnoreCase( currentLanguage ) );
        langMenu.insertItem( langMenuItem, 0 );
      }
    }

    buildFavoritesAndRecent( false );

    UserSettingsManager.getInstance().getUserSettings( new AsyncCallback<JsArray<JsSetting>>() {
      public void onSuccess( JsArray<JsSetting> settings ) {
        processSettings( settings );
      }

      public void onFailure( Throwable caught ) {
      }
    }, false );

    EventBusUtil.EVENT_BUS.addHandler( UserSettingsLoadedEvent.TYPE, new UserSettingsLoadedEventHandler() {
      public void onUserSettingsLoaded( UserSettingsLoadedEvent event ) {
        processSettings( event.getSettings() );
      }

    } );

    // install themes
    RequestBuilder getActiveThemeRequestBuilder =
        new RequestBuilder( RequestBuilder.GET, GWT.getHostPageBaseURL() + "api/theme/active" ); //$NON-NLS-1$
    try {
      getActiveThemeRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
      getActiveThemeRequestBuilder.sendRequest( null, new RequestCallback() {

        public void onError( Request request, Throwable exception ) {
          // showError(exception);
        }

        public void onResponseReceived( Request request, Response response ) {
          final String activeTheme = response.getText();
          RequestBuilder getThemesRequestBuilder =
              new RequestBuilder( RequestBuilder.GET, GWT.getHostPageBaseURL() + "api/theme/list" ); //$NON-NLS-1$
          getThemesRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
          getThemesRequestBuilder.setHeader( HTTP_REQUEST_ACCEPT_HEADER, JSON_REQUEST_HEADER ); //$NON-NLS-1$ //$NON-NLS-2$

          try {
            getThemesRequestBuilder.sendRequest( null, new RequestCallback() {
              public void onError( Request arg0, Throwable arg1 ) {
              }

              public void onResponseReceived( Request request, Response response ) {
                try {
                  final String url = GWT.getHostPageBaseURL() + "api/repo/files/canAdminister"; //$NON-NLS-1$
                  RequestBuilder requestBuilder = new RequestBuilder( RequestBuilder.GET, url );
                  requestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
                  requestBuilder.setHeader( HTTP_REQUEST_ACCEPT_HEADER, "text/plain" ); //$NON-NLS-1$ //$NON-NLS-2$
                  requestBuilder.sendRequest( null, new RequestCallback() {

                    public void onError( Request request, Throwable caught ) {
                    }

                    public void onResponseReceived( Request request, Response response ) {
                      toolsMenu.setVisible( "true".equalsIgnoreCase( response.getText() ) ); //$NON-NLS-1$
                      showHiddenFilesMenuItem.setVisible( "true".equalsIgnoreCase( response.getText() ) ); //$NON-NLS-1$
                    }

                  } );
                } catch ( RequestException e ) {
                  Window.alert( e.getMessage() );
                }

                JsArray<JsTheme> themes = JsTheme.getThemes( JsonUtils.escapeJsonForEval( response.getText() ) );

                for ( int i = 0; i < themes.length(); i++ ) {
                  JsTheme theme = themes.get( i );
                  PentahoMenuItem themeMenuItem =
                      new PentahoMenuItem( theme.getName(), new SwitchThemeCommand( theme.getId() ) );
                  themeMenuItem.getElement().setId( theme.getId() + "_menu_item" ); //$NON-NLS-1$
                  themeMenuItem.setUseCheckUI( true );
                  themeMenuItem.setChecked( theme.getId().equals( activeTheme ) );
                  ( (MenuBar) themesMenu.getManagedObject() ).addItem( themeMenuItem );
                }

                bf.createBinding( model, "saveEnabled", saveMenuItem, "!disabled" ); //$NON-NLS-1$ //$NON-NLS-2$
                bf.createBinding( model, "saveAsEnabled", saveAsMenuItem, "!disabled" ); //$NON-NLS-1$ //$NON-NLS-2$

                if ( PerspectiveManager.getInstance().isLoaded() ) {
                  executeAdminContent();
                } else {
                  EventBusUtil.EVENT_BUS.addHandler( PerspectivesLoadedEvent.TYPE,
                      new PerspectivesLoadedEventHandler() {
                        public void onPerspectivesLoaded( PerspectivesLoadedEvent event ) {
                          executeAdminContent();
                        }
                      } );
                }

                setupNativeHooks( MantleController.this );
              }
            } );

          } catch ( RequestException e ) {
            // showError(e);
          }
        }

      } );

    } catch ( RequestException e ) {
      Window.alert( e.getMessage() );
      // showError(e);
    }
  }

  public void processSettings( JsArray<JsSetting> settings ) {
    if ( settings == null ) {
      return;
    }

    for ( int i = 0; i < settings.length(); i++ ) {
      JsSetting setting = settings.get( i );
      try {
        if ( IMantleUserSettingsConstants.MANTLE_SHOW_NAVIGATOR.equals( setting.getName() ) ) {
          boolean showNavigator = "true".equals( setting.getValue() ); //$NON-NLS-1$
          model.setShowNavigatorSelected( showNavigator );
        } else if ( IMantleUserSettingsConstants.MANTLE_SHOW_DESCRIPTIONS_FOR_TOOLTIPS.equals( setting.getName() ) ) {
          boolean checked = "true".equals( setting.getValue() ); //$NON-NLS-1$
          ( (PentahoMenuItem) useDescriptionsMenuItem.getManagedObject() ).setChecked( checked );
        } else if ( IMantleUserSettingsConstants.MANTLE_SHOW_HIDDEN_FILES.equals( setting.getName() ) ) {
          boolean checked = "true".equals( setting.getValue() ); //$NON-NLS-1$
          ( (PentahoMenuItem) showHiddenFilesMenuItem.getManagedObject() ).setChecked( checked );
        }
      } catch ( Exception e ) {
        MessageDialogBox dialogBox =
            new MessageDialogBox(
                Messages.getString( "error" ), Messages.getString( "couldNotGetUserSettings" ), false, false, true ); //$NON-NLS-1$ //$NON-NLS-2$
        dialogBox.center();
      }
    }

  }

  /**
   * 
   * @param force
   *          Force the reload of user settings from server rather than use cache.
   * 
   */
  public void buildFavoritesAndRecent( boolean force ) {

    loadRecentAndFavorites( force );
    refreshPickListMenu( recentMenu, recentPickList, PickListType.RECENT );
    refreshPickListMenu( favoriteMenu, favoritePickList, PickListType.FAVORITE );

    recentPickList.addPickListListener( new IFilePickListListener<RecentPickItem>() {

      public void itemsChanged( AbstractFilePickList<RecentPickItem> filePickList ) {
        refreshPickListMenu( recentMenu, recentPickList, PickListType.RECENT );
        recentPickList.save( "recent" );
      }

      public void onSaveComplete( AbstractFilePickList<RecentPickItem> filePickList ) {
        EventBusUtil.EVENT_BUS.fireEvent( new RecentsChangedEvent() );
      }
    } );

    favoritePickList.addPickListListener( new IFilePickListListener<FavoritePickItem>() {

      public void itemsChanged( AbstractFilePickList<FavoritePickItem> filePickList ) {
        refreshPickListMenu( favoriteMenu, favoritePickList, PickListType.FAVORITE );
        favoritePickList.save( "favorites" );
      }

      public void onSaveComplete( AbstractFilePickList<FavoritePickItem> filePickList ) {
        EventBusUtil.EVENT_BUS.fireEvent( new FavoritesChangedEvent() );
      }
    } );
  }

  /**
   * Loads an arbitrary <code>FilePickList</code> into a menu
   * 
   * @param pickMenu
   *          The XulMenuBar to host the menu entries
   * @param filePickList
   *          The files to list in natural order
   */
  private void refreshPickListMenu( XulMenubar pickMenu,
      final AbstractFilePickList<? extends IFilePickItem> filePickList, PickListType type ) {
    final MenuBar menuBar = (MenuBar) pickMenu.getManagedObject();
    menuBar.clearItems();

    final String menuClearMessage = Messages.getString( type.getMenuItemKey() );
    final String clearMessage = Messages.getString( type.getMessageKey() );

    if ( filePickList.size() > 0 ) {
      for ( IFilePickItem filePickItem : filePickList.getFilePickList() ) {
        final String text = filePickItem.getFullPath();
        menuBar.addItem( filePickItem.getTitle(), new Command() {
          public void execute() {
            SolutionBrowserPanel.getInstance().openFile( text, COMMAND.RUN );
          }
        } );
      }
      menuBar.addSeparator();
      menuBar.addItem( menuClearMessage, new Command() {
        public void execute() {
          // confirm the clear
          GwtConfirmBox warning = new GwtConfirmBox();
          warning.setHeight( 117 );
          warning.setMessage( clearMessage );
          warning.setTitle( menuClearMessage );
          warning.setAcceptLabel( Messages.getString( "clearRecentAcceptButtonLabel" ) );
          warning.setCancelLabel( Messages.getString( "clearRecentCancelButtonLabel" ) );
          warning.addDialogCallback( new XulDialogCallback<String>() {
            public void onClose( XulComponent sender, Status returnCode, String retVal ) {
              if ( returnCode == Status.ACCEPT ) {
                filePickList.clear();
              }
            }

            public void onError( XulComponent sender, Throwable t ) {
            }
          } );
          warning.show();
        }
      } );
    } else {
      menuBar.addItem( Messages.getString( "empty" ), new Command() { //$NON-NLS-1$
          public void execute() {
            // Do nothing
          }
        } );
    }
  }

  private void loadRecentAndFavorites( boolean force ) {
    UserSettingsManager.getInstance().getUserSettings( new AsyncCallback<JsArray<JsSetting>>() {

      public void onSuccess( JsArray<JsSetting> result ) {
        if ( result == null ) {
          return;
        }
        JsSetting setting;
        for ( int j = 0; j < result.length(); j++ ) {
          setting = result.get( j );
          if ( "favorites".equalsIgnoreCase( setting.getName() ) ) { //$NON-NLS-1$
            try {
              // handle favorite
              JSONArray favorites = JSONParser.parseLenient( setting.getValue() ).isArray();
              if ( favorites != null ) {
                // Create the FavoritePickList object from the JSONArray
                favoritePickList = FavoritePickList.getInstanceFromJSON( favorites );
              } else {
                favoritePickList = FavoritePickList.getInstance();
              }
            } catch ( Throwable t ) {
              //ignore
            }
          } else if ( "recent".equalsIgnoreCase( setting.getName() ) ) { //$NON-NLS-1$
            try {
              // handle recent
              JSONArray recents = JSONParser.parseLenient( setting.getValue() ).isArray();
              if ( recents != null ) {
                // Create the RecentPickList object from the JSONArray
                recentPickList = RecentPickList.getInstanceFromJSON( recents );
              } else {
                recentPickList = RecentPickList.getInstance();
              }
              recentPickList.setMaxSize( 10 );
            } catch ( Throwable t ) {
              //ignore
            }
          }
        }
      }

      public void onFailure( Throwable caught ) {
      }

    }, force );
  }

  private void executeAdminContent() {

    try {
      RequestCallback internalCallback = new RequestCallback() {

        public void onError( Request request, Throwable exception ) {
        }

        public void onResponseReceived( Request request, Response response ) {
          JsArray<JsSetting> jsSettings = null;
          try {
            jsSettings = JsSetting.parseSettingsJson( response.getText() );
          } catch ( Throwable t ) {
            // happens when there are no settings
          }
          if ( jsSettings == null ) {
            return;
          }
          for ( int i = 0; i < jsSettings.length(); i++ ) {
            String content = jsSettings.get( i ).getValue();
            StringTokenizer nameValuePairs = new StringTokenizer( content, ";" ); //$NON-NLS-1$
            String perspective = null, content_panel_id = null, content_url = null;
            for ( int j = 0; j < nameValuePairs.countTokens(); j++ ) {
              String currentToken = nameValuePairs.tokenAt( j ).trim();
              if ( currentToken.startsWith( "perspective=" ) ) { //$NON-NLS-1$
                perspective = currentToken.substring( "perspective=".length() ); //$NON-NLS-1$
              }
              if ( currentToken.startsWith( "content-panel-id=" ) ) { //$NON-NLS-1$
                content_panel_id = currentToken.substring( "content-panel-id=".length() ); //$NON-NLS-1$
              }
              if ( currentToken.startsWith( "content-url=" ) ) { //$NON-NLS-1$
                content_url = currentToken.substring( "content-url=".length() ); //$NON-NLS-1$
              }
            }

            if ( content_panel_id != null && content_url != null ) {
              overrideContentPanelId = content_panel_id;
              overrideContentUrl = content_url;
            }

            if ( perspective != null ) {
              PerspectiveManager.getInstance().setPerspective( perspective );
            }

            if ( perspective == null && content_panel_id == null && content_url == null ) {
              GwtMessageBox warning = new GwtMessageBox();
              warning.setTitle( Messages.getString( "warning" ) ); //$NON-NLS-1$
              warning.setMessage( content );
              warning.setButtons( new Object[GwtMessageBox.ACCEPT] );
              warning.setAcceptLabel( Messages.getString( "close" ) ); //$NON-NLS-1$
              warning.show();
            }
          }
        }
      };

      RequestBuilder builder =
          new RequestBuilder( RequestBuilder.GET, GWT.getHostPageBaseURL() + "api/mantle/getAdminContent" ); //$NON-NLS-1$
      builder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
      builder.setHeader( HTTP_REQUEST_ACCEPT_HEADER, JSON_REQUEST_HEADER ); //$NON-NLS-1$ //$NON-NLS-2$
      builder.sendRequest( null, internalCallback );
      // TO DO Reset the menuItem click for browser and workspace here?
    } catch ( RequestException e ) {
      //ignore
    }
  }

  public native void setupNativeHooks( MantleController controller )
  /*-{
    $wnd.mantle_isToolbarButtonEnabled = function(id) {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      return controller.@org.pentaho.mantle.client.ui.xul.MantleController::isToolbarButtonEnabled(Ljava/lang/String;)(id);      
    }
    $wnd.mantle_setToolbarButtonEnabled = function(id, enabled) {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      controller.@org.pentaho.mantle.client.ui.xul.MantleController::setToolbarButtonEnabled(Ljava/lang/String;Z)(id, enabled);      
    }
    $wnd.mantle_doesToolbarButtonExist = function(id) {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      return controller.@org.pentaho.mantle.client.ui.xul.MantleController::doesToolbarButtonExist(Ljava/lang/String;)(id);      
    }
    $wnd.mantle_isMenuItemEnabled = function(id) {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      return controller.@org.pentaho.mantle.client.ui.xul.MantleController::isMenuItemEnabled(Ljava/lang/String;)(id);      
    }
    $wnd.mantle_setMenuBarEnabled = function(id, enabled) {
          //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
          controller.@org.pentaho.mantle.client.ui.xul.MantleController::setMenuBarEnabled(Ljava/lang/String;Z)(id, enabled);
      }
    $wnd.mantle_setMenuItemEnabled = function(id, enabled) {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      controller.@org.pentaho.mantle.client.ui.xul.MantleController::setMenuItemEnabled(Ljava/lang/String;Z)(id, enabled);      
    }
    $wnd.mantle_doesMenuItemExist = function(id) {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      return controller.@org.pentaho.mantle.client.ui.xul.MantleController::doesMenuItemExist(Ljava/lang/String;)(id);      
    }
    $wnd.mantle_loadOverlay = function(id) { 
      controller.@org.pentaho.mantle.client.ui.xul.MantleController::loadOverlay(Ljava/lang/String;)(id);      
    }
    $wnd.mantle_removeOverlay = function(id) { 
      controller.@org.pentaho.mantle.client.ui.xul.MantleController::removeOverlay(Ljava/lang/String;)(id);      
    }    
    $wnd.mantle_registerSysAdminPanel = function(sysAdminPanel) {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      controller.@org.pentaho.mantle.client.ui.xul.MantleController::registerSysAdminPanel(Lorg/pentaho/mantle/client/admin/JsSysAdminPanel;)(sysAdminPanel);      
    } 
    $wnd.mantle_activateWaitingSecurityPanel = function(okToSwitchToNewPanel) {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      controller.@org.pentaho.mantle.client.ui.xul.MantleController::activateWaitingSecurityPanel(Z)(okToSwitchToNewPanel);      
    } 
    $wnd.mantle_enableUsersRolesTreeItem = function(enabled) { 
      controller.@org.pentaho.mantle.client.ui.xul.MantleController::enableUsersRolesTreeItem(Z)(enabled);      
    } 
    $wnd.mantle_selectAdminCatTreeTreeItem = function(treeLabel) {
      //CHECKSTYLE IGNORE LineLength FOR NEXT 1 LINES
      controller.@org.pentaho.mantle.client.ui.xul.MantleController::selectAdminCatTreeTreeItem(Ljava/lang/String;)(treeLabel);      
    }
    $wnd.mantle_buildFavoritesAndRecent = function(force) {
      controller.@org.pentaho.mantle.client.ui.xul.MantleController::buildFavoritesAndRecent(Z)(force);
    }
    $wnd.mantle_openChangePasswordDialog = function() {
      controller.@org.pentaho.mantle.client.ui.xul.MantleController::openChangePasswordDialog()();
    }
  }-*/;

  public void enableUsersRolesTreeItem( boolean enabled ) {
    MantleXul.getInstance().enableUsersRolesTreeItem( enabled );
  }

  public void selectAdminCatTreeTreeItem( String treeLabel ) {
    MantleXul.getInstance().selectAdminCatTreeTreeItem( treeLabel );
  }

  public void registerSysAdminPanel( JsSysAdminPanel sysAdminPanel ) {
    sysAdminPanelsMap.put( sysAdminPanel.getId(), sysAdminPanel );
  }

  public void activateWaitingSecurityPanel( boolean activate ) {
    if ( activate && ( adminPanelAwaitingActivation != null ) ) {
      for ( int i = 0; i < MantleXul.getInstance().getAdminContentDeck().getWidgetCount(); i++ ) {
        Widget w = MantleXul.getInstance().getAdminContentDeck().getWidget( i );
        if ( adminPanelAwaitingActivation.id.equals( w.getElement().getId() ) ) {
          ISysAdminPanel sysAdminPanel = sysAdminPanelsMap.get( adminPanelAwaitingActivation.id );
          if ( sysAdminPanel != null ) {
            sysAdminPanel.activate();
          }
          break;
        }
      }

      GWT.runAsync( new RunAsyncCallback() {
        public void onSuccess() {
          if ( UserRolesAdminPanelController.getInstance().getId().equals( adminPanelAwaitingActivation.id ) ) {
            model.loadUserRolesAdminPanel();
            UserRolesAdminPanelController.getInstance().getElement().setId(
                ( UserRolesAdminPanelController.getInstance() ).getId() );
          } else if ( ( EmailAdminPanelController.getInstance() ).getId().equals( adminPanelAwaitingActivation.id ) ) {
            model.loadEmailAdminPanel();
            EmailAdminPanelController.getInstance().getElement().setId(
                ( EmailAdminPanelController.getInstance() ).getId() );
          } else if ( ( ContentCleanerPanel.getInstance() ).getId().equals( adminPanelAwaitingActivation.id ) ) {
            model.loadSettingsPanel();
            ContentCleanerPanel.getInstance().getElement().setId( ( ContentCleanerPanel.getInstance() ).getId() );
          } else {
            model.loadAdminContent( adminPanelAwaitingActivation.id, adminPanelAwaitingActivation.url );
          }
        }

        public void onFailure( Throwable reason ) {
        }
      } );

    } else if ( !activate ) {
      adminPanelAwaitingActivation = null;
    }
  }

  public boolean isToolbarButtonEnabled( String id ) {
    XulToolbarbutton button = (XulToolbarbutton) document.getElementById( id );
    return !button.isDisabled();
  }

  public void setToolbarButtonEnabled( String id, boolean enabled ) {
    XulToolbarbutton button = (XulToolbarbutton) document.getElementById( id );
    button.setDisabled( !enabled );
  }

  public boolean doesToolbarButtonExist( String id ) {
    try {
      XulToolbarbutton button = (XulToolbarbutton) document.getElementById( id );
      return ( button != null );
    } catch ( Throwable t ) {
      return false;
    }
  }

  @Bindable
  public void setEditContentSelected( boolean selected ) {
    contentEditBtn.setSelected( selected, false );
  }

  @Bindable
  public void openClicked() {
    model.executeOpenFileCommand();
  }
  @Bindable
  public void newClicked() {
    model.launchNewDropdownCommand( newBtn );
  }

  // region Burger Menu
  private Widget getBurgerButtonWidget() {
    return ( (ToolbarButton) burgerBtn.getManagedObject() ).getPushButton();
  }

  public MenuBar getMainMenubarWidget() {
    return (MenuBar) mainMenubar.getManagedObject();
  }

  private Element getPUCWrapperElement() {
    return Document.get().getElementById( "pucWrapper" );
  }

  private void initializeBurgerMenu() {
    // Listen to Media Query that controls Burger Mode and set the mantle model's initial value.
    boolean matchesMediaQuery = setupNativeBurgerModeMediaQueryListener( this, BURGER_MODE_MEDIA_QUERY );
    model.setBurgerMode( matchesMediaQuery );

    // Update DOM to match initial model values.
    onBurgerModeChanged();

    // Start listening for model changes.
    model.addPropertyChangeListener( "burgerMode", evt -> onBurgerModeChanged() );

    getBurgerButtonWidget().getElement().setAttribute( "aria-haspopup", "menu" );
  }

  private native boolean setupNativeBurgerModeMediaQueryListener( MantleController controller, String mediaQuery ) /*-{
    var x = $wnd.matchMedia(mediaQuery);

    x.addEventListener("change", function(x) {
      controller.@org.pentaho.mantle.client.ui.xul.MantleController::onBurgerModeMediaQueryChange(Z)(x.matches);
    });

    return x.matches;
  }-*/;

  private void onBurgerModeMediaQueryChange( boolean matches ) {
    model.setBurgerMode( matches );
  }

  private void onBurgerModeChanged() {
    boolean isBurgerMode = model.isBurgerMode();

    MenuBar mainMenubarWidget = getMainMenubarWidget();

    boolean hadFocus;

    // Close menu popups, if open.
    // Detect if focus should be set to the other "menu" after the (CSS) mode transition.
    if ( isBurgerMode ) {
      // Entering burger mode.
      hadFocus = MenuBarUtils.getPopup( mainMenubarWidget ) != null
        || ElementUtils.isActiveElement( mainMenubarWidget.getElement() );

      // Close any open menus.
      mainMenubarWidget.closeAllChildren( false );
      PerspectiveManager.getInstance().hidePopup();

      // May be null during initialization.
      UserDropDown userDropDown = MantleApplication.getUserDropDown();
      if ( userDropDown != null ) {
        userDropDown.hidePopup();
      }
    } else {
      // Exiting burger mode.
      hadFocus = closeBurgerMenuPopup();
    }

    Element pucWrapperElem = getPUCWrapperElement();
    if ( pucWrapperElem != null ) {
      if ( isBurgerMode ) {
        pucWrapperElem.addClassName( "burger-mode" );
      } else {
        pucWrapperElem.removeClassName( "burger-mode" );
      }
    }

    if ( hadFocus ) {
      if ( isBurgerMode ) {
        ( (Focusable) getBurgerButtonWidget() ).setFocus( true );
      } else {
        mainMenubarWidget.focus();
      }
    }
  }

  private boolean closeBurgerMenuPopup() {
    if ( burgerMenuPopup != null ) {
      burgerMenuPopup.hide();
      burgerMenuPopup = null;
      return true;
    }

    return false;
  }

  private void toggleBurgerMenuExpanded() {
    Widget burgerButton = getBurgerButtonWidget();
    if ( !burgerButton.getElement().hasAttribute( "aria-expanded" ) ) {
      closeBurgerMenuPopup();

      burgerMenuPopup = new BurgerMenuPopup( burgerButton, createBurgerMenuBar( getMainMenubarWidget() ) );
      burgerMenuPopup.showMenu();
    }
  }

  private BurgerMenuBar createBurgerMenuBar( MenuBar mainMenuBar ) {
    MenuCloner<BurgerMenuBar> menuCloner = new MenuCloner<>( m -> new BurgerMenuBar() );
    BurgerMenuBar burgerMenuBar = menuCloner.clone( mainMenuBar );

    // add perspectives menu
    burgerMenuBar.addItem( PerspectiveManager.getInstance().getBurgerBarPerspectiveMenuItem() );

    // add admin/UserDropDown menu
    MenuItem burgerUserDropDown = new MenuItem( UserDropDown.getUsername(), (Scheduler.ScheduledCommand) null );
    burgerUserDropDown.setSubMenu( menuCloner.clone( MantleApplication.getUserDropDown().getMenuBar() ) );
    burgerMenuBar.addItem( burgerUserDropDown );

    // Lastly, add back menu items.
    burgerMenuBar.addBackItemToDescendantMenus();

    return burgerMenuBar;
  }

  @Bindable
  public void burgerMenuButtonClicked() {
    toggleBurgerMenuExpanded();
  }

  // endregion

  @Bindable
  public void saveClicked() {
    model.executeSaveCommand();
  }

  @Bindable
  public void saveAsClicked() {
    model.executeSaveAsCommand();
  }

  @Bindable
  public void openChangePasswordDialog() {
    model.openChangePasswordDialog( this );
  }

  public void updatePassword( String user, String newPassword, String oldPassword, final ServiceCallback callback ) {

    String userName = user;
    String serviceUrl = GWT.getHostPageBaseURL() + "api/userroledao/user";
    RequestBuilder executableTypesRequestBuilder = new RequestBuilder( RequestBuilder.PUT, serviceUrl );
    try {
      executableTypesRequestBuilder.setHeader( HTTP_REQUEST_ACCEPT_HEADER, JSON_REQUEST_HEADER );
      executableTypesRequestBuilder.setHeader( "If-Modified-Since", "01 Jan 1970 00:00:00 GMT" );
      executableTypesRequestBuilder.setHeader( "Content-Type", JSON_REQUEST_HEADER );
      String json =
              "{\"userName\": \"" + encodeUri( userName ) + "\", \"newPassword\": \"ENC:" + b64encode( newPassword ) + "\", \"oldPassword\": \"ENC:" + b64encode( oldPassword ) + "\"}";
      executableTypesRequestBuilder.sendRequest( json, new RequestCallback() {
        public void onError( Request request, Throwable exception ) {
          handleCallbackError( callback, false, "" );
        }

        public void onResponseReceived( Request request, Response response ) {
          if ( response.getStatusCode() != Response.SC_NO_CONTENT ) {
            handleCallbackError( callback, false, response.getHeader( PUC_VALIDATION_ERROR_MESSAGE ) );
          } else {
            callback.serviceResult( true );
          }
        }
      } );
    } catch ( RequestException e ) {
      showXulErrorMessage( Messages.getString( CHANGE_PASS_ERROR_TITLE ), Messages.getString( CHANGE_PASS_ERROR_MESSAGE ) );
      callback.serviceResult( false );
    }

  }

  private void handleCallbackError( ServiceCallback callback, boolean serviceResult, String details ) {

    String errorMsg = Messages.getString( CHANGE_PASS_ERROR_MESSAGE );
    if ( details != null ) {
      errorMsg = errorMsg + "\n" + details;
    }
    showXulErrorMessage( Messages.getString( CHANGE_PASS_ERROR_TITLE ), errorMsg );
    callback.serviceResult( serviceResult );
  }

  private void showXulErrorMessage( String title, String message ) {
    GwtMessageBox messageBox = new GwtMessageBox();
    messageBox.setTitle( title );
    messageBox.setMessage( message );
    messageBox.setButtons( new Object[GwtMessageBox.ACCEPT] );
    messageBox.setWidth( 520 );
    messageBox.show();
  }

  private final native String encodeUri( String uri )
  /*-{
    return encodeURIComponent(uri);
  }-*/;

  private static native String b64encode( String a ) /*-{
    return window.btoa(a);
  }-*/;

  @Bindable
  public void showNavigatorClicked() {
    boolean show = !model.isShowNavigatorSelected();
    model.setShowNavigatorSelected( show ); // toggle first
    ShowBrowserCommand showBrowserCommand = new ShowBrowserCommand( show );
    showBrowserCommand.execute();
  }

  @Bindable
  public void setSaveEnabled( boolean flag ) {
    // called by the MainToolbarModel to change state.
    saveBtn.setVisible( flag );
  }

  @Bindable
  public void setSaveAsEnabled( boolean flag ) {
    // called by the MainToolbarModel to change state.
    saveAsBtn.setVisible( flag );
  }

  @Override
  public String getName() {
    return "mantleXulHandler"; //$NON-NLS-1$
  }

  @Bindable
  public void executeCallback( String jsScript ) {
    executeJS( model.getCallback(), jsScript );
  }

  @Bindable
  public void executeMantleFunc( String funct ) {
    executeMantleCall( funct );
  }

  private native void executeMantleCall( String js )
  /*-{
    try{
      $wnd.eval(js);
    } catch (e){
      try {
        console.warn( "Error invoking: " + js + ": " + e.message );
      } catch ( ignore ) {}
    }
  }-*/;

  private void passivateActiveSecurityPanels( final String idOfSecurityPanelToBeActivated,
      final String urlOfSecurityPanelToBeActivated ) {
    adminPanelAwaitingActivation =
        new SysAdminPanelInfo( idOfSecurityPanelToBeActivated, urlOfSecurityPanelToBeActivated );
    int visiblePanelIndex = MantleXul.getInstance().getAdminContentDeck().getVisibleWidget();
    if ( visiblePanelIndex >= 0 ) {
      String visiblePanelId =
          MantleXul.getInstance().getAdminContentDeck().getWidget( visiblePanelIndex ).getElement().getId();
      if ( ( visiblePanelId != null ) && !visiblePanelId.equals( idOfSecurityPanelToBeActivated ) ) {
        ISysAdminPanel sysAdminPanel = sysAdminPanelsMap.get( visiblePanelId );
        if ( sysAdminPanel != null ) {
          sysAdminPanel.passivate( new AsyncCallback<Boolean>() {
            public void onFailure( Throwable caught ) {
            }

            public void onSuccess( Boolean passivateComplete ) {
              if ( passivateComplete ) {
                activateWaitingSecurityPanel( passivateComplete );
              }
            }
          } );
        } else {
          activateWaitingSecurityPanel( true );
        }
      } else {
        activateWaitingSecurityPanel( false );
      }
    } else {
      activateWaitingSecurityPanel( true );
    }
  }

  @Bindable
  public void loadAdminContent( final String panelId, final String url ) {
    passivateActiveSecurityPanels( panelId, url );
  }

  @Bindable
  public void loadSettingsPanel() {
    GWT.runAsync( new RunAsyncCallback() {
      public void onSuccess() {
        String contentCleanerPanelId = ContentCleanerPanel.getInstance().getId();
        if ( !sysAdminPanelsMap.containsKey( contentCleanerPanelId ) ) {
          sysAdminPanelsMap.put( contentCleanerPanelId, ContentCleanerPanel.getInstance() );
        }
        loadAdminContent( contentCleanerPanelId, null );
      }

      public void onFailure( Throwable reason ) {
      }
    } );
  }

  @Bindable
  public void loadUserRolesAdminPanel() {
    GWT.runAsync( new RunAsyncCallback() {
      public void onSuccess() {

        if ( overrideContentPanelId != null && overrideContentUrl != null ) {
          loadAdminContent( overrideContentPanelId, overrideContentUrl );
          overrideContentPanelId = null;
          overrideContentUrl = null;
        } else {
          String usersAndGroupsPanelId = UserRolesAdminPanelController.getInstance().getId();
          if ( !sysAdminPanelsMap.containsKey( usersAndGroupsPanelId ) ) {
            sysAdminPanelsMap.put( usersAndGroupsPanelId, UserRolesAdminPanelController.getInstance() );
          }
          loadAdminContent( usersAndGroupsPanelId, null );
        }
      }

      public void onFailure( Throwable reason ) {
      }
    } );
  }

  @Bindable
  public void loadEmailAdminPanel() {
    GWT.runAsync( new RunAsyncCallback() {
      public void onSuccess() {
        String emailPanelId = EmailAdminPanelController.getInstance().getId();
        if ( !sysAdminPanelsMap.containsKey( emailPanelId ) ) {
          sysAdminPanelsMap.put( emailPanelId, EmailAdminPanelController.getInstance() );
        }
        loadAdminContent( emailPanelId, null );
      }

      public void onFailure( Throwable reason ) {
      }
    } );
  }

  @Bindable
  public void executeMantleCommand( String cmd ) {
    String js = "executeCommand('" + cmd + "')"; //$NON-NLS-1$ //$NON-NLS-2$
    executeMantleCall( js );
  }

  private native void executeJS( JavaScriptObject obj, String js )
  /*-{
    try{
      var tempObj = obj;
      eval("tempObj."+js);
    } catch (e){
      $wnd.mantle_showMessage("Javascript Error",e.message+"          "+"tempObj."+js);
    }
  }-*/;

  @Bindable
  public native void openUrl( String title, String name, String uri )
  /*-{
    try {
      $wnd.eval("openURL('"+name+"','"+title+"','"+uri+"')");
    } catch (e) {
      $wnd.mantle_showMessage("Javascript Error",e.message);
    }
  }-*/;

  @Bindable
  public void setContentEditEnabled( boolean enable ) {
    contentEditBtn.setVisible( enable );
  }

  @Bindable
  public void setContentEditSelected( boolean selected ) {
    contentEditBtn.setSelected( selected );
  }

  @Bindable
  /*
   * Notifies currently active Javascript callback of an edit event.
   */
  public void editContentClicked() {
    // set delay for edit/view mode switching
    setContentEditEnabled( false );
    Timer loadOverlayTimer = new Timer() {
      public void run() {
        setContentEditEnabled( true );
      }
    };
    loadOverlayTimer.schedule( 1800 );

    model.setContentEditToggled();

    toggleEditContentBtnTooltip( model, contentEditBtn );

    executeEditContentCallback( SolutionBrowserPanel.getInstance().getContentTabPanel().getCurrentFrame().getFrame()
        .getElement(), model.isContentEditSelected() );
  }

  private void toggleEditContentBtnTooltip( MantleModel model, XulComponent button ) {
    if ( model.isContentEditSelected() ) {
      button.setTooltiptext( "View Mode" );
    } else {
      button.setTooltiptext( "Edit Content" );
    }
  }

  @Bindable
  public void printClicked() {
    model.executePrintCommand();
  }

  private native void executeEditContentCallback( Element obj, boolean selected )
  /*-{
    try {
      obj.contentWindow.editContentToggled(selected);
    } catch (e){if(console){console.log(e);}}
  }-*/;

  public MantleModel getModel() {

    return model;
  }

  public void setModel( MantleModel model ) {

    this.model = model;
  }

  public boolean isMenuItemEnabled( String id ) {
    XulMenuitem item = (XulMenuitem) document.getElementById( id );
    return !item.isDisabled();
  }

  public void setMenuItemEnabled( String id, boolean enabled ) {
    XulMenuitem item = (XulMenuitem) document.getElementById( id );
    item.setDisabled( !enabled );
  }

  public void setMenuBarEnabled( String id, boolean enabled ) {
    XulMenubar bar = (XulMenubar) document.getElementById( id );
    bar.setVisible( enabled );
  }

  public void setToolBarButtonEnabled( String id, boolean enabled ) {
    XulToolbarbutton button = (XulToolbarbutton) document.getElementById( id );
    button.setVisible( enabled );
  }

  public boolean doesMenuItemExist( String id ) {
    try {
      XulMenuitem item = (XulMenuitem) document.getElementById( id );
      return ( item != null );
    } catch ( Throwable t ) {
      return false;
    }
  }

  @Bindable
  public boolean isSaveEnabled() {
    return saveMenuItem.isDisabled();
  }

  @Bindable
  public boolean isSaveAsEnabled() {
    return saveAsMenuItem.isDisabled();
  }

  @Bindable
  public void propertiesClicked() {
    model.executePropertiesCommand();
  }

  @Bindable
  public void shareContentClicked() {
    model.executeShareContent();
  }

  @Bindable
  public void scheduleContentClicked() {
    model.executeScheduleContent();
  }

  @Bindable
  public void useDescriptionsForTooltipsClicked() {
    boolean checked = ( (PentahoMenuItem) useDescriptionsMenuItem.getManagedObject() ).isChecked();
    ( (PentahoMenuItem) useDescriptionsMenuItem.getManagedObject() ).setChecked( !checked );
    model.toggleUseDescriptionsForTooltips();
  }

  @Bindable
  public void showHiddenFilesClicked() {
    boolean checked = ( (PentahoMenuItem) showHiddenFilesMenuItem.getManagedObject() ).isChecked();
    ( (PentahoMenuItem) showHiddenFilesMenuItem.getManagedObject() ).setChecked( !checked );
    SolutionBrowserPanel.getInstance().toggleShowHideFilesCommand.execute();
  }

  @Bindable
  public void refreshContent() {

    model.refreshContent();
  }

  @Bindable
  public void documentationClicked() {
    model.openDocumentation();
  }

  @Bindable
  public void kettleStatusPageClicked() {
    model.openKettleStatusPage();
  }

  public void loadOverlay( String id ) {
    // TODO We need to convert ths to use the common interface method,
    // once they become available
    GwtXulDomContainer container = (GwtXulDomContainer) getXulDomContainer();
    try {
      container.loadOverlay( id );
    } catch ( XulException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public void removeOverlay( String id ) {
    // TODO We need to convert ths to use the common interface method,
    // once they become available
    GwtXulDomContainer container = (GwtXulDomContainer) getXulDomContainer();
    try {
      container.removeOverlay( id );
    } catch ( XulException e ) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}

enum PickListType {
  FAVORITE( "clearFavoriteList", "clearFavoriteItemsMessage" ), RECENT( "clearRecentList", "clearRecentItemsMessage" );

  String menuItemKey = null;
  String messageKey = null;

  PickListType( String menuItemKey, String messageKey ) {
    this.menuItemKey = menuItemKey;
    this.messageKey = messageKey;
  }

  String getMenuItemKey() {
    return this.menuItemKey;
  }

  String getMessageKey() {
    return this.messageKey;
  }

}
