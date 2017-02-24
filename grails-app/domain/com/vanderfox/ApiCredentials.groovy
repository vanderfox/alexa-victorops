package com.vanderfox

class ApiCredentials {

    static constraints = {
        user(nullable: false)
        apiId(nullable:false)
        apiKey(nullable: false)
        username(nullable: false)
        active(nullable:false)
    }
    User user
    String apiId
    String apiKey
    String username
    Boolean active = true
    int id

}
