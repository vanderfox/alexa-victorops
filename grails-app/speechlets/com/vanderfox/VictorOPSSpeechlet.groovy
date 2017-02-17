package com.vanderfox

import com.amazon.speech.slu.Slot
import com.amazon.speech.speechlet.IntentRequest
import com.amazon.speech.speechlet.LaunchRequest
import com.amazon.speech.speechlet.Session
import com.amazon.speech.speechlet.SessionEndedRequest
import com.amazon.speech.speechlet.SessionStartedRequest
import com.amazon.speech.speechlet.SpeechletException
import com.amazon.speech.speechlet.SpeechletResponse
import com.amazon.speech.ui.LinkAccountCard
import com.amazon.speech.ui.PlainTextOutputSpeech
import com.amazon.speech.ui.Reprompt
import com.amazon.speech.ui.SimpleCard
import com.amazon.speech.speechlet.Context
import com.amazon.speech.speechlet.PlaybackFailedRequest
import com.amazon.speech.speechlet.PlaybackFinishedRequest
import com.amazon.speech.speechlet.PlaybackNearlyFinishedRequest
import com.amazon.speech.speechlet.PlaybackStartedRequest
import com.amazon.speech.speechlet.PlaybackStoppedRequest
import com.amazon.speech.speechlet.SystemExceptionEncounteredRequest
import com.amazon.speech.speechlet.Speechlet
import grails.config.Config
import grails.core.support.GrailsConfigurationAware
import grails.web.Controller
import groovy.util.logging.Slf4j
import groovyx.net.http.*
import static groovyx.net.http.ContentType.JSON

import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter


@Slf4j
class VictorOPSSpeechlet implements GrailsConfigurationAware, Speechlet {

    def grailsApplication

    Config grailsConfig
    def speechletService


    def index() {
        speechletService.doSpeechlet(request,response, this)
    }


    @Override
    SpeechletResponse onPlaybackStarted(PlaybackStartedRequest playbackStartedRequest, Context context) throws SpeechletException {
        return null
    }

    @Override
    SpeechletResponse onPlaybackFinished(PlaybackFinishedRequest playbackFinishedRequest, Context context) throws SpeechletException {
        return null
    }

    @Override
    void onPlaybackStopped(PlaybackStoppedRequest playbackStoppedRequest, Context context) throws SpeechletException {

    }

    @Override
    SpeechletResponse onPlaybackNearlyFinished(PlaybackNearlyFinishedRequest playbackNearlyFinishedRequest, Context context) throws SpeechletException {
        return null
    }

    @Override
    SpeechletResponse onPlaybackFailed(PlaybackFailedRequest playbackFailedRequest, Context context) throws SpeechletException {
        return null
    }

    @Override
    void onSystemException(SystemExceptionEncounteredRequest systemExceptionEncounteredRequest) throws SpeechletException {

    }

    /**
     * This is called when the session is started
     * Add an initialization setup for the session here
     * @param request SessionStartedRequest
     * @param session Session
     * @throws SpeechletException
     */
    public void onSessionStarted(final SessionStartedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionStarted requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId())

    }

    /**
     * This is called when the skill/speechlet is launched on Alexa
     * @param request LaunchRequest
     * @param session Session
     * @return
     * @throws SpeechletException
     */
    public SpeechletResponse onLaunch(final LaunchRequest request, final Session session)
            throws SpeechletException {
        log.info("onLaunch requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId())

        return getWelcomeResponse()
    }

    /**
     * This is the method fired when an intent is called
     *
     * @param request IntentRequest intent called from Alexa
     * @param session Session
     * @return SpeechletResponse tell or ask type
     * @throws SpeechletException
     */
    public SpeechletResponse onIntent(final IntentRequest request, final Session session, Context context)
            throws SpeechletException {
        log.info("onIntent requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId())

        log.debug("invoking intent:${request.intent.name}")

        switch(request.intent.name) {
           case "OpenIncidentsIntent":
                   getOpenIncidents()
                break
            case "ResolveIncident":
                Slot incidentNumber = request.intent.getSlot("username")
                log.debug("incident number:"+incidentNumber.value)
                   changeIncidentStatus(incidentNumber.value,"resolve")
                break
            case "AckIncident":
                Slot incidentNumber = request.intent.getSlot("username")
                log.debug("incident number:"+incidentNumber.value)
                changeIncidentStatus(incidentNumber.value,"ack")
                break
            case "AMAZON.StopIntent":
            case "AMAZON.CancelIntent":
                sayGoodbye()
                break
            case "AMAZON.HelpIntent":
                getHelpResponse()
                break
            default:
                didNotUnderstand()
                break
        }


    }



    private SpeechletResponse askResponse(String cardText, String speechText) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard()
        card.setTitle("DevOps Assistant")
        card.setContent(cardText)

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech()
        speech.setText(speechText)

        // Create reprompt
        Reprompt reprompt = new Reprompt()
        reprompt.setOutputSpeech(speech)

        SpeechletResponse.newAskResponse(speech, reprompt, card)
    }

    private SpeechletResponse tellResponse(String cardText, String speechText) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard()
        card.setTitle("DevOps Assistant")
        card.setContent(cardText)

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech()
        speech.setText(speechText)

        // Create reprompt
        Reprompt reprompt = new Reprompt()
        reprompt.setOutputSpeech(speech)

        SpeechletResponse.newTellResponse(speech, card)
    }

    private SpeechletResponse sayGoodbye() {
        String speechText = "OK.  I'm going to stop now."
        tellResponse(speechText, speechText)
    }

    private SpeechletResponse didNotUnderstand() {
        String speechText = "I'm sorry.  I didn't understand what you said.  Say list open incidents to list open incidents"
        askResponse(speechText, speechText)
    }
    /**
     * Grails config is injected here for configuration of your speechlet
     * @param co Config
     */
    void setConfiguration(Config co) {
        this.grailsConfig = co
    }

    /**
     * this is where you do session cleanup
     * @param request SessionEndedRequest
     * @param session
     * @throws SpeechletException
     */
    public void onSessionEnded(final SessionEndedRequest request, final Session session)
            throws SpeechletException {
        log.info("onSessionEnded requestId={}, sessionId={}", request.getRequestId(),
                session.getSessionId())
        // any cleanup logic goes here
    }

    SpeechletResponse getWelcomeResponse()  {
        String speechText = "Welcome to the Victor OPS skill - say List Open Incidents for open incidents"

        // Create the Simple card content.
        SimpleCard card = new SimpleCard(title: "VictorOPS", content: speechText)

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech(text:speechText)

        // Create reprompt
        Reprompt reprompt = new Reprompt(outputSpeech: speech)

        SpeechletResponse.newAskResponse(speech, reprompt, card)
    }


    SpeechletResponse whoIsOnCall() {

        RESTClient client = new RESTClient('https://api.victorops.com/api-public/v1/')
        client.defaultRequestHeaders.'X-VO-Api-Id' = grailsApplication.config.victorOPS.apiId
        client.defaultRequestHeaders.'X-VO-Api-Key' = grailsApplication.config.victorOPS.apiKey
        client.defaultRequestHeaders.'Accept' = "application/json"
        log.debug("Using API id:${grailsApplication.config.victorOPS.apiId} apiKey: ${grailsApplication.config.victorOPS.apiKey}")
        def response = client.get(path:'incidents')
        log.debug("Got incidents")

        String speechText = ""

        response.data.get("incidents").each { incident ->


        }
        tellResponse(speechTest,speechText)
    }


    SpeechletResponse changeIncidentStatus(String username, String status) {

        // send either 'ack' or 'resolve' on the status param
        RESTClient client = new RESTClient('https://api.victorops.com/api-public/v1/incidents/byUser/')
        client.defaultRequestHeaders.'X-VO-Api-Id' = grailsApplication.config.victorOPS.apiId
        client.defaultRequestHeaders.'X-VO-Api-Key' = grailsApplication.config.victorOPS.apiKey
        client.defaultRequestHeaders.'Accept' = "application/json"
        log.debug("Using API id:${grailsApplication.config.victorOPS.apiId} apiKey: ${grailsApplication.config.victorOPS.apiKey}")
        def postBody = [userName: username, message: 'updatedbyAlexaSkill'] // will be url-encoded

        def response = client.patch(path:status,body: postBody, requestContentType: JSON)

        log.debug("Got response for ${username}")

        String speechText = ""

        response.data.get("results").each { result ->
             speechText += "Incident ${result.incidentNumber} set to ${result.message}"
        }
        tellResponse(speechText,speechText)
    }


    SpeechletResponse getOpenIncidents() {


        RESTClient client = new RESTClient('https://api.victorops.com/api-public/v1/')
        client.defaultRequestHeaders.'X-VO-Api-Id' = grailsApplication.config.victorOPS.apiId
        client.defaultRequestHeaders.'X-VO-Api-Key' = grailsApplication.config.victorOPS.apiKey
        client.defaultRequestHeaders.'Accept' = "application/json"
        log.debug("Using API id:${grailsApplication.config.victorOPS.apiId} apiKey: ${grailsApplication.config.victorOPS.apiKey}")
        def response = client.get(path:'incidents')
        log.debug("Got incidents")

        String speechText = ""
        int incidentCount = 0
        response.data.get("incidents").each { incident ->
            if (incident.currentPhase != "RESOLVED") {
                incidentCount++
            }
        }
        response.data.get("incidents").each { incident ->
            DateTimeFormatter f = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault())
            ZonedDateTime zdt = ZonedDateTime.parse(incident.startTime, f)
            if (incident.currentPhase != "RESOLVED") {
                if (speechText == "") {
                    speechText = "You have ${incidentCount} incidents.\n\nYour first incident is:\n\n"
                } else {
                    speechText += "Next incident\n\n"
                }
                speechText += "incident i d ${incident.incidentNumber}\n\n${incident.entityDisplayName}\n\nstarted at ${zdt.format(DateTimeFormatter.RFC_1123_DATE_TIME)}\n\nand is currently\n\n${incident.currentPhase}\n\n\n"
            }
        }

        tellResponse(speechText, speechText)
    }

    /**
     * default responder when a help intent is launched on how to use your speechlet
     * @return
     */
    SpeechletResponse getHelpResponse() {
        String speechText = "Say something when the skill need help"
        // Create the Simple card content.
        SimpleCard card = new SimpleCard(title:"YourHelpCardTitle",
                content:speechText)
        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech(text:speechText)
        // Create reprompt
        Reprompt reprompt = new Reprompt(outputSpeech: speech)
        SpeechletResponse.newAskResponse(speech, reprompt, card)
    }

    /**
     * if you are using account linking, this is used to send a card with a link to your app to get started
     * @param session
     * @return
     */
    SpeechletResponse createLinkCard(Session session) {

        String speechText = "Please use the alexa app to link account."
        // Create the Simple card content.
        LinkAccountCard card = new LinkAccountCard()
        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech(text:speechText)
        log.debug("Session ID=${session.sessionId}")
        // Create reprompt
        Reprompt reprompt = new Reprompt(outputSpeech: speech)
        SpeechletResponse.newTellResponse(speech, card)
    }

     void getUsers() {
        RESTClient client = new RESTClient('https://api.victorops.com/api-public/v1/user')
        client.defaultRequestHeaders.'X-VO-Api-Id' = grailsApplication.config.victorOPS.apiId
        client.defaultRequestHeaders.'X-VO-Api-Key' = grailsApplication.config.victorOPS.apiKey
        client.defaultRequestHeaders.'Accept' = "application/json"
        def response = client.get()


        System.out.println(response)
    }


}




/**
 * this inner controller handles incoming requests - be sure to white list it with SpringSecurity
 * or whatever you are using
 */
@Controller
class VictorOPSController {

    def speechletService
    def victorOPSSpeechlet


    def index() {
        speechletService.doSpeechlet(request,response, victorOPSSpeechlet)
    }

}