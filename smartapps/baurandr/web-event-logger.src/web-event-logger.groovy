/**
 *  Web Event Logger
 *
 *  Copyright 2018 Andrew Baur
 *
 */
definition(
    name: "Web Event Logger",
    namespace: "baurandr",
    author: "Andrew Baur",
    description: "Log events",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Select contacts...") {
		input("contact1", "capability.contactSensor", title: "Which contact sensor(s)?", multiple: true, required: false)
    }
	section("Select motion sensors..."){
		input "motion1", "capability.motionSensor", title: "Which motion sensor(s)?", multiple: true, required: false
	}
	section("Select buttons..."){
		input "button1", "capability.button", title: "Which button(s)?", multiple: true, required: false
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	subscribe(contact1, "contact", contactHandler)
    subscribe(motion1, "motion", contactHandler)
    subscribe(button1, "button", contactHandler)
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
    subscribe(contact1, "contact", contactHandler)
    subscribe(motion1, "motion", contactHandler)
    subscribe(button1, "button", contactHandler)
	initialize()
}

def initialize() {
	// TODO: subscribe to attributes, devices, locations, etc.
}

def contactHandler(evt) {

    log.debug "$evt.device $evt.name: $evt.value DateTime: $evt.date"

    def eventDate = evt.date
    //def eventDate = evt.isoDate.replaceAll('.','_');
    //def eventDateString = eventDate.format('yyyy-MM-dd HH:mm:ss')
    def eventDateString = eventDate.format("yyyy-MM-dd HH:mm:ss", location.timeZone)

//    log.debug "event date string: ${eventDateString}"

    def params = [
        uri: "http://www.baurfam.com/addEvent",
        query: [eventType: evt.name,
                eventValue: evt.value,
                eventDateTime: eventDateString,
                eventDeviceName: evt.device]
        ]
    try {

        httpPost(params){ response ->
           /*
           response.headers.each {
                log.debug "${it.name} : ${it.value}"
            }
            
           log.debug "response contentType: ${response.contentType}"
		   log.debug "raw response: $response.data"
           log.debug "response status: $response.status"
           */
            if (response.status != 200) {
                log.debug "Logging failed, status = ${response.status}"
            } else {
                log.debug "Accepted event(s)"              
            }

        }
    }
     catch (e) {
        log.debug "something went wrong: $e"
    }
    catch (groovyx.net.http.ResponseParseException e) {
			// ignore error 200, bogus exception
			if (e.statusCode != 200) {
				log.error "Baurfam: ${e}"
			} else {
				log.debug "Baurfam accepted event(s)"
			}                  
		} catch (e) {
			def errorInfo = "Error sending value: ${e}"               
		}
}
