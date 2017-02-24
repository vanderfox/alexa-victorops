<!DOCTYPE html>
<html>
    <head>
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'apiCredentials.label', default: 'ApiCredentials')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <a href="#show-apiCredentials" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
        <div class="nav" role="navigation">
            <ul>
                <li><a class="home" href="${createLink(uri: '/')}"><g:message code="default.home.label"/></a></li>
                <li><g:link class="list" action="index"><g:message code="default.list.label" args="[entityName]" /></g:link></li>
                <li><g:link class="create" action="create"><g:message code="default.new.label" args="[entityName]" /></g:link></li>
            </ul>
        </div>
        <div id="show-apiCredentials" class="content scaffold-show" role="main">
            <h1><g:message code="default.show.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message" role="status">${flash.message}</div>
            </g:if>

            <ol class="property-list apiCredentials">

               %{-- <li class="fieldcontain">
                    <span id="user-label" class="property-label">User</span>
                    <div class="property-value" aria-labelledby="user-label"><f:display bean="apiCredential" property="user.username"/></div>
                </li>--}%

                <li class="fieldcontain">
                    <span id="apiId-label" class="property-label">Api Id</span>
                    <div class="property-value" aria-labelledby="apiId-label"><f:display bean="apiCredentials" property="apiId"/></div>
                </li>

                <li class="fieldcontain">
                    <span id="apiKey-label" class="property-label">Api Key</span>
                    <div class="property-value" aria-labelledby="apiKey-label"><f:display bean="apiCredentials" property="apiKey"/></div>
                </li>

                <li class="fieldcontain">
                    <span id="username-label" class="property-label">Username</span>
                    <div class="property-value" aria-labelledby="username-label"><f:display bean="apiCredentials" property="username"/></div>
                </li>



            </ol>
            <g:form resource="${this.apiCredentials}" method="DELETE">
                <fieldset class="buttons">
                    <g:link class="edit" action="edit" resource="${this.apiCredentials}"><g:message code="default.button.edit.label" default="Edit" /></g:link>
                    <input class="delete" type="submit" value="${message(code: 'default.button.delete.label', default: 'Delete')}" onclick="return confirm('${message(code: 'default.button.delete.confirm.message', default: 'Are you sure?')}');" />
                </fieldset>
            </g:form>
        </div>
    </body>
</html>
