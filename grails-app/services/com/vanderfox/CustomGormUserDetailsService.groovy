package com.vanderfox

import grails.plugin.springsecurity.oauthprovider.GormClientDetailsService
import grails.transaction.Transactional
import org.springframework.security.oauth2.provider.*

class CustomGormClientDetailsService extends GormClientDetailsService {
    @Transactional(readOnly = true)
    ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        super.loadClientByClientId(clientId)
    }
}