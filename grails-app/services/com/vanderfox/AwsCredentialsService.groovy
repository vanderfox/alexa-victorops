package com.vanderfox

import grails.transaction.Transactional


class AwsCredentialsService {

    def serviceMethod() {

    }

    /**
     * this should lookup the provided auth token from account linking
     * and return the associated user
     * @param authToken
     * @return
     */
    User getUserForAccessToken(String authToken) {
        def token = AccessToken.findByValue(authToken)
        if (token) {
            return User.findByUsername(token.username)
        }
        return null


    }

    @Transactional
    void markOthersInactive(ApiCredentials twitterCredentials, User currentUser) {
        if (twitterCredentials.active) {
            // turn off active on all others
            def otherCreds = ApiCredentials.findAllByUserAndActive(currentUser, true)
            if (otherCreds?.size() > 0) {
                otherCreds.each { cred ->
                    cred.active = false
                    cred.save(flush: true) //TODO batch these up for perf
                }
            }
        }
    }

    ApiCredentials getCredentialsForUser(User user) {
        return ApiCredentials.findByUser(user)
    }
}
