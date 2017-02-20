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
                   getOpenIncidents(session)
                break
            case "ResolveIncident":
                resolveIncident(session)
                break
            case "AckIncident":
                ackIncident(session)
                break
            case "NextIncident":
                nextIncident(session)
                break
            case "ResolveIncidentsForUser":
                Slot incidentNumber = request.intent.getSlot("username")
                log.debug("incident number:"+incidentNumber.value)
                   changeIncidentStatus(incidentNumber.value,"resolve")
                break
            case "AckIncidentsForUser":
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


    private resolveIncident(Session speechletSession) {
        List<Map> incidents = speechletSession.getAttribute("incidents") as List<Map>
        int incidentIndex = speechletSession.getAttribute("incidentIndex") as Integer

        changeIncidentStatus("resolve",incidents[incidentIndex], speechletSession)
    }

    private ackIncident(Session speechletSession) {
        List<Map> incidents = speechletSession.getAttribute("incidents") as List<Map>
        int incidentIndex = speechletSession.getAttribute("incidentIndex") as Integer

        changeIncidentStatus("ack",incidents[incidentIndex], speechletSession)
    }

    private nextIncident(Session speechletSession) {

        sayIncident(speechletSession,false)
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
        String speechText = "<speak><say-as interpret-as=\"interjection\">uh-oh!</say-as>  I didn't understand what you said.  Say list open incidents to list open incidents</speak>"
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
        String speechText = "Welcome to the victorops skill - say List Open Incidents for open incidents"

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
        tellResponse(speechText,speechText)
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
             speechText += "Incident ${result.incidentNumber} set to ${status}"
        }
        if (speechText.length() == 0) {
            speechText += "There were no incidents to update"
        }
        tellResponse(speechText,speechText)
    }

    SpeechletResponse changeIncidentStatus(String status, Map incident, Session speechletSession) {

        // send either 'ack' or 'resolve' on the status param
        if (status == "ack" && incident.currentPhase == "ACKED") {
            // say next incident
            int incidentIndex = speechletSession.getAttribute("incidentIndex") as Integer
            speechletSession.setAttribute("incidentIndex",++incidentIndex)
            return sayIncident(speechletSession,false,"\n Incident is already acknowledged.\ns")
        }
        RESTClient client = new RESTClient("https://api.victorops.com/api-public/v1/incidents/${status}")
        client.defaultRequestHeaders.'X-VO-Api-Id' = grailsApplication.config.victorOPS.apiId
        client.defaultRequestHeaders.'X-VO-Api-Key' = grailsApplication.config.victorOPS.apiKey
        client.defaultRequestHeaders.'Accept' = "application/json"
        log.debug("Using API id:${grailsApplication.config.victorOPS.apiId} apiKey: ${grailsApplication.config.victorOPS.apiKey}")
        def postBody = [userName: grailsApplication.config.victorOPS.userName, incidentNames: [incident.entityDisplayName], message: 'updatedbyAlexaSkill'] // will be url-encoded

        def response = client.patch(path:status,body: postBody, requestContentType: JSON)

        log.debug("Got response for incident ${incident}")

        String speechText = ""

        response.data.get("results").each { result ->
            speechText += "Incident ${result.incidentNumber} set to ${status}"
        }
        if (speechText.length() == 0) {
            speechText += "There were no incidents to update"
        }
        // say next incident
        int incidentIndex = speechletSession.getAttribute("incidentIndex") as Integer
        speechletSession.setAttribute("incidentIndex",++incidentIndex)
        sayIncident(speechletSession,false)

    }

    SpeechletResponse sayIncident(Session speechletSession, Boolean sayCount = false, String speechText = "") {

        int indicentIndex = speechletSession.getAttribute("incidentIndex") as Integer
        List<Map> incidents = speechletSession.getAttribute("incidents") as List<Map>

        if (sayCount) {
            speechText = "You have ${incidents.size()} incidents"

        }
        DateTimeFormatter f = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault())
        def incident = incidents[indicentIndex]
        if (!incident) {
            def s = "You have no more incidents."
            return tellResponse(s,s)
        }
        ZonedDateTime zdt = ZonedDateTime.parse(incident.startTime, f)
        speechText += "incident i d ${incident.incidentNumber}\n\n${incident.entityDisplayName}\n\nstarted at ${zdt.format(DateTimeFormatter.RFC_1123_DATE_TIME)}\n\nand is currently\n\n${incident.currentPhase}\n\n\n"
        if (incident.currentPhase == "ACKED") {
            speechText += "Would you like to Resolve or go to next incident?"
        } else {
            speechText += "Would you like to Acknowledge Resolve or go to next incident?"
        }


        askResponse(speechText, speechText)

    }
    SpeechletResponse getOpenIncidents(Session speechletSession) {


        RESTClient client = new RESTClient('https://api.victorops.com/api-public/v1/')
        client.defaultRequestHeaders.'X-VO-Api-Id' = grailsApplication.config.victorOPS.apiId
        client.defaultRequestHeaders.'X-VO-Api-Key' = grailsApplication.config.victorOPS.apiKey
        client.defaultRequestHeaders.'Accept' = "application/json"
        log.debug("Using API id:${grailsApplication.config.victorOPS.apiId} apiKey: ${grailsApplication.config.victorOPS.apiKey}")
        def response = client.get(path:'incidents')
        log.debug("Got incidents")

        String speechText = ""
        List<Map> filteredIncidents = new ArrayList<Map>()
        response.data.get("incidents").each { Map incident ->
            if (incident.currentPhase != "RESOLVED") {
                filteredIncidents.add(incident)
            }
        }
        //speechText = "You have ${filteredIncidents.size()} incidents"

        if (filteredIncidents.size() > 0) {
            speechletSession.setAttribute("incidents",filteredIncidents)
            speechletSession.setAttribute("incidentIndex",0)
            sayIncident(speechletSession, true)
        } else {
            speechText = "You have no open incidents."
            tellResponse(speechText, speechText)
        }

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