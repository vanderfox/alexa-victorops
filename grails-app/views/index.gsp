<!doctype html>
<html>
    <head>
        <meta name="layout" content="main"/>
        <title>Welcome to Alexa VictorOPS Control</title>
        <style type="text/css" media="screen">
            #status {
                background-color: #eee;
                border: .2em solid #fff;
                margin: 2em 2em 1em;
                padding: 1em;
                width: 12em;
                float: left;
                -moz-box-shadow: 0px 0px 1.25em #ccc;
                -webkit-box-shadow: 0px 0px 1.25em #ccc;
                box-shadow: 0px 0px 1.25em #ccc;
                -moz-border-radius: 0.6em;
                -webkit-border-radius: 0.6em;
                border-radius: 0.6em;
            }

            #status ul {
                font-size: 0.9em;
                list-style-type: none;
                margin-bottom: 0.6em;
                padding: 0;
            }

            #status li {
                line-height: 1.3;
            }

            #status h1 {
                text-transform: uppercase;
                font-size: 1.1em;
                margin: 0 0 0.3em;
            }

            #page-body {
                margin: 2em 1em 1.25em 18em;
            }

            h2 {
                margin-top: 1em;
                margin-bottom: 0.3em;
                font-size: 1em;
            }

            p {
                line-height: 1.5;
                margin: 0.25em 0;
            }

            #controller-list ul {
                list-style-position: inside;
            }

            #controller-list li {
                line-height: 1.3;
                list-style-position: inside;
                margin: 0.25em 0;
            }

            @media screen and (max-width: 480px) {
                #status {
                    display: none;
                }

                #page-body {
                    margin: 0 1em 1em;
                }

                #page-body h1 {
                    margin-top: 0;
                }
            }
        </style>
    </head>
    <body>
    <g:set var="springSecurityService" bean="springSecurityService"/>
        <a href="#page-body" class="skip"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
        <div id="page-body" role="main">
            <h1>Welcome to Alexa VictorOPS Control</h1>
            <p>Create an account here and link an API key to your account via the AwsCredentials so the assistant can help you! You can only have one active set of credentials at a time. Toggle them via the active flag.

            </p>

            <ul>
                <sec:ifLoggedIn><li><a href="${g.createLink(absolute: true,uri:'/user/edit')}">Edit user profile</a></li></sec:ifLoggedIn>
                <sec:ifLoggedIn><li><a href="${g.createLink(absolute: true,uri:'/awsCredentials/index')}">Link AWS Credentials</a></li></sec:ifLoggedIn>
            </ul>
        </div>
    </body>
</html>
