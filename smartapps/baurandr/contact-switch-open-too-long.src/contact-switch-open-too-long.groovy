/**
 *  Author: Baur
 */

definition(
    name: "Contact Switch Open Too Long",
    namespace: "baurandr",
    author: "A. Baur",
    description: "Monitor your contact sensors and get a text message if they are open too long",
    category: "Safety & Security",
    parent: "baurandr:Baur Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_contact.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/garage_contact@2x.png"
)

preferences {
	section("When the contact is open...") {
		input "contact1", "capability.contactSensor", title: "Which contact sensor?", multiple: true
	}
	section("For too long...") {
		input "maxOpenTime", "number", title: "Minutes?"
	}
    section("Keep sending reminder texts every ??? minutes (optional)") {
		input "reminderTime", "number", title: "Minutes?", required: false
	}
	section("Text me at (optional, sends a push notification if not specified)...") {
        input("recipients", "contact", title: "Notify", description: "Send notifications to") {
            input "phone", "phone", title: "Phone number?", required: false
        }
	}
}

def installed()
{
	clearStatus()
	subscribe(contact1, "contact", contactHandler)
}

def updated()
{
	unsubscribe()
    clearStatus()
	subscribe(contact1, "contact", contactHandler)
}

def contactHandler(evt) {
	log.debug "$evt.device $evt.name: $evt.value"
	def isOpen = evt.value == "open"
    //def deviceName = evt.device
    def isNotScheduled = state.status != "scheduled"
    def openContacts = contact1.findAll{it.currentValue("contact") == "open"}
	def scheduledContacts = state.scheduledContacts
	def bSchedule = false

    if (!openContacts) {
        log.debug "All contacts closed. Cancelling runIn and clearing status."
        clearStatus()
        unschedule(takeAction)
    } else if (isOpen){
    	openContacts.each {
        	if(!scheduledContacts?.contains(it.label)){
            	scheduledContacts << it.label
				bSchedule = true
				log.debug "New scheduled contacts:$it, All scheduled contacts:$scheduledContacts"
			}
        }
        state.scheduledContacts = scheduledContacts
        if (bSchedule) {
            state.status = "scheduled"
            runIn(maxOpenTime * 60, takeAction, [overwrite: false])
        }
    }
}

def takeAction(){
	if (state.status == "scheduled")
	{
        def openContacts = contact1.findAll{it.currentValue("contact") == "open"}
        def openTooLong = []
        
        openContacts.each { 
        	def openState = it.currentState("contact")
            def elapsed = now() - openState.rawDateCreated.time
            def threshold = 1000 * 60 * maxOpenTime - 1000
            if (elapsed >= threshold) {
            	//log.debug "$it Open Too Long"
                openTooLong << it
            }
        }
        //log.debug "Contacts that have been open too long:$openTooLong"
        if (openTooLong){
            sendTextMessage(openTooLong, maxOpenTime)
            if (reminderTime) {
                runIn(reminderTime * 60, takeAction, [overwrite: false])
            }
        }
	} else {
		log.trace "Status is no longer scheduled. Not sending text."
	}
}

def sendTextMessage(openContacts, openMinutes) {

	log.debug "$openContacts was open too long, texting phone"
    
    def msg = "Your ${openContacts.label ?: openContacts.name} has been open for more than ${openMinutes} minutes!"
    
    if (maxOpenTime <= 0){
    	msg = "Your ${openContacts.label ?: openContacts.name} has been opened!"
    }
    
	if (location.contactBookEnabled) {
        sendNotificationToContacts(msg, recipients)
    }
    else {
        if (phone) {
            sendSms(phone, msg)
        } else {
            sendPush msg
        }
    }
}

def clearStatus() {
	state.status = null
    state.scheduledContacts = []
}