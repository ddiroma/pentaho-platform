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


define([
  "common-ui/util/PentahoSpinner",
  "common-ui/util/spin",
  "pentaho/csrf/service",
  "common-ui/util/_a11y",
  "common-ui/util/xss",
  "pentaho/shim/css.escape"
], function(spinner, Spinner, csrfService, a11yUtil, xssUtil) {

  var local = {
    name: "favorites",
    template: {
      id: "favoritesTemplate",
      itemIterator: "eachFavorite"
    },
    displayContainerId: "favoritesContianer",
    contentPanelId: "favorites-content-panel",
    serviceUrl: "api/user-settings/favorites",
    favoritesUrl: "api/user-settings/favorites",
    spinContainer: "favoritesSpinner",

    // assume this will be supplied by the controller via configuration
    i18nMap: undefined,

    helperRegistered: false,

    knownExtensions: {
      xanalyzer: "xanalyzer",
      prpt: "prpt",
      prpti: "prpti",
      xaction: "xaction",
      url: "url",
      xdash: "xdash",
      xcdf: "xcdf",
      html: "html",
      cda: "cda",
      wcdf: "wcdf",
      ktr: "ktr"
    },

    init: function () {
    },

    /**
     * register the Handlebars helper. can't do this on init since we allow post creation extension via $.extend().
     */
    registerHelper: function () {
      if (!this.helperRegistered) {
        var that = this;
        Handlebars.registerHelper(this.template.itemIterator, function (context, options) {
          var ret = "";
          var isEmpty = that.isEmptyList(context);
          for (var i = 0, j = context.length; i < j; i++) {
            var repositoryPath = context[i].fullPath;
            if (repositoryPath) {
              var extension = repositoryPath.substr((repositoryPath.lastIndexOf('.') + 1));

              context[i].extension = extension;
              context[i].extensionCssClass = "file-" + CSS.escape(extension);
              if (extension && that.knownExtensions[extension]) {
                context[i][extension] = true;
              } else {
                context[i].unknownType = true;
              }
            }
            context[i].isEmpty = isEmpty;
            context[i].isFavorite = that.isItemAFavorite(context[i].fullPath);
            ret = ret + options.fn(context[i]);
          }

          return ret;
        });

        Handlebars.registerHelper("hasItems", function (context, options) {
          if (that.isEmptyList(context)) {
            return options.inverse(this);
          }
          return options.fn(this);
        });

        Handlebars.registerHelper('escapeQuotes', function(stringInput) {
          return stringInput.replace(/(['"])/g, '\\$1');
        });

        this.helperRegistered = true;
      }
    },

    isEmptyList: function (context) {
      return (context.length == 0 || (context.length == 1 && context[0].title == this.i18nMap.emptyList));
    },

    load: function (/*Optional|Function*/callback) {
      this._contentRefreshed = callback;
      this._beforeLoad();
      this.registerHelper();
      var context = {};
      context.i18n = this.i18nMap;

      if (this.disabled) {
        return;
      }

      var that = this;
      this.getContent(function (items) {
        that.showList(items, context);
      });
    },

    _beforeLoad: function () {
      this.currentItems = [];
    },

    getUrlBase: function () {
      if (!this.urlBase) {
        this.urlBase = window.location.pathname.substring(0, window.location.pathname.indexOf("/mantle/home")) + "/";
      }
      return this.urlBase;
    },

    getContent: function (/*Function*/ callback) {
      var now = new Date();
      var that = this;
      $.ajax({
        url: that.getUrlBase() + that.serviceUrl + "?ts=" + now.getTime(),

        success: function (result, status) {
          callback(status == "nocontent" ? [] : result);
          if (that._contentRefreshed) {
            that._contentRefreshed();
          }
        },

        error: function (err) {
          console.log(that.i18nMap["error_could_not_get" + that.name] + " - " + err);
        },

        beforeSend: function () {
          that.showWaiting();
        }

      });
    },

    _contentRefreshed: function () {

    },

    doClear: function (callback) {
      var that = this;
      var context = {};
      context.i18n = that.i18nMap;

      var headers = {};
      var url = that.getUrlBase() + that.serviceUrl;

      var csrfToken = csrfService.getToken(url);
      if(csrfToken != null) {
        headers[csrfToken.header] = csrfToken.token;
      }

      $.ajax({
        url: url,
        type: 'POST',
        data: [],
        headers: headers,

        success: function (result) {
          that.showList(result, context);
          if (callback) {
            callback();
          }
        },

        error: function (err) {
          console.log(that.i18nMap["error_could_not_clear_" + that.name] + " - " + err);
        },

        beforeSend: function () {
          that.showWaiting()
        }

      });
    },

    showWaiting: function () {
      var config = spinner.getLargeConfig();
      config.color = "#BBB";
      this.spinner = new Spinner(config);
      var s = this.spinner.spin();
      var $container = $("#" + this.contentPanelId);
      $container.empty();
      $container.append(s.el);
    },

    showList: function (items, context) {
      if (!this.template.html) {
        this.template.html = $("#" + this.template.id).html();
      }
      var template = Handlebars.compile(this.template.html);
      if (items.length > 0) {
        try {
          context[this.name] = items;
          context.isEmpty = context[this.name].length == 0;
          this.currentItems = context[this.name];
          if (context.isEmpty) {
            context[this.name] = [
              {title: this.i18nMap.emptyList}
            ];
            this.currentItems = [];
          }
        } catch (err) {
          context[this.name] = [
            {title: this.i18nMap.emptyList}
          ];
          context.isEmpty = true;
          this.currentItems = [];
        }
      } else {
        context[this.name] = [
          {title: this.i18nMap.emptyList}
        ];
        context.isEmpty = true;
        this.currentItems = [];
      }
      var html = template(context);
      var that = this;
      // make sure the spinner is visible long enough for the user to see it
      setTimeout(function () {
        that.spinner.stop();
        xssUtil.setHtmlUnsafe($("#" + that.displayContainerId), html);
        $("#"+that.contentPanelId).find("a").first().attr("tabindex",0);

        $("#"+that.contentPanelId).find("a").keydown(function(event) {
          var nextItem;
          var keyCode = event.which || event.keyCode;
          if (keyCode == a11yUtil.keyCodes.arrowUp) {
            nextItem = $(this).parent().prev();
          } else if(keyCode == a11yUtil.keyCodes.arrowDown) {
            nextItem = $(this).parent().next();
          } else if (keyCode == a11yUtil.keyCodes.space) {
            $(this).find(".pull-right").click();
            event.preventDefault();
          }

          if ( nextItem != null && nextItem.length !== 0) {
            $("#"+that.contentPanelId+" a[tabindex=0]").attr("tabindex",-1);
            $(nextItem).find("a").attr("tabindex", 0);
            nextItem.children().focus();
          }
        });
      }, 100);
    },

    clear: function (callback) {
      this.doClear(callback);
    },

    /**
     * override this for recents to have logic.
     * @param fullPath
     * @returns {boolean}
     */
    isItemAFavorite: function (fullPath) {
      return true;
    },

    getFavorites: function () {
      return this.getCurrentItems();
    },

    getCurrentItems: function () {
      return this.currentItems;
    },

    unmarkFavorite: function (fullPath) {
      //let mantle add the favorite
      if (window.parent.mantle_removeFavorite) {
        window.parent.mantle_removeFavorite(fullPath);
      } else {
        console.log(this.i18nMap.error_could_not_unmark_favorite);
      }
    },

    markFavorite: function (fullPath, title) {
      //let mantle add the favorite
      if (window.parent.mantle_addFavorite) {
        window.parent.mantle_addFavorite(fullPath, title);
      } else {
        console.log(this.i18nMap.error_could_not_mark_favorite);
      }
    },

    indexOf: function (fullPath) {
      var items = this.getCurrentItems();
      var index = -1;
      if (items) {
        $.each(items, function (idx, item) {
          if (item.fullPath == fullPath) {
            index = idx;
            return false;
          }
        });
      }
      return index;
    },
    indexOfFavorite: function (fullPath) {
      var items = this.getFavorites();
      var index = -1;
      if (items) {
        $.each(items, function (idx, item) {
          if (item.fullPath == fullPath) {
            index = idx;
            return false;
          }
        });
      }
      return index;
    }
  };

  var favorite = function () {
    this.init();
  }
  favorite.prototype = local;
  return favorite;

});
