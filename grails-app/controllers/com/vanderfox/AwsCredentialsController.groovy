package com.vanderfox

import grails.plugin.springsecurity.annotation.Secured
import grails.transaction.Transactional
import org.springframework.http.HttpStatus

@Transactional(readOnly = true)
class AwsCredentialsController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]
    def springSecurityService
    def awsCredentialsService

    @Secured(['ROLE_USER'])
    def index(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        User currentUser = springSecurityService.currentUser
        currentUser = User.findByUsername(currentUser.username)
        //params.put("user",currentUser)
        def awsCriteria = AwsCredentials.createCriteria()
        def listOfUsers = awsCriteria.list(params) { eq('user', currentUser) }
        respond listOfUsers, model:[awsCredentialsCount: awsCriteria.count()]
    }

    @Secured(['ROLE_USER'])
    def show(AwsCredentials awsCredentials) {
        User currentUser = springSecurityService.currentUser
        if (awsCredentials.user != currentUser) {
            render status: HttpStatus.UNAUTHORIZED
        } else {
            respond awsCredentials
        }
    }

    @Secured(['ROLE_USER'])
    def create() {
        respond new AwsCredentials(params)
    }

    @Transactional
    @Secured(['ROLE_USER'])
    def save(AwsCredentials awsCredentials) {
        if (awsCredentials == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        User currentUser = springSecurityService.currentUser
        awsCredentials.user = currentUser
        awsCredentialsService.markOthersInactive(awsCredentials, currentUser)
        awsCredentials.active = true
        awsCredentials.save flush:true
        if (awsCredentials.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond awsCredentials.errors, view:'create'
            return
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'awsCredentials.label', default: 'AwsCredentials'), awsCredentials.id])
                redirect awsCredentials
            }
            '*' { respond awsCredentials, [status: HttpStatus.CREATED] }
        }
    }



    @Secured(['ROLE_USER'])
    def edit(AwsCredentials awsCredentials) {
        respond awsCredentials
    }

    @Transactional
    @Secured(['ROLE_USER'])
    def update(AwsCredentials awsCredentials) {
        if (awsCredentials == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        if (awsCredentials.hasErrors()) {
            transactionStatus.setRollbackOnly()
            respond awsCredentials.errors, view:'edit'
            return
        }
        User currentUser = springSecurityService.currentUser
        awsCredentialsService.markOthersInactive(awsCredentials, currentUser)
        awsCredentials.active = true
        if (awsCredentials.user == currentUser) {
            awsCredentials.save flush: true
        }

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'awsCredentials.label', default: 'AwsCredentials'), awsCredentials.id])
                redirect awsCredentials
            }
            '*'{ respond awsCredentials, [status: HttpStatus.OK] }
        }
    }

    @Transactional
    @Secured(['ROLE_ADMIN'])
    def delete(AwsCredentials awsCredentials) {

        if (awsCredentials == null) {
            transactionStatus.setRollbackOnly()
            notFound()
            return
        }

        awsCredentials.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'awsCredentials.label', default: 'AwsCredentials'), awsCredentials.id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: HttpStatus.NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'awsCredentials.label', default: 'AwsCredentials'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: HttpStatus.NOT_FOUND }
        }
    }
}
