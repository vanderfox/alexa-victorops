package alexa.victorops


import com.vanderfox.Client
import com.vanderfox.Role
import com.vanderfox.User
import com.vanderfox.UserRole
import grails.core.GrailsApplication


class BootStrap {

    GrailsApplication grailsApplication

    def init = { servletContext ->

        if (User.count() < 1) {
            def adminRole = new Role(authority: 'ROLE_ADMIN').save(flush: true)
            def userRole = new Role(authority: 'ROLE_USER').save(flush: true)

            def testUser = new User(username: "admin", password: "changeme",email:"rvanderwerf@gmail.com")
            testUser.save(flush: true)

            def role1 = UserRole.create testUser, Role.findByAuthority("ROLE_ADMIN"), true
            def role2 = UserRole.create testUser, Role.findByAuthority("ROLE_USER"), true

            assert User.count() == 1
            assert Role.count() == 2
            assert UserRole.count() == 2
        }
        //assert UserRoleGroup.count() > 0
        Client.deleteAll() // drop and add these each time as we add them often
        new Client(
                clientId: 'my-client',
                authorizedGrantTypes: ['authorization_code', 'refresh_token', 'implicit', 'password', 'client_credentials'],
                authorities: ['ROLE_CLIENT'],
                scopes: ['read', 'write'],
                redirectUris: grailsApplication.config.getProperty('alexaSkills.oauth.redirectUrls')
        ).save(flush: true)
        //TODO make these configurable
        new Client(
                clientId: 'alexa-skill',
                authorizedGrantTypes: ['authorization_code', 'refresh_token', 'implicit', 'password', 'client_credentials'],
                authorities: ['ROLE_CLIENT'],
                scopes: ['read', 'write'],
                redirectUris: grailsApplication.config.getProperty('alexaSkills.oauth.redirectUrls')
        ).save(flush: true)
        //TODO workaround until we can get oauth2 plugin to support redirectUris longer than 255 chars
        new Client(
                clientId: 'alexa-skill-ryan',
                authorizedGrantTypes: ['authorization_code', 'refresh_token', 'implicit', 'password', 'client_credentials'],
                authorities: ['ROLE_CLIENT'],
                scopes: ['read', 'write'],
                redirectUris: ['https://layla.amazon.com/spa/skill/account-linking-status.html?vendorId=MX4X7ECUS4TZT',
                'https://pitangui.amazon.com/spa/skill/account-linking-status.html?vendorId=MX4X7ECUS4TZT']
        ).save(flush: true)

    }
    def destroy = {
    }
}
