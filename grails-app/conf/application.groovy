

// Added by the Spring Security Core plugin:
grails.plugin.springsecurity.userLookup.userDomainClassName = 'com.vanderfox.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'com.vanderfox.UserRole'
grails.plugin.springsecurity.authority.className = 'com.vanderfox.Role'
grails.plugin.springsecurity.authority.groupAuthorityNameField = 'authorities'
grails.plugin.springsecurity.useRoleGroups = false
grails.plugin.springsecurity.controllerAnnotations.staticRules = [
		[pattern: '/',               access: ['permitAll']],
		[pattern: '/error',          access: ['permitAll']],
		[pattern: '/index',          access: ['permitAll']],
		[pattern: '/index.gsp',      access: ['permitAll']],
		[pattern: '/shutdown',       access: ['permitAll']],
		[pattern: '/assets/**',      access: ['permitAll']],
		[pattern: '/**/js/**',       access: ['permitAll']],
		[pattern: '/**/css/**',      access: ['permitAll']],
		[pattern: '/**/images/**',   access: ['permitAll']],
		[pattern: '/**/favicon.ico', access: ['permitAll']],
		[pattern: '/logout**', access: ['permitAll']],
		[pattern: '/register/**', access: ['permitAll']],
		//[pattern: '/logout/**', access: ['permitAll']],
		[pattern: '/registrationCode/**', access: ['permitAll']],
		[pattern: '/apiCredentials/**', access: ['isFullyAuthenticated()']],
		[pattern: '/victorops/victorOPS/**', access: ['permitAll']],
		[pattern: '/victorOPS/**', access: ['permitAll']],
		[pattern: '/securityInfo/**', access: ['permitAll']],
		[pattern: '/user/**', access: ['permitAll']],
		[pattern: '/role/**', access: ['ROLE_ADMIN']],
		[pattern: '/securityInfo/**', access: ['ROLE_ADMIN']],
		[pattern: '/requestMap/**', access: ['ROLE_ADMIN']],
		[pattern: '/oauth/authorize',           access: "isFullyAuthenticated() and (request.getMethod().equals('GET') or request.getMethod().equals('POST'))"],
		[pattern: '/oauth/token',               access: "isFullyAuthenticated() and request.getMethod().equals('POST')"],
		[pattern: '/victorops/oauth/authorize',           access: "isFullyAuthenticated() and (request.getMethod().equals('GET') or request.getMethod().equals('POST'))"],
		[pattern: '/victorops/oauth/token',               access: "isFullyAuthenticated() and request.getMethod().equals('POST')"]
]


grails.plugin.springsecurity.filterChain.chainMap = [
		[pattern: '/assets/**',      filters: 'none'],
		[pattern: '/**/js/**',       filters: 'none'],
		[pattern: '/**/css/**',      filters: 'none'],
		[pattern: '/**/images/**',   filters: 'none'],
		[pattern: '/**/favicon.ico', filters: 'none'],
		[pattern: '/oauth/token',               filters: 'JOINED_FILTERS,-oauth2ProviderFilter,-securityContextPersistenceFilter,-logoutFilter,-authenticationProcessingFilter,-rememberMeAuthenticationFilter,-exceptionTranslationFilter'],
		[pattern: '/securedOAuth2Resources/**', filters: 'JOINED_FILTERS,-securityContextPersistenceFilter,-logoutFilter,-authenticationProcessingFilter,-rememberMeAuthenticationFilter,-oauth2BasicAuthenticationFilter,-exceptionTranslationFilter'],
		[pattern: '/**',                        filters: 'JOINED_FILTERS,-statelessSecurityContextPersistenceFilter,-oauth2ProviderFilter,-clientCredentialsTokenEndpointFilter,-oauth2BasicAuthenticationFilter,-oauth2ExceptionTranslationFilter']
]



// Added by the Spring Security OAuth2 Provider plugin:
grails.plugin.springsecurity.oauthProvider.clientLookup.className = 'com.vanderfox.Client'
grails.plugin.springsecurity.oauthProvider.authorizationCodeLookup.className = 'com.vanderfox.AuthorizationCode'
grails.plugin.springsecurity.oauthProvider.accessTokenLookup.className = 'com.vanderfox.AccessToken'
grails.plugin.springsecurity.oauthProvider.refreshTokenLookup.className = 'com.vanderfox.RefreshToken'
grails.plugin.springsecurity.ui.register.emailFrom = 'skills@vanderfox.com'
grails.plugin.springsecurity.logout.postOnly = false
grails.plugin.springsecurity.ui.forgotPassword.emailFrom="skills@vanderfox.com"
//you can put fallback credentials here - if a user is not linked, or has none it will use these to demo

com.amazon.speech.speechlet.servlet.disableRequestSignatureCheck=true
// these are fallback appids comma delimited

alexaSkills.supportedApplicationIds="amzn1.ask.skill.44951a75-4d6f-41f7-b558-0f0b68b8b1e4,amzn1.ask.skill.0865a19e-597b-4050-a64e-21e98e36ab90"
alexaSkills.disableVerificationCheck = true // helpful for debugging or replay a command via curl
alexaSkills.serializeRequests = true // this logs the requests to disk to help you debug
alexaSkills.serializeRequestsOutputPath = "/tmp/"
// these urls come from the developer console - you may need to try it once to get the REAL url
alexaSkills.oauth.redirectUrls = ['https://pitangui.amazon.com/spa/skill/account-linking-status.html?vendorId=M1M1AMAXAW4WEK',
								  'https://layla.amazon.com/spa/skill/account-linking-status.html?M1M1AMAXAW4WEK']
victorOPS.apiId = "" // not used with account linking
victorOPS.apiKey = "" // not used with account linking
victorOPS.userName = "vanderfoxalexa"

grails {
	mail {
		host = "email-smtp.us-east-1.amazonaws.com"
		port = 587
		username = System.getProperty("mail.username")
		password = System.getProperty("mail.password")
		props = ["mail.smtp.starttls.enable":"true",
				 "mail.smtp.port":"587"]
	}
}