<!doctype html>
<html lang="en" class="no-js">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <link rel="icon" href="/assets/favicon.ico" type="image/x-icon">
        <title><g:layoutTitle default="Grails"/></title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <asset:stylesheet src="application.css"/>
        <asset:javascript src="application.js"/>
        <style type="text/css" media="screen">
            .banner {
                float:left;
            }
            .logout {
                float:right;
            }
        </style>
        <g:layoutHead/>
    </head>
    <body>
        <div id="container">
            <div class="banner" id="victorOPSLogo"><asset:image src="victorops_logo.png" alt="VictorOPS"/></div>
            <sec:ifLoggedIn>
            <div class="logout" id="login"><a href="${g.createLink(absolute: true,uri:'/logout/index')}">Logout</a></div>
            </sec:ifLoggedIn>
            <sec:ifNotLoggedIn>
                <div class="logout" id="login"><a href="${g.createLink(absolute: true,uri:'/login/auth')}">Login</a> | <a href="${g.createLink(absolute: true,uri:'/register/register')}">Register</a></div>
            </sec:ifNotLoggedIn>

        </div>
        <g:layoutBody/>
        <div class="footer" role="contentinfo"></div>
        <div id="spinner" class="spinner" style="display:none;"><g:message code="spinner.alt" default="Loading&hellip;"/></div>
    </body>
</html>
