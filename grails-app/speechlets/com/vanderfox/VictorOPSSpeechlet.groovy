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
import com.amazon.speech.ui.SsmlOutputSpeech
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.dynamodbv2.document.DynamoDB
import com.amazonaws.services.dynamodbv2.document.Item
import com.amazonaws.services.dynamodbv2.document.Table
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
    def apiCredentialsService

    static final String INCIDENT_INDEX = "incidentIndex"
    static final String INCIDENTS = "incidents"
    static final String TEAMS = "teams"
    static final String TEAM_INDEX = "teamIndex"


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
                   changeIncidentStatus(incidentNumber.value,"resolve",session)
                break
            case "AckIncidentsForUser":
                Slot incidentNumber = request.intent.getSlot("username")
                log.debug("incident number:"+incidentNumber.value)
                changeIncidentStatus(incidentNumber.value,"ack",session)
                break
            case "ListTeams":
                listTeams(session)
                break
            case "NextTeam":
                nextTeam(session)
                break
            case "SayTeamOnCallSchedule":
                int teamIndex = session.getAttribute(TEAM_INDEX) as Integer
                sayTeamOncallSchedule(teamIndex,session)
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
        List<Map> incidents = speechletSession.getAttribute(INCIDENTS) as List<Map>
        int incidentIndex = speechletSession.getAttribute(INCIDENT_INDEX) as Integer
        incrementResolveIncidentMetrics
        changeIncidentStatus("resolve",incidents[incidentIndex], speechletSession)
    }

    private ackIncident(Session speechletSession) {
        List<Map> incidents = speechletSession.getAttribute(INCIDENTS) as List<Map>
        int incidentIndex = speechletSession.getAttribute(INCIDENT_INDEX) as Integer
        incrementAcknowledgeIncidentMetrics
        changeIncidentStatus("ack",incidents[incidentIndex], speechletSession)
    }

    private nextIncident(Session speechletSession) {
        int incidentIndex = speechletSession.getAttribute(INCIDENT_INDEX) as Integer
        speechletSession.setAttribute(INCIDENT_INDEX, incidentIndex+1)
        sayIncident(speechletSession,false)
    }

    private SpeechletResponse askResponse(String cardText, String speechText) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard()
        card.setTitle("VictorOPS")
        card.setContent(cardText)

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech()
        speech.setText(speechText)

        // Create reprompt
        Reprompt reprompt = new Reprompt()
        reprompt.setOutputSpeech(speech)

        SpeechletResponse.newAskResponse(speech, reprompt, card)
    }


    private SpeechletResponse askResponseFancy(String cardText, String speechText) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard()
        card.setTitle("VictorOPS")
        card.setContent(cardText)

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech()
        speech.setText(speechText)
        log.info("making ssml")
        SsmlOutputSpeech fancySpeech = new SsmlOutputSpeech()
        fancySpeech.ssml = speechText
        log.info("finished ssml")
        // Create reprompt
        Reprompt reprompt = new Reprompt()
        reprompt.setOutputSpeech(fancySpeech)

        SpeechletResponse.newAskResponse(fancySpeech, reprompt, card)
    }

    private SpeechletResponse tellResponseFancy(String cardText, String speechText) {
        // Create the Simple card content.
        SimpleCard card = new SimpleCard()
        card.setTitle("VictorOPS")
        card.setContent(cardText)

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech()
        speech.setText(speechText)
        log.info("making ssml")
        SsmlOutputSpeech fancySpeech = new SsmlOutputSpeech()
        fancySpeech.ssml = speechText
        log.info("finished ssml")
        // Create reprompt
        Reprompt reprompt = new Reprompt()
        reprompt.setOutputSpeech(fancySpeech)

        SpeechletResponse.newTellResponse(fancySpeech, card)
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
        String speechText = "<speak><say-as interpret-as=\"interjection\">uh-oh!</say-as>  I didn't understand what you said. Say list incidents or acknowledge incidents for user or resolve incidents for user or list teams</speak>"
        askResponseFancy(speechText, speechText)
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
        String speechText = "Welcome to the victorops skill - say List Open Incidents for open incidents or list teams"

        // Create the Simple card content.
        SimpleCard card = new SimpleCard(title: "VictorOPS", content: speechText)

        // Create the plain text output.
        PlainTextOutputSpeech speech = new PlainTextOutputSpeech(text:speechText)

        incrementLoginMetrics()

        // Create reprompt
        Reprompt reprompt = new Reprompt(outputSpeech: speech)



        SpeechletResponse.newAskResponse(speech, reprompt, card)
    }

    private void incrementLoginMetrics() {
        DynamoDB dynamoDB = new DynamoDB(new AmazonDynamoDBClient(new BasicAWSCredentials(System.getProperty("aws.access.key"), System.getProperty("aws.secret.key"))));
        Table table = dynamoDB.getTable("VictorOpsMetrics");
        Item item = table.getItem("id", 0);
        int usedCount = 0;
        if (item != null) {
            usedCount = item.getInt("used")
        }
        usedCount++
        Item newItem = new Item()
        newItem.withInt("id", 0)
        newItem.withString("metric", "Login")
        newItem.withInt("used", usedCount)
        table.putItem(newItem)
    }

    private void incrementAcknowledgeIncidentMetrics() {
        DynamoDB dynamoDB = new DynamoDB(new AmazonDynamoDBClient(new BasicAWSCredentials(System.getProperty("aws.access.key"), System.getProperty("aws.secret.key"))));
        Table table = dynamoDB.getTable("VictorOpsMetrics");
        Item item = table.getItem("id", 1);
        int usedCount = 0;
        if (item != null) {
            usedCount = item.getInt("used")
        }
        usedCount++
        Item newItem = new Item()
        newItem.withInt("id", 1)
        newItem.withString("metric", "AcknowledgeIncident")
        newItem.withInt("used", usedCount)
        table.putItem(newItem)
    }

    private void incrementResolveIncidentMetrics() {
        DynamoDB dynamoDB = new DynamoDB(new AmazonDynamoDBClient(new BasicAWSCredentials(System.getProperty("aws.access.key"), System.getProperty("aws.secret.key"))));
        Table table = dynamoDB.getTable("VictorOpsMetrics");
        Item item = table.getItem("id", 2);
        int usedCount = 0;
        if (item != null) {
            usedCount = item.getInt("used")
        }
        usedCount++
        Item newItem = new Item()
        newItem.withInt("id", 2)
        newItem.withString("metric", "ResolveIncident")
        newItem.withInt("used", usedCount)
        table.putItem(newItem)
    }

    private void incrementListIncidentMetrics() {
        DynamoDB dynamoDB = new DynamoDB(new AmazonDynamoDBClient(new BasicAWSCredentials(System.getProperty("aws.access.key"), System.getProperty("aws.secret.key"))));
        Table table = dynamoDB.getTable("VictorOpsMetrics");
        Item item = table.getItem("id", 3);
        int usedCount = 0;
        if (item != null) {
            usedCount = item.getInt("used")
        }
        usedCount++
        Item newItem = new Item()
        newItem.withInt("id", 3)
        newItem.withString("metric", "ListIncidents")
        newItem.withInt("used", usedCount)
        table.putItem(newItem)
    }

    private void incrementListTeamsMetrics() {
        DynamoDB dynamoDB = new DynamoDB(new AmazonDynamoDBClient(new BasicAWSCredentials(System.getProperty("aws.access.key"), System.getProperty("aws.secret.key"))));
        Table table = dynamoDB.getTable("VictorOpsMetrics");
        Item item = table.getItem("id", 4);
        int usedCount = 0;
        if (item != null) {
            usedCount = item.getInt("used")
        }
        usedCount++
        Item newItem = new Item()
        newItem.withInt("id", 4)
        newItem.withString("metric", "ListTeams")
        newItem.withInt("used", usedCount)
        table.putItem(newItem)
    }



    SpeechletResponse whoIsOnCall(Session speechletSession) {
        ApiCredentials apiCredentials = getApiCredentials(speechletSession)
        if (!apiCredentials) {
            return createLinkCard(speechletSession)
        }


        RESTClient client = new RESTClient('https://api.victorops.com/api-public/v1/')
        client.defaultRequestHeaders.'X-VO-Api-Id' = apiCredentials.apiId
        client.defaultRequestHeaders.'X-VO-Api-Key' = apiCredentials.apiKey
        client.defaultRequestHeaders.'Accept' = "application/json"
        log.debug("Using API id:${grailsApplication.config.victorOPS.apiId} apiKey: ${grailsApplication.config.victorOPS.apiKey}")
        def response = client.get(path:'incidents')
        log.debug("Got incidents")

        String speechText = ""

        response.data.get("incidents").each { incident ->


        }
        tellResponse(speechText,speechText)
    }


    SpeechletResponse changeIncidentStatus(String username, String status, Session speechletSession) {

        ApiCredentials userCredentials = getApiCredentials(speechletSession)
        if (!userCredentials) {
            return createLinkCard(speechletSession)
        }
        // send either 'ack' or 'resolve' on the status param
        RESTClient client = new RESTClient('https://api.victorops.com/api-public/v1/incidents/byUser/')
        client.defaultRequestHeaders.'X-VO-Api-Id' = userCredentials.apiId
        client.defaultRequestHeaders.'X-VO-Api-Key' = userCredentials.apiKey
        client.defaultRequestHeaders.'Accept' = "application/json"
        log.debug("Using API id:${userCredentials.apiId} apiKey: ${userCredentials.apiKey}")
        def postBody = [userName: username, message: 'updatedbyAlexaSkill'] // will be url-encoded

        def response = client.patch(path:status,body: postBody, requestContentType: JSON)

        log.debug("Got response for ${username}")

        String speechText = ""

        response.data.get("results").each { result ->
             speechText += "Incident ${result.incidentNumber} set to ${status}\n"
        }
        if (speechText.length() == 0) {
            speechText += "There were no incidents to update.\n"
        }
        tellResponse(speechText,speechText)
    }

    SpeechletResponse changeIncidentStatus(String status, Map incident, Session speechletSession) {

        ApiCredentials apiCredentials = getApiCredentials(speechletSession)
        if (!apiCredentials) {
            return createLinkCard(speechletSession)
        }
        // send either 'ack' or 'resolve' on the status param
        if (status == "ack" && incident.currentPhase == "ACKED") {
            // say next incident
            int incidentIndex = speechletSession.getAttribute(INCIDENT_INDEX) as Integer
            speechletSession.setAttribute(INCIDENT_INDEX,incidentIndex+1)
            return sayIncident(speechletSession,false,"\n Incident is already acknowledged.\ns")
        }
        RESTClient client = new RESTClient("https://api.victorops.com/api-public/v1/incidents/${status}")
        client.defaultRequestHeaders.'X-VO-Api-Id' = apiCredentials.apiId
        client.defaultRequestHeaders.'X-VO-Api-Key' =apiCredentials.apiKey
        client.defaultRequestHeaders.'Accept' = "application/json"
        log.debug("Using API id:${apiCredentials.apiId} apiKey: ${apiCredentials.apiKey}")
        def postBody = [userName: grailsApplication.config.victorOPS.userName, incidentNames: [incident.incidentNumber], message: 'updatedbyAlexaSkill'] // will be url-encoded

        def response = client.patch(path:status,body: postBody, requestContentType: JSON)

        log.debug("Got response for incident ${incident}")

        String speechText = ""

        response.data.get("results").each { result ->
            speechText += "Incident ${result.incidentNumber} set to ${status}.\n"
        }
        if (speechText.length() == 0) {
            speechText += "There were no incidents to update.\n"
        }
        // say next incident
        int incidentIndex = speechletSession.getAttribute(INCIDENT_INDEX) as Integer
        speechletSession.setAttribute(INCIDENT_INDEX,incidentIndex+1)
        sayIncident(speechletSession,false,speechText)

    }

    SpeechletResponse sayIncident(Session speechletSession, Boolean sayCount = false, String speechText = "") {

        int indicentIndex = speechletSession.getAttribute(INCIDENT_INDEX) as Integer
        List<Map> incidents = speechletSession.getAttribute(INCIDENTS) as List<Map>

        if (sayCount) {
            speechText += "You have ${incidents.size()} incidents"

        }
        DateTimeFormatter f = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault())
        def incident = incidents[indicentIndex]
        if (!incident) {
            speechText += "You have no more incidents."
            return tellResponse(speechText,speechText)
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

    ApiCredentials getApiCredentials(Session speechletSession) {
        ApiCredentials userCredentials = null

        if (speechletSession?.user?.accessToken) {
            log.debug("Looking up user for access token ${speechletSession.user.accessToken}")
            User awsUser = apiCredentialsService.getUserForAccessToken(speechletSession.user.accessToken)
            if (awsUser) {
                log.debug("Looking up credentials for user u:${awsUser.username} id:${awsUser.id} and for access token ${speechletSession.user.accessToken}")
                userCredentials = ApiCredentials.findByUserAndActive(awsUser,true)

            } else {
                log.error("Unable to find user for access token ${speechletSession.user.accessToken}")
            }
            return userCredentials

        }
    }
    SpeechletResponse getOpenIncidents(Session speechletSession) {

        ApiCredentials userCredentials = getApiCredentials(speechletSession)
        if (!userCredentials) {
            return createLinkCard(speechletSession)
        }
        incrementListIncidentMetrics()

        log.debug("Using API id:${userCredentials.apiId} apiKey: ${userCredentials.apiKey}")
        RESTClient client = new RESTClient('https://api.victorops.com/api-public/v1/')
        client.defaultRequestHeaders.'X-VO-Api-Id' = userCredentials.apiId
        client.defaultRequestHeaders.'X-VO-Api-Key' = userCredentials.apiKey
        client.defaultRequestHeaders.'Accept' = "application/json"

        def response = client.get(path:'incidents')
        log.debug("Got incidents")

        String speechText = ""
        List<Map> filteredIncidents = new ArrayList<Map>()
        response.data.get("incidents").each { Map incident ->
            if (incident.currentPhase != "RESOLVED") {
                filteredIncidents.add(incident)
            }
        }

        if (filteredIncidents.size() > 0) {
            speechletSession.setAttribute(INCIDENTS,filteredIncidents)
            speechletSession.setAttribute(INCIDENT_INDEX,0)
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
        String speechText = "Say list incidents or acknowledge incidents for user or resolve incidents for user"
        // Create the Simple card content.
        SimpleCard card = new SimpleCard(title:"VictorOPS Help",
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

        String speechText = "I see you have not linked your account. Please use the alexa app to link your account and enter your API credentials."
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

        def response = client.get()
        RESTClient client = buildRestClient('https://api.victorops.com/api-public/v1/user')
        System.out.println(response)
    }


    SpeechletResponse listTeams(Session session, int startIndex = 0) {
        ApiCredentials userCredentials = getApiCredentials(session)
        if (!userCredentials) {
            return createLinkCard(session)
        }

        incrementListTeamsMetrics()
        
        log.debug("Using API id:${userCredentials.apiId} apiKey: ${userCredentials.apiKey}")
        RESTClient client = buildRestClient("https://api.victorops.com/api-public/v1/",userCredentials)

        def response = client.get(path:'team')
        log.debug("Got teams")

        String speechText = ""
        int teamcount = response.data.size()
        if (startIndex > 0) {
            speechText = "You have ${teamcount} teams.\n"
        }

        session.setAttribute(TEAMS,response.data)
        session.setAttribute(TEAM_INDEX,startIndex as String)
        if (teamcount > 0 && startIndex <= teamcount) {
            speechText += "Team ${response.data[startIndex].name} - would you like to hear their on-call schedule?"
            askResponse(speechText,speechText)
        } else {
            speechText += "You have no teams configured."
            tellResponse(speechText,speechText)
        }


    }


    SpeechletResponse nextTeam(Session session) {
        int teamIndex = session.getAttribute(TEAM_INDEX) as Integer
        teamIndex++
        listTeams(session, teamIndex)

    }

    SpeechletResponse sayTeamOncallSchedule(int teamIndex, Session session) {
        ApiCredentials userCredentials = getApiCredentials(session)
        if (!userCredentials) {
            return createLinkCard(session)
        }

        log.debug("Using API id:${userCredentials.apiId} apiKey: ${userCredentials.apiKey}")
        List teams = session.getAttribute(TEAMS)
        RESTClient client = buildRestClient("https://api.victorops.com/api-public/v1/team/${teams[teamIndex].slug}/oncall/",userCredentials)

        def response = client.get(path:'schedule',query:['daysForward':'7'])
        log.debug("Got teams using index:${teamIndex}")

        String speechText = ""
        teamIndex--

        if (teams[teamIndex] && response.data.team) {
            speechText += "Team ${response.data.team} - on call schedule:\n"
            speechText += "Rotation ${response.data.schedule.rotationName} Shift ${response.data.schedule.shiftName}\n"
            response.data.schedule.rolls.each { roll ->
                speechText += "User ${roll.onCall} is on call between ${roll.change} until ${roll.util}\n"
            }

            speechText +" say next team or yes to go to the next team."
            if (teamIndex <= teams.size()) {
                askResponse(speechText, speechText)
            } else {
                speechText += "There are no more teams."
                tellResponse(speechText,speechText)
            }
        } else {
            speechText += "Unable to retrieve on call schedule for team ${teams[teamIndex].name}."
            tellResponse(speechText,speechText)
        }


    }

    RESTClient buildRestClient(String url, ApiCredentials userCredentials) {
        RESTClient client = new RESTClient(url)
        client.defaultRequestHeaders.'X-VO-Api-Id' = userCredentials.apiId
        client.defaultRequestHeaders.'X-VO-Api-Key' = userCredentials.apiKey
        client.defaultRequestHeaders.'Accept' = "application/json"
        client
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