package com.vanderfox

import grails.orm.PagedResultList
import grails.plugin.springsecurity.annotation.Secured
import grails.transaction.Transactional
import org.springframework.http.HttpStatus

@Transactional(readOnly = true)
class ApiCredentialsController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]
    def springSecurityService
    def apiCredentialsService

    @Secured(['ROLE_USER'])
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        User currentUser = springSecurityService.currentUser
        currentUser = User.findByUsername(currentUser.username)
        //params.put("user",currentUser)
        def awsCriteria = ApiCredentials.createCriteria()
        PagedResultList listOfUsers = awsCriteria.list(params) { eq('user', currentUser) }
        respond listOfUsers, model:[apiCredentialsCount: listOfUsers.size(),apiCredentialsList:listOfUsers]
    }

    @Secured(['ROLE_USER'])
    def show(ApiCredentials apiCredentials) {
        User currentUser = springSecurityService.currentUser
        if (apiCredentials.user != currentUser) {
            render status: HttpStatus.UNAUTHORIZED
        } else {
            respond apiCredentials
        }
    }

    @Secured(['ROLE_USER'])
    def create() {
        respond new ApiCredentials(params)
    }

    @Transactional
    @Secured(['ROLE_USER'])
    def save(ApiCredentials awsCredentials) {
        if (awsCredentials == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        User currentUser = springSecurityService.currentUser
        awsCredentials.user = currentUser
        apiCredentialsService.markOthersInactive(awsCredentials, currentUser)
        awsCredentials.active = true
        awsCredentials.save flush:true
        if (awsCredentials.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond awsCredentials.errors, view:'create'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'apiCredentials.label', default: 'ApiCredentials'), awsCredentials.id])
                redirect awsCredentials
            }
            '*' { respond awsCredentials, [status: HttpStatus.CREATED] }
        }
    }



    @Secured(['ROLE_USER'])
    def edit(ApiCredentials apiCredentials) {
        //ApiCredentials apiCredentials = ApiCredentials.get(params.id)
        respond apiCredentials
    }
    @Transactional
    @Secured(['ROLE_USER'])
    def update(ApiCredentials apiCredentials) {
        if (apiCredentials == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (apiCredentials.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond apiCredentials.errors, view:'edit'
            return
        }
        User currentUser = springSecurityService.currentUser
        apiCredentialsService.markOthersInactive(apiCredentials, currentUser)
        apiCredentials.active = true
        if (apiCredentials.user == currentUser) {
            apiCredentials.save flush: true
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'apiCredentials.label', default: 'ApiCredentials'), apiCredentials.id])
                redirect apiCredentials
            }
            '*'{ respond apiCredentials, [status: HttpStatus.OK] }
        }
    }

    @Transactional
    @Secured(['ROLE_ADMIN'])
    def delete(ApiCredentials awsCredentials) {

        if (awsCredentials == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        awsCredentials.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'apiCredentials.label', default: 'ApiCredentials'), awsCredentials.id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: HttpStatus.NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'apiCredentials.label', default: 'ApiCredentials'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: HttpStatus.NOT_FOUND }
        }
    }
}
