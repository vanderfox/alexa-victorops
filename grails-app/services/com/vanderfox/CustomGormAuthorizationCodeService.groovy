package com.vanderfox

import grails.plugin.springsecurity.oauthprovider.GormAuthorizationCodeService
import grails.transaction.Transactional
import org.springframework.security.oauth2.provider.OAuth2Authentication

class CustomGormAuthorizationCodeService extends GormAuthorizationCodeService {
    @Transactional //this will make sure hibernate session is available
    protected void store(String code, OAuth2Authentication authentication) {
        super.store(code, authentication)
    }

    @Transactional
    protected OAuth2Authentication remove(String code) {
        super.remove(code)
    }
}