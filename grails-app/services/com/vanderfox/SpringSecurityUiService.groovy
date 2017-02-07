package com.vanderfox

import grails.transaction.Transactional

@Transactional
class SpringSecurityUiService extends grails.plugin.springsecurity.ui.SpringSecurityUiService {

    def serviceMethod() {

    }


    void updateUser(Map properties, Object user) {
        String oldPassword = uiPropertiesStrategy.getProperty(user, 'password')

        uiPropertiesStrategy.setProperties properties, user, transactionStatus
        if (properties.password && properties.password != oldPassword) {
            updatePassword user, properties.password, transactionStatus
        }

        save [:], user, 'updateUser', transactionStatus
        if (user.hasErrors()) {
            return
        }

        removeUserFromCache user
    }
}
