package com.vanderfox

class AwsCredentials {

    static constraints = {
        user(nullable: false)
        accessToken(nullable:false)
        accessTokenSecret(nullable: false)
        active(nullable:false)
    }
    User user
    String accessToken
    String accessTokenSecret
    Boolean active = true

}
