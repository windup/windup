<#if index_page??>
    <#assign redHatLogoPrefix = navUrlPrefix>
<#else>
    <#assign redHatLogoPrefix = "">
</#if>
            <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-responsive-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </button>
            <span class="wu-navbar-header">
              <strong class="wu-navbar-header">Migration Toolkit for Applications</strong><img align="right" class="wu-navbar-header" src="${redHatLogoPrefix}resources/img/Logo-RedHat-A-Reverse-RGB.png" />
            </span>